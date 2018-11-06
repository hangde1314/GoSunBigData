package com.hzgc.service.imsi.controller;

import com.hzgc.common.service.api.service.InnerService;
import com.hzgc.common.service.error.RestErrorCode;
import com.hzgc.common.service.response.ResponseResult;
import com.hzgc.common.service.rest.BigDataPath;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.jniface.PictureData;
import com.hzgc.seemmo.util.BASE64Util;
import com.hzgc.service.imsi.model.ImsiInfo;
import com.hzgc.service.imsi.model.MacInfo;
import com.hzgc.service.imsi.service.ImsiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ImsiController {

    @Autowired
    private ImsiService imsiService;

    @Autowired
    private InnerService innerService;

    @RequestMapping(value = BigDataPath.IMSI_SEARCH_BY_TIME, method = RequestMethod.GET)
    public List <ImsiInfo> queryByTime(Long time) {
        if (null == time) {
            log.error("Start search imsi by time, but search option is null");
            return null;
        }
        log.info("Start search imsi by time, this time is: " + time);
        return imsiService.queryByTime(time);
    }

    @RequestMapping(value = "/aaa", method = RequestMethod.GET)
    public void sjdajd(){
        String imageStr = BASE64Util.getImageStr("C:\\Users\\g10255\\Desktop\\测试图片数据\\08.31.23[M][0@0][face1].jpg");
        PictureData pictureData = innerService.faceFeautreExtract(imageStr);
        System.out.println(JacksonUtil.toJson(pictureData));
    }
}
