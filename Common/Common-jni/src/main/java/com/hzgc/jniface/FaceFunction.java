package com.hzgc.jniface;

import org.xerial.snappy.Snappy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;

public class FaceFunction {
    private static final String SPLIT = ":";

    /**
     * 特征提取方法 （内）（赵喆）
     *
     * @param imageData 将图片转为字节数组传入
     * @return 输出float[]形式的特征值
     */
    public static FaceAttribute featureExtract(byte[] imageData) {
        BufferedImage faceImage;
        try {
            if (null != imageData) {
                FaceAttribute faceAttribute = new FaceAttribute();
                int successOrfailue;
                faceImage = ImageIO.read(new ByteArrayInputStream(imageData));
                int height = faceImage.getHeight();
                int width = faceImage.getWidth();
                int[] rgbArray = new int[height * width * 3];
                for (int h = 0; h < height; h++) {
                    for (int w = 0; w < width; w++) {
                        int pixel = faceImage.getRGB(w, h);//RGB颜色模型
                        rgbArray[h * width * 3 + w * 3] = (pixel & 0xff0000) >> 16;//0xff0000 红色
                        rgbArray[h * width * 3 + w * 3 + 1] = (pixel & 0xff00) >> 8;
                        rgbArray[h * width * 3 + w * 3 + 2] = (pixel & 0xff);
                    }
                }
                successOrfailue = NativeFunction.feature_extract(faceAttribute, rgbArray, width, height);
                if (successOrfailue == 0) {
                    return faceAttribute;
                } else {
                    return new FaceAttribute();
                }
            } else {
                throw new NullPointerException("The data of picture is null");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FaceAttribute();
    }

    /**
     * 将特征值（String）转换为特征值（float[]）（内）（赵喆）
     *
     * @param feature 传入编码为UTF-8的String
     * @return 返回float[]类型的特征值
     */
    @Deprecated
    public static float[] string2floatArray(String feature) {
        if (feature != null && feature.length() > 0) {
            float[] featureFloat = new float[512];
            String[] strArr = feature.split(SPLIT);
            for (int i = 0; i < strArr.length; i++) {
                featureFloat[i] = Float.valueOf(strArr[i]);
            }
            return featureFloat;
        }
        return new float[0];
    }

    /**
     * 相似度比较，范围[0-1]
     *
     * @param currentFeatureStr 需要比对的特征值
     * @param historyFeatureStr 库中的特征值
     * @return 相似度
     */
    @Deprecated
    public static float featureCompare(String currentFeatureStr, String historyFeatureStr) {
        float[] currentFeature = string2floatArray(currentFeatureStr);
        float[] historyFeature = string2floatArray(historyFeatureStr);
        return featureCompare(currentFeature, historyFeature);
    }

    public static float featureCompare(float[] currentFeature, float[] historyFeature) {
        double similarityDegree = 0;
        double currentFeatureMultiple = 0;
        double historyFeatureMultiple = 0;
        for (int i = 0; i < currentFeature.length; i++) {
            similarityDegree = similarityDegree + currentFeature[i] * historyFeature[i];
            currentFeatureMultiple = currentFeatureMultiple + Math.pow(currentFeature[i], 2);//pow 返回currentFeature[i] 平方
            historyFeatureMultiple = historyFeatureMultiple + Math.pow(historyFeature[i], 2);
        }

        double tempSim = similarityDegree / Math.sqrt(currentFeatureMultiple) / Math.sqrt(historyFeatureMultiple);//sqrt 平方根 余弦相似度
        double actualValue = new BigDecimal((0.5 + (tempSim / 2)) * 100).//余弦相似度表示为cosineSIM=0.5cosθ+0.5
                setScale(2, BigDecimal.ROUND_HALF_UP).//ROUND_HALF_UP=4 保留两位小数四舍五入
                doubleValue();
        if (actualValue >= 100) {
            return 100;
        }
        return (float) actualValue;
    }

    /**
     * 将特征值（float[]）转换为字符串（String）（内）（赵喆）
     *
     * @param feature 传入float[]类型的特征值
     * @return 输出指定编码为UTF-8的String
     */
    @Deprecated
    public static String floatArray2string(float[] feature) {
        if (feature != null && feature.length == 512) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < feature.length; i++) {
                if (i == 511) {
                    sb.append(feature[i]);
                } else {
                    sb.append(feature[i]).append(":");
                }
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * 将float特征值转为Base64加密的String类型
     * @param feature float特征值
     * @return Base64加密的String类型
     */
    public static String floatFeature2Base64Str(float[] feature) {
        if (feature != null && feature.length == 512) {
            try {
                byte[] zipFeautre = Snappy.compress(feature);
                return Base64.getEncoder().encodeToString(zipFeautre);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 将Base64加密的特征值转为float特征值
     * @param base64Str Base64加密的特征值
     * @return float特征值
     */
    public static float[] base64Str2floatFeature(String base64Str) {
        if (base64Str != null && !"".equals(base64Str)) {
            byte[] zipFeature = Base64.getDecoder().decode(base64Str);
            try {
                return Snappy.uncompressFloatArray(zipFeature);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new float[0];
    }

    /**
     * 将bit特征值转为Base64字符串
     * @param bitFeature bit特征值
     * @return Base64字符串
     */
    public static String bitFeautre2Base64Str(byte[] bitFeature) {
        if (bitFeature != null && bitFeature.length > 0) {
            return Base64.getEncoder().encodeToString(bitFeature);
        }
        return "";
    }

    /**
     * 将Base64字符串转为bit特征值
     * @param base64Str Base64字符串
     * @return bit特征值
     */
    public static byte[] base64Str2BitFeature(String base64Str) {
        if (base64Str != null && !"".equals(base64Str)) {
            return Base64.getDecoder().decode(base64Str);
        }
        return new byte[0];
    }
}
