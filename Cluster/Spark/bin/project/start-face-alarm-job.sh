#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-face-alarm-job.sh
## Description: to start faceAlarmJob(启动组合告警任务)
## Author:      liushanbin
## Created:     2018-01-27
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#
cd `dirname $0`
## bin目录
BIN_DIR=`pwd`
cd ..
CLUSTER_DIR=`pwd`
cd ..
DEPLOY_DIR=`pwd`
######## cluster目录 ########
CLUSTER_CONF_DIR=${CLUSTER_DIR}/conf
CLUSTER_LIB_DIR=${CLUSTER_DIR}/lib
CLUSTER_LOG_DIR=${CLUSTER_DIR}/logs
LOG_FILE=${CLUSTER_LOG_DIR}/sparkFaceAlarmJob.log
######## common目录 ########
COMMON_CONF_DIR=${DEPLOY_DIR}/common/conf
COMMON_LIB_DIR=${DEPLOY_DIR}/common/lib
######## ftp目录 #########
FTP_CONF_DIR=${DEPLOY_DIR}/ftp/conf
FTP_LIB_DIR=${DEPLOY_DIR}/ftp/lib
######## service目录 ########
SERVICE_CONF_DIR=${DEPLOY_DIR}/service/conf
SERVICE_LIB_DIR=${DEPLOY_DIR}/service/lib
## bigdata_env
BIGDATA_ENV=/opt/hzgc/env_bigdata.sh
## spark class
SPARK_CLASS_PARAM=com.hzgc.cluster.alarm.FaceAlarmJob
## bigdata cluster path
BIGDATA_CLUSTER_PATH=/opt/hzgc/bigdata

#---------------------------------------------------------------------#
#                              jar版本控制                            #
#---------------------------------------------------------------------#
## module version(模块)
BIGDATA_API_VERSION=`ls ${COMMON_LIB_DIR}| grep ^bigdata-api-[0-9].[0-9].[0-9].jar$`
CLUSTER_VERSION=`ls ${COMMON_LIB_DIR}| grep ^cluster-[0-9].[0-9].[0-9].jar$`
FTP_VERSION=`ls ${COMMON_LIB_DIR}| grep ^ftp-[0-9].[0-9].[0-9].jar$`
JNI_VERSION=`ls ${COMMON_LIB_DIR}| grep ^jni-[0-9].[0-9].[0-9].jar$`
SERVICE_VERSION=`ls ${COMMON_LIB_DIR}| grep ^service-[0-9].[0-9].[0-9].jar$`
UTIL_VERSION=`ls ${COMMON_LIB_DIR}| grep ^util-[0-9].[0-9].[0-9].jar$`
## quote version(引用)
GSON_VERSION=`ls ${FTP_LIB_DIR}| grep ^gson-[0-9].[0-9].[0-9].jar`
JACKSON_CORE_VERSION=`ls ${FTP_LIB_DIR}| grep ^jackson-core-[0-9].[0-9].[0-9].jar`
SPARK_STREAMING_KAFKA_VERSION=`ls ${CLUSTER_LIB_DIR}| grep ^spark-streaming-kafka-[0-9]-[0-9]_[0-9].[0-9][0-9]-[0-9].[0-9].[0-9].jar`
HBASE_SERVER_VERSION=`ls ${CLUSTER_LIB_DIR}| grep ^hbase-server-[0-9].[0-9].[0-9].jar`
HBASE_CLIENT_VERSION=`ls ${CLUSTER_LIB_DIR}| grep ^hbase-client-[0-9].[0-9].[0-9].jar`
HBASE_COMMON_VERSION=`ls ${CLUSTER_LIB_DIR}| grep ^hbase-common-[0-9].[0-9].[0-9].jar`
HBASE_PROTOCOL_VERSION=`ls ${CLUSTER_LIB_DIR}| grep ^hbase-protocol-[0-9].[0-9].[0-9].jar`
KAFKA_VERSION=`ls ${CLUSTER_LIB_DIR}| grep ^kafka_[0-9].[0-9][0-9]-[0-9].[0-9].[0-9].[0-9].jar`
ELASTICSEARCH_VERSION=`ls ${CLUSTER_LIB_DIR}| grep ^elasticsearch-[0-9].[0-9].jar`
ROCKETMQ_CLIENT_VERSION=`ls ${FTP_LIB_DIR}| grep ^rocketmq-client-[0-9].[0-9].[0-9].jar$`
ROCKETMQ_COMMON_VERSION=`ls ${FTP_LIB_DIR}| grep ^rocketmq-common-[0-9].[0-9].[0-9].jar$`
ROCKETMQ_REMOTING_VERSION=`ls ${FTP_LIB_DIR}| grep ^rocketmq-remoting-[0-9].[0-9].[0-9].jar$`
FASTJSON_VERSION=`ls ${SERVICE_LIB_DIR}| grep ^fastjson-[0-9].[0-9].[0-9][0-9].jar`
KAFKA_CLIENTS_VERSION=`ls ${CLUSTER_LIB_DIR}| grep ^kafka-clients-[0-9].[0-9].[0-9].jar`
METRICS_CORE_VERSION=metrics-core-3.1.2.jar


############ 创建log目录 ###############

if [ ! -d ${CLUSTER_LOG_DIR} ];then
   mkdir ${CLUSTER_LOG_DIR}
fi

############ 判断是否存在大数据集群 ###################
if [ ! -d ${BIGDATA_CLUSTER_PATH} ];then
   echo "${BIGDATA_CLUSTER_PATH} does not exit,please go to the node of the existing bigdata cluster !"
   exit 0
fi

############### 判断是否存在配置文件 ##################
if [ ! -e ${SERVICE_CONF_DIR}/es-config.properties ];then
    echo "${SERVICE_CONF_DIR}/es-config.properties does not exit!"
    exit 0
fi
if [ ! -e ${FTP_CONF_DIR}/rocketmq.properties ];then
    echo "${FTP_CONF_DIR}/rocketmq.properties does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_CONF_DIR}/sparkJob.properties ];then
    echo "${CLUSTER_CONF_DIR}/sparkJob.properties does not exit!"
    exit 0
fi
if [ ! -e ${SERVICE_CONF_DIR}/hbase-site.xml ];then
    echo "${SERVICE_CONF_DIR}/hbase-site.xml does not exit!"
    exit 0
fi
if [ ! -e ${FTP_CONF_DIR}/ftpAddress.properties ];then
    echo "${FTP_CONF_DIR}/ftpAddress.properties does not exit!"
    exit 0
fi

################# 判断是否存在jar ###################
if [ ! -e ${CLUSTER_LIB_DIR}/${HBASE_CLIENT_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${HBASE_CLIENT_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${HBASE_COMMON_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${HBASE_COMMON_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${GSON_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${GSON_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${JACKSON_CORE_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${JACKSON_CORE_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${SPARK_STREAMING_KAFKA_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${SPARK_STREAMING_KAFKA_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${SERVICE_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${SERVICE_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${HBASE_SERVER_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${HBASE_SERVER_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${HBASE_PROTOCOL_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${HBASE_PROTOCOL_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${JNI_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${JNI_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${KAFKA_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${KAFKA_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${SERVICE_LIB_DIR}/${ELASTICSEARCH_VERSION} ];then
    echo "${SERVICE_LIB_DIR}/${ELASTICSEARCH_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${FTP_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${FTP_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${BIGDATA_API_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${BIGDATA_API_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${FTP_LIB_DIR}/${ROCKETMQ_CLIENT_VERSION} ];then
    echo "${FTP_LIB_DIR}/${ROCKETMQ_CLIENT_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${FTP_LIB_DIR}/${ROCKETMQ_COMMON_VERSION} ];then
    echo "${FTP_LIB_DIR}/${ROCKETMQ_COMMON_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${FTP_LIB_DIR}/${ROCKETMQ_REMOTING_VERSION} ];then
    echo "${FTP_LIB_DIR}/${ROCKETMQ_REMOTING_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${FTP_LIB_DIR}/${FASTJSON_VERSION} ];then
    echo "${FTP_LIB_DIR}/${FASTJSON_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${UTIL_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${UTIL_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${KAFKA_CLIENTS_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${KAFKA_CLIENTS_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${COMMON_LIB_DIR}/${CLUSTER_VERSION} ];then
    echo "${COMMON_LIB_DIR}/${CLUSTER_VERSION} does not exit!"
    exit 0
fi
if [ ! -e ${CLUSTER_LIB_DIR}/${METRICS_CORE_VERSION} ];then
    echo "${CLUSTER_LIB_DIR}/${METRICS_CORE_VERSION} does not exit!"
    exit 0
fi

################## 组合告警任务 ###################
source /etc/profile
source ${BIGDATA_ENV}
nohup spark-submit \
--master yarn \
--deploy-mode cluster \
--executor-memory 4g \
--executor-cores 2 \
--num-executors 4 \
--class ${SPARK_CLASS_PARAM} \
--jars ${CLUSTER_LIB_DIR}/${GSON_VERSION},\
${CLUSTER_LIB_DIR}/${JACKSON_CORE_VERSION},\
${CLUSTER_LIB_DIR}/${SPARK_STREAMING_KAFKA_VERSION},\
${COMMON_LIB_DIR}/${SERVICE_VERSION},\
${CLUSTER_LIB_DIR}/${HBASE_SERVER_VERSION},\
${CLUSTER_LIB_DIR}/${HBASE_CLIENT_VERSION},\
${CLUSTER_LIB_DIR}/${HBASE_COMMON_VERSION},\
${CLUSTER_LIB_DIR}/${HBASE_PROTOCOL_VERSION},\
${COMMON_LIB_DIR}/${JNI_VERSION},\
${CLUSTER_LIB_DIR}/${KAFKA_VERSION},\
${SERVICE_LIB_DIR}/${ELASTICSEARCH_VERSION},\
${COMMON_LIB_DIR}/${FTP_VERSION},\
${COMMON_LIB_DIR}/${BIGDATA_API_VERSION},\
${FTP_LIB_DIR}/${ROCKETMQ_CLIENT_VERSION},\
${FTP_LIB_DIR}/${ROCKETMQ_COMMON_VERSION},\
${FTP_LIB_DIR}/${ROCKETMQ_REMOTING_VERSION},\
${FTP_LIB_DIR}/${FASTJSON_VERSION},\
${COMMON_LIB_DIR}/${UTIL_VERSION},\
${CLUSTER_LIB_DIR}/${KAFKA_CLIENTS_VERSION},\
${CLUSTER_LIB_DIR}/${METRICS_CORE_VERSION} \
--files ${SERVICE_CONF_DIR}/es-config.properties,\
${SERVICE_CONF_DIR}/hbase-site.xml,\
${FTP_CONF_DIR}/ftpAddress.properties,\
${CLUSTER_CONF_DIR}/sparkJob.properties,\
${FTP_CONF_DIR}/rocketmq.properties \
${COMMON_LIB_DIR}/${CLUSTER_VERSION} > ${LOG_FILE} 2>&1 &