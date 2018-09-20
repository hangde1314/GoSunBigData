package com.hzgc.cluster.spark.consumer

import java.sql.Timestamp
import java.util.{Base64, Properties}

import com.google.common.base.Stopwatch
import com.hzgc.cluster.spark.util.PropertiesUtil
import com.hzgc.common.collect.bean.{CarObject, FaceObject, PersonObject}
import com.hzgc.common.service.facedynrepo.{FaceTable, PersonTable, VehicleTable}
import com.hzgc.common.util.json.JacksonUtil
import com.hzgc.jniface.FaceFunction
import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.ZkClient
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils}
import org.apache.spark.streaming.{Durations, StreamingContext}

object KafkaToParquet {
  val log: Logger = Logger.getLogger(KafkaToParquet.getClass)

  case class Face(id: String, sftpurl: String, bftpurl: String, timestamp: Timestamp, ipcid: String, hostname: String,
                  babsolutepath: String, sabsolutepath: String, eyeglasses: Int, gender: Int, age: Int, mask: Int,
                  huzi: Int, feature: Array[Float], bitfeature: String) {
    def toEsMap: Map[String, Any] = Map("id" -> id, "sftpurl" -> sftpurl, "bftpurl" -> bftpurl, "timestamp" -> timestamp.toString,
      "ipcid" -> ipcid, "hostname" -> hostname, "babsolutepath" -> babsolutepath, "sabsolutepath" -> sabsolutepath,
      "eyeglasses" -> eyeglasses, "gender" -> gender, "age" -> age, "mask" -> mask, "huzi" -> huzi,
      "feature" -> FaceFunction.floatFeature2Base64Str(feature), "bitfeature" -> bitfeature)
  }

  case class Person(id: String, sftpurl: String, bftpurl: String, timestamp: String, ipcid: String, hostname: String,
                    babsolutepath: String, sabsolutepath: String, age: String, baby: String, bag: String,
                    bottomcolor: String, bottomtype: String, hat: String, hair: String, knapsack: String,
                    messengerbag: String, orientation: String, sex: String, shoulderbag: String, umbrella: String,
                    uppercolor: String, uppertype: String, cartype: String, feature: String, bitfeature: String) {
    def toEsMap: Map[String, Any] = Map(
      "id" -> id, "sftpurl" -> sftpurl, "bftpurl" -> bftpurl, "timestamp" -> timestamp, "ipcid" -> ipcid,
      "hostname" -> hostname, "babsolutepath" -> babsolutepath, "sabsolutepath" -> sabsolutepath, "age" -> age,
      "baby" -> baby, "bag" -> bag, "bottomcolor" -> bottomcolor, "bottomtype" -> bottomtype, "hat" -> hat,
      "hair" -> hair, "knapsack" -> knapsack, "messengerbag" -> messengerbag, "orientation" -> orientation,
      "sex" -> sex, "shoulderbag" -> shoulderbag, "umbrella" -> umbrella, "uppercolor" -> uppercolor,
      "uppertype" -> uppertype, "cartype" -> cartype)
  }

  case class Car(id: String, sftpurl: String, bftpurl: String, timestamp: String, ipcid: String, hostname: String,
                 babsolutepath: String, sabsolutepath: String, vehicle_object_type: String, belt_maindriver: String,
                 belt_codriver: String, brand_name: String, call_code: String, vehicle_color: String, crash_code: String,
                 danger_code: String, marker_code: String, plate_schelter_code: String, plate_flag_code: String,
                 plate_licence: String, plate_destain_code: String, plate_color_code: String, plate_type_code: String,
                 rack_code: String, sunroof_code: String, vehicle_type: String, feature: String, bitfeature: String) {
    def toEsMap: Map[String, Any] = Map(
      "id" -> id, "sftpurl" -> sftpurl, "bftpurl" -> bftpurl, "timestamp" -> timestamp, "ipcid" -> ipcid, "hostname" -> hostname,
      "babsolutepath" -> babsolutepath, "sabsolutepath" -> sabsolutepath, "vehicle_object_type" -> vehicle_object_type,
      "belt_maindriver" -> belt_maindriver, "belt_codriver" -> belt_codriver, "brand_name" -> brand_name,
      "call_code" -> call_code, "vehicle_color" -> vehicle_color, "crash_code" -> crash_code, "danger_code" -> danger_code,
      "marker_code" -> marker_code, "plate_schelter_code" -> plate_schelter_code, "plate_flag_code" -> plate_flag_code,
      "plate_licence" -> plate_licence, "plate_destain_code" -> plate_destain_code, "plate_color_code" -> plate_color_code,
      "plate_type_code" -> plate_type_code, "rack_code" -> rack_code, "sunroof_code" -> sunroof_code,
      "vehicle_type" -> vehicle_type)
  }

  def main(args: Array[String]): Unit = {

    val properties: Properties = PropertiesUtil.getProperties
    val appName = properties.getProperty("job.faceObjectConsumer.appName")
    val brokers = properties.getProperty("job.faceObjectConsumer.broker.list")
    val kafkaGroupId = properties.getProperty("job.faceObjectConsumer.group.id")
    val timeInterval = properties.getProperty("job.faceObjectConsumer.timeInterval")
    val storeAddress = properties.getProperty("job.storeAddress")
    val zkHosts = properties.getProperty("job.zkDirAndPort")
    val faceTopic = Set(properties.getProperty("job.faceObjectConsumer.faceTopic"))
    val personTopic = Set(properties.getProperty("job.faceObjectConsumer.personTopic"))
    val carTopic = Set(properties.getProperty("job.faceObjectConsumer.carTopic"))
    val zkFacePath = properties.getProperty("job.kafkaToParquet.zkFacePath")
    val zkPersonPath = properties.getProperty("job.kafkaToParquet.zkPersonPath")
    val zkCarPath = properties.getProperty("job.kafkaToParquet.zkCarPath")
    val esNodes = properties.getProperty("job.offLine.esNodes")
    val esPort = properties.getProperty("job.offLine.esPort")

    val zkClient = new ZkClient(zkHosts)
    val conf = new SparkConf().setAppName(appName)
      .set("es.index.auto.create", "true")
      .set("es.nodes", esNodes)
      .set("es.port", esPort)
    val spark: SparkSession = SparkSession.builder().config(conf).appName(appName).getOrCreate()
    val sc = spark.sparkContext
    val ssc: StreamingContext = new StreamingContext(sc, Durations.seconds(timeInterval.toLong))
    val kafkaParams: Map[String, String] = Map(
      "metadata.broker.list" -> brokers,
      "group.id" -> kafkaGroupId)

    face2es(spark, ssc, zkClient, kafkaParams, faceTopic, zkHosts, zkFacePath, storeAddress)
    person2es(ssc, zkClient, kafkaParams, personTopic, zkHosts, zkPersonPath)
    car2es(ssc, zkClient, kafkaParams, carTopic, zkHosts, zkCarPath)

    ssc.start()
    ssc.awaitTermination()
  }


  def face2es(spark: SparkSession, ssc: StreamingContext, zkClient: ZkClient, kafkaParams: Map[String, String],
              topics: Set[String], zkHosts: String, zkPath: String, storeAddress: String): Unit = {
    import org.elasticsearch.spark.streaming._
    val messages = createCustomDirectKafkaStream(ssc, kafkaParams, zkHosts, zkPath, topics)
    val kafkaDF = messages.map(data => (data._1, JacksonUtil.toObject(data._2, classOf[FaceObject])))
      .map(faceobject => {
        Face(faceobject._1, faceobject._2.getsFtpUrl(), faceobject._2.getbFtpUrl(),
          Timestamp.valueOf(faceobject._2.getTimeStamp), faceobject._2.getIpcId, faceobject._2.getHostname,
          faceobject._2.getbAbsolutePath(), faceobject._2.getsAbsolutePath(), faceobject._2.getAttribute.getEyeglasses,
          faceobject._2.getAttribute.getGender, faceobject._2.getAttribute.getAge, faceobject._2.getAttribute.getMask,
          faceobject._2.getAttribute.getHuzi, faceobject._2.getAttribute.getFeature,
          Base64.getEncoder.encodeToString(faceobject._2.getAttribute.getBitFeature))
      })
    kafkaDF.foreachRDD(rdd => {
      import spark.implicits._
      rdd.map(rdd => rdd).repartition(1).toDF().write.mode(SaveMode.Append).parquet(storeAddress)
    })
    kafkaDF.map(data => data.toEsMap).saveToEs(FaceTable.DYNAMIC_INDEX + "/" + FaceTable.PERSON_INDEX_TYPE,
      Map("es.mapping.id" -> "id"))
    messages.foreachRDD(rdd => saveOffsets(zkClient, zkHosts, zkPath, rdd))
  }

  def person2es(ssc: StreamingContext, zkClient: ZkClient, kafkaParams: Map[String, String],
                topics: Set[String], zkHosts: String, zkPath: String) {
    import org.elasticsearch.spark.streaming._
    val messages = createCustomDirectKafkaStream(ssc, kafkaParams, zkHosts, zkPath, topics)
    val kafkaDF = messages.map(data => (data._1, JacksonUtil.toObject(data._2, classOf[PersonObject])))
      .map(personObject => Person(
        personObject._1, personObject._2.getsFtpUrl(), personObject._2.getbFtpUrl(), personObject._2.getTimeStamp,
        personObject._2.getIpcId, personObject._2.getHostname, personObject._2.getbAbsolutePath(),
        personObject._2.getsAbsolutePath(), personObject._2.getAttribute.getAge_code, personObject._2.getAttribute.getBaby_code,
        personObject._2.getAttribute.getBag_code, personObject._2.getAttribute.getBottomcolor_code,
        personObject._2.getAttribute.getBottomtype_code, personObject._2.getAttribute.getHat_code,
        personObject._2.getAttribute.getHair_code, personObject._2.getAttribute.getKnapsack_code,
        personObject._2.getAttribute.getMessengerbag_code, personObject._2.getAttribute.getOrientation_code,
        personObject._2.getAttribute.getSex_code, personObject._2.getAttribute.getShoulderbag_code,
        personObject._2.getAttribute.getUmbrella_code, personObject._2.getAttribute.getUppercolor_code,
        personObject._2.getAttribute.getUppertype_code, personObject._2.getAttribute.getCar_type,
        personObject._2.getFeature, personObject._2.getBitfeature
      ))
    kafkaDF.saveToEs(PersonTable.DYNAMIC_INDEX + "/" + PersonTable.PERSON_INDEX_TYPE,
      Map("es.mapping.id" -> "id"))
    messages.foreachRDD(rdd => saveOffsets(zkClient, zkHosts, zkPath, rdd))
  }

  def car2es(ssc: StreamingContext, zkClient: ZkClient, kafkaParams: Map[String, String], topics: Set[String],
             zkHosts: String, zkPath: String) {
    import org.elasticsearch.spark.streaming._
    val messages = createCustomDirectKafkaStream(ssc, kafkaParams, zkHosts, zkPath, topics)
    val kafkfDF = messages.map(data => (data._1, JacksonUtil.toObject(data._2, classOf[CarObject])))
      .map(carObject => Car(
        carObject._1, carObject._2.getsFtpUrl(), carObject._2.getbFtpUrl(), carObject._2.getTimeStamp,
        carObject._2.getIpcId, carObject._2.getHostname, carObject._2.getbAbsolutePath(), carObject._2.getsAbsolutePath(),
        carObject._2.getAttribute.getVehicle_object_type, carObject._2.getAttribute.getBelt_maindriver,
        carObject._2.getAttribute.getBelt_codriver, carObject._2.getAttribute.getBrand_name,
        carObject._2.getAttribute.getCall_code, carObject._2.getAttribute.getVehicle_color,
        carObject._2.getAttribute.getCrash_code, carObject._2.getAttribute.getDanger_code,
        carObject._2.getAttribute.getMarker_code, carObject._2.getAttribute.getPlate_schelter_code,
        carObject._2.getAttribute.getPlate_flag_code, carObject._2.getAttribute.getPlate_licence,
        carObject._2.getAttribute.getPlate_destain_code, carObject._2.getAttribute.getPlate_color_code,
        carObject._2.getAttribute.getPlate_type_code, carObject._2.getAttribute.getRack_code,
        carObject._2.getAttribute.getSunroof_code, carObject._2.getAttribute.getVehicle_type,
        carObject._2.getFeature, carObject._2.getBitfeature))
    kafkfDF.map(data => data.toEsMap).saveToEs(VehicleTable.INDEX + "/" + VehicleTable.TYPE,
      Map("es.mapping.id" -> "id"))
    messages.foreachRDD(rdd => saveOffsets(zkClient, zkHosts, zkPath, rdd))

  }

  private def createCustomDirectKafkaStream(ssc: StreamingContext, kafkaParams: Map[String, String], zkHosts: String
                                            , zkPath: String, topics: Set[String]): InputDStream[(String, String)] = {
    val topic = topics.last
    val zKClient = new ZkClient(zkHosts)
    val storedOffsets = readOffsets(zKClient, zkHosts, zkPath, topic)
    val kafkaStream = storedOffsets match {
      case None =>
        KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)
      case Some(fromOffsets) =>
        val messageHandler = (mmd: MessageAndMetadata[String, String]) => (mmd.key(), mmd.message())
        KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder
          , (String, String)](ssc, kafkaParams, fromOffsets, messageHandler)
    }
    kafkaStream
  }

  private def readOffsets(zkClient: ZkClient, zkHosts: String, zkPath: String, topic: String): Option[Map[TopicAndPartition, Long]] = {
    log.info("=========================== Read Offsets =============================")
    log.info("Reading offsets from Zookeeper")
    val stopwatch = Stopwatch.createUnstarted()
    val (offsetsRangesStrOpt, _) = ZkUtils.readDataMaybeNull(zkClient, zkPath)
    offsetsRangesStrOpt match {
      case Some(offsetsRangesStr) =>
        log.info(s"Read offset ranges: $offsetsRangesStr")
        val offsets = offsetsRangesStr.split(",")
          .map(x => x.split(":"))
          .map {
            case Array(partitionStr, offsetStr) => TopicAndPartition(topic, partitionStr.toInt) -> offsetStr.toLong
          }.toMap
        log.info("Done reading offsets from Zookeeper. Took " + stopwatch)
        Some(offsets)
      case None =>
        log.info("No offsets found in Zookeeper. Took " + stopwatch)
        log.info("==================================================================")
        None
    }
  }

  private def saveOffsets(zkClient: ZkClient, zkHosts: String, zkPath: String, rdd: RDD[_]): Unit = {
    log.info("==========================Save Offsets============================")
    log.info("Saving offsets to Zookeeper")
    val stopwatch = Stopwatch.createUnstarted()
    val offsetsRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
    offsetsRanges.foreach(offsetRange => log.debug(s"Using $offsetRange"))
    val offsetsRangesStr = offsetsRanges.map(offsetRange => s"${offsetRange.partition}:${offsetRange.fromOffset}")
      .mkString(",")
    log.info("Writing offsets to Zookeeper zkClient=" + zkClient + " zkHosts=" + zkHosts + "zkPath=" + zkPath + " offsetsRangesStr:" + offsetsRangesStr)
    ZkUtils.updatePersistentPath(zkClient, zkPath, offsetsRangesStr)
    log.info("Done updating offsets in Zookeeper. Took " + stopwatch)
    log.info("==================================================================")
  }
}

