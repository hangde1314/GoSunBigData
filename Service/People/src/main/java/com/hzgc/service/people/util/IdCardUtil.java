package com.hzgc.service.people.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class IdCardUtil {
    public static String getBirthday(String idCard){
        return idCard.substring(6, 10) + "年" +
                idCard.substring(10, 12) + "月" +
                idCard.substring(12, 14) + "日";
    }

    public static Integer getAge(String idCard){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Integer age = null;
        try {
            Date birthday = sdf.parse(idCard.substring(6, 14));
            age = Math.toIntExact((new Date().getTime() - birthday.getTime()) / (24L * 60L * 60L * 1000L * 365L));
        } catch (ParseException e) {
            log.error("Age parse error, idCard: " + idCard);
            e.printStackTrace();
        }
        return age;
    }
}
