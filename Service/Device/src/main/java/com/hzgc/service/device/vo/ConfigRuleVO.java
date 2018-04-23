package com.hzgc.service.device.vo;

import com.hzgc.service.device.service.WarnRule;

import java.util.List;

public class ConfigRuleVO {

    /*
     *设备ID
     */
    private String ipcID;
    /*
     *设备ID的集合
     */
    private List<String> ipcIDs;
    /*
     *告警规则集合
     */
    private List<WarnRule> rules;
    /*
     *对象类型
     */
    private String objectType;

    public String getIpcID() {
        return ipcID;
    }

    public void setIpcID(String ipcID) {
        this.ipcID = ipcID;
    }

    public List <String> getIpcIDs() {
        return ipcIDs;
    }

    public void setIpcIDs(List <String> ipcIDs) {
        this.ipcIDs = ipcIDs;
    }

    public List <WarnRule> getRules() {
        return rules;
    }

    public void setRules(List <WarnRule> rules) {
        this.rules = rules;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    @Override
    public String toString() {
        return "ConfigRuleVO{" +
                "ipcID='" + ipcID + '\'' +
                ", ipcIDs=" + ipcIDs +
                ", rules=" + rules +
                ", objectType='" + objectType + '\'' +
                '}';
    }
}