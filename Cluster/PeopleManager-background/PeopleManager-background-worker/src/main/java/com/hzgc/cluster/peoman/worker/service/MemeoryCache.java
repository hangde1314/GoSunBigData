package com.hzgc.cluster.peoman.worker.service;

import com.hzgc.cluster.peoman.worker.dao.PictureMapper;
import com.hzgc.cluster.peoman.worker.model.Picture;
import com.hzgc.jniface.CompareResult;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceFeatureInfo;
import com.hzgc.jniface.FaceJNI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class MemeoryCache {
    @Autowired
    @SuppressWarnings("unused")
    private PictureMapper pictureMapper;

    @Value("face.float.threshold")
    @SuppressWarnings("unused")
    private float floatThreshold;

    @Value("face.float.compare.open")
    @SuppressWarnings("unused")
    private boolean isOpen;

    @Value("face.bit.threshold")
    @SuppressWarnings("unused")
    private float featureThreshold;

    private Map<Integer, String> indexToPictureKey = new HashMap<>();
    private LinkedList<byte[]> bitFeatureList = new LinkedList<>();
    private Map<String, List<ComparePicture>> pictureMap = new HashMap<>();
    private AtomicInteger atomicInteger = new AtomicInteger();
    private ReentrantLock lock = new ReentrantLock();

    public MemeoryCache() {
        FaceJNI.init();
    }

    void putData(List<ComparePicture> pictureList) {
        try {
            lock.lock();
            for (ComparePicture picture : pictureList) {
                int index = atomicInteger.getAndIncrement();
                bitFeatureList.add(index, picture.getBitFeature());
                indexToPictureKey.put(index, picture.getPeopleId());
                picture.setIndex(index);
                if (pictureMap.containsKey(picture.getPeopleId())) {
                    List<ComparePicture> comparePictures = pictureMap.get(picture.getPeopleId());
                    comparePictures.add(picture);
                } else {

                    List<ComparePicture> comparePictures = new ArrayList<>();
                    comparePictures.add(picture);
                    pictureMap.put(picture.getPeopleId(), comparePictures);
                }
            }

        } finally {
            lock.unlock();
        }
    }

    public ComparePicture comparePicture(FaceAttribute faceAttribute) {
        byte[] bitFeature = faceAttribute.getBitFeature();
        if (bitFeature != null && bitFeature.length > 0) {
            byte[][] queryList = new byte[1][];
            queryList[0] = bitFeature;
            ArrayList<CompareResult> compareResList =
                    FaceJNI.faceCompareBit(bitFeatureList.toArray(new byte[0][]), queryList, 1);
            CompareResult compareResult = compareResList.get(0);
            ArrayList<FaceFeatureInfo> featureInfos = compareResult.getPictureInfoArrayList();
            FaceFeatureInfo faceFeatureInfo = featureInfos.get(0);
            int index = faceFeatureInfo.getIndex();
            String pictureKey = indexToPictureKey.get(index);
            List<ComparePicture> comparePictures = pictureMap.get(pictureKey);
            ComparePicture comparePicture = null;
            for (ComparePicture pic : comparePictures) {
                if (pic.getIndex() == index) {
                    comparePicture = pic;
                }
            }
            if (isOpen && comparePicture != null) {
                Picture picture = pictureMapper.selectByPictureId(comparePicture.getId());
                String floatFeatureStr = picture.getFeature();
                if (floatFeatureStr != null && !"".equals(floatFeatureStr)) {
                    float[] floatFeature = FaceJNI.string2floatArray(floatFeatureStr);
                    if (floatFeature.length == 512 && faceAttribute.getFeature().length == 512) {
                        float sim = FaceJNI.featureCompare(floatFeature, faceAttribute.getFeature());
                        if (sim >= this.floatThreshold) {
                            return comparePicture;
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                if (faceFeatureInfo.getScore() >= 90) {
                    return comparePicture;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }


    List<ComparePicture> getPeople(String peopleid) {
        return pictureMap.get(peopleid);
    }
}
