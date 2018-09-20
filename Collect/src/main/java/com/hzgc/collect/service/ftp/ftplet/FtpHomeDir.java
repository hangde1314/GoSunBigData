package com.hzgc.collect.service.ftp.ftplet;

import com.hzgc.collect.config.CollectConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FtpHomeDir {
    private final Logger LOG = LoggerFactory.getLogger(FtpHomeDir.class);
    // ftp配置的所有homeDir集合
    private List<String> ftpConfHomeDirs = new LinkedList<>();
    // ftp已满载的所有homeDir集合
    private static List<String> ladenHomeDirs = new LinkedList<>();
    // ftp未满载的所有homeDir集合（包含当前使用的homeDir）
    private static List<String> notLadenHomeDirs = new LinkedList<>();
    // 磁盘使用率
    private float usageRate;
    // 当前使用rootDir
    private static String rootDir;
    // 定时检测周期
    private long period;

    public FtpHomeDir() {
        LOG.info("Start get 'homeDirs' configuration from collect.properties");
        setFtpConfHomeDirs();
        LOG.info("Start distribution homeDirs to laden homeDirs or not laden homeDirs");
        setLadenOrNotLadenHomeDirs();
    }

    private void setFtpConfHomeDirs() {
        String[] homeDirs = CollectConfiguration.getHomeDirs().split(",");
        float diskUsageRate = CollectConfiguration.getDiskUsageRate();
        long periodConf = CollectConfiguration.getPeriod();
        if (homeDirs.length == 0 || diskUsageRate == 0) {
            LOG.error("Get 'homeDirs' or 'diskUsageRate' configuration failed from collect.properties");
            return;
        }
        for (String dir : homeDirs){
            String lastStr = dir.trim().substring(dir.length() - 1);
            if (lastStr.equals("/")){
                this.ftpConfHomeDirs.add(dir);
            } else {
                this.ftpConfHomeDirs.add(dir + "/");
            }
        }
        LOG.info("Collect.properties configuration: homeDirs: " + Arrays.toString(ftpConfHomeDirs.toArray()));
        LOG.info("Collect.properties configuration: diskUsageRate: " + diskUsageRate);
        this.usageRate = diskUsageRate;
        this.period = periodConf;
    }

    private void setLadenOrNotLadenHomeDirs(){
        if (ftpConfHomeDirs != null && ftpConfHomeDirs.size() > 0) {
            for (String dir : ftpConfHomeDirs) {
                float diskUsage = getDiskUsageRate(dir);
                if (diskUsage >= usageRate) {
                    ladenHomeDirs.add(dir);
                    LOG.info("LadenHomeDirs add: " + dir);
                } else {
                    notLadenHomeDirs.add(dir);
                    LOG.info("NotLadenHomeDirs add: " + dir);
                }
            }
            rootDir = notLadenHomeDirs.get(0);
            LOG.info("LadenHomeDirs: " + Arrays.toString(ladenHomeDirs.toArray()));
            LOG.info("NotLadenHomeDirs: " + Arrays.toString(notLadenHomeDirs.toArray()));
            LOG.info("RootDir : " + rootDir);
        } else {
            LOG.error("Get 'homeDirs' configuration failed from collect.properties");
        }
    }

    public static String getRootDir() {
        return rootDir;
    }

    public static List<String> getLadenHomeDirs() {
        return ladenHomeDirs;
    }

    public void periodicallyCheckCurrentRootDir(){
        Runnable runnable = () -> {
            float diskUsage = getDiskUsageRate(rootDir);
            LOG.info("Periodically check current disk usage, rootDir is: "
                    + rootDir + ", current disk usage: " + diskUsage);
            if (diskUsage >= usageRate){
                ladenHomeDirs.add(rootDir);
                LOG.info("The current disk is full, so ladenHomeDirs add: "
                        + rootDir + ", ladenHomeDirs: " + Arrays.toString(ladenHomeDirs.toArray()));
                notLadenHomeDirs.remove(rootDir);
                LOG.info("The current disk is full, so notLadenHomeDirs remove: "
                        + rootDir + ", notLadenHomeDirs: " + Arrays.toString(notLadenHomeDirs.toArray()));
                if (!StringUtils.isBlank(notLadenHomeDirs.get(0))){
                    rootDir = notLadenHomeDirs.get(0);
                    LOG.info("The current disk is full, switch to the next disk: " + rootDir);
                }
            }
            if (notLadenHomeDirs.size() == 0){
                LOG.error("All the spare disks are full!");
            }
        };
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(runnable, 1, period, TimeUnit.MINUTES);
    }

    private float getDiskUsageRate(String dir){
        File disk = new File(dir);
        float totalSpace = disk.getTotalSpace();
        float usableSpace = disk.getUsableSpace();
        return (totalSpace - usableSpace) / totalSpace;
    }
}
