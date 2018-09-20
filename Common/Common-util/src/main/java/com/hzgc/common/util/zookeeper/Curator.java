package com.hzgc.common.util.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;

public class Curator {

    private static final Logger LOG = Logger.getLogger(Curator.class);
    private CuratorFramework client;

    public Curator(String zkAddress, int sessionTimeoutMs, int connectionTimeoutMs){
        this.client = connectionZookeeper(zkAddress, sessionTimeoutMs, connectionTimeoutMs);
    }

    public Curator(String zkAddress) {
        this.client = connectionZookeeper(zkAddress, 12000, 12000);
    }

    private CuratorFramework connectionZookeeper(String zkAddress, int sessionTimeoutMs, int connectionTimeoutMs) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .sessionTimeoutMs(sessionTimeoutMs)
                .connectionTimeoutMs(connectionTimeoutMs)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        return client;
    }

    public CuratorFramework getClient() {
        return client;
    }

    /**
     * 创建一个节点
     */
    public void createNode(String nodePath, byte[] nodeData, CreateMode createMode) {
        try {
            client.create()
                    .creatingParentContainersIfNeeded()
                    .withMode(createMode)
                    .forPath(nodePath, nodeData);
        } catch (Exception e) {
            LOG.error("Create znode failed, znode :" + nodePath);
            e.printStackTrace();
        }
    }

    /**
     * 删除一个子节点
     */
    public void deleteChildNode(String childNodePath) {
        try {
            client.delete()
                    .forPath(childNodePath);
        } catch (Exception e) {
            LOG.error("Delete childNode failed, znode :" + childNodePath);
            e.printStackTrace();
        }
    }

    /**
     * 删除一个父节点
     */
    public void deleteParentNode(String parentNodePath) {
        try {
            client.delete()
                    .deletingChildrenIfNeeded()
                    .forPath(parentNodePath);
        } catch (Exception e) {
            LOG.error("Delete parentNode failed, znode: " + parentNodePath);
            e.printStackTrace();
        }
    }

    /**
     * 更新一个子节点
     */
    public void setNodeDate(String nodePath, byte[] nodeData) {
        try {
            client.setData().forPath(nodePath, nodeData);
        } catch (Exception e) {
            LOG.error("Update data failed, znode :" + nodePath);
            e.printStackTrace();
        }
    }

    /**
     * 获取一个子节点数据
     */
    public byte[] getNodeData(String nodePath) {
        byte[] data = null;
        try {
            data = client.getData().forPath(nodePath);
        } catch (Exception e) {
            LOG.error("Get data failed, znode: " + nodePath);
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 获取一个父节点下所有子节点路径
     */
    public List<String> getParenNodePath(String parentNodePath){
        List<String> list = new ArrayList<String>();
        try {
            list = client.getChildren().forPath(parentNodePath);
        } catch (Exception e) {
            LOG.error("Get children nodePath failed, znode: " + parentNodePath);
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 判断当前节点是否存在
     */
    public boolean nodePathExists(String nodePath){
        try {
            Stat stat = client.checkExists().forPath(nodePath);
            if (stat != null){
                return true;
            }
        } catch (Exception e) {
            LOG.error("Get nodePath stat failed, znode: " + nodePath);
            e.printStackTrace();
        }
        return false;
    }
}
