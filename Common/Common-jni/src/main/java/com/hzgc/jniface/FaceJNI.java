package com.hzgc.jniface;

import java.io.Serializable;
import java.util.ArrayList;

public class FaceJNI implements Serializable {

    static {
        System.loadLibrary("JNILIB");
    }

    /**
     * 算法初始化操作，此处为初始化一次,若需要调用n次可以循环调用次方法
     * 算法初始化次数增加在一定程度上会提高特征提取效率,但是会没初始化一次
     * 会占用一定的显存,所以需要适量
     */
    public static native void init();

    /**
     * 算法初始化操作，此处可指定初始化次数,若需要调用n次可以循环调用次方法
     * 算法初始化次数增加在一定程度上会提高特征提取效率,但是会没初始化一次
     * 会占用一定的显存,所以需要适量
     */
    public static native void initThreadNum(int threadNum);

    /**
     * 大图检测功能,此方法可以同时检测人,车,脸三种
     *
     * @param pictureStream 二进制图片
     * @param pictureFormat 图片格式
     * @return 检测结果,检测不到返回 null
     */
    public static native ArrayList<SmallImage> bigPictureCheck(byte[] pictureStream, String pictureFormat);

    /**
     * 大图检测功能,此方法只能检测人脸图片
     *
     * @param pictureStream 二进制图片
     * @param pictureFormat 图片格式
     * @return 检测结果,检测不到返回 null
     */
    public static native ArrayList<SmallImage> faceCheck(byte[] pictureStream, String pictureFormat);

    /**
     * 大图检测功能,此方法只能检测行人图片
     *
     * @param pictureStream 二进制图片
     * @param pictureFormat 图片格式
     * @return 检测结果,检测不到返回 null
     */
    public static native ArrayList<SmallImage> personCheck(byte[] pictureStream, String pictureFormat);

    /**
     * 大图检测功能,此方法只能检测车辆图片
     *
     * @param pictureStream 二进制图片
     * @param pictureFormat 图片格式
     * @return 检测结果,检测不到返回 null
     */
    public static native ArrayList<SmallImage> carCheck(byte[] pictureStream, String pictureFormat);

    /**
     * 大图检测功能,此方法可同时检测人脸和行人图片
     *
     * @param pictureStream 二进制图片
     * @param pictureFormat 图片格式
     * @return 检测结果,检测不到返回 null
     */
    public static native ArrayList<SmallImage> faceAndPersonCheck(byte[] pictureStream, String pictureFormat);

    /**
     * 大图检测功能,此方法可同时检测人脸和车辆图片
     *
     * @param pictureStream 二进制图片
     * @param pictureFormat 图片格式
     * @return 检测结果,检测不到返回 null
     */
    public static native ArrayList<SmallImage> faceAndCarCheck(byte[] pictureStream, String pictureFormat);

    /**
     * 大图检测功能,此方法可同时检测行人和车辆图片
     *
     * @param pictureStream 二进制图片
     * @param pictureFormat 图片格式
     * @return 检测结果,检测不到返回 null
     */
    public static native ArrayList<SmallImage> personAndCarCheck(byte[] pictureStream, String pictureFormat);

    /**
     * 人脸特征提取
     *
     * @param pictureStream 小图二进制图片
     * @param pictureFormat 图片格式
     * @return 提取结果,提取不到则返回 null
     */
    public static native FaceAttribute faceFeatureExtract(byte[] pictureStream, String pictureFormat);


    /**
     * 人脸比对粗筛
     * @param featureList 要查询的特征值集合,即可查单条或者多条
     * @param queryList 被比对的特征值集合
     * @param topN 取前 n 条
     * @return 比对结果
     */
    public static native ArrayList<CompareResult> faceCompareBit(byte[][] featureList, byte[][] queryList, int topN);

    /**
     * 人脸比对精筛
     * @param diku  数据底库
     * @param queryList 图片数据
     * @param topN 取前 n 条
     * @return 比对结果
     */
    public static native ArrayList<CompareResult> faceCompareFloat(float[][] diku, float[][] queryList, int topN);
}
