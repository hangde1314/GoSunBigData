package com.hzgc.service.dispatch.service;

import com.hzgc.common.table.dispatch.DispatchTable;
import com.hzgc.common.util.json.JSONUtil;
import com.hzgc.service.dispatch.bean.*;
import com.hzgc.service.dispatch.dao.HBaseDao;
import com.hzgc.service.dispatch.util.IpcIdsUtil;
import com.hzgc.service.util.api.bean.DeviceDTO;
import com.hzgc.service.util.api.service.DeviceQueryService;
import com.hzgc.service.util.error.RestErrorCode;
import com.hzgc.service.util.response.ResponseResult;

import java.io.IOException;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WarnRuleService {

    @Autowired
    @SuppressWarnings("unused")
    private HBaseDao hBaseDao;

    @Autowired
    private DeviceQueryService deviceQueryService;

    public void configRules(List<String> ipcIDs, List<Warn> warns) {
        hBaseDao.configRules(ipcIDs, warns);
    }

    /**
     * 删除设备的布控规则
     *
     * @param ipcIDs 设备 ipcID 列表
     */
    public void deleteRules(List<String> ipcIDs) {
        hBaseDao.deleteRules(ipcIDs);
    }

    //存储原数据
    public ResponseResult<String> saveOriginData(Map<String, Dispatch> map) throws IOException {
        return this.hBaseDao.saveOriginData(map);
    }

    //根据ruleId进行全部参数查询
    public ResponseResult<Dispatch> searchByRuleId(String id) throws IOException {
        Map<String, Dispatch> map = hBaseDao.searchByRuleId();
        for (String ruleId : map.keySet()) {
            if (ruleId.equals(id)) {
                Dispatch dispatch = map.get(ruleId);
                List<Warn> warnList = dispatch.getRule().getWarns();
                List <Device> deviceList = dispatch.getDevices();
                String[] strings = new String[warnList.size()];
                for (int i = 0; i < warnList.size(); i++) {
                    strings[i] = (warnList.get(i)).getObjectType();
                }
                log.info("Strings is " + JSONUtil.toJson(strings));
                Map<String, Map<String, String>> responseResult = hBaseDao.getObjectTypeName(strings);
                Map<String, String> m = responseResult.get("restbody");
                for (Warn warn : warnList) {
                    for (String s : m.keySet()) {
                        if (warn.getObjectType().equals(s)) {
                            if (null != m.get(s)) {
                                warn.setObjectTypeName(m.get(s));
                            }
                        }
                    }
                }
                //查询ipcid
                List<Long> list = IpcIdsUtil.toDeviceIdList(deviceList);
                Map<String, DeviceDTO> mapDTO = deviceQueryService.getDeviceInfoByBatchId(list);
                //动态获取需要删除的设备对应的ipcid
                ArrayList<String> delIpcs = new ArrayList <>();
                //需要更新的ipcid
                ArrayList <String> ipcids = new ArrayList <>();
                Iterator<Device> iterator = deviceList.iterator();
                while (iterator.hasNext()){
                    Device device = iterator.next();
                    //数据库中的设备id
                    String dataId = device.getId();
                    //数据库中的ipcid
                    String ipcId = device.getIpcId();
                    //查看是否存在这个设备,不存在就删除
                    if (!(mapDTO.get(dataId).getSerial().length() > 0) && !mapDTO.containsKey(dataId)){
                        delIpcs.add(ipcId);
                        iterator.remove();
                        log.info("Device is deleted , device id is : " + dataId);
                    }else {
                        //设备存在动态同步最新的ipcid
                        DeviceDTO deviceDTO = mapDTO.get(dataId);
                        //最新的ipcid
                        String serial = deviceDTO.getSerial();
                        log.info("New serial is " + serial);
                       //查看设备ipcid是否更改,如果更改了就删除原来的ipcid
                        if (null != serial && serial.length() <= 0 || !serial.equals(ipcId)){
                            log.info("IpcId happen change , changed ipcid is : " + ipcId);
                            if (serial.length() > 0){
                                ipcids.add(serial);
                                //设置最新的ipcid
                                device.setIpcId(serial);
                            }
                            delIpcs.add(ipcId);
                        }
                        if (null == serial || serial.length() <= 0){
                            iterator.remove();
                        }
                    }
                }
                //删除设备不存在和ipcid修改了的设备的ipcid
                delIpcs.removeAll(Collections.singleton(null));
                if (delIpcs.size() > 0){
                    log.info("Device ipcid change or delete");
                    hBaseDao.deleteRules(delIpcs);
                    //同步大数据
                    if (ipcids.size() > 0 && warnList.size() > 0){
                        hBaseDao.configRules(ipcids,warnList);
                    }
                }
                //同步设备表
                hBaseDao.updateRule(dispatch);
                //动态获取设置设备名称
                for (String s :mapDTO.keySet()){
                    DeviceDTO deviceDTO = mapDTO.get(s);
                    String name = deviceDTO.getName();
                    for (Device device:deviceList){
                        String dataid = device.getId();
                        if (s.equals(dataid)){
                            device.setName(name);
                            log.info("Now acquire device name" + name);
                        }
                    }
                }
                log.info("Dispatch all info is " + JSONUtil.toJson(dispatch));
                return ResponseResult.init(dispatch);
            }
        }
        log.info("Id query Data is null");
        return ResponseResult.error(RestErrorCode.RECORD_NOT_EXIST);
    }

    //修改规则
    public ResponseResult<Boolean> updateRule(Dispatch dispatch) throws IOException {
        return hBaseDao.updateRule(dispatch);
    }

    //删除规则
    public List<Long> delRules(IdsType<String> idsType) throws IOException {
        return hBaseDao.delRules(idsType);
    }

    //分页获取规则列表
    public ResponseResult<List> getRuleList(PageBean pageBean) throws IOException {
        return hBaseDao.getRuleList(pageBean);
    }

    //获取某个规则绑定的所有设备
    public ResponseResult<List> getDeviceList(String rule_id) throws IOException {
        return hBaseDao.getDeviceList(rule_id);
    }

}