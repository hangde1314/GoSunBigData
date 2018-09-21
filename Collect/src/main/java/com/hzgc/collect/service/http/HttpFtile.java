package com.hzgc.collect.service.http;

import com.hzgc.common.util.basic.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HttpFtile {
    public byte[] getFile(String filePath) {
        return FileUtil.fileToByteArray(filePath);
    }
}
