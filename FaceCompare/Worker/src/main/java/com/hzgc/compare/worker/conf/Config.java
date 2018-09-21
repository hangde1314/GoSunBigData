package com.hzgc.compare.worker.conf;

import com.hzgc.compare.worker.util.PropertiesUtil;

import java.util.Properties;

public class Config {
    public static final int SAVE_TO_LOCAL = 0;
    public static final int SAVE_TO_HDFS = 1;
    public static final int WORKER_CHECK_TASK_TIME = 1000; //检查任务列表的时间间隔
    public static final long WORKER_HBASE_WRITE_TIME = 2000; //写HBase任务的时间间隔
    public static final String KAFKA_GROUP_ID = "distributedCompare";
    public static final String KAFKA_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
    public static final String JOB_PATH = "/worker_job";

    public static int WORKER_BUFFER_SIZE_MAX;
    public static int WORKER_CACHE_SIZE_MAX;
    public static long WORKER_MEMORY_CHECK_TIME;
    public static long WORKER_RECORD_TIME_OUT;
    public static long WORKER_FILE_CHECK_TIME;
    public static String WORKER_FILE_PATH;
    public static long WORKER_FILE_SIZE;
    public static int WORKER_FILE_SAVE_SYSTEM;
    public static int WORKER_FLUSH_PROGRAM;
    public static int WORKER_READFILES_PER_THREAD;
    public static int WORKER_EXECUTORS_TO_COMPARE;
    public static int WORKER_EXECUTORS_TO_LOADFILE;
    public static String KAFKA_TOPIC;
    public static String KAFKA_BOOTSTRAP_SERVERS;
    public static int KAFKA_MAXIMUM_TIME;
    public static String ZOOKEEPER_ADDRESS;
    public static String WORKER_ADDRESS;
    public static int WORKER_RPC_PORT;
    public static String WORKER_ID;
    public static int DELETE_OPEN;
    public static String CLUSTER_NAME ;
    public static String TRACKER_GROUP;
    public static String ES_CLUSTER_NAME;
    public static String ES_HOST;
    public static int ES_CLUSTER_PORT;

    static {
        Properties prop = PropertiesUtil.getProperties();
        WORKER_BUFFER_SIZE_MAX = Integer.parseInt(prop.getProperty("worker.buffer.size.max", 1000 + ""));
        WORKER_CACHE_SIZE_MAX = Integer.parseInt(prop.getProperty("worker.cach.size.max", 20000000 + ""));//内存中缓存数据的最大值
        WORKER_MEMORY_CHECK_TIME = Long.parseLong(prop.getProperty("worker.memory.check.time", 1000L * 60 * 30 + ""));//内存数据的检查时间间隔
        WORKER_RECORD_TIME_OUT = Long.parseLong(prop.getProperty("work.record.time.out", 35 + ""));//内存中记录的过期时间
        WORKER_FILE_CHECK_TIME = Long.parseLong(prop.getProperty("worker.file.check.time", 24 * 60 * 60 * 1000L + "")); //文件检查时间间隔
        WORKER_FILE_PATH = prop.getProperty("worker.file.path", "");
        WORKER_FILE_SIZE = Long.parseLong(prop.getProperty("worker.file.size", 128L * 1024 * 1024L + "")); //文件保存大小
        WORKER_FILE_SAVE_SYSTEM = Integer.parseInt(prop.getProperty("worker.file.save.system", 0 + "")); //数据持久化的文件系统 0 本地  1 HDFS
        WORKER_FLUSH_PROGRAM = Integer.parseInt(prop.getProperty("worker.flush.program", 0 + "")); //持久化触发方式 0 定期触发  1定量触发
        WORKER_READFILES_PER_THREAD = Integer.parseInt(prop.getProperty("worker.readfiles_per_thread", 1 + "")); //每个线程读取的文件
        WORKER_EXECUTORS_TO_COMPARE = Integer.parseInt(prop.getProperty("worker.executors.to.compare", 10 + ""));
        WORKER_EXECUTORS_TO_LOADFILE = Integer.parseInt(prop.getProperty("worker.executors.to.loadfile", 10 + ""));
        KAFKA_TOPIC = prop.getProperty("kafka.topic");
        KAFKA_BOOTSTRAP_SERVERS = prop.getProperty("kafka.bootstrap.servers");
        KAFKA_MAXIMUM_TIME = Integer.parseInt(prop.getProperty("kafka.maximum.time"));
        ZOOKEEPER_ADDRESS = prop.getProperty("zookeeper.address");
        WORKER_ADDRESS = prop.getProperty("worker.address");
        DELETE_OPEN = Integer.parseInt(prop.getProperty("delete.open"));
        CLUSTER_NAME = prop.getProperty("cluster.name");
        TRACKER_GROUP = prop.getProperty("tasktracker.group");
        ES_CLUSTER_NAME = prop.getProperty("es.cluster.name");
        ES_HOST = prop.getProperty("es.hosts");
        ES_CLUSTER_PORT = Integer.parseInt(prop.getProperty("es.cluster.port"));
    }
}
