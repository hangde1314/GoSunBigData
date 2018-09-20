package com.hzgc.cluster.spark.consumer;

import com.hzgc.cluster.spark.util.PropertiesUtil;
import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.common.service.facedynrepo.FaceTable;
import com.hzgc.common.util.es.ElasticSearchHelper;
import com.hzgc.jniface.FaceAttribute;
import org.apache.log4j.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.rest.RestStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PutDataToEs implements Serializable {
    private TransportClient esClient;

    private PutDataToEs() {
        Properties properties = PropertiesUtil.getProperties();
        String es_cluster = properties.getProperty("es.cluster.name");
        String es_hosts = properties.getProperty("es.hosts");
        Integer es_port = Integer.parseInt(properties.getProperty("es.cluster.port"));
        esClient = ElasticSearchHelper.getEsClient(es_cluster, es_hosts, es_port);
    }

    private static PutDataToEs instance = null;

    public static PutDataToEs getInstance() {
        if (instance == null) {
            synchronized (PutDataToEs.class) {
                if (instance == null) {
                    instance = new PutDataToEs();
                }
            }
        }
        return instance;
    }

    public int putDataToEs(String ftpurl, FaceObject faceObject) {
        IndexResponse indexResponse = new IndexResponse();
        Map<String, Object> map = new HashMap<>();
        if (faceObject.getId() != null && faceObject.getId().length() > 0) {
            map.put(FaceTable.HOSTNAME, faceObject.getHostname());
            map.put(FaceTable.TIMESTAMP, faceObject.getTimeStamp());
            map.put(FaceTable.IPCID, faceObject.getIpcId());
            map.put(FaceTable.SFTPURL, faceObject.getsFtpUrl());
            map.put(FaceTable.BFTPURL, faceObject.getbFtpUrl());
            map.put(FaceTable.BABSOLUTEPATH, faceObject.getbAbsolutePath());
            map.put(FaceTable.SABSOLUTEPATH, faceObject.getsAbsolutePath());
            map.put(FaceTable.HOSTNAME, faceObject.getHostname());
            map.put(FaceTable.AGE, faceObject.getAttribute().getAge());
            map.put(FaceTable.MASK, faceObject.getAttribute().getMask());
            map.put(FaceTable.EYEGLASSES, faceObject.getAttribute().getEyeglasses());
            map.put(FaceTable.GENDER, faceObject.getAttribute().getGender());
            map.put(FaceTable.HUZI, faceObject.getAttribute().getHuzi());
            indexResponse = esClient.prepareIndex(FaceTable.DYNAMIC_INDEX,
                    FaceTable.PERSON_INDEX_TYPE, faceObject.getId()).setSource(map).get();
        }
        if (indexResponse.getVersion() == 1) {
            return 1;
        } else {
            return 0;
        }
    }
}
