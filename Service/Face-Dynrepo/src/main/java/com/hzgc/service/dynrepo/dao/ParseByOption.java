package com.hzgc.service.dynrepo.dao;

import com.hzgc.common.service.faceattribute.bean.Attribute;
import com.hzgc.common.service.faceattribute.bean.Logistic;
import com.hzgc.common.service.facedynrepo.FaceTable;
import com.hzgc.common.util.basic.IsEmpty;
import com.hzgc.jniface.FaceUtil;
import com.hzgc.service.dynrepo.bean.SearchOption;
import com.hzgc.service.dynrepo.bean.SortParam;
import com.hzgc.service.dynrepo.util.DeviceToIpcs;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

class ParseByOption {

    private static Logger LOG = Logger.getLogger(ParseByOption.class);

    private static String MID_FIELD;

    static {
        MID_FIELD = FaceTable.IPCID +
                ", " +
                FaceTable.TIMESTAMP +
                ", " +
                FaceTable.SABSOLUTEPATH +
                ", " +
                FaceTable.BABSOLUTEPATH +
                ", " +
                FaceTable.HOSTNAME +
                ", " +
                FaceTable.GENDER +
                ", " +
                FaceTable.AGE +
                ", " +
                FaceTable.HUZI +
                ", " +
                FaceTable.EYEGLASSES +
                ", " +
                FaceTable.MASK +
                ", " +
                FaceTable.SHARPNESS;
    }

    static String getFinalSQLwithOption(SearchOption option, boolean printSql) throws SQLException {
        if (option.getImages().size() == 1) {
            String feature = FaceUtil.
                    floatArray2string(option.getImages().get(0).getFeature().getFeature());
            return getNotOnePersonSQL(option, feature, printSql);
        } else if (!option.isSinglePerson()) {
            return getNotOnePersonSQL(option, null, printSql);
        } else if (option.isSinglePerson()) {
            return getOnePersonSQL(option, printSql);
        } else {
            throw new SQLException("Arguments error, method:getFinalSQLwithOption()");
        }
    }

    /**
     * 多图搜索时,如果搜索条件为交集时（多张图当做一个人进行搜索）调用此方法
     *
     * @param option   搜索参数
     * @param printSql 当为true时,所有的特征值将被“”代替,因为特征值太长了,当为false是,将正常拼接
     * @return 最终的SQL语句
     */
    private static String getOnePersonSQL(SearchOption option, boolean printSql) {
        StringBuilder finalSql = new StringBuilder();
        finalSql.append("select * from (select ")
                .append(MID_FIELD)
                .append(", ")
                .append(getAttributes(option))
                .append("greatest(");
        String[] simFieldConatiner = new String[option.getImages().size()];
        for (int i = 0; i < option.getImages().size(); i++) {
            String simField = FaceTable.SIMILARITY + i;
            simFieldConatiner[i] = simField;
            if (option.getImages().size() - i > 1) {
                finalSql.append(simField).append(", ");
            } else {
                finalSql.append(simField).append(") as ")
                        .append(FaceTable.SIMILARITY)
                        .append(" from (");
            }
        }
        StringBuilder prefix = getOnePersonPrefix(option, simFieldConatiner, printSql);
        finalSql.append(prefix)
                .append(FaceTable.PERSON_TABLE)
                .append(" union all ")
                .append(prefix)
                .append(FaceTable.MID_TABLE)
                .append(")) as temp_table ")
                .append(getFilterOption(option));
        return finalSql.toString();
    }

    /**
     * 多图搜索时,如果搜索条件为并集时（每张图都是独立的人进行搜索）调用此方法
     *
     * @param option  搜索参数
     * @param feature 当此字段不为null的时候只存在于单张图进行以图搜图,不需要复杂的拼接;当此字段为null时需要进行复杂拼接
     * @return 最终的SQL语句
     */
    private static String getNotOnePersonSQL(SearchOption option, String feature, boolean printSql) {
        StringBuilder finalSql = new StringBuilder();
        if (feature != null) {
            if (printSql) {
                feature = "";
            }
            String prefix = getNotOnePersonPrefix(feature, option);
            finalSql.append("select ")
                    .append(MID_FIELD)
                    .append(", ")
                    .append(getAttributes(option))
                    .append(FaceTable.SIMILARITY)
                    .append(" from (")
                    .append(prefix)
                    .append(" from ")
                    .append(FaceTable.PERSON_TABLE)
                    .append(" union all ")
                    .append(prefix)
                    .append(" from ")
                    .append(FaceTable.MID_TABLE)
                    .append(") temp_table ")
                    .append(getFilterOption(option));
            return finalSql.toString();
        } else {
            finalSql.append("select * from (");
            for (int i = 0; i < option.getImages().size(); i++) {
                if (printSql) {
                    feature = "";
                } else {
                    feature = FaceUtil.floatArray2string(option.getImages().get(i).getFeature().getFeature());
                }
                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append("(select ")
                        .append("'")
                        .append(option.getImages().get(i).getImageID())
                        .append("' as ")
                        .append(FaceTable.GROUP_FIELD)
                        .append(", ")
                        .append(MID_FIELD)
                        .append(", ")
                        .append(getAttributes(option))
                        .append(FaceTable.SIMILARITY)
                        .append(" from (")
                        .append(getNotOnePersonPrefix(feature, option))
                        .append(" from ")
                        .append(FaceTable.PERSON_TABLE)
                        .append(" union all ")
                        .append(getNotOnePersonPrefix(feature, option))
                        .append(" from ")
                        .append(FaceTable.MID_TABLE)
                        .append(") temp_table ")
                        .append(getFilterOption(option))
                        .append(")");
                if (option.getImages().size() - i > 1) {
                    strBuilder.append(" union all ");
                }
                finalSql.append(strBuilder);
            }
            finalSql.append(") temp_table ");
            return finalSql.toString();
        }

    }

    /**
     * 生成排序语句
     *
     * @param finalSql 正在被拼装的SQL
     * @param option   搜索参数
     */
    private static void getSortParams(StringBuilder finalSql, SearchOption option) {
        finalSql.append(" order by ");
        List <SortParam> sortParamList = option.getSort()
                .stream().map(param -> SortParam.values()[param]).collect(Collectors.toList());
        for (int i = 0; i < option.getSort().size(); i++) {
            switch (sortParamList.get(i)) {
                case TIMEDESC:
                    finalSql.append(FaceTable.TIMESTAMP).append(" desc");
                    if (sortParamList.size() - 1 > i) {
                        finalSql.append(", ");
                    }
                    break;
                case SIMDESC:
                    finalSql.append(FaceTable.SIMILARITY).append(" desc");
                    if (sortParamList.size() - 1 > i) {
                        finalSql.append(", ");
                    }
                    break;
                case SIMDASC:
                    finalSql.append(FaceTable.SIMILARITY);
                    if (sortParamList.size() - 1 > i) {
                        finalSql.append(", ");
                    }
                    break;
                case TIMEASC:
                    finalSql.append(FaceTable.TIMESTAMP);
                    if (sortParamList.size() - 1 > i) {
                        finalSql.append(", ");
                    }
                    break;
            }
        }
    }

    /**
     * 生成符合条件的设备列表
     *
     * @param finalSql 最终的SQL语句
     * @param option   搜索参数
     */
    private static void getDeviceIpcId(StringBuilder finalSql, SearchOption option) {
        finalSql.append(" and ")
                .append(FaceTable.IPCID)
                .append(" in ")
                .append("(");
        for (int i = 0; option.getDeviceIpcs().size() > i; i++) {
            String ipcid = DeviceToIpcs.getIpcs(option.getDeviceIpcs()).get(i);
            if (option.getDeviceIpcs().size() - 1 > i) {
                finalSql.append("'")
                        .append(ipcid)
                        .append("'")
                        .append(",");
            } else {
                finalSql.append("'")
                        .append(ipcid)
                        .append("'")
                        .append(")");
            }
        }
    }

    /**
     * 拼装查询时间范围
     *
     * @param finalSql 最终的SQL语句
     * @param option   搜索参数
     */
    private static void getData(StringBuilder finalSql, SearchOption option) {
        //判断开始时间和结束时间 数据格式 年-月-日 时:分:秒
        finalSql
                .append(" and ")
                .append(FaceTable.TIMESTAMP)
                .append(">=")
                .append("'")
                .append(option.getStartTime())
                .append("'")
                .append(" and ")
                .append(FaceTable.TIMESTAMP)
                .append("<=")
                .append("'")
                .append(option.getEndTime())
                .append("'");
    }

    /**
     * 拼装属性查询字段
     *
     * @param option 查询参数
     */
    private static String getAttributes(SearchOption option) {
        StringBuilder SQL = new StringBuilder();
        if (IsEmpty.listIsRight(option.getAttributes())) {
            for (Attribute attribute : option.getAttributes()) {
                if (attribute.getValues() != null && attribute.getValues().size() > 0) {
                    if (attribute.getLogistic() == Logistic.AND) {
                        SQL.append(attribute.getIdentify().toLowerCase()).append(", ");
                    }
                }
            }
        }
        return SQL.toString();
    }

    /**
     * 生成人脸属性检索条件
     *
     * @param option 搜索参数
     * @return 人脸属性检索条件
     */
    private static String getAttributesAndValues(SearchOption option) {
        StringBuilder SQL = new StringBuilder();
        if (IsEmpty.listIsRight(option.getAttributes())) {
            for (Attribute attribute : option.getAttributes()) {
                if (IsEmpty.listIsRight(attribute.getValues()) && attribute.getLogistic() == Logistic.AND) {
                    StringBuilder tempStr = new StringBuilder();
                    for (int i = 0; i < attribute.getValues().size(); i++) {
                        if (attribute.getValues().get(i).getValue() != 0) {
                            if (tempStr.length() == 0) {
                                tempStr.append(" and ")
                                        .append(attribute.getIdentify().toLowerCase())
                                        .append(" in ")
                                        .append("(");
                            }
                            if (attribute.getValues().size() - 1 > i) {
                                tempStr.append(attribute.getValues().get(i).getValue())
                                        .append(",");
                            } else {
                                tempStr.append(attribute.getValues().get(i).getValue());
                            }
                        }
                    }
                    if (tempStr.length() != 0) {
                        tempStr.append(")");
                        SQL.append(tempStr);
                    }
                } else {
                    if (attribute.getLogistic() == Logistic.OR) {
                        LOG.error("Logistic is or, so ignore this condition");
                    }
                }
            }
        }
        return SQL.toString();
    }

    /**
     * 生成当多张图查询条件是并集的情况下（每张图都是独立的一个人）查询前缀,例如select x from
     *
     * @param searchFeaStr 待拼装的特征值
     * @param option       搜索参数
     * @return 返回前缀语句
     */
    private static String getNotOnePersonPrefix(String searchFeaStr, SearchOption option) {
        //date分区字段
        return "select " +
                MID_FIELD +
                ", " +
                getAttributes(option) +
                FaceTable.FUNCTION_NAME +
                "('" +
                searchFeaStr +
                "', " +
                FaceTable.FEATURE +
                ") as " +
                FaceTable.SIMILARITY;
    }

    /**
     * 生成当多张图查询条件是交集的情况下（多张图当做同一个人）查询前缀,例如select x from
     *
     * @param option         搜索参数
     * @param fieldContainer 数组里每一个字段对应了一张图片相似度的引用
     * @param printSql       当为true时,所有的特征值将被“”代替,因为特征值太长了,当为false是,将正常拼接
     * @return 返回前缀语句
     */
    private static StringBuilder getOnePersonPrefix(SearchOption option, String[] fieldContainer, boolean printSql) {
        StringBuilder prefix = new StringBuilder();
        prefix.append("select ")
                .append(MID_FIELD)
                .append(",")
                .append(getAttributes(option));
        for (int i = 0; i < option.getImages().size(); i++) {
            String feature;
            if (printSql) {
                feature = "";
            } else {
                feature = FaceUtil.
                        floatArray2string(option.getImages().get(i).getFeature().getFeature());
            }
            prefix.append(FaceTable.FUNCTION_NAME)
                    .append("('")
                    .append(feature)
                    .append("', ")
                    .append(FaceTable.FEATURE)
                    .append(") as ")
                    .append(fieldContainer[i]);
            if (option.getImages().size() - i > 1) {
                prefix.append(", ");
            }
        }
        return prefix.append(" from ");
    }

    /**
     * 生成过滤参数语句
     *
     * @param option 搜索参数
     * @return 返回过滤参数语句
     */
    private static String getFilterOption(SearchOption option) {
        StringBuilder finalSql = new StringBuilder();
        finalSql.append("where ")
                .append(FaceTable.SIMILARITY)
                .append(">=")
                .append(option.getSimilarity())
                .append(getAttributesAndValues(option));
        if (option.getStartTime() != null && option.getEndTime() != null) {
            getData(finalSql, option);
        }

        if (option.getDeviceIpcs() != null && option.getDeviceIpcs().size() > 0) {
            getDeviceIpcId(finalSql, option);
        }

        if (option.getSort() != null && option.getSort().size() > 0) {
            getSortParams(finalSql, option);
        }
        finalSql.append(" limit 1000");
        return finalSql.toString();
    }
}