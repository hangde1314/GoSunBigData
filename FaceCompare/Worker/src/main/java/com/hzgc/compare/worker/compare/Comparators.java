package com.hzgc.compare.worker.compare;

import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.compare.Feature;
import com.hzgc.compare.SearchResult;
import javafx.util.Pair;

import java.util.List;
import java.util.Map;

/**
 * 对比两次，第一次用比byte[]做对比，得到对比结果，然后读取HBase得到特征值，用feature去对比
 */
public interface Comparators {

    /**
     * 先对内存数据进行过滤，根据参数1和参数2
     * @param dateStart
     * @param dateEnd
     * @return Record<rowkey, feature>
     */
     List<Pair<String, byte[]>> filter(String dateStart, String dateEnd);

    /**
     * 若数据量过大则需要第一次对比
     * @param feature
     * @param num
     * @param data
     * @return List<rowkey>
     */
    List<String> compareFirst(byte[] feature, int num, List<Pair<String, byte[]>> data);

    /**
     * 若数据量过大则需要第一次对比(多图多人)
     * @param features
     * @param num
     * @param data
     * @return List<rowkey>
     */
    List<String> compareFirstNotSamePerson(List<Feature> features, int num, List<Pair<String, byte[]>> data);

    /**
     * 若数据量过大则需要第一次对比(多图单人)
     * @param features
     * @param num
     * @param data
     * @return List<rowkey>
     */
    List<String> compareFirstTheSamePerson(List<byte[]> features, int num, List<Pair<String, byte[]>> data);

    /**
     * 把从HBase读取的数据，进行第二次对比
     * @param feature
     * @param sim
     * @param data
     * @return
     */
    SearchResult compareSecond(float[] feature, float sim, List<FaceObject> data, List<Integer> sorts);

    SearchResult compareSecondTheSamePerson(List<float[]> features, float sim, List<FaceObject> data,
                                            List<Integer> sorts);

    Map<String, SearchResult> compareSecondNotSamePerson(List<Feature> features, float sim, List<FaceObject> data,
                                                         List<Integer> sorts);
}
