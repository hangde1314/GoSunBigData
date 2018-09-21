package com.hzgc.collect.controller;

import com.hzgc.collect.service.http.HttpFtile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class FileController {

    @Autowired
    private HttpFtile httpFtile;

    @RequestMapping("/image")
    public ResponseEntity<byte[]> getFile(@RequestParam(name = "url") String path) {
        log.info("Receive file path:" + path);
        byte[] image;
        if (path != null && !"".equals(path)) {
            image = httpFtile.getFile(path);
        } else {
            image = new byte[0];
        }
        if (image == null || image.length == 0) {
            return ResponseEntity.badRequest().contentType(MediaType.IMAGE_JPEG).body(null);
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
    }
}
