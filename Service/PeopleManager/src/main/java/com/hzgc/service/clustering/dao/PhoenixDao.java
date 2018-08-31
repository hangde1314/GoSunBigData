package com.hzgc.service.clustering.dao;

import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.common.faceclustering.table.PeopleManagerTable;
import com.hzgc.common.faceclustering.table.PeopleRecognizeTable;
import com.hzgc.common.faceclustering.table.PersonRegionTable;
import com.hzgc.common.hbase.HBaseHelper;
import com.hzgc.common.service.api.bean.DeviceDTO;
import com.hzgc.common.service.api.bean.WebgisMapPointDTO;
import com.hzgc.common.service.api.service.DeviceQueryService;
import com.hzgc.common.util.json.JSONUtil;
import com.hzgc.service.clustering.bean.export.*;
import com.hzgc.service.clustering.bean.param.GetResidentParam;
import com.hzgc.service.clustering.bean.param.ResidentParam;
import com.hzgc.service.clustering.service.ClusteringServiceHelper;
import com.hzgc.service.clustering.service.ParseByOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
@Slf4j
public class PhoenixDao implements Serializable {

    @Resource(name = "phoenixJdbcTemplate")
    @SuppressWarnings("unused")
    private JdbcTemplate jdbcTemplate;
    @Autowired
    @SuppressWarnings("unused")
    private ParseByOption parseByOption;

    @Autowired
    @SuppressWarnings("unused")
    private DeviceQueryService deviceQueryService;

    @Autowired
    @SuppressWarnings("unused")
    private ClusteringServiceHelper clusteringServiceHelper;

    public List<String> getAllObjectIdcard() {
        List<String> idcardList = new ArrayList<>();
        String sql = parseByOption.getAllObjectIdcard();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        while (sqlRowSet.next()) {
            String idcard = sqlRowSet.getString(PeopleManagerTable.IDCARD);
            idcardList.add(idcard);
        }
        return idcardList;
    }

    /**
     * 针对单个对象信息的添加处理  （外）
     *
     * @return 返回值为0，表示插入成功，返回值为1，表示插入失败
     */
    public Integer addPerson(ResidentParam person) {
        String sql = parseByOption.addPerson();
        log.info("Start add person,SQL is: " + sql);
        try {
            Timestamp createTime = new Timestamp(System.currentTimeMillis());
            if (person.getPictureDatas().getFeature() != null) {
                float[] in = person.getPictureDatas().getFeature().getFeature();
                Object[] out = new Object[in.length];
                for (int i = 0; i < in.length; i++) {
                    out[i] = in[i];
                }
                //生成phoenix可以识别的float数组
                Connection connection = jdbcTemplate.getDataSource().getConnection();
                Array sqlArray = connection.createArrayOf("FLOAT", out);
                connection.close();
                jdbcTemplate.update(sql,
                        person.getId(),
                        person.getName(),
                        person.getRegionID(),
                        person.getIdcard(),
                        person.getSex(),
                        person.getPictureDatas().getImageData(),
                        sqlArray,
                        person.getReason(),
                        person.getCreator(),
                        person.getCreatorContactWay(),
                        createTime,
                        createTime,
                        person.getFollowLevel(),
                        person.getStatus(),
                        person.getCareLevel());
            } else {
                jdbcTemplate.update(sql,
                        person.getId(),
                        person.getName(),
                        person.getRegionID(),
                        person.getIdcard(),
                        person.getSex(),
                        person.getPictureDatas().getImageData(),
                        null,
                        person.getReason(),
                        person.getCreator(),
                        person.getCreatorContactWay(),
                        createTime,
                        createTime,
                        person.getFollowLevel(),
                        person.getStatus(),
                        person.getCareLevel());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        log.info("add person successfully!");
        return 0;
    }

    public Integer deletePerson(List<String> rowkeyList) {
        String sql = parseByOption.deletePerson();
        log.info("Start delete person,SQL is : " + sql);
        try {
            List<Object[]> batchArgs = new ArrayList<>();
            for (String rowkey : rowkeyList) {
                batchArgs.add(new Object[]{rowkey});
            }
            jdbcTemplate.batchUpdate(sql, batchArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        log.info("Delete person successfully!");
        return 0;
    }

    /**
     * 根据id查询对象
     *
     * @param objectId 对象ID
     * @return ObjectInfo
     */
    public Resident getPerson(String objectId) {
        String sql = parseByOption.getPerson();
        log.info("Start to get person,SQL is : " + sql);
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, objectId);
        Resident resident = new Resident();
        Timestamp createTime = null;
        Timestamp updateTime = null;
        while (sqlRowSet.next()) {
            resident.setName(sqlRowSet.getString(PeopleManagerTable.NAME));
            resident.setIdcard(sqlRowSet.getString(PeopleManagerTable.IDCARD));
            resident.setSex(sqlRowSet.getInt(PeopleManagerTable.SEX));
            resident.setReason(sqlRowSet.getString(PeopleManagerTable.REASON));
            resident.setCreator(sqlRowSet.getString(PeopleManagerTable.CREATOR));
            resident.setCreatorContactWay(sqlRowSet.getString(PeopleManagerTable.CPHONE));
            createTime = sqlRowSet.getTimestamp(PeopleManagerTable.CREATETIME);
            updateTime = sqlRowSet.getTimestamp(PeopleManagerTable.UPDATETIME);
            resident.setFollowLevel(sqlRowSet.getInt(PeopleManagerTable.IMPORTANT));
            resident.setStatus(sqlRowSet.getInt(PeopleManagerTable.STATUS));
            resident.setCareLevel(sqlRowSet.getInt(PeopleManagerTable.CARE));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (createTime != null) {
            Date createTime_date = new Date(createTime.getTime());
            String createTime_str = sdf.format(createTime_date);
            resident.setCreateTime(createTime_str);
        }
        if (updateTime != null) {
            Date updateTime_date = new Date(updateTime.getTime());
            String updateTime_str = sdf.format(updateTime_date);
            resident.setUpdateTime(updateTime_str);
        }
        log.info("Get person successfully,result is : " + JSONUtil.toJson(resident));
        return resident;
    }

    public String getObjectIdCard(String id) {
        String idCard = null;
        String sql = parseByOption.getObjectIdCard();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, id);
        while (sqlRowSet.next()) {
            idCard = sqlRowSet.getString(PeopleManagerTable.IDCARD);
        }
        return idCard;
    }

    /**
     * 修改对象的信息  （外）
     *
     * @param param K-V 对，里面存放的是字段和值之间的一一对应关系，参考添加里的描述
     * @return 返回值为0，表示更新成功，返回值为1，表示更新失败
     */
    public Integer updatePerson(ResidentParam param) {
        try {
            ConcurrentHashMap<String, CopyOnWriteArrayList<Object>> sqlAndSetValues = parseByOption.getUpdateSqlFromPerson(param);
            String sql = null;
            CopyOnWriteArrayList<Object> setValues = new CopyOnWriteArrayList<>();
            for (Map.Entry<String, CopyOnWriteArrayList<Object>> entry : sqlAndSetValues.entrySet()) {
                sql = entry.getKey();
                setValues = entry.getValue();
            }
            log.info("Start update person, SQL is : " + sql);
            log.info("Start update person, SQLArgs is : " + JSONUtil.toJson(setValues));
            List<Object[]> batchArgs = new ArrayList<>();
            Object[] objects = new Object[setValues.size()];
            for (int i = 0; i < setValues.size(); i++) {
                objects[i] = setValues.get(i);
            }
            batchArgs.add(objects);
            log.info("Start update person, batchArgs is : " + JSONUtil.toJson(batchArgs));
            jdbcTemplate.batchUpdate(sql, batchArgs);
            log.info("11111111111111");
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        log.info("Update person successfully!");
        return 0;
    }

    /**
     * 抓拍次数查询
     *
     * @param rowkeylist 常驻人口库中某个人的ID
     * @reture map 返回这个人的抓拍次数的key-value对
     */
    public Map<String, Integer> getCaptureCount(List<String> rowkeylist) {
        Map<String, Integer> map = new HashMap<>();
        Table table = HBaseHelper.getTable(PeopleRecognizeTable.TABLE_NAME);
        for (String rowkey : rowkeylist) {
            Get get = new Get(Bytes.toBytes(rowkey));
            try {
                Result result = table.get(get);
                String listString = Bytes.toString(result.getValue(PeopleRecognizeTable.COLUMNFAMILY, PeopleRecognizeTable.FACEOBJECT));
                List<FaceObject> faceObjectList = JSONUtil.toArray(listString, FaceObject.class);
                Integer count = faceObjectList.size();
                map.put(rowkey, count);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 抓拍历史查询
     *
     * @param rowkeyList 常驻人口库ID的list
     * @return 返回一个人的抓拍历史
     */
    public Map<String, List<FaceObject>> getCaptureHistory(RowkeyList rowkeyList) {
        Map<String, List<FaceObject>> map = new HashMap<>();
        Table table = HBaseHelper.getTable(PeopleRecognizeTable.TABLE_NAME);
        List<String> rowkeylist = rowkeyList.getRowkeyList();
        log.info("rowkeyList is : " + rowkeylist);
        for (String rowkey : rowkeylist) {
            Get get = new Get(Bytes.toBytes(rowkey));
            try {
                Result result = table.get(get);
                log.info("Result's size is :" + result.size());
                String listString = Bytes.toString(result.getValue(PeopleRecognizeTable.COLUMNFAMILY, PeopleRecognizeTable.FACEOBJECT));
                List<FaceObject> list = JSONUtil.toArray(listString, FaceObject.class);
                for (FaceObject faceObject : list) {
                    faceObject.setSurl(clusteringServiceHelper.getFtpUrl(faceObject.getSurl()));
                    faceObject.setBurl(clusteringServiceHelper.getFtpUrl(faceObject.getBurl()));
                }
                List<String> ipcIdList = new ArrayList<>();
                for (FaceObject faceObject : list) {
                    String ipcId = faceObject.getIpcId();
                    ipcIdList.add(ipcId);
                }
                Map<String, DeviceDTO> ipcMap = deviceQueryService.getDeviceInfoByBatchIpc(ipcIdList);
                if (ipcMap != null) {
                    for (FaceObject faceObject : list) {
                        for (String key : ipcMap.keySet()) {
                            if (faceObject.getIpcId().equals(key)) {
                                faceObject.setIpcId(String.valueOf(ipcMap.get(key)));
                            }
                        }
                    }
                }
                map.put(rowkey, list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Map<String, List<FaceObject>> mapResult = new HashMap<>();
        int start = rowkeyList.getStart();
        int limit = rowkeyList.getLimit();
        Set set = map.keySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            List<FaceObject> newValue;
            List<FaceObject> value = map.get(key);
            if ((start + limit) > value.size()) {
                newValue = value.subList(start, value.size());
            } else {
                newValue = value.subList(start, start + limit);
            }
            mapResult.put(key, newValue);
        }
        return mapResult;
    }

    public SqlRowSet searchResident(GetResidentParam param) {
        //封装的sql以及需要设置的值
        SqlAndArgs sqlAndArgs = parseByOption.getSqlFromGetResidentParam(param);
        if (sqlAndArgs == null) {
            log.warn("Start get resident,generate sql is failed!");
            return null;
        }
        log.info("Start get resident,generate sql successfully!");
        log.info("Start get resident, SQL is : " + sqlAndArgs.getSql());
        log.info("Start get resident, SQL args is : " + sqlAndArgs.getArgs().toString());
        return jdbcTemplate.queryForRowSet(sqlAndArgs.getSql(), sqlAndArgs.getArgs().toArray());
    }

    /**
     * 获取常驻人口库照片
     *
     * @param objectID 对象ID
     *                 return byte[]
     */
    public byte[] getResidentPhoto(String objectID) {
        String sql = parseByOption.getPhotoByObjectID();
        log.info("Start to get the object photo, SQL is : " + sql);
        byte[] photo = null;
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, objectID);
        while (sqlRowSet.next()) {
            photo = (byte[]) sqlRowSet.getObject(PeopleManagerTable.PHOTO);
        }
        if (photo != null && photo.length > 0) {
            log.info("Start to get the object photo successfully!");
        }
        return photo;
    }

    public List<CapatureLocus> getCaptureLocus(List<String> rowkeylist) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<CapatureLocus> capatureLocusList = new ArrayList<>();
        Table table = HBaseHelper.getTable(PeopleRecognizeTable.TABLE_NAME);
        CapatureLocus capatureLocus = new CapatureLocus();
        for (String rowkey : rowkeylist) {
            capatureLocus.setRowkey(rowkey);
            Get get = new Get(Bytes.toBytes(rowkey));
            try {
                Result result = table.get(get);
                log.info("rowkey is : " + rowkey);
                String listString = Bytes.toString(result.getValue(PeopleRecognizeTable.COLUMNFAMILY, PeopleRecognizeTable.FACEOBJECT));
                List<FaceObject> faceObjectList = JSONUtil.toArray(listString, FaceObject.class);
                log.info("faceobjectList is : " + faceObjectList);
                List<Locus> locusList = new ArrayList<>();
                List<Long> deviceIdList = new ArrayList<>();
                for (FaceObject faceObject : faceObjectList) {
                    log.info("locusList is : " + locusList);
                    if (locusList != null) {
                        for (Locus locus : locusList) {
                            deviceIdList.add(locus.getDeviceId());
                        }
                    }
                    log.info("deviceIdList is : " + deviceIdList);
                    List<Long> deviceIds = new ArrayList<>();
                    Locus locus = new Locus();
                    Long id = Long.valueOf(deviceQueryService.getDeviceInfoByIpc(faceObject.getIpcId()).getId());
                    log.info("deviceid is : " + id);
                    deviceIds.add(id);
                    Map<Long, WebgisMapPointDTO> map = deviceQueryService.getDeviceInfoByBatchIdByDevice(deviceIds);
                    WebgisMapPointDTO w = map.get(id);
                    log.info("webgis is : " + w);
                    log.info("boolean is : " + deviceIdList.contains(id));
                    if (!deviceIdList.contains(id)) {
                        locus.setDeviceId(id);
                        locus.setMarsLongitude(w.getMarsLongitude());
                        locus.setMarsLatitude(w.getMarsLatitude());
                        locus.setCount(1);
                        locus.setTime(faceObject.getStartTime());
                        log.info("The first locus is : " + locus);
                        locusList.add(locus);
                    } else {
                        String faceTime = faceObject.getStartTime();
                        for (Locus locus1 : locusList) {
                            if (Objects.equals(locus1.getDeviceId(), id)) {
                                Locus locus2 = new Locus();
                                log.info("The second locus is : " + locus1);
                                log.info("the second locus's count is : " + locus1.getCount());
                                int count = locus1.getCount() + 1;
                                log.info("count is : " + count);
                                locus2.setCount(count);
                                String time = locus1.getTime();
                                if (simpleDateFormat.parse(faceTime).getTime() > simpleDateFormat.parse(time).getTime()) {
                                    locus2.setTime(faceTime);
                                } else {
                                    locus2.setTime(time);
                                }
                                locus2.setMarsLatitude(locus1.getMarsLatitude());
                                locus2.setMarsLongitude(locus1.getMarsLongitude());
                                locus2.setDeviceId(locus1.getDeviceId());
                                log.info("The third locul is : " + locus2);
                                locusList.remove(locus1);
                                log.info("after remove the locusList is : " + locusList);
                                locusList.add(locus2);
                                log.info("after add the locusList is : " + locusList);
                            }
                        }
                    }
                    capatureLocus.setLocusList(locusList);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        capatureLocusList.add(capatureLocus);
        log.info("captureLocusList is : " + capatureLocusList);
        return capatureLocusList;
    }

    /**
     * 判断规则表中是否存在这个小区
     *
     * @param regular
     * @return
     */
    public Boolean isExists_region(Regular regular) {
        Table table = HBaseHelper.getTable(PersonRegionTable.TABLE_NAME);
        Scan scan = new Scan();
        List<String> stringList = new ArrayList<>();
        try {
            ResultScanner results = table.getScanner(scan);
            for (Result result : results) {
                String rowkey = Bytes.toString(result.getRow());
                stringList.add(rowkey);
            }
            log.info("StringList is : " + stringList);
            log.info("Regular's regionid is : " + regular.getRegionID());
            if (stringList.contains(regular.getRegionID())) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}













