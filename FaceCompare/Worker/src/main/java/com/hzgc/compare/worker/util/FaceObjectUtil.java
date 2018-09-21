package com.hzgc.compare.worker.util;

import com.google.gson.Gson;
import com.hzgc.common.collect.bean.FaceObject;

public class FaceObjectUtil {
    private static Gson gson;
    static {
        gson = new Gson();
    }
    public static FaceObject jsonToObject(String json) {
        return gson.fromJson(json, FaceObject.class);
    }

    public static float[] jsonToArray(String json) {return  gson.fromJson(json, float[].class); }

    public static String objectToJson(Object obj){
        return gson.toJson(obj);
    }

    public static String arrayToJson(float[] feature) { return  gson.toJson(feature); }
}