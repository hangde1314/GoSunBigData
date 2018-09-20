package com.hzgc.compare;

import com.hzgc.common.collect.bean.FaceObject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SearchResult {
    private static Integer size = 1000;
    private Record[] records;

    public SearchResult(){
        records = new Record[0];
    }

    public SearchResult(Record[] records){
        this.records = records;
    }

    /**
     * 获取数据的前num条，封装成新的SearchResult
     * @param num
     * @return
     */
    public SearchResult take(int num){
        if(num > records.length){
            return this;
        }
        Record[] recordsTemp = new Record[num];
        System.arraycopy(records, 0, recordsTemp, 0, num);
        return new SearchResult(recordsTemp);
    }

    /**
     * 将当前的records根据Sim排序
     */
    public void sortBySim(){ //TODO 选择合适的排序
//        Arrays.sort(records);
        Arrays.sort(records, new Comparator<Record>() {
            @Override
            public int compare(Record o1, Record o2) {
                return Double.compare(o2.sim, o1.sim);
            }
        });
//        quickSort(records, 0, records.length - 1);
    }

    public void sort(List<Integer> sorts){
        List<SortParam> sortParams = sorts.stream().map(param -> SortParam.values()[param]).collect(Collectors.toList());
        Arrays.sort(records, new Comparator<Record>(){
            @Override
            public int compare(Record o1, Record o2) {
                FaceObject face1 = (FaceObject)o1.getValue();
                FaceObject face2 = (FaceObject)o2.getValue();
                int flug = 0;
                for(SortParam sortParam : sortParams){
                    if(flug == 0){
                        switch (sortParam){
                            case IPC:
                                flug = face1.getIpcId().compareTo(face2.getIpcId());
                                break;
                            case TIMEASC:
                                flug = face1.getTimeStamp().compareTo(face2.getTimeStamp());
                                break;
                            case TIMEDESC:
                                flug = face2.getTimeStamp().compareTo(face1.getTimeStamp());
                                break;
                            case SIMDASC:
                                flug = Double.compare(o2.sim, o1.sim);
                                break;
                            case SIMDESC:
                                flug = Double.compare(o1.sim, o2.sim);
                                break;
                        }
                    }
                }
                return flug;
            }
        });
    }

//    public static void main(String args[]){
//        List<Integer> sorts = new ArrayList<>();
//        sorts.add(1);
//        sorts.add(2);
//        List<SortParam> sortParams = sorts.stream().map(param -> SortParam.values()[param]).collect(Collectors.toList());
//        System.out.println(sortParams);
//    }

    /**
     * 将多个SearchResult的 records 合并，并根据Sim排序
     * @param result
     * @return
     */
    public void merge(SearchResult result){
        if(result == null || result.getRecords().length == 0){
            return;
        }
        if(records == null|| records.length == 0) {
            records = result.getRecords();
        } else {
            Record[] arr1 = records;
            Record[] arr2 = result.getRecords();
            Record[] arr3 =  new Record[arr2.length + arr1.length];
            int i , j , k;
            i = j = k = 0;
            while (i < arr1.length && j < arr2.length){
                if(arr1[i].compareTo(arr2[j]) > 0){
                    arr3[k++] = arr1[i++];
                } else {
                    arr3[k++] = arr2[j++];
                }
            }
            while (i < arr1.length){
                arr3[k++] = arr1[i++];
            }
            while (j < arr2.length){
                arr3[k++] = arr2[j++];
            }
            records = arr3;
        }
    }

//    private void quickSort(Record[] records , int begin, int end){
//        int tbegin = begin;
//        int tend = end;
//        if(begin >= end)
//        {
//            return;
//        }
//        Record key = records[begin];
//        int i = begin;
//        int j = end;
//        while (i < j){
//            while (records[j].sim <= key.sim && i < j){
//                j --;
//            }
//            while (records[i].sim >= key.sim && i < j){
//                i ++;
//            }
//
//            if(i < j){
//                Record temp = records[i];
//                records[i] = records[j];
//                records[j] = temp;
//            }
//        }
//        records[begin] = records[i];
//        records[i] = key;
//        quickSort(records, begin, i-1);
//        quickSort(records, i + 1, end);
//    }

    public Record[] getRecords(){
        return records;
    }


    public static class Record implements  Comparable<Record>{
        double sim;
        Object body;
        public Record(double sim, Object body){
            this.sim = sim;
            this.body = body;
        }

        public double getKey(){
            return sim;
        }

        public Object getValue(){
            return body;
        }

        public int compareTo(Record o) {
            return Double.compare(this.sim, o.sim);
        }

        @Override
        public String toString() {
            return "Record{" +
                    "sim=" + sim +
                    ", body=" + body +
                    '}';
        }
    }
}
