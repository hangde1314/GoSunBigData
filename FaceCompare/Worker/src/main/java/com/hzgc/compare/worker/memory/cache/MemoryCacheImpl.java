package com.hzgc.compare.worker.memory.cache;

import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.compare.worker.common.collects.BatchBufferQueue;
import com.hzgc.compare.worker.common.collects.CustomizeArrayList;
import com.hzgc.compare.worker.common.collects.DoubleBufferQueue;
import com.hzgc.compare.worker.common.taskhandle.FlushTask;
import com.hzgc.compare.worker.common.taskhandle.TaskToHandleQueue;
import com.hzgc.compare.worker.common.tuple.Triplet;
import com.hzgc.compare.worker.conf.Config;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存缓存模块，单例模式，内部存储三种数据buffer和cacheRecords，以及recordToHBase
 * 从kafka读入的数据先存储在recordToHBase，再由持久化模块不断将recordToHBase中的数据存入HBase中，然后生成元数据，将它保存在buffer中
 * 当buffer数据量达到一定时，将buffer持久化，并加入cacheRecords，buffer清空
 */
public class MemoryCacheImpl{
    private static final Logger logger = LoggerFactory.getLogger(MemoryCacheImpl.class);
    private static MemoryCacheImpl memoryCache;
    private int flushProgram = 0; //flush 方案 0 定期flush  1 定量flush
    private Integer bufferSizeMax = 1000; // buffer存储上限，默认1000
    private BatchBufferQueue<FaceObject> faceObjects; //这里应该是一个类似阻塞队列的集合
    private Map<String, List<Pair<String, byte[]>>> cacheRecords;
    private DoubleBufferQueue<Triplet<String, String, byte[]>> buffer;


    private MemoryCacheImpl(){
        init();
    }

    public static MemoryCacheImpl getInstance(){
        if(memoryCache == null){
            memoryCache = new MemoryCacheImpl();
        }
        return memoryCache;
    }


    private void init() {
        bufferSizeMax = Config.WORKER_BUFFER_SIZE_MAX;
        flushProgram = Config.WORKER_FLUSH_PROGRAM;
        faceObjects = new BatchBufferQueue<>();
        cacheRecords = new ConcurrentHashMap<>();//ConcurrentHashMap
        buffer = new DoubleBufferQueue<>();
    }

    /**
     * 返回faceObjects
     * @return
     */
    public List<FaceObject> getObjects() {
        return faceObjects.get();
    }

    public List<Triplet<String, String, byte[]>> getBuffer(){
        return buffer.get();
    }

    /**
     * 返回cacheRecords
     * @return
     */
    public Map<String, List<Pair<String, byte[]>>> getCacheRecords() {
        return cacheRecords;
    }

    public void setBufferSizeMax(int size) {
        this.bufferSizeMax = size;
    }

    /**
     * 增加recordToHBase
     */
    public void addFaceObjects(List<FaceObject> objs) {
        if(objs.size() > 0) {
            faceObjects.push(objs);
        }
    }

    /**
     * 增加多条record
     * @param records
     */
    public void loadCacheRecords(Map<String, List<Pair<String, byte[]>>> records) {
        for(Map.Entry<String, List<Pair<String, byte[]>>> entry : records.entrySet()){
            String key = entry.getKey();
            List<Pair<String, byte[]>> value = entry.getValue();
            List<Pair<String, byte[]>> list = cacheRecords.get(key);
            if(list == null || list.size() == 0){
                cacheRecords.put(key, value);
            }else {
                list.addAll(value);
            }
        }
    }

    /**
     * 将多条记录加入buffer，然后检查buffer是否满了
     * @param records 要添加的记录
     */
    public void addBuffer(List<Triplet<String, String, byte[]>> records) {
//        if(buffer == null || buffer.size() == 0){
//            buffer = records;
//        }else {
//            buffer.addAll(records);
//        }
        buffer.push(records);
        if(flushProgram == 1){
            check();
        }
    }

    /**
     * 检查buffer是否满了, 如果满了，则在TaskToHandle中添加一个FlushTask任务,并将buffer加入cacheRecords，buffer重新创建
     */
    private void check() {
        logger.info("To check The Buferr if it is to be flushed.");
        if(buffer.getWriteListSize() >= bufferSizeMax){
            List<Triplet<String, String, byte[]>> records = buffer.get();
            TaskToHandleQueue.getTaskQueue().addTask(new FlushTask(records));
            moveToCacheRecords(records);
        }
    }

    public void flush(){
        if(buffer.getWriteListSize() > 0) {
            logger.info("To flush the buffer.");
            List<Triplet<String, String, byte[]>> records = buffer.get();
            TaskToHandleQueue.getTaskQueue().addTask(new FlushTask(records));
            moveToCacheRecords(records);
        }
    }

    public void showMemory(){
        int cacheCount = 0;
        for(Map.Entry<String, List<Pair<String, byte[]>>> entry : cacheRecords.entrySet()){
            cacheCount += entry.getValue().size();
        }
        logger.info("The size of cache used to compare is : " + cacheCount);
        logger.info("The size of faceObject used to write to HBase is : " + faceObjects.size());
        logger.info("The size of buffer used to persistence is : " + buffer.getWriteListSize());
    }

    /**
     * 将数据加入cacheRecords
     */
    public void moveToCacheRecords(List<Triplet<String, String, byte[]>> records) {
        logger.info("Move records from buffer to cacheRecords.");
        for(Triplet<String, String, byte[]> record : records){
            String key = record.getFirst();
            Pair<String, byte[]> value = new Pair<>(record.getSecond(), record.getThird());
            List<Pair<String, byte[]>> list = cacheRecords.computeIfAbsent(key, k -> new CustomizeArrayList<>());
            list.add(value);
        }
    }
}
