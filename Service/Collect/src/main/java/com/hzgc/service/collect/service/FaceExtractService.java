package com.hzgc.service.collect.service;

import com.hzgc.common.util.basic.UuidUtil;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceJNI;
import com.hzgc.jniface.PictureData;
import com.hzgc.service.collect.util.FtpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FaceExtractService {
    @Autowired
    @SuppressWarnings("unused")
    private FaceExtractService faceExtractService;

    public FaceExtractService() {
        try {
            log.info("Start FaceJNI init....");
             FaceJNI.init();
            log.info("Init FaceJNI successful!");
        } catch (Exception e) {
            log.error("Init FaceJNI failure!");
            e.printStackTrace();
        }
    }

    /**
     * 特征提取
     *
     * @param imageBytes 图片的字节数组
     * @return float[] 特征值:长度为512的float[]数组
     */
    public PictureData featureExtractByImage(byte[] imageBytes) {
        PictureData pictureData = new PictureData();
        pictureData.setImageID(UuidUtil.getUuid());
        pictureData.setImageData(imageBytes);
        FaceAttribute faceAttribute = FaceJNI.featureExtract(imageBytes);
        if (faceAttribute != null) {
            log.info("Face extract successful, image contains feature");
            pictureData.setFeature(faceAttribute);
            return pictureData;
        } else {
            log.info("Face extract failed, image not contains feature");
            return null;
        }
    }

    //ftp获取特征值
    public PictureData getFeatureExtractByFtp(String pictureUrl){
        log.info("PictureUrl is :" + pictureUrl);
        PictureData pictureData;
        //FTP匿名账号Anonymous和密码
        byte[] bytes = FtpUtils.downloadftpFile2Bytes(pictureUrl,"anonymous",null);
        if (null != bytes){
            log.info("Face extract successful, pictureUrl contains feature");
            pictureData = faceExtractService.featureExtractByImage(bytes);
            return pictureData;
        }else {
            log.info("Face extract failed, pictureUrl not contains feature");
            return null;
        }
    }

}
