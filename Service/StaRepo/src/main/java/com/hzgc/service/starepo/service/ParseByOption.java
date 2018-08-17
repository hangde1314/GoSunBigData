package com.hzgc.service.starepo.service;

import com.hzgc.common.table.starepo.ObjectInfoTable;
import com.hzgc.common.table.starepo.ObjectTypeTable;
import com.hzgc.jni.FaceAttribute;
import com.hzgc.jni.FaceFunction;
import com.hzgc.jni.PictureData;
import com.hzgc.service.starepo.bean.*;
import com.hzgc.service.starepo.bean.param.GetObjectInfoParam;
import com.hzgc.service.starepo.bean.param.ObjectInfoParam;
import com.hzgc.service.starepo.dao.SqlAndArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class ParseByOption {

    public String getAllObjectTypeNames() {
        return "select " + ObjectTypeTable.TYPE_NAME + " from " + ObjectTypeTable.TABLE_NAME;
    }

    public String addObjectType() {
        return "upsert into " + ObjectTypeTable.TABLE_NAME + "( "
                + ObjectTypeTable.ROWKEY + ", "
                + ObjectTypeTable.TYPE_NAME + ", "
                + ObjectTypeTable.TYPE_CREATOR + ", "
                + ObjectTypeTable.TYPE_REMARK + ", "
                + ObjectTypeTable.TYPE_ADD_TIME
                + ") values (?,?,?,?,?)";
    }

    public String deleteObjectType_select() {
        return "select id from " + ObjectInfoTable.TABLE_NAME + " where " + ObjectInfoTable.PKEY + " = ?";
    }

    public String deleteObjectType_delete() {
        return "delete from " + ObjectTypeTable.TABLE_NAME + " where " + ObjectTypeTable.ROWKEY + " = ?";
    }

    public String updateObjectType() {
        return "upsert into " + ObjectTypeTable.TABLE_NAME + "( "
                + ObjectTypeTable.ROWKEY + ", "
                + ObjectTypeTable.TYPE_NAME + ", "
                + ObjectTypeTable.TYPE_CREATOR + ", "
                + ObjectTypeTable.TYPE_REMARK + ", "
                + ObjectTypeTable.TYPE_UPDATE_TIME
                + ") values (?,?,?,?,?)";
    }

    public String searchObjectType() {
        return "select " + ObjectTypeTable.ROWKEY + ", "
                + ObjectTypeTable.TYPE_NAME + ", "
                + ObjectTypeTable.TYPE_CREATOR + ", "
                + ObjectTypeTable.TYPE_REMARK + ", "
                + ObjectTypeTable.TYPE_ADD_TIME
                + " from " + ObjectTypeTable.TABLE_NAME
                + " where " + ObjectTypeTable.ROWKEY + " > ?" + " LIMIT ?";
    }

    public String countObjectType() {
        return "select count(*) as num from " + ObjectTypeTable.TABLE_NAME;
    }

    public SqlAndArgs getSqlFromGetObjectInfoParm(GetObjectInfoParam param) {
        StringBuilder sql = new StringBuilder();
        List<Object> setValues = new ArrayList<>();
        List<PictureData> pictureDataList = param.getPictureDataList();
        List<StaticSortParam> params = param.getSortParamList();
        sql.append("select ");
        if (pictureDataList != null && pictureDataList.size() != 0) {
            // 最终需要返回的内容
            sql.append(sameFieldNeedReturn());
            if (param.isSinglePerson() && pictureDataList.size() == 1) {
                sql.append(", sim as sim")
                        .append(" from (select ")
                        .append(sameFieldNeedReturn())
                        .append(", FACECOMP(")
                        .append(ObjectInfoTable.FEATURE)
                        .append(", ?");
                StringBuilder featureString = new StringBuilder();
                int size = pictureDataList.size();
                int count = 1;
                for (PictureData picture : pictureDataList) {
                    FaceAttribute faceAttribute = picture.getFeature();
                    if (faceAttribute != null) {
                        if (count == size) {
                            featureString.append(FaceFunction.floatArray2string(faceAttribute.getFeature()));
                        } else {
                            featureString.append(FaceFunction.floatArray2string(faceAttribute.getFeature())).append(",");
                        }
                    }
                    count++;
                }
                setValues.add(new String(featureString));
                sql.append(") as sim from ")
                        .append(ObjectInfoTable.TABLE_NAME);
                List<Object> whereParamList = new ArrayList<>();
                String whereSql = sameWhereSql(param, whereParamList);
                if (whereParamList.size() > 0) {
                    sql.append(" where ").append(whereSql);
                }
                log.info("Where SQL :" + whereSql);
                log.info("Where SQL param list :" + Arrays.toString(whereParamList.toArray()));
                sql.append(")").append(" where sim >= ? ");
                setValues.add(param.getSimilarity());
                sql.append("order by ")
                        .append(sameSortSql(params, true));
            } else if (!param.isSinglePerson() && pictureDataList.size() != 1) {
                sql.append(", type")
                        .append(", sim")
                        .append(" from (");

                // 拼装子sql
                List<StringBuffer> subSqls = new ArrayList<>();
                for (PictureData photo : pictureDataList) {
                    String key = photo.getImageID();
                    FaceAttribute faceAttribute = photo.getFeature();
                    float[] feature = null;
                    if (faceAttribute != null && faceAttribute.getFeature() != null) {
                        feature = faceAttribute.getFeature();
                    }
                    StringBuffer subSql = new StringBuffer("");
                    subSql.append("select ")
                            .append(sameFieldNeedReturn())
                            .append(", ? as type");
                    setValues.add(key);
                    subSql.append(", FACECOMP(")
                            .append(ObjectInfoTable.FEATURE)
                            .append(", ?");
                    setValues.add(FaceFunction.floatArray2string(feature));
                    subSql.append(") as sim from ")
                            .append(ObjectInfoTable.TABLE_NAME);
                    List<Object> whereParamList = new ArrayList<>();
                    String whereSql = sameWhereSql(param, whereParamList);
                    if (whereParamList.size() > 0){
                        subSql.append(" where ").append(whereSql);
                    }
                    log.info("Where SQL :" + whereSql);
                    log.info("Where SQL param list :" + Arrays.toString(whereParamList.toArray()));
                    subSqls.add(subSql);
                }

                //完成子sql 的拼装
                sql.append(subSqls.get(0));
                for (int i = 1; i < subSqls.size(); i++) {
                    sql.append(" union all ");
                    sql.append(subSqls.get(i));
                }

                sql.append(") where sim >= ? ");
                setValues.add(param.getSimilarity());
                //排序sql 拼装
                if (params != null && params.size() != 0) {
                    sql.append(" order by type,");
                }
                sql.append(sameSortSql(params, true));
            } else {
                log.info("Start get object info, param is error");
                return null;
            }
        } else {
            sql.append(sameFieldNeedReturn())
                    .append(" from ")
                    .append(ObjectInfoTable.TABLE_NAME);
            List<Object> whereParamList = new ArrayList<>();
            String whereSql = sameWhereSql(param, whereParamList);
            log.info("Where SQL :" + whereSql);
            log.info("Where SQL param list :" + Arrays.toString(whereParamList.toArray()));
            if (whereParamList.size() > 0) {
                sql.append(" where ").append(whereSql);
            }
            Integer followLevel = param.getFollowLevel();
            boolean bb = false;
            if (followLevel != 1  && followLevel != 2 ){
                sql.append(" order by ").append(ObjectInfoTable.IMPORTANT).append(" desc");
                bb = true;
            }
            if (params != null && params.size() > 1) {
                if (bb){
                    sql.append(sameSortSql(params, false));
                }else {
                    sql.append(" order by ").append(sameSortSql(params, false));
                }
            }
        }
        // 进行分组
        SqlAndArgs sqlAndArgs = new SqlAndArgs();
        sqlAndArgs.setArgs(setValues);
        sqlAndArgs.setSql(sql.toString());
        return sqlAndArgs;
    }

    /**
     * 返回排序sql 自句
     *
     * @param params        排序参数
     * @param serarchByPics 是否有图片
     * @return 返回排序参数语句
     */
    private StringBuilder sameSortSql(List<StaticSortParam> params, boolean serarchByPics) {
        StringBuilder sameSortSql = new StringBuilder();
        if (params != null) {
            int count = 0;
            if (serarchByPics) {
                if (params.contains(StaticSortParam.RELATEDASC)) {
                    sameSortSql.append("sim asc");
                    if (params.size() > 1) {
                        sameSortSql.append(", ");
                    }
                }
                if (params.contains(StaticSortParam.RELATEDDESC)) {
                    sameSortSql.append("sim desc");
                    if (params.size() > 1) {
                        sameSortSql.append(", ");
                    }
                }

            }
            if (params.contains(StaticSortParam.IMPORTANTASC)) {
                sameSortSql.append(ObjectInfoTable.IMPORTANT);
                sameSortSql.append(" asc");
                count++;
            }
            if (params.contains(StaticSortParam.IMPORTANTDESC)) {
                sameSortSql.append(ObjectInfoTable.IMPORTANT);
                sameSortSql.append(" desc");
                count++;
            }
            if (params.contains(StaticSortParam.TIMEASC)) {
                if (count > 0) {
                    sameSortSql.append(", ");
                    sameSortSql.append(ObjectInfoTable.CREATETIME);
                    sameSortSql.append(" asc");
                } else {
                    sameSortSql.append(ObjectInfoTable.CREATETIME);
                    sameSortSql.append(" asc");
                }
            }
            if (params.contains(StaticSortParam.TIMEDESC)) {
                if (count > 0) {
                    sameSortSql.append(", ");
                    sameSortSql.append(ObjectInfoTable.CREATETIME);
                    sameSortSql.append(" desc");
                } else {
                    sameSortSql.append(ObjectInfoTable.CREATETIME);
                    sameSortSql.append(" desc");
                }
            }
        }
        return sameSortSql;
    }

    /**
     * @return 不同情况下需要返回的相同的字段
     */
    private StringBuffer sameFieldNeedReturn() {
        StringBuffer sameFieldReturn = new StringBuffer("");
        sameFieldReturn.append(ObjectInfoTable.ROWKEY);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.PKEY);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.NAME);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.SEX);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.IDCARD);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.CREATOR);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.CPHONE);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.CREATETIME);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.UPDATETIME);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.REASON);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.IMPORTANT);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.STATUS);
        sameFieldReturn.append(", ");
        sameFieldReturn.append(ObjectInfoTable.LOCATION);
        return sameFieldReturn;
    }


    /**
     * 封装共同的子where 查询
     *
     * @param param       传过来的搜索参数
     * @param whereParamList 需要对sql设置的参数
     * @return 子where查询
     */
    private String sameWhereSql(GetObjectInfoParam param, List<Object> whereParamList) {
        StringBuilder whereQuery = new StringBuilder();
        boolean isChanged = false;

        // 关于姓名的搜索
        String objectName = param.getObjectName();
        if (!StringUtils.isBlank(objectName)) {
            whereQuery.append(ObjectInfoTable.NAME)
                    .append(" like '%").append(objectName).append("%'");
            whereParamList.add(objectName);
            isChanged = true;
        }

        // 关于身份证号的查询
        String idcard = param.getIdcard();
        if (!StringUtils.isBlank(idcard)) {
            if (isChanged) {
                whereQuery.append(" and ");
            }
            whereQuery.append(ObjectInfoTable.IDCARD)
                    .append(" like '%").append(idcard).append("%'");
            whereParamList.add(idcard);
            isChanged = true;
        }

        // 关于性别的查询
        Integer sex = param.getSex();
        if (sex != null){
            if (sex == 0 || sex == 1 || sex == 2) {
                if (isChanged) {
                    whereQuery.append(" and ");
                }
                whereQuery.append(ObjectInfoTable.SEX).append(" = ").append(sex);
                whereParamList.add(sex);
                isChanged = true;
            }
        }

        // 关于人员类型列表的查询
        List<String> pkeys = param.getObjectTypeKeyList();
        if (pkeys != null && pkeys.size() > 0) {
            if (pkeys.size() == 1) {
                if (isChanged) {
                    whereQuery.append(" and ").append(ObjectInfoTable.PKEY)
                            .append(" = '").append(pkeys.get(0)).append("'");
                    whereParamList.add(pkeys.get(0));
                } else {
                    whereQuery.append(ObjectInfoTable.PKEY)
                            .append(" = '").append(pkeys.get(0)).append("'");
                    whereParamList.add(pkeys.get(0));
                    isChanged = true;
                }
            } else {
                int count = 0;
                if (isChanged) {
                    whereQuery.append(" and ");
                }
                whereQuery.append(ObjectInfoTable.PKEY).append(" in (");
                for (int i = 0; i < pkeys.size(); i++) {
                    if (count < pkeys.size() - 1) {
                        whereQuery.append("'").append(pkeys.get(i)).append("', ");
                        count++;
                    } else {
                        whereQuery.append("'").append(pkeys.get(i)).append("')");

                    }
                    whereParamList.add(pkeys.get(i));
                }
                isChanged = true;
            }
        }

        // 关于创建人姓名的查询
        String creator = param.getCreator();
        if (!StringUtils.isBlank(creator)) {
            if (isChanged) {
                whereQuery.append(" and ");
            }
            whereQuery.append(ObjectInfoTable.CREATOR).append(" like '%").append(creator).append("%'");
            whereParamList.add(creator);
            isChanged = true;
        }

        // 关于布控人手机号的查询
        String creatorConractWay = param.getCreatorConractWay();
        if (!StringUtils.isBlank(creatorConractWay)) {
            if (isChanged) {
                whereQuery.append(" and ");
            }
            whereQuery.append(ObjectInfoTable.CPHONE).append(" like '%").append(creatorConractWay).append("%'");
            whereParamList.add(creatorConractWay);
            isChanged = true;
        }

        //查询人员状态值
        Integer status = param.getStatus();
        if (status != null){
            if (status == 0 || status == 1) {
                if (isChanged) {
                    whereQuery.append(" and ");
                }
                whereQuery.append(ObjectInfoTable.STATUS).append(" = ").append(status);
                whereParamList.add(status);
                isChanged = true;
            }
        }

        // 关于是否是重点人员的查询
        Integer followLevel = param.getFollowLevel();
        if (followLevel != null){
            if (followLevel == 1 || followLevel == 2) {
                if (isChanged) {
                    whereQuery.append(" and ");
                }
                whereQuery.append(ObjectInfoTable.IMPORTANT).append(" = ").append(followLevel);
                whereParamList.add(followLevel);
            }
        }
        return whereQuery.toString();
    }

    /**
     * 根据传过来的person的封装的数据Map，进行生成一个sql,用来进行插入和更新
     *
     * @param objectInfo 需要更新的数据
     * @return 拼装成的sql 以及需要设置的值
     */
    public ConcurrentHashMap<String, CopyOnWriteArrayList<Object>> getUpdateSqlFromObjectInfo(ObjectInfoParam objectInfo) {
        CopyOnWriteArrayList<Object> setValues = new CopyOnWriteArrayList<>();
        StringBuffer sql = new StringBuffer("");
        sql.append("upsert into ");
        sql.append(ObjectInfoTable.TABLE_NAME);
        sql.append("(");
        sql.append(ObjectInfoTable.ROWKEY);
        setValues.add(objectInfo.getId());

        String name = objectInfo.getName();
        if (name != null) {
            sql.append(", ");
            sql.append(ObjectInfoTable.NAME);
            setValues.add(name);
        }
        String pkey = objectInfo.getObjectTypeKey();
        if (pkey != null) {
            sql.append(", ");
            sql.append(ObjectInfoTable.PKEY);
            setValues.add(pkey);
        }
        String idcard = objectInfo.getIdcard();
        if (idcard != null) {
            sql.append(", ");
            sql.append(ObjectInfoTable.IDCARD);
            setValues.add(idcard);
        }

        Integer sex = objectInfo.getSex();
        if (sex != null) {
            sql.append(", ");
            sql.append(ObjectInfoTable.SEX);
            setValues.add(sex);
        }

        String reason = objectInfo.getReason();
        if (reason != null) {
            sql.append(", ");
            sql.append(ObjectInfoTable.REASON);
            setValues.add(reason);
        }
        String creator = objectInfo.getCreator();
        if (creator != null) {
            sql.append(", ")

                    .append(ObjectInfoTable.CREATOR);
            setValues.add(creator);
        }
        String cphone = objectInfo.getCreatorConractWay();
        if (cphone != null) {
            sql.append(", ");
            sql.append(ObjectInfoTable.CPHONE);
            setValues.add(cphone);
        }

        Integer important = objectInfo.getFollowLevel();
        if (important != null) {
            sql.append(", ");
            sql.append(ObjectInfoTable.IMPORTANT);
            setValues.add(important);
        }

        sql.append(") values(?");
        StringBuilder tmp = new StringBuilder("");
        for (int i = 0; i <= setValues.size() - 2; i++) {
            tmp.append(", ?");
        }
        sql.append(tmp);
        sql.append(")");
        ConcurrentHashMap<String, CopyOnWriteArrayList<Object>> sqlAndSetValues = new ConcurrentHashMap<>();
        sqlAndSetValues.put(new String(sql), setValues);
        return sqlAndSetValues;
    }

    public String getObjectInfo_status() {
        return "select " + ObjectInfoTable.STATUS
                + " from " + ObjectInfoTable.TABLE_NAME
                + " where " + ObjectInfoTable.ROWKEY + " = ?";
    }

    public String getPhotoByObjectId() {
        return "select " + ObjectInfoTable.PHOTO
                + " from " + ObjectInfoTable.TABLE_NAME
                + " where " + ObjectInfoTable.ROWKEY + " = ?";
    }

    public String getTypeNameMapping(List<String> objectTypeKeys) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(ObjectTypeTable.TYPE_NAME)
                .append(", ")
                .append(ObjectTypeTable.ROWKEY)
                .append(" from ")
                .append(ObjectTypeTable.TABLE_NAME)
                .append(" where ")
                .append(ObjectTypeTable.ROWKEY)
                .append(" in (");
        int count = 0;
        for (String ignored : objectTypeKeys) {
            sql.append("'")
                    .append(ignored)
                    .append("'");
            count++;
            if (count == objectTypeKeys.size()) {
                sql.append(")");
            } else {
                sql.append(", ");
            }
        }
        return sql.toString();
    }

    public String getObjectIdCard() {
        return "select " + ObjectInfoTable.IDCARD
                + " from " + ObjectInfoTable.TABLE_NAME
                + " where " + ObjectInfoTable.ROWKEY + " = ?";
    }

    public String getAllObjectIdcard() {
        return "select " + ObjectInfoTable.IDCARD + " from " + ObjectInfoTable.TABLE_NAME;
    }

    public String getAllObjectTypeKeys() {
        return "select " + ObjectTypeTable.ROWKEY + " from " + ObjectTypeTable.TABLE_NAME;
    }

    public String addObjectInfo() {
        return "upsert into " + ObjectInfoTable.TABLE_NAME + "("
                + ObjectInfoTable.ROWKEY + ", "
                + ObjectInfoTable.NAME + ", "
                + ObjectInfoTable.PKEY + ", "
                + ObjectInfoTable.IDCARD + ", "
                + ObjectInfoTable.SEX + ", "
                + ObjectInfoTable.PHOTO + ", "
                + ObjectInfoTable.FEATURE + ", "
                + ObjectInfoTable.REASON + ", "
                + ObjectInfoTable.CREATOR + ", "
                + ObjectInfoTable.CPHONE + ", "
                + ObjectInfoTable.CREATETIME + ", "
                + ObjectInfoTable.UPDATETIME + ", "
                + ObjectInfoTable.IMPORTANT + ", "
                + ObjectInfoTable.STATUS
                + ") values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    public String deleteObjectInfo() {
        return "delete from " + ObjectInfoTable.TABLE_NAME + " where " + ObjectInfoTable.ROWKEY + " = ?";
    }

    public String getObjectTypeById(String id) {
        return "select " + ObjectTypeTable.ROWKEY + ", "
                + ObjectTypeTable.TYPE_CREATOR + ", "
                + ObjectTypeTable.TYPE_REMARK + " from "
                + ObjectTypeTable.TABLE_NAME + " where "
                + ObjectTypeTable.ROWKEY + " = '" + id + "'";
    }

    public String getObjectInfo() {
        return "select " + ObjectInfoTable.NAME + ", "
                + ObjectInfoTable.PKEY + ", "
                + ObjectInfoTable.IDCARD + ", "
                + ObjectInfoTable.SEX + ", "
                + ObjectInfoTable.REASON + ", "
                + ObjectInfoTable.CREATOR + ", "
                + ObjectInfoTable.CPHONE + ", "
                + ObjectInfoTable.CREATETIME + ", "
                + ObjectInfoTable.UPDATETIME + ", "
                + ObjectInfoTable.IMPORTANT + ", "
                + ObjectInfoTable.STATUS + " from "
                + ObjectInfoTable.TABLE_NAME + " where "
                + ObjectInfoTable.ROWKEY + " = ?";
    }

    public String getObjectTypeNameById() {
        return "select " + ObjectTypeTable.TYPE_NAME + " from "
                + ObjectTypeTable.TABLE_NAME + " where "
                + ObjectTypeTable.ROWKEY + " = ?";
    }

    public String countStatus() {
        return "select count(*) as num from "
                + ObjectInfoTable.TABLE_NAME + " where "
                + ObjectInfoTable.STATUS + " = 0";
    }

    public String objectTypeCount() {
        return "select count(" + ObjectTypeTable.TYPE_NAME
                + ") as num from " + ObjectTypeTable.TABLE_NAME;
    }

    public String updateObjectInfo_status() {
        return "upsert into " + ObjectInfoTable.TABLE_NAME + "("
                + ObjectInfoTable.ROWKEY + ","
                + ObjectInfoTable.STATUS + ","
                + ObjectInfoTable.STATUSTIME
                + ") values (?,?,?)";
    }

    public String emigrationCount() {
        return "select count(*) as num from "
                + ObjectInfoTable.TABLE_NAME + " where "
                + ObjectInfoTable.STATUS + " = 1 and "
                + ObjectInfoTable.STATUSTIME + " > ? AND "
                + ObjectInfoTable.STATUSTIME + " < ?";
    }
}
