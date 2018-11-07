package com.hzgc.collect.controller;

import com.hzgc.collect.bean.Base64Bean;
import com.hzgc.collect.bean.ImageDTO;
import com.hzgc.collect.service.extract.CarExtractService;
import com.hzgc.collect.service.extract.FaceExtractService;
import com.hzgc.collect.service.extract.PersonExtractService;
import com.hzgc.common.service.carattribute.bean.CarAttribute;
import com.hzgc.common.service.carattribute.service.CarAttributeService;
import com.hzgc.common.service.error.RestErrorCode;
import com.hzgc.common.service.faceattribute.bean.Attribute;
import com.hzgc.common.service.faceattribute.service.AttributeService;
import com.hzgc.common.service.personattribute.bean.PersonAttribute;
import com.hzgc.common.service.personattribute.service.PersonAttributeService;
import com.hzgc.common.service.response.ResponseResult;
import com.hzgc.common.service.rest.BigDataPath;
import com.hzgc.jniface.*;
import com.hzgc.seemmo.util.BASE64Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.List;

@RestController
@Api(tags = "特征属性服务")
@Slf4j
public class ExtractController {

    @Autowired
    @SuppressWarnings("unused")
    private FaceExtractService faceExtractService;

    @Autowired
    @SuppressWarnings("unused")
    private AttributeService faceAttributeService;

    @Autowired
    @SuppressWarnings("unused")
    private PersonExtractService personExtractService;

    @Autowired
    @SuppressWarnings("unused")
    private PersonAttributeService personAttributeService;

    @Autowired
    @SuppressWarnings("unused")
    private CarExtractService carExtractService;

    @Autowired
    @SuppressWarnings("unused")
    private CarAttributeService carAttributeService;

    @ApiOperation(value = "特征值提取", response = BigPictureData.class)
    @RequestMapping(value = BigDataPath.FEATURE_EXTRACT, method = RequestMethod.POST)
    public ResponseResult featureExtract(@ApiParam(name = "image", value = "图片") MultipartFile image,
                                         @ApiParam(name = "type", value = "检测类型")ImageDTO imageDTO) {
        byte[] imageBin = null;
        try {
            imageBin = image.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if ("0".equals(imageDTO.getType())) { //人脸提取特征
            BigPictureData bigPictureData = faceExtractService.featureExtractByImage(imageBin);
            bigPictureData.setImageType("0");
            return ResponseResult.init(bigPictureData);
        }
        if ("1".equals(imageDTO.getType())) { //行人提取特征
            BigPersonPictureData bigPersonPictureData = personExtractService.featureExtractByImage(imageBin);
            bigPersonPictureData.setImageType("1");
            return ResponseResult.init(bigPersonPictureData);
        }
        if ("2".equals(imageDTO.getType())) { //车辆提取特征
            BigCarPictureData bigCarPictureData = carExtractService.carExtractByImage(imageBin);
            bigCarPictureData.setImageType("2");
            return ResponseResult.init(bigCarPictureData);
        }
        return null;
    }

    @ApiOperation(value = "人脸特征值提取", response = BigPictureData.class)
    @RequestMapping(value = BigDataPath.FEATURE_EXTRACT_BIN, method = RequestMethod.POST)
    public ResponseResult <BigPictureData> faceFeatureExtract(@ApiParam(name = "image", value = "图片") MultipartFile image) {
        byte[] imageBin = null;
        if (image == null) {
            log.error("Start extract feature by binary, image is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT);
        }
        try {
            imageBin = image.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BigPictureData bigPictureData = faceExtractService.featureExtractByImage(imageBin);
        if (null == bigPictureData) {
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "提取不到特征值");
        }
        return ResponseResult.init(bigPictureData);
    }

    //根据图片提取人脸特征值
    @ApiIgnore
    @RequestMapping(value = BigDataPath.FEATURE_EXTRACT_BYTES, method = RequestMethod.POST)
    public PictureData getFeatureExtract(@RequestBody byte[] bytes) {
        if (null != bytes) {
            return faceExtractService.featureExtractByImage(bytes);
        }
        log.info("Bytes param is null");
        return null;
    }

    @ApiOperation(value = "人脸属性查询", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.FACE_ATTRIBUTE, method = RequestMethod.GET)
    public ResponseResult <List <Attribute>> getAttribute() {
        List <Attribute> attributeList;
        attributeList = faceAttributeService.getAttribute();
        if (null != attributeList) {
            log.info("AttributeList acquires is success");
            return ResponseResult.init(attributeList);
        } else {
            log.error("AttributeList acquires is null");
            return ResponseResult.error(RestErrorCode.RECORD_NOT_EXIST);
        }
    }

    @ApiOperation(value = "行人属性提取", response = BigPersonPictureData.class)
    @RequestMapping(value = BigDataPath.PERSON_FEATURE_EXTRACT_BIN, method = RequestMethod.POST)
    public ResponseResult <BigPersonPictureData> personFeatureExtract(@ApiParam(name = "image", value = "图片") MultipartFile image) {
        byte[] imageBin = null;
        if (null == image) {
            log.error("Start extract person feature by binary, image is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT);
        }
        try {
            imageBin = image.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BigPersonPictureData bigPersonPictureData = personExtractService.featureExtractByImage(imageBin);
        if (null == bigPersonPictureData) {
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "提取不到特征值");
        }
        return ResponseResult.init(bigPersonPictureData);
    }

    @ApiOperation(value = "行人属性查询", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.PERSON_ATTRIBUTE, method = RequestMethod.GET)
    public ResponseResult <List <PersonAttribute>> getPersonAttribute() {
        List <PersonAttribute> attributeList = personAttributeService.getPersonAttribute();
        if (null != attributeList) {
            log.info("AttributeList acquire is succeed");
            return ResponseResult.init(attributeList);
        } else {
            log.error("AttributeList is null");
            return ResponseResult.error(RestErrorCode.RECORD_NOT_EXIST);
        }
    }

    @ApiOperation(value = "车辆属性提取", response = BigCarPictureData.class)
    @RequestMapping(value = BigDataPath.CAR_EXTRACT, method = RequestMethod.POST)
    public ResponseResult <BigCarPictureData> carExtract(@ApiParam(name = "image", value = "图片") MultipartFile image) {
        byte[] imageBin = null;
        if (image == null) {
            log.error("Start car extract by binary, image is null");
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT);
        }
        try {
            imageBin = image.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BigCarPictureData bigCarPictureData = carExtractService.carExtractByImage(imageBin);
        if (null == bigCarPictureData) {
            return ResponseResult.error(RestErrorCode.ILLEGAL_ARGUMENT, "提取不到特征值");
        }
        return ResponseResult.init(bigCarPictureData);
    }

    @ApiOperation(value = "车辆属性查询", response = ResponseResult.class)
    @RequestMapping(value = BigDataPath.CAR_ATTRIBUTE, method = RequestMethod.GET)
    public ResponseResult <List <CarAttribute>> getCarAttribute() {
        List <CarAttribute> attributeList = carAttributeService.getCarAttribute();
        if (null != attributeList) {
            log.info("AttributeList acquire is succeed");
            return ResponseResult.init(attributeList);
        } else {
            log.error("AttributeList is null");
            return ResponseResult.error(RestErrorCode.RECORD_NOT_EXIST);
        }
    }

    //车辆base64提取特征
    @ApiOperation(value = "车辆图片base64提取特征", response = BigPictureData.class)
    @RequestMapping(value = BigDataPath.CAR_FEATURE_CHECK_BASE64, method = RequestMethod.POST)
    public ResponseEntity <BigCarPictureData> carFeatureExtract_base64(@RequestBody Base64Bean base64Bean) {
        if (null != base64Bean) {
            String baseStr  = base64Bean.getBase64String();
            if (null != baseStr && baseStr.length() > 0) {
                byte[] imageBin = BASE64Util.base64Str2BinArry(baseStr);
                BigCarPictureData bigCarPictureData = carExtractService.carExtractByImage(imageBin);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(bigCarPictureData);
            }
        }
        log.info("Car base64 is null");
        return null;
    }

    //行人base64提取特征
    @ApiOperation(value = "行人图片base64提取特征", response = BigPictureData.class)
    @RequestMapping(value = BigDataPath.PERSON_FEATURE_CHECK_BASE64, method = RequestMethod.POST)
    public ResponseEntity <BigPersonPictureData> personFeatureExtract_base64(@RequestBody Base64Bean base64Bean) {
        if (null != base64Bean) {
            String baseStr = base64Bean.getBase64String();
            if (null != baseStr && baseStr.length() > 0) {
                byte[] imageBin = BASE64Util.base64Str2BinArry(baseStr);
                BigPersonPictureData bigPersonPictureData = personExtractService.featureExtractByImage(imageBin);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(bigPersonPictureData);
            }
        }
        log.info("Person basestr is null");
        return null;
    }

    @ApiIgnore(value = "内部调用的人脸提特征接口,入参为图片的Base64字符串")
    @RequestMapping(value = BigDataPath.FEATURE_EXTRACT_BASE64, method = RequestMethod.POST)
    public ResponseEntity <PictureData> faceFeatureExtract_base64(@RequestBody String baseStr) {
        PictureData pictureData = faceExtractService.featureExtractBySamllImage(baseStr);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(pictureData);
    }

    @ApiOperation(value = "人脸图片base64提取特征", response = BigPictureData.class)
    @RequestMapping(value = BigDataPath.FACE_FEATURE_EXTRACT_BASE64, method = RequestMethod.POST)
    public ResponseEntity <BigPictureData> faceFeatureExtract_base64(@RequestBody Base64Bean base64Bean) {
        if (null != base64Bean) {
            String baseStr = base64Bean.getBase64String();
            if (null != baseStr && baseStr.length() > 0) {
                BigPictureData pictureData = faceExtractService.featureExtractByImage( BASE64Util.base64Str2BinArry(baseStr));
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(pictureData);
            }
        }
        log.info("Face basestr is null");
        return null;
    }

    @ApiIgnore(value = "内部调用的人脸检测接口,入参为图片的Base64字符串")
    @RequestMapping(value = BigDataPath.FEATURE_CHECK_BASE64, method = RequestMethod.POST)
    public ResponseEntity <PictureData> faceFeatureCheck_base64(@RequestBody String baseStr) {
        PictureData pictureData = faceExtractService.featureCheckByImage(baseStr);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(pictureData);
    }
}
