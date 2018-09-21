package com.hzgc.common.collect.facesub;

import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.common.util.zookeeper.Curator;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.CreateMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FtpSubscribeClient implements Serializable {
    private static Map<String, List<String>> sessionMap = new ConcurrentHashMap<>();
    private final String ftp_subscribe_path = "/ftp_subscribe";
    private Curator subscribeClient;


    public FtpSubscribeClient(String zkAddress) {
        subscribeClient = new Curator(zkAddress, 20000, 15000);
        if (subscribeClient.nodePathExists(ftp_subscribe_path)) {
            log.info("Ftp subscribe root path '" + ftp_subscribe_path + "' is exists");
        } else {
            subscribeClient.createNode(ftp_subscribe_path, null, CreateMode.PERSISTENT);
            log.info("Ftp subscribe root path '" + ftp_subscribe_path + "' create successfully");
        }
        initPathCache(subscribeClient.getClient());
    }

    /**
     * 创建/更新抓拍订阅用户
     *
     * @param sessionId
     * @param ipcIdList
     */
    public void updateSessionPath(String sessionId, List<String> ipcIdList) {
        if (sessionId != null && sessionId.length() > 0
                && ipcIdList != null && ipcIdList.size() > 0) {
            String nodePath = ftp_subscribe_path + "/" + sessionId;
            byte[] nodeData = JacksonUtil.toJson(ipcIdList).getBytes();
            if (subscribeClient.nodePathExists(nodePath)) {
                subscribeClient.setNodeDate(nodePath, nodeData);
                log.info("Update ftp subscribe child node path: " + nodePath
                        + " successfully, update data: " + JacksonUtil.toJson(ipcIdList));
            } else {
                subscribeClient.createNode(nodePath, nodeData, CreateMode.PERSISTENT);
                log.info("Create ftp subscribe child node path: " + nodePath
                        + " successfully, data: " + JacksonUtil.toJson(ipcIdList));
            }
        }
    }

    /**
     * 删除抓拍订阅用户
     *
     * @param sessionId
     */
    public void deleteSessionPath(String sessionId) {
        String nodePath = ftp_subscribe_path + "/" + sessionId;
        if (subscribeClient.nodePathExists(nodePath)) {
            subscribeClient.deleteChildNode(nodePath);
            log.info("Delete ftp subscribe child node path: " + nodePath + " successful");
        } else {
            log.info("Delete ftp subscribe child node path: " + nodePath + " is not exists");
        }
    }

    private void initPathCache(CuratorFramework curatorFramework) {
        final PathChildrenCache pathCache =
                new PathChildrenCache(curatorFramework, ftp_subscribe_path, true);
        try {
            pathCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            pathCache.getListenable().addListener((client, event) -> {
                String data = event.getData() != null ? event.getData().getPath() : null;
                log.info("Ftp subscribe child event [type:" + event.getType()
                        + ", path:" + data + "]");
                switch (event.getType()) {
                    case CHILD_ADDED:
                        refreshData(pathCache.getCurrentData());
                        break;
                    case CHILD_UPDATED:
                        refreshData(pathCache.getCurrentData());
                        break;
                    case CHILD_REMOVED:
                        refreshData(pathCache.getCurrentData());
                        break;
                    default:
                        break;
                }
            });
            //尝试第一次刷新节点下数据
            refreshData(pathCache.getCurrentData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshData(List<ChildData> currentData) {
        if (currentData != null && currentData.size() > 0) {
            sessionMap.clear();
            for (ChildData childData : currentData) {
                String path = childData.getPath();
                String sessionId = path.replace(ftp_subscribe_path + "/", "");
                List<String> ipcIds = JacksonUtil.toObject(new String(childData.getData()), List.class);
                sessionMap.put(sessionId, ipcIds);
            }
            log.info("Ftp subscribe info:" + JacksonUtil.toJson(sessionMap));
        }
    }

    public static Map<String, List<String>> getSessionMap() {
        Map<String, List<String>> ipcMappingUser = new HashMap<>();
        sessionMap.keySet().forEach(sessionId -> {
            List<String> ipcList = sessionMap.get(sessionId);
            for (String ipc : ipcList) {
                if (!ipcMappingUser.containsKey(ipc)) {
                    List<String> userList = new ArrayList<>();
                    userList.add(sessionId);
                    ipcMappingUser.put(ipc, userList);
                } else {
                    List<String> userList = ipcMappingUser.get(ipc);
                    userList.add(sessionId);
                    ipcMappingUser.put(ipc, userList);
                }
            }
        });
        return ipcMappingUser;
    }
}
