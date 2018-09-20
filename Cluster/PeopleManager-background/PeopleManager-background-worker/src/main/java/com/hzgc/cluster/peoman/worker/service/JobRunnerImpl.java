package com.hzgc.cluster.peoman.worker.service;

import com.github.ltsopensource.spring.boot.annotation.JobRunner4TaskTracker;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.github.ltsopensource.tasktracker.runner.JobRunner;
import com.hzgc.common.service.peoman.JobRunnerProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@JobRunner4TaskTracker
@Slf4j
public class JobRunnerImpl implements JobRunner {
    @Autowired
    @SuppressWarnings("unused")
    private InnerConsumer innerConsumer;

    @Autowired
    @SuppressWarnings("unused")
    private LoadDataFromTiDB loadDataFromTiDB;

    @Value("lts.tasktracker.node-group")
    @SuppressWarnings("unused")
    private String workId;

    @Autowired
    @SuppressWarnings("unused")
    private WorkerRegister workerRegister;

    private boolean isRun = false;

    @Override
    public Result run(JobContext jobContext) throws Throwable {
        Map<String, String> extParams = jobContext.getJob().getExtParams();
        Result result = new Result();
        if (extParams.containsKey(JobRunnerProtocol.STOP)) {
            result.setMsg(JobRunnerProtocol.SUCCESSFULL);
            log.info("Start stop worker, worker id is ?", workId);
            System.exit(0);
        }
        if (extParams.containsKey(JobRunnerProtocol.RUN)) {
            if (!isRun) {
                log.info("Start run worker, worker id is ?", workId);
                int offset = Integer.parseInt(extParams.get(JobRunnerProtocol.OFFSET));
                int limit = Integer.parseInt(extParams.get(JobRunnerProtocol.LIMIT));
                loadDataFromTiDB.load(offset, limit);
                innerConsumer.initInnerConsumer();
                Thread thread = new Thread(innerConsumer);
                thread.start();
                isRun = true;
            } else {
                log.info("Worker is alreadt run ,worker id is ?", workId);
                result.setMsg(JobRunnerProtocol.ALREADYRUN);
            }
        }
        return result;
    }
}
