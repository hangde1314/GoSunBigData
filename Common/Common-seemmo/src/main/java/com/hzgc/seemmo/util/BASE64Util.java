package com.hzgc.seemmo.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class BASE64Util {

    //图片路径
    public static String getImageStr(String imgFile) {
        InputStream inputStream = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream(imgFile);
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Base64.getEncoder().encodeToString(data);
    }

    //根据图片字节数组进行提特征
    public static String getImageStr(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
