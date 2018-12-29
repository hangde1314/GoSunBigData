package com.hzgc.collect.service.parser.impl;

import com.hzgc.collect.config.CollectContext;
import com.hzgc.collect.service.parser.AbstractParser;
import com.hzgc.collect.service.parser.FtpPathMetaData;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 海康球机路径解析(人脸检测功能)
 */
@Slf4j
public class HiKVisionParser_DS_2DF8226XYZ_BC extends AbstractParser {
    public HiKVisionParser_DS_2DF8226XYZ_BC(CollectContext collectContext) {
        super(collectContext);
    }

    @Override
    public boolean canParse(String path) {
        return !path.contains("unknown") && path.contains(".jpg");
    }

    /**
     * /DS-2DF8226XYZ-BC/DS-2DF8226XYZ-BC20180306AACHC08029035W/2018_11_23-2018_11_23/12.7.0.37_01_20181212095726453_FACE_DETECTION.jpg
     */
    @Override
    public FtpPathMetaData parse(String path) {
        FtpPathMetaData message = new FtpPathMetaData();
        try {
            String ipcID = path.substring(path.indexOf("/", 1))
                    .substring(1, path.substring(path.indexOf("/", 1)).indexOf("/", 1));
            message.setIpcid(ipcID);
            message.setTimeStamp(dateFormat.format(new Date()));
        } catch (Exception e) {
            log.error("Parse failed, path is:?" + path);
        }
        return message;
    }

    /**
     * eg:
     * ftp://s100:2121/DS-2DF8226XYZ-BC/DS-2DF8226XYZ-BC20180306AACHC08029035W/2018_11_23-2018_11_23/12.7.0.37_01_20181212095726453_FACE_DETECTION_face1.jpg
     * ------>
     * ftp://s100:2121/DS-2DF8226XYZ-BC/DS-2DF8226XYZ-BC20180306AACHC08029035W/2018_11_23-2018_11_23/12.7.0.37_01_20181212095726453_FACE_DETECTION.jpg
     */
    @Override
    public String surlToBurl(String surl) {
        return surl.substring(0, surl.lastIndexOf("_")) + ".jpg";
    }

    @Override
    public String ftpUrl_b2s(String burl, String type, int index) {
        return burl.replace(".jpg", "_" + type + index + ".jpg");
    }

    @Override
    public String path_b2s(String burl, String type, int index) {
        return burl.replace(".jpg", "_" + type + index + ".jpg");
    }
}