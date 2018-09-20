package com.hzgc.compare.worker;


import com.hzgc.compare.worker.common.FaceInfoTable;
import com.hzgc.compare.worker.common.taskhandle.TaskToHandleQueue;
import com.hzgc.compare.worker.comsumer.Comsumer;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import com.hzgc.compare.worker.memory.manager.MemoryManager;
import com.hzgc.compare.worker.persistence.*;
import com.hzgc.compare.worker.util.HBaseHelper;
import com.hzgc.jniface.FaceJNI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * 整合所有组件
 */
public class Worker {
    private static final Logger logger = LoggerFactory.getLogger(Worker.class);
    private Comsumer comsumer;
    private MemoryManager memoryManager;
    private FileManager fileManager;
//    private HBaseClient hBaseClient;


    public void init(String workId, String port){
        Config.WORKER_ID = workId;
        Config.WORKER_RPC_PORT =  Integer.parseInt(port);
        comsumer = new Comsumer();
        logger.info("To start worker " + workId);
        logger.info("To init the memory module.");
        MemoryCacheImpl.getInstance();
        memoryManager = new MemoryManager();
        logger.info("To init persistence module.");
        int saveParam = Config.WORKER_FILE_SAVE_SYSTEM;
        if(Config.SAVE_TO_LOCAL == saveParam){
            fileManager = new LocalFileManager();
        } else if(Config.SAVE_TO_HDFS == saveParam){
            fileManager = new HDFSFileManager();
        }
//        hBaseClient = new HBaseClient();
        try {
            logger.info("Load data from file System.");
            if(Config.SAVE_TO_LOCAL == saveParam){
                FileReader fileReader = new LocalFileReader();
                fileReader.loadRecord();
            } else if(Config.SAVE_TO_HDFS == saveParam){
                FileReader fileReader = new HDFSFileReader();
                fileReader.loadRecord();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        HBaseHelper.getTable(FaceInfoTable.TABLE_NAME);
        TaskToHandleQueue.getTaskQueue();
    }


    public void start(){
        comsumer.start();
        memoryManager.startToCheck();
        memoryManager.toShowMemory();
        if(Config.WORKER_FLUSH_PROGRAM == 0){
            memoryManager.timeToCheckFlush();
        }
//        fileManager.checkFile();
        fileManager.checkTaskTodo();
//        fileManager.checkFile();
//        hBaseClient.timeToWrite();
        Thread thread = new Thread(new RPCRegistry());
        thread.start();
        FaceJNI.init();
    }

    public static void main(String args[]){
        if(args.length != 4){
            return;
        }

        String workerId = args[0];
        String nodeGroup = args[1];
        String port = args[2];
        String taskId = args[3];
        Worker worker = new Worker();
        worker.init(workerId, port);
        worker.start();
        Thread thread = new Thread(new ZookeeperRegistry(workerId, nodeGroup, port, taskId));
        thread.start();
    }
}
