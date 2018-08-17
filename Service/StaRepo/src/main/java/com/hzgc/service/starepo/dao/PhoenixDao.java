package com.hzgc.service.starepo.dao;

import com.hzgc.common.table.dynrepo.SearchRecordTable;
import com.hzgc.common.table.starepo.ObjectInfoTable;
import com.hzgc.common.table.starepo.ObjectTypeTable;
import com.hzgc.common.util.empty.IsEmpty;
import com.hzgc.common.util.json.JSONUtil;
import com.hzgc.common.util.uuid.UuidUtil;
import com.hzgc.jni.FaceAttribute;
import com.hzgc.jni.PictureData;
import com.hzgc.service.starepo.bean.export.ObjectInfo;
import com.hzgc.service.starepo.bean.export.PersonSingleResult;
import com.hzgc.service.starepo.bean.param.GetObjectInfoParam;
import com.hzgc.service.starepo.bean.param.ObjectInfoParam;
import com.hzgc.service.starepo.bean.param.ObjectTypeParam;
import com.hzgc.service.starepo.service.ParseByOption;
import com.hzgc.service.util.bean.PeopleManagerCount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.io.Serializable;
import java.sql.*;
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

    public List<String> getAllObjectTypeNames() {
        List<String> names = new ArrayList<>();
        String sql = parseByOption.getAllObjectTypeNames();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        while (sqlRowSet.next()) {
            String name = sqlRowSet.getString(ObjectTypeTable.TYPE_NAME);
            names.add(name);
        }
        return names;
    }

    /**
     * 添加 objectType
     *
     * @param name    对象名称
     * @param creator 创建者
     * @param remark  备注
     * @return boolean
     */
    public boolean addObjectType(String name, String creator, String remark) {
        long start = System.currentTimeMillis();
        String typeId = "type_" + start + UuidUtil.getUuid().substring(0, 8);
        log.info("Start add object type, id = " + typeId);
        String sql = parseByOption.addObjectType();
        log.info("Start add object type, SQL is : " + sql);
        try {
            jdbcTemplate.update(sql, typeId, name, creator, remark, new Timestamp(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        log.info("add object type successfull");
        return true;
    }

    /**
     * 删除objectType
     *
     * @param objectTypeKeyList 类型Key列表
     * @return boolean
     */
    public boolean deleteObjectType(List<String> objectTypeKeyList) {
        String sql_select = parseByOption.deleteObjectType_select();
        log.info("Start delete object type, select SQL is : " + sql_select);
        String sql_delete = parseByOption.deleteObjectType_delete();
        log.info("Start delete object type, delete SQL is : " + sql_delete);
        try {
            for (String id : objectTypeKeyList) {
                SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql_select, id);
                if (sqlRowSet.next()) {
                    log.info("Object info exists under this object type, so can't delete this object type : " + id);
                    return false;
                } else {
                    jdbcTemplate.update(sql_delete, id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        log.info("delete object type list successfull");
        return true;
    }

    /**
     * 修改ObjectType
     *
     * @param id      objectKey
     * @param name    objectName
     * @param creator 创建者
     * @param remark  备  注
     * @return boolean
     */
    public boolean updateObjectType(String id, String name, String creator, String remark) {
        if (StringUtils.isBlank(creator) || StringUtils.isBlank(remark)) {
            log.info("Start update object type, but creator or remark is null, the database needs to be queried");
            ObjectTypeParam objectType = getObjectTypeById(id);
            if (!IsEmpty.strIsRight(creator)) {
                creator = objectType.getCreator();
                log.info("update object type param ：creator is null, query database creator = " + creator);
            }
            if (!IsEmpty.strIsRight(remark)) {
                remark = objectType.getRemark();
                log.info("update object type param ：remark is null, query database remark = " + remark);
            }
        }
        String sql = parseByOption.updateObjectType();
        log.info("Start update object type, SQL is : " + sql);
        try {
            jdbcTemplate.update(sql, id, name, creator, remark, new Timestamp(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        log.info("update object type successfull");
        return true;
    }

    /**
     * 查询单个objectType(创建人、备注)  (内)
     *
     * @param id 对象类型Key
     * @return ObjectTypeParam
     */
    private ObjectTypeParam getObjectTypeById(String id) {
        String sql = parseByOption.getObjectTypeById(id);
        ObjectTypeParam objectType = new ObjectTypeParam();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        while (sqlRowSet.next()) {
            objectType.setCreator(sqlRowSet.getString(ObjectTypeTable.TYPE_CREATOR));
            objectType.setRemark(sqlRowSet.getString(ObjectTypeTable.TYPE_REMARK));
        }
        log.info("Get object type by id result : " + objectType.toString());
        return objectType;
    }

    /**
     * 查询objectTypeName
     *
     * @param personKey 对象类型列表
     * @return Map
     */
    public Map<String, String> searchTypeNames(List<String> personKey) {
        Map<String, String> map = new HashMap<>();
        String sql = parseByOption.getTypeNameMapping(personKey);
        log.info("Start search object type names, SQL is : " + sql);
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        while (sqlRowSet.next()) {
            String name = sqlRowSet.getString(ObjectTypeTable.TYPE_NAME);
            String typeKey = sqlRowSet.getString(ObjectTypeTable.ROWKEY);
            map.put(typeKey, name);
        }
        log.info("search object type names result : " + JSONUtil.toJson(map));
        return map;
    }

    /**
     * 查询objectType
     *
     * @param start 查询起始位置
     * @param end 查询结束位置
     * @return List<ObjectTypeParam>
     */
    public List<ObjectTypeParam> searchObjectType(int start, int end) {
        String sql = parseByOption.searchObjectType();
        log.info("Start search object type, SQL is : " + sql);
        List<ObjectTypeParam> result = null;
        SqlRowSet sqlRowSet;
        try {
            if (start == 0) {
                sqlRowSet = jdbcTemplate.queryForRowSet(sql, "0", end);
                result = getResult(sqlRowSet);
            } else {
                sqlRowSet = jdbcTemplate.queryForRowSet(sql, "0", start);
                String lastRowkey = getLastRowkey(sqlRowSet);
                sqlRowSet = jdbcTemplate.queryForRowSet(sql, lastRowkey, end);
                result = getResult(sqlRowSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        log.info("search object type result : " + JSONUtil.toJson(result));
        return result;
    }

    /**
     * 查询objectType （内部方法）
     */
    private List<ObjectTypeParam> getResult(SqlRowSet sqlRowSet) throws SQLException {
        List<ObjectTypeParam> result = new ArrayList<>();
        while (sqlRowSet.next()) {
            ObjectTypeParam objectTypeParam = new ObjectTypeParam();
            objectTypeParam.setObjectTypeKey(sqlRowSet.getString(ObjectTypeTable.ROWKEY));
            objectTypeParam.setObjectTypeName(sqlRowSet.getString(ObjectTypeTable.TYPE_NAME));
            objectTypeParam.setCreator(sqlRowSet.getString(ObjectTypeTable.TYPE_CREATOR));
            objectTypeParam.setRemark(sqlRowSet.getString(ObjectTypeTable.TYPE_REMARK));
            result.add(objectTypeParam);
        }
        return result;
    }

    /**
     * 查询objectType （内部方法）
     */
    private String getLastRowkey(SqlRowSet sqlRowSet) throws SQLException {
        String lastRowKey = null;
        while (sqlRowSet.next()) {
            lastRowKey = sqlRowSet.getString(ObjectTypeTable.ROWKEY);
        }
        return lastRowKey;
    }

    /**
     * 统计对象类型数量
     *
     * @return 对象类型数量
     */
    public int countObjectType() {
        String sql = parseByOption.countObjectType();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        int count = 0;
        while (sqlRowSet.next()) {
            count = sqlRowSet.getInt("num");
        }
        return count;
    }

    public String getObjectIdCard(String id) {
        String idCard = null;
        String sql = parseByOption.getObjectIdCard();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, id);
        while (sqlRowSet.next()) {
            idCard = sqlRowSet.getString(ObjectInfoTable.IDCARD);
        }
        return idCard;
    }

    public List<String> getAllObjectIdcard() {
        List<String> idcardList = new ArrayList<>();
        String sql = parseByOption.getAllObjectIdcard();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        while (sqlRowSet.next()) {
            String idcard = sqlRowSet.getString(ObjectInfoTable.IDCARD);
            idcardList.add(idcard);
        }
        return idcardList;
    }

    public List<String> getAllObjectTypeKeys() {
        List<String> typeList = new ArrayList<>();
        String sql = parseByOption.getAllObjectTypeKeys();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        while (sqlRowSet.next()) {
            String typeKey = sqlRowSet.getString(ObjectTypeTable.ROWKEY);
            typeList.add(typeKey);
        }
        return typeList;
    }

    /**
     * 针对单个对象信息的添加处理  （外）
     *
     * @return 返回值为0，表示插入成功，返回值为1，表示插入失败
     */
    public Integer addObjectInfo(ObjectInfoParam objectInfo) {
        String sql = parseByOption.addObjectInfo();
        log.info("Start add object info, SQL is : " + sql);
        try {
            Timestamp createTime = new Timestamp(System.currentTimeMillis());
            if (objectInfo.getPictureDatas().getFeature() != null) {
                float[] in = objectInfo.getPictureDatas().getFeature().getFeature();
                Object[] out = new Object[in.length];
                for (int i = 0; i < in.length; i++) {
                    out[i] = in[i];
                }
                //生成phoenix识别的float数组
                Connection connection = jdbcTemplate.getDataSource().getConnection();
                Array sqlArray = connection.createArrayOf("FLOAT", out);
                connection.close();
                jdbcTemplate.update(sql,
                        objectInfo.getId(),
                        objectInfo.getName(),
                        objectInfo.getObjectTypeKey(),
                        objectInfo.getIdcard(),
                        objectInfo.getSex(),
                        objectInfo.getPictureDatas().getImageData(),
                        sqlArray,
                        objectInfo.getReason(),
                        objectInfo.getCreator(),
                        objectInfo.getCreatorConractWay(),
                        createTime,
                        createTime,
                        objectInfo.getFollowLevel(),
                        objectInfo.getStatus());
            } else {
                jdbcTemplate.update(sql,
                        objectInfo.getId(),
                        objectInfo.getName(),
                        objectInfo.getObjectTypeKey(),
                        objectInfo.getIdcard(),
                        objectInfo.getSex(),
                        objectInfo.getPictureDatas().getImageData(),
                        null,
                        objectInfo.getReason(),
                        objectInfo.getCreator(),
                        objectInfo.getCreatorConractWay(),
                        createTime,
                        createTime,
                        objectInfo.getFollowLevel(),
                        objectInfo.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        log.info("add object info successfully");
        return 0;
    }

    /**
     * 删除 ObjectInfo
     *
     * @param rowkeys 对象ID列表
     * @return 返回值为0，表示删除成功，返回值为1，表示删除失败
     */
    public Integer deleteObjectInfo(List<String> rowkeys) {
        String sql = parseByOption.deleteObjectInfo();
        log.info("Start delete object info, SQL is : " + sql);
        try {
            List<Object[]> batchArgs = new ArrayList<>();
            for (String rowkey : rowkeys) {
                batchArgs.add(new Object[]{rowkey});
            }
            jdbcTemplate.batchUpdate(sql, batchArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        log.info("delete object info successfully");
        return 0;
    }

    /**
     * 修改对象的信息   （外）（李第亮）
     *
     * @param objectInfo K-V 对，里面存放的是字段和值之间的一一对应关系，参考添加里的描述
     * @return 返回值为0，表示更新成功，返回值为1，表示更新失败
     */
    public Integer updateObjectInfo(ObjectInfoParam objectInfo) {
        try {
            ConcurrentHashMap<String, CopyOnWriteArrayList<Object>> sqlAndSetValues = parseByOption.getUpdateSqlFromObjectInfo(objectInfo);
            String sql = null;
            CopyOnWriteArrayList<Object> setValues = new CopyOnWriteArrayList<>();
            for (Map.Entry<String, CopyOnWriteArrayList<Object>> entry : sqlAndSetValues.entrySet()) {
                sql = entry.getKey();
                setValues = entry.getValue();
            }
            log.info("Start update object info, SQL is : " + sql);
            List<Object[]> batchArgs = new ArrayList<>();
            Object[] objects = new Object[setValues.size()];
            for (int i = 0; i < setValues.size(); i++) {
                objects[i] = setValues.get(i);
            }
            batchArgs.add(objects);
            jdbcTemplate.batchUpdate(sql, batchArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        log.info("update object info successfully");
        return 0;
    }

    public int getObjectInfo_status(String objectId) {
        String sql = parseByOption.getObjectInfo_status();
        log.info("Search object status, SQL is : " + sql);
        int status = 0;
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, objectId);
        while (sqlRowSet.next()) {
            status = sqlRowSet.getInt(ObjectInfoTable.STATUS);
        }
        return status;
    }

    /**
     * 更新人员信息状态值
     *
     * @param objectId 对象ID
     * @param status   状态值
     * @return 返回值为0，表示更新成功，返回值为1，表示更新失败
     */
    public Integer updateObjectInfo_status(String objectId, int status) {
        String sql = parseByOption.updateObjectInfo_status();
        log.info("Start update object status, SQL is : " + sql);
        try {
            jdbcTemplate.update(sql, objectId, status, new Timestamp(System.currentTimeMillis()));
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        log.info("update object status successfully");
        return 0;
    }

    /**
     * 根据id查询对象
     *
     * @param objectId 对象ID
     * @return ObjectInfo
     */
    public ObjectInfo getObjectInfo(String objectId) {
        String sql = parseByOption.getObjectInfo();
        log.info("Start get object info, SQL is : " + sql);
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, objectId);
        ObjectInfo objectInfo = new ObjectInfo();
        String objectTypeKey = null;
        Timestamp createTime = null;
        Timestamp updateTime = null;
        while (sqlRowSet.next()) {
            objectInfo.setName(sqlRowSet.getString(ObjectInfoTable.NAME));
            objectTypeKey = sqlRowSet.getString(ObjectInfoTable.PKEY);
            objectInfo.setIdCardNumber(sqlRowSet.getString(ObjectInfoTable.IDCARD));
            objectInfo.setSex(sqlRowSet.getInt(ObjectInfoTable.SEX));
            objectInfo.setCreatedReason(sqlRowSet.getString(ObjectInfoTable.REASON));
            objectInfo.setCreator(sqlRowSet.getString(ObjectInfoTable.CREATOR));
            objectInfo.setCreatorPhone(sqlRowSet.getString(ObjectInfoTable.CPHONE));
            createTime = (Timestamp) sqlRowSet.getObject(ObjectInfoTable.CREATETIME);
            updateTime = (Timestamp) sqlRowSet.getObject(ObjectInfoTable.UPDATETIME);
            objectInfo.setFollowLevel(sqlRowSet.getInt(ObjectInfoTable.IMPORTANT));
            objectInfo.setStatus(sqlRowSet.getInt(ObjectInfoTable.STATUS));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        if (createTime != null) {
            java.util.Date createTime_data = new java.util.Date(createTime.getTime());
            String createTime_str = sdf.format(createTime_data);
            objectInfo.setCreateTime(createTime_str);
        }
        if (updateTime != null) {
            java.util.Date updateTime_data = new java.util.Date(updateTime.getTime());
            String updateTime_str = sdf.format(updateTime_data);
            objectInfo.setUpdateTime(updateTime_str);
        }
        if (!StringUtils.isBlank(objectTypeKey)) {
            String objectTypeName = getObjectTypeNameById(objectTypeKey);
            objectInfo.setObjectTypeName(objectTypeName);
        }
        log.info("Get object info successfully, result : " + JSONUtil.toJson(objectInfo));
        return objectInfo;
    }

    public String getObjectTypeNameById(String objectTypeId) {
        String sql = parseByOption.getObjectTypeNameById();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, objectTypeId);
        String objectTypeName = null;
        while (sqlRowSet.next()) {
            objectTypeName = sqlRowSet.getString(ObjectTypeTable.TYPE_NAME);
        }
        return objectTypeName;
    }

    /**
     * 可以匹配精确查找，以图搜索人员信息，模糊查找   （外）（李第亮）
     *
     * @param param 查询参数
     * @return 返回搜索所需要的结果封装成的对象，包含搜索id，成功与否标志，记录数，记录信息，照片id
     */
    public SqlRowSet searchObjectInfo(GetObjectInfoParam param) {
        //封装的sql 以及需要设置的值
        SqlAndArgs sqlAndArgs = parseByOption.getSqlFromGetObjectInfoParm(param);
        // 取出封装的sql 以及需要设置的值，进行sql 查询
        if (sqlAndArgs == null) {
            log.warn("Start get object info, generate sql failed");
            return null;
        }
        log.info("Start get object info, generate sql successfully");
        log.info("Start get object info, SQL is : " + sqlAndArgs.getSql());
        log.info("Start get object info, SQL args is : " + sqlAndArgs.getArgs().toString());
        return jdbcTemplate.queryForRowSet(sqlAndArgs.getSql(), sqlAndArgs.getArgs().toArray());
    }

    /**
     * 根据rowkey 返回人员的照片
     *
     * @param objectId 人员在对象信息库中的唯一标志。
     * @return 图片的byte[] 数组
     */
    public byte[] getPhotoByObjectId(String objectId) {
        String sql = parseByOption.getPhotoByObjectId();
        log.info("Start get object photo, SQL is : " + sql);
        byte[] photo = null;
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, objectId);
        while (sqlRowSet.next()) {
            photo = (byte[]) sqlRowSet.getObject(ObjectInfoTable.PHOTO);
        }
        if (photo != null && photo.length > 0){
            log.info("Start get object photo successfully");
        }
        return photo;
    }

    /**
     * 根据传过来的搜索rowkey 返回搜索记录 （外） （李第亮）
     *
     * @param subQueryId 子查询Id
     * @return 返回一个ObjectSearchResult 对象，
     * @author 李第亮
     */
    public PersonSingleResult getRocordOfObjectInfo(String subQueryId) {
        // sql 查询语句
        String sql = "select " + SearchRecordTable.ID + ", " + SearchRecordTable.RESULT + " from "
                + SearchRecordTable.TABLE_NAME + " where " + SearchRecordTable.ID + " = ?";
        PersonSingleResult personSingleResult = null;

        try {
            RowMapper<PersonSingleResult> rowMapper = new BeanPropertyRowMapper<>(PersonSingleResult.class);
            personSingleResult = jdbcTemplate.queryForObject(sql, rowMapper, subQueryId);
            ResultSet resultSet = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return personSingleResult;
    }

    /**
     * 统计常住人口
     *
     * @return int 常住人口数量
     */
    public int countStatus() {
        log.info("Start count objectInfo person.status from " + ObjectInfoTable.TABLE_NAME + "table.");
        String sql = parseByOption.countStatus();
        log.info("Start count permanent population, SQL is : " + sql);
        int count = 0;
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql);
        while (sqlRowSet.next()) {
            count = sqlRowSet.getInt("num");
        }
        log.info("Count permanent population is: " + count);
        return count;
    }

    public PeopleManagerCount emigrationCount(String month, Timestamp startTime, Timestamp endTime) {
        log.info("Start count emigration population, param is : month = " + month
                + ", startTime = " + startTime + ", endTime = " + endTime);
        String sql = parseByOption.emigrationCount();
        log.info("Start count emigration population, SQL is: " + sql);
        PeopleManagerCount count = null;
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, startTime, endTime);
        while (sqlRowSet.next()) {
            count = new PeopleManagerCount();
            count.setMonth(month);
            int i = sqlRowSet.getInt("num");
            count.setRemovePeople(i);
        }
        if (count != null) {
            log.info("Count emigration population is : [ month = " + count.getMonth() + ", count = " + count.getRemovePeople() + "]");
        }
        return count;
    }
}
