package com.hzgc.service.starepo.service;

import com.hzgc.common.table.seachres.SearchResultTable;
import com.hzgc.common.table.starepo.ObjectInfoTable;
import com.hzgc.common.util.empty.IsEmpty;
import com.hzgc.common.util.json.JSONUtil;
import com.hzgc.common.util.uuid.UuidUtil;
import com.hzgc.jni.PictureData;
import com.hzgc.service.starepo.bean.StaticSortParam;
import com.hzgc.service.starepo.bean.export.*;
import com.hzgc.service.starepo.bean.param.GetObjectInfoParam;
import com.hzgc.service.starepo.bean.param.ObjectInfoParam;
import com.hzgc.service.starepo.bean.param.SearchRecordParam;
import com.hzgc.service.starepo.bean.param.SubQueryParam;
import com.hzgc.service.starepo.dao.HBaseDao;
import com.hzgc.service.starepo.dao.PhoenixDao;
import com.hzgc.service.starepo.util.DocHandlerUtil;
import com.hzgc.service.util.bean.PeopleManagerCount;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hzgc.service.starepo.service.StaticProtocol.*;

@Service
@Slf4j
public class ObjectInfoHandlerService {

    @Autowired
    private HBaseDao hbaseDao;
    @Autowired
    private PhoenixDao phoenixDao;
    @Autowired
    private StaticRepoProducer staticProducer;
    @Autowired
    private ObjectInfoHandlerTool objectInfoHandlerTool;
    @Autowired
    private ParseByOption parseByOption;
    @Autowired
    private RestTemplate restTemplate;

    public ObjectInfoHandlerService() {
    }

    /**
     * 判断身份证格式是否正确
     *
     * @param objectInfo
     * @return true:正确,false:不正确
     */
    public boolean authentication_idCode(ObjectInfoParam objectInfo) {
        if (!StringUtils.isBlank(objectInfo.getIdcard())) {
            return idCodeAuthentication(objectInfo.getIdcard());
        }
        return true;
    }

    /**
     * 获取数据库中idCard
     *
     * @param objectInfo
     * @return idCard
     */
    public String getObjectIdCard(ObjectInfoParam objectInfo) {
        return phoenixDao.getObjectIdCard(objectInfo.getId());
    }

    /**
     * 身份证唯一性判断
     *
     * @param objectInfo
     * @return true:存在  false:不存在
     */
    public boolean isExists_idCode(ObjectInfoParam objectInfo) {
        if (!StringUtils.isBlank(objectInfo.getIdcard())) {
            List<String> idcards = phoenixDao.getAllObjectIdcard();
            return idcards.contains(objectInfo.getIdcard());
        }
        return false;
    }

    /**
     * 判断ObjectTypeKey是否存在
     *
     * @param objectInfo
     * @return true:存在  false:不存在
     */
    public boolean isExists_objectTypeKey(ObjectInfoParam objectInfo) {
        List<String> objectTypeKeys = phoenixDao.getAllObjectTypeKeys();
        return objectTypeKeys.contains(objectInfo.getObjectTypeKey());
    }


    /**
     * Add objectInfo
     *
     * @param objectInfo 添加对象信息
     * @return 返回值为0，表示插入成功，返回值为1，表示插入失败
     */
    public Integer addObjectInfo(ObjectInfoParam objectInfo) {
        objectInfo.setId(UuidUtil.getUuid());
        log.info("Start add object info, object id is:" + objectInfo.getId());
        //数据库操作
        Integer i = phoenixDao.addObjectInfo(objectInfo);
        //向告警中同步数据
        sendKafka(objectInfo, ADD);
        return i;
    }

    /**
     * 删除对象的信息
     *
     * @param rowkeyList 对象ID
     * @return 返回值为0，表示删除成功，返回值为1，表示删除失败
     */
    public int deleteObjectInfo(List<String> rowkeyList) {
        int i = phoenixDao.deleteObjectInfo(rowkeyList);
        if (i == 0) {
            for (String rowkey : rowkeyList) {
                staticProducer.sendKafkaMessage(INNERTOPIC, DELETE, rowkey);
            }
        }
        return i;
    }

    /**
     * 修改对象的信息   （外）（李第亮）
     *
     * @param param 修改对象信息
     * @return 返回值为0，表示更新成功，返回值为1，表示更新失败
     */
    public Integer updateObjectInfo(ObjectInfoParam param) {
        Integer i = phoenixDao.updateObjectInfo(param);
        if (i == 0){
            sendKafka(param, UPDATE);
        }
        return i;
    }

    private void sendKafka(ObjectInfoParam param, String option) {
        StaticRepoObject object = new StaticRepoObject();
        if (param.getPictureDatas() != null && param.getPictureDatas().getFeature() != null) {
            object.setFeature(param.getPictureDatas().getFeature().getFeature());
        }
        //向告警同步数据
        object.setPkey(param.getObjectTypeKey());
        object.setRowkey(param.getId());
        staticProducer.sendKafkaMessage(INNERTOPIC, option, JSONUtil.toJson(object));
    }

    /**
     * 认证身份证格式是否正确。
     *
     * @param idCode 身份证ID
     * @return false 身份证不正确 true 身份证正确
     */
    private boolean idCodeAuthentication(String idCode) {
        if (idCode == null || idCode.isEmpty() || idCode.length() != 18) {
            return false;
        }
        String regEX = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$";
        Pattern pattern = Pattern.compile(regEX);
        Matcher matcher = pattern.matcher(idCode);
        return matcher.matches();
    }

    /**
     * 更新人员信息状态值
     *
     * @param objectId 对象ID
     * @param status   状态值
     * @return 返回值为0，表示更新成功，返回值为1，表示更新失败
     */
    public int updateObjectInfo_status(String objectId, int status) {
        // 查询更新对象当前状态值
        int info_status = phoenixDao.getObjectInfo_status(objectId);
        if (info_status == status) {
            log.error("Start update object status, but object status is the same as in the database, " +
                    "so doesn't need to be updated");
            return 1;
        }
        //数据库更新操作
        return phoenixDao.updateObjectInfo_status(objectId, status);
    }

    /**
     * 根据id查询对象
     *
     * @param objectId 对象ID
     * @return ObjectInfo
     */
    public ObjectInfo getObjectInfo(String objectId) {
        return phoenixDao.getObjectInfo(objectId);
    }

    /**
     * 可以匹配精确查找，以图搜索人员信息，模糊查找   （外）（李第亮）
     *
     * @param param 搜索参数的封装
     * @return 返回搜索所需要的结果封装成的对象，包含搜索id，成功与否标志，记录数，记录信息，照片id
     */
    public ObjectSearchResult searchObjectInfo(GetObjectInfoParam param) {
        ObjectSearchResult objectSearchResult;
        SqlRowSet sqlRowSet = phoenixDao.searchObjectInfo(param);
        if (sqlRowSet == null) {
            return new ObjectSearchResult();
        }
        if (param.getPictureDataList() != null && param.getPictureDataList().size() > 0) {
            Map<String, PictureData> photosMap =
                    param.getPictureDataList().stream().collect(Collectors.toMap(PictureData::getImageID, data -> data));
            if (!param.isSinglePerson()) {
                log.info("Start get object info, according to not the same person");
                objectSearchResult = getObjectInfoNotOnePerson(sqlRowSet, photosMap);
                //存储搜索结果
                hbaseDao.saveSearchRecord(param, objectSearchResult);
                log.info("Get object info successfull, save search result to 'searchRes' table successfull");
            } else {
                //同一个人的情况下
                log.info("Start get object info, according to the same person");
                objectSearchResult = getObjectInfoOnePerson(sqlRowSet, photosMap);
                //存储搜索结果
                hbaseDao.saveSearchRecord(param, objectSearchResult);
                log.info("Get object info successfull, save search result to 'searchRes' table successfull");
            }
        } else {
            log.info("Start get object info not search picture");
            //封装personSingleResult
            String searchId = UuidUtil.getUuid();
            objectSearchResult = new ObjectSearchResult();
            objectSearchResult.setSearchId(searchId);
            PersonSingleResult personSingleResult = new PersonSingleResult();
            List<PersonSingleResult> singleResults = new ArrayList<>();
            String personSingleResultId = UuidUtil.getUuid();
            personSingleResult.setSearchId(personSingleResultId);
            objectInfoHandlerTool.getPersonSingleResult(personSingleResult, sqlRowSet, false);
            singleResults.add(personSingleResult);
            objectSearchResult.setSingleSearchResults(singleResults);
            objectSearchResult.setSearchId(UuidUtil.getUuid());
            hbaseDao.saveSearchRecord(param, objectSearchResult);
            log.info("Get object info successfull, save search result to 'searchRes' table successfull");
        }
        //返回分页结果
        objectInfoHandlerTool.formatTheObjectSearchResult(objectSearchResult, param.getStart(), param.getLimit());
        log.info("Get object info successfull, object info search result : " + JSONUtil.toJson(objectSearchResult));
        return objectSearchResult;
    }

    private ObjectSearchResult getObjectInfoOnePerson(SqlRowSet sqlRowSet, Map<String, PictureData> photosMap) {
        ObjectSearchResult objectSearchResult = new ObjectSearchResult();
        PersonSingleResult personSingleResult = new PersonSingleResult();
        String searchId = UuidUtil.getUuid();
        objectSearchResult.setSearchId(searchId);
        personSingleResult.setSearchId(searchId);
        List<PersonSingleResult> singleResults = new ArrayList<>();
        List<String> searchPhotoIds = new ArrayList<>();
        for (Map.Entry<String, PictureData> entryV1 : photosMap.entrySet()) {
            String rowkey = entryV1.getValue().getImageID();
            searchPhotoIds.add(rowkey);
        }
        personSingleResult.setImageNames(searchPhotoIds);
        // 封装personSingleResult
        objectInfoHandlerTool.getPersonSingleResult(personSingleResult, sqlRowSet, true);
        singleResults.add(personSingleResult);
        objectSearchResult.setSingleSearchResults(singleResults);
        return objectSearchResult;
    }

    private ObjectSearchResult getObjectInfoNotOnePerson(SqlRowSet sqlRowSet, Map<String, PictureData> photosMap) {
        ObjectSearchResult objectSearchResult = new ObjectSearchResult();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String searchId = UuidUtil.getUuid();
        objectSearchResult.setSearchId(searchId);
        // 创建总结果list
        List<PersonSingleResult> singleResults = new ArrayList<>();
        // 分类的人
        Map<String, List<PersonObject>> personObjectsMap = new HashMap<>();
        List<PersonObject> personObjects = new ArrayList<>();
        List<String> types = new ArrayList<>();
        int lable = 0;
        while (sqlRowSet.next()) {
            String type = sqlRowSet.getString(ObjectInfoTable.PKEY);
            if (!types.contains(type)) {
                types.add(type);
                if (types.size() > 1) {
                    personObjectsMap.put(types.get(lable), personObjects);
                    personObjects = new ArrayList<>();
                    lable++;
                }
            }

            String objectTypeKey = sqlRowSet.getString(ObjectInfoTable.PKEY);
            String objectTypeName = "";
            if (!StringUtils.isBlank(objectTypeKey)) {
                objectTypeName = phoenixDao.getObjectTypeNameById(objectTypeKey);
            }

            Timestamp createTime = sqlRowSet.getTimestamp(ObjectInfoTable.CREATETIME);
            String createTime_str = "";
            if (createTime != null) {
                java.util.Date createTime_data = new java.util.Date(createTime.getTime());
                createTime_str = sdf.format(createTime_data);
            }

            PersonObject personObject = PersonObject.builder()
                    .setObjectID(sqlRowSet.getString(ObjectInfoTable.ROWKEY))
                    .setObjectTypeKey(objectTypeKey)
                    .setObjectTypeName(objectTypeName)
                    .setName(sqlRowSet.getString(ObjectInfoTable.NAME))
                    .setSex(sqlRowSet.getInt(ObjectInfoTable.SEX))
                    .setIdcard(sqlRowSet.getString(ObjectInfoTable.IDCARD))
                    .setCreator(sqlRowSet.getString(ObjectInfoTable.CREATOR))
                    .setCreatorConractWay(sqlRowSet.getString(ObjectInfoTable.CPHONE))
                    .setCreateTime(createTime_str)
                    .setReason(sqlRowSet.getString(ObjectInfoTable.REASON))
                    .setFollowLevel(sqlRowSet.getInt(ObjectInfoTable.IMPORTANT))
                    .setSimilarity(sqlRowSet.getFloat(ObjectInfoTable.RELATED))
                    .setLocation(sqlRowSet.getString(ObjectInfoTable.LOCATION));
            personObjects.add(personObject);
        }
        // 封装最后需要返回的结果
        for (Map.Entry<String, List<PersonObject>> entryVV : personObjectsMap.entrySet()) {
            String key = entryVV.getKey();
            List<PersonObject> persons = entryVV.getValue();
            PersonSingleResult personSingleResult = new PersonSingleResult();
            personSingleResult.setSearchId(searchId + key);
            personSingleResult.setTotal(persons.size());
            personSingleResult.setObjectInfoBeans(persons);
            String rowkey = photosMap.get(key).getImageID();
            List<String> photoNames = new ArrayList<>();
            photoNames.add(rowkey);
            personSingleResult.setImageNames(photoNames);
            singleResults.add(personSingleResult);
        }
        objectSearchResult.setSingleSearchResults(singleResults);
        return objectSearchResult;
    }

    /**
     * 根据rowkey 返回人员的照片
     *
     * @param objectId 人员在对象信息库中的唯一标志。
     * @return 图片的byte[] 数组
     */
    public byte[] getPhotoByKey(String objectId) {
        return phoenixDao.getPhotoByObjectId(objectId);
    }

    /**
     * 根据传过来的搜索rowkey 返回搜索记录 （外） （李第亮）
     *
     * @param searchRecordParam 历史查询参数
     * @return 返回一个ObjectSearchResult 对象，
     * @author 李第亮
     * 里面包含了本次查询ID，查询成功标识，
     * 查询照片ID（无照片，此参数为空），结果数，人员信息列表
     */
    public ObjectSearchResult getRocordOfObjectInfo(SearchRecordParam searchRecordParam) {
        log.info("searchRecordParam: " + searchRecordParam);
        // 传过来的参数中为空，或者子查询为空，或者子查询大小为0，都返回查询错误。
        if (searchRecordParam == null) {
            log.info("SearchRecordParam 为空，请确认参数是否正确.");
            return null;
        }
        // 总的searchId
        List<SubQueryParam> subQueryParamList = searchRecordParam.getSubQueryParamList();
        if (subQueryParamList == null || subQueryParamList.size() == 0) {
            log.info("子查询列表为空，请确认参数是否正确.");
            return null;
        }

        SubQueryParam subQueryParam = subQueryParamList.get(0);
        if (subQueryParam == null) {
            log.info("子查询对象SubQueryOpts 对象为空，请确认参数是否正确.");
            return null;
        }

        // 子查询Id
        String subQueryId = subQueryParam.getQueryId();
        if (subQueryId == null) {
            log.info("子查询Id 为空");
            return null;
        }
        PersonSingleResult personSingleResult = phoenixDao.getRocordOfObjectInfo(subQueryId);
        // 需要分组的pkeys
        List<String> pkeys = subQueryParamList.get(0).getObjectTypekeyList();
        // 排序参数
        List<StaticSortParam> staticSortParams = searchRecordParam.getStaticSortParams();
        ObjectSearchResult finnalObjectSearchResult = new ObjectSearchResult();
        List<PersonSingleResult> personSingleResults = new ArrayList<>();
        if (personSingleResult != null) {
            List<PersonObject> personObjects = personSingleResult.getObjectInfoBeans();
            List<PersonObjectGroupByPkey> personObjectGroupByPkeyList = new ArrayList<>();
            if (personObjects != null && staticSortParams != null && staticSortParams.contains(StaticSortParam.PEKEY)) {
                Map<String, List<PersonObject>> groupingByPkeys = personObjects.stream()
                        .collect(Collectors.groupingBy(PersonObject::getObjectTypeKey));
                for (Map.Entry<String, List<PersonObject>> entry : groupingByPkeys.entrySet()) {
                    PersonObjectGroupByPkey personObjectGroupByPkey = new PersonObjectGroupByPkey();
                    String pkey = entry.getKey();
                    personObjectGroupByPkey.setObjectTypeKey(pkey);
                    personObjectGroupByPkey.setObjectTypeName(entry.getValue().get(0).getObjectTypeName());
                    List<PersonObject> personObjectList = entry.getValue();
                    // 对结果进行排序
                    objectInfoHandlerTool.sortPersonObject(personObjectList, staticSortParams);

                    // 如果指定了需要返回的Pkey
                    if (pkeys != null && pkeys.size() > 0 && pkeys.contains(pkey)) {
                        personObjectGroupByPkey.setPersonObjectList(personObjectList);
                        personObjectGroupByPkeyList.add(personObjectGroupByPkey);
                        continue;
                    }
                    if (pkeys == null || pkeys.size() == 0) {
                        personObjectGroupByPkey.setPersonObjectList(personObjectList);
                        personObjectGroupByPkeyList.add(personObjectGroupByPkey);
                    }
                }
                personSingleResult.setSingleObjKeyResults(personObjectGroupByPkeyList);
                personSingleResult.setObjectInfoBeans(null);
            } else if (personObjects != null && staticSortParams != null && !staticSortParams.contains(StaticSortParam.PEKEY)) {
                personSingleResult.setSingleObjKeyResults(null);
                objectInfoHandlerTool.sortPersonObject(personObjects, staticSortParams);
                personSingleResult.setObjectInfoBeans(personObjects);
            }
        }
        personSingleResults.add(personSingleResult);
        finnalObjectSearchResult.setSingleSearchResults(personSingleResults);
        int pageSize = searchRecordParam.getSize();
        int start = searchRecordParam.getStart();
        objectInfoHandlerTool.formatTheObjectSearchResult(finnalObjectSearchResult, start, pageSize);
        return finnalObjectSearchResult;
    }

    /**
     * 根据传过来的rowkey查询并生成结果文件并保存
     *
     * @param opts 历史查询参数
     * @return 返回文件的rowkey
     */
    public String exportPeoples(SearchRecordParam opts) {
        //查询搜索记录
        String searchId = opts.getTotalSearchId();
        byte[] bytes = hbaseDao.get(searchId, SearchResultTable.STAREPO_COLUMN_SEARCHMESSAGE);
        if (bytes == null || bytes.length <= 0) {
            log.info("Start create emphasis personnel word, but get search result is null from 'searchRes' table");
            return null;
        }
        ObjectSearchResult objectSearchResult = JSONUtil.toObject(new String(bytes), ObjectSearchResult.class);
        if (objectSearchResult == null || objectSearchResult.getSingleSearchResults() == null) {
            log.info("Start create emphasis personnel word, but search result is null");
            return null;
        }
        PersonSingleResult result = objectSearchResult.getSingleSearchResults().get(0);

        //查询所有的类型名
        List<PersonObject> perList;
        if (result != null) {
            perList = result.getObjectInfoBeans();
            List<String> typeKey = perList.stream().map(PersonObject::getObjectTypeKey).collect(Collectors.toList());
            //Map<pkey,类型名>
            Map<String, String> map = phoenixDao.searchTypeNames(typeKey);
            //填充结果数据到 objectData
            List<Map<String, Object>> objectData = new ArrayList<>();
            fillPeopleDoc(objectData, result, opts.getStart(), opts.getSize(), map);
            String exportFile = ConfigConstants.EXPORT_FILE_NAME;
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put(ConfigConstants.PEOPLE_DATA_KEY, objectData);
            byte[] buff = DocHandlerUtil.createDoc(dataMap, File.separator + exportFile);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String data = sdf.format(new Date());
            //把文件插入HBase表中
            String rowkey = "file_" + data + "_" + UuidUtil.getUuid().substring(0, 4) + ".doc";
            hbaseDao.insert_word(rowkey, buff);
            //返回文件Id
            log.info("create emphasis personnel word successfull");
            return rowkey;
        }
        log.info("create emphasis personnel word failed");
        return null;
    }

    /**
     * 根据传过来的搜索rowkey 返回一个子查询
     *
     * @param opts 历史查询参数
     * @return 返回一个PersonSingleResult 对象
     * @author 李第亮
     * 里面包含了本次查询ID，查询成功标识，
     * 查询照片ID（无照片，此参数为空），结果数，人员信息列表
     */
    private PersonSingleResult getSearchResult(SearchRecordParam opts) {
        log.info("searchRecordOpts: " + opts);
        // 传过来的参数中为空，或者子查询为空，或者子查询大小为0，都返回查询错误。
        if (opts == null) {
            log.info("SearchRecordParam 为空，请确认参数是否正确.");
            return null;
        }
        // 总的searchId
        List<SubQueryParam> subQueryParamList = opts.getSubQueryParamList();
        if (subQueryParamList == null || subQueryParamList.size() == 0) {
            log.info("子查询列表为空，请确认参数是否正确.");
            return null;
        }

        SubQueryParam subQueryParam = subQueryParamList.get(0);
        if (subQueryParam == null) {
            log.info("子查询对象SubQueryOpts 对象为空，请确认参数是否正确.");
            return null;
        }

        // 子查询Id
        String subQueryId = subQueryParam.getQueryId();
        if (subQueryId == null) {
            log.info("子查询Id 为空");
            return null;
        }
        return phoenixDao.getRocordOfObjectInfo(subQueryId);
    }

    /**
     * 填充结果数据
     *
     * @param objectData 需要填充的数据集
     * @param result     查询返回结果
     * @param start      填充开始位置
     * @param limit      填充数据量
     */
    private void fillPeopleDoc(List<Map<String, Object>> objectData, PersonSingleResult result,
                               int start, int limit, Map<String, String> typeMap) {
        List<PersonObject> persons = result.getObjectInfoBeans();
        for (int i = 0; i < persons.size(); i++) {
            PersonObject personObject = persons.get(i);
            Map<String, Object> map = new HashMap<>();
            if (i >= start - 1 && i < start + limit - 1) {
                // 序号
                map.put("index", "序号");
                map.put("indexData", i);
                // 布控时间
                map.put("time", "时间");
                if (null != personObject.getCreateTime()) {
                    map.put("timeData", personObject.getCreateTime());
                } else {
                    map.put("timeData", "");
                }
                // 对象类型
                map.put(ConfigConstants.TYPE_KEY, ConfigConstants.TYPE_KEY_DATA);
                if (IsEmpty.strIsRight(typeMap.get(personObject.getObjectTypeKey()))) {
                    map.put(ConfigConstants.TYPE_DATA_KEY, typeMap.get(personObject.getObjectTypeKey()));
                } else {
                    map.put(ConfigConstants.TYPE_DATA_KEY, ConfigConstants.NO_DATA);
                }
                // 对象名称
                map.put(ConfigConstants.NAME_KEY, ConfigConstants.NAME_KEY_DATA);
                if (IsEmpty.strIsRight(personObject.getName())) {
                    map.put(ConfigConstants.NAME_DATA_KEY, personObject.getName());
                } else {
                    map.put(ConfigConstants.NAME_DATA_KEY, ConfigConstants.NO_DATA);
                }
                // 性别
                map.put(ConfigConstants.SEX_KEY, ConfigConstants.SEX_KEY_DATA);
                if (IsEmpty.strIsRight(String.valueOf(personObject.getSex()))) {
                    map.put(ConfigConstants.SEX_DATA_KEY, personObject.getSex() == 1 ? "男" : (personObject.getSex() == 2 ? "女" : "未知"));
                } else {
                    map.put(ConfigConstants.SEX_DATA_KEY, ConfigConstants.NO_DATA);
                }
                // 相似度
                map.put(ConfigConstants.SIMILARITY_KEY, ConfigConstants.SIMILARITY_KEY_DATA);
                map.put(ConfigConstants.SIMILARITY_DATA_KEY, personObject.getSimilarity());
                // 布控人
                map.put(ConfigConstants.CHARGE_KEY, ConfigConstants.CHARGE_KEY_DATA);
                if (IsEmpty.strIsRight(personObject.getCreator())) {
                    map.put(ConfigConstants.CHARGE_DATA_KEY, personObject.getCreator());
                } else {
                    map.put(ConfigConstants.CHARGE_DATA_KEY, ConfigConstants.NO_DATA);
                }
                // 联系方式
                map.put(ConfigConstants.TELEPHNOE_KEY, ConfigConstants.TELEPHNOE_KEY_DATA);
                if (IsEmpty.strIsRight(personObject.getCreatorConractWay())) {
                    map.put(ConfigConstants.TELEPHONE_DATA_KEY, personObject.getCreatorConractWay());
                } else {
                    map.put(ConfigConstants.TELEPHONE_DATA_KEY, ConfigConstants.NO_DATA);
                }
                // 身份证
                map.put(ConfigConstants.ID_KEY, ConfigConstants.ID_KEY_DATA);
                if (IsEmpty.strIsRight(personObject.getIdcard())) {
                    map.put(ConfigConstants.IDDATA_KEY, personObject.getIdcard());
                } else {
                    map.put(ConfigConstants.IDDATA_KEY, ConfigConstants.NO_DATA);
                }
                // 布控原因
                map.put(ConfigConstants.REASON_KEY, ConfigConstants.REASON_KEY_DATA);
                if (IsEmpty.strIsRight(personObject.getReason())) {
                    map.put(ConfigConstants.REASON_DATA_KEY, personObject.getReason());
                } else {
                    map.put(ConfigConstants.REASON_DATA_KEY, ConfigConstants.NO_DATA);
                }
                byte[] photo = phoenixDao.getPhotoByObjectId(personObject.getObjectID());
                // 填充图片内容
                if (photo != null && photo.length > 0) {
                    map.put(ConfigConstants.PICTURE_KEY, new String(org.apache.commons.codec.binary.Base64.encodeBase64(photo)));
                } else {
                    map.put(ConfigConstants.PICTURE_KEY, ConfigConstants.NO_DATA);
                }
                // 图片序号
                map.put(ConfigConstants.PICTURE_INDEX_KEY, i);
                objectData.add(map);
            }
        }
    }

    /**
     * 根据Id获取静态库库中的特征值
     *
     * @param id 对象ID
     * @return PictureData
     */
    public PictureData getFeature(String id) {
        PictureData pictureData = null;
        byte[] photo = phoenixDao.getPhotoByObjectId(id);
        if (photo != null) {
            pictureData = restTemplate.postForObject("http://face/extract_bytes", photo, PictureData.class);
        }
        log.info("Get object picture data successfully, picture data : " + JSONUtil.toJson(pictureData));
        return pictureData;
    }

    /**
     * 根据rowkey在SearchRecordTable中获取对应数据（Word、搜索原图）
     *
     * @param rowkey SearchRecordTable ID
     * @return byte[]
     */
    public byte[] getDataFromHBase(String rowkey, byte[] column) {
        return hbaseDao.get(rowkey, column);
    }

    /**
     * 统计常住人口
     *
     * @return int 常住人口数量
     */
    public int permanentPopulationCount() {
        return phoenixDao.countStatus();
    }

    public List<PeopleManagerCount> emigrationCount(String start_time, String end_time) {
        List<PeopleManagerCount> emigrationCountList = new ArrayList<>();
        List<String> monthList = getMonthsInRange(start_time, end_time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (String month : monthList) {
            String start = month + "-" + "01";
            int dates = getActualMaximum(month);
            String end = month + "-" + dates;
            Timestamp startTime = null;
            Timestamp endTime = null;
            try {
                Date startDate = sdf.parse(start);
                Date endDate = sdf.parse(end);
                startTime = new Timestamp(startDate.getTime());
                endTime = new Timestamp(endDate.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            PeopleManagerCount emigrationCount = phoenixDao.emigrationCount(month, startTime, endTime);
            emigrationCountList.add(emigrationCount);
        }
        return emigrationCountList;
    }

    /**
     * 获取某段时间内所有的月份值
     *
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return 月份列表
     */
    private static List<String> getMonthsInRange(String startTime, String endTime) {
        List<String> monthList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        try {
            Calendar start = Calendar.getInstance();
            start.setTime(sdf.parse(startTime));
            Calendar end = Calendar.getInstance();
            end.setTime(sdf.parse(endTime));
            Long startTimeL = start.getTimeInMillis();
            Long endTimeL = end.getTimeInMillis();
            while (startTimeL <= endTimeL) {
                Date everyTime = new Date(startTimeL);
                monthList.add(sdf.format(everyTime));

                start.add(Calendar.MONTH, 1);
                startTimeL = start.getTimeInMillis();
            }
            return monthList;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return monthList;
    }

    /**
     * 获取当前月份共有多少天
     *
     * @param date 时间 格式：2018-09
     * @return 当月天数
     */
    public static int getActualMaximum(String date) {
        int year = Integer.parseInt(String.valueOf(date.charAt(0)) + date.charAt(1) + date.charAt(2) + date.charAt(3));
        int month = Integer.parseInt(String.valueOf(date.charAt(5)) + date.charAt(6));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);             // 当前月份减1
        return cal.getActualMaximum(Calendar.DATE);
    }
}

@Data
class StaticRepoObject implements Serializable {
    private float[] feature;
    private String pkey;
    private String rowkey;
}


class StaticProtocol {
    static String INNERTOPIC = "staticrepo";
    static final String DELETE = "DELETE";
    static final String ADD = "ADD";
    static final String UPDATE = "UPDATE";
}