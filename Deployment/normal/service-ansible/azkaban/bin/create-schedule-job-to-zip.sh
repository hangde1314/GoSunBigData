#!/bin/bash
########################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    create-schedule-job-to-zip.sh
## Description: 将定时任务生成job文件并打包成zip包
## Author:      chenke
## Created:     2018-03-27
#########################################################################
#set -x ##用于调试使用，不用的时候可以注释掉

#----------------------------------------------------------------------#
#                              定义变量                                #
#----------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`   ###bin目录
cd ..
AZKABAN_DIR=`pwd`  ###azkaban目录
LOG_DIR=${AZKABAN_DIR}/logs  ###集群log日志目录
LOG_FILE=${LOG_DIR}/create-schedule-job-to-zip.log  ##log日志文件

DEVICE_RECOGIZE_TABLE="device_recogize_table.sh"
IMSI_BLACKLIST_TABLE="imsi_blacklist_table.sh"
NEWPEOPLE_TABLE="newpeople_table.sh"
FUSION_IMSI_TABLE="fusion_imsi_table.sh"
HOUR_COUNT_TABLE="24hour_count_table.sh"
OUTPEOPLE_TABLE="outpeople_table.sh"

mkdir -p ${AZKABAN_DIR}/job
AZKABAN_JOB_DIR=${AZKABAN_DIR}/job

mkdir -p ${AZKABAN_DIR}/zip
AZKABAN_ZIP_DIR=${AZKABAN_DIR}/zip

cd ..
ANSIBLE_DIR=`pwd`                                 ## 根目录
cd roles/vars
VARS_DIR=`pwd`
MAIN_FILE=${VARS_DIR}/main.yml

MYSQL_IP=`grep 'tidb_hostname:' ${MAIN_FILE} | cut -d ' ' -f2`
MYSQLPORT=`grep 'tidb_port:' ${MAIN_FILE} | cut -d ' ' -f2`

cd ${AZKABAN_DIR}/bin

sed -i "s#^IP=.*#IP=${MYSQL_IP}#g" `grep -r "IP=" ./*.sh | awk -F ":" '{print $1}'`
sed -i "s#^PORT=.*#PORT=${MYSQLPORT}#g" `grep -r "PORT=" ./*.sh | awk -F ":" '{print $1}'`

if [[ ! -f "${DEVICE_RECOGIZE_TABLE}"  ]]; then
    echo "the device_recogize_table_one_day.sh is not exist!!!"
    else
    touch ${AZKABAN_JOB_DIR}/device_recogize_table_one_day.job
    echo "type=command" > ${AZKABAN_JOB_DIR}/device_recogize_table_one_day.job
 echo "command=sh ${BIN_DIR}/${DEVICE_RECOGIZE_TABLE}" >> ${AZKABAN_JOB_DIR}/device_recogize_table_one_day.job
fi

if [[ ! -f "${NEWPEOPLE_TABLE}"  ]]; then
    echo "the newpeople_table_one_month.sh is not exist!!!"
    else
    touch ${AZKABAN_JOB_DIR}/newpeople_table_one_month.job
    echo "type=command" > ${AZKABAN_JOB_DIR}/newpeople_table_one_month.job
    echo "command=sh ${BIN_DIR}/${NEWPEOPLE_TABLE}" >> ${AZKABAN_JOB_DIR}/newpeople_table_one_month.job
fi

if [[ ! -f "${OUTPEOPLE_TABLE}"  ]]; then
    echo "the outpeople_table_one_month.sh is not exist!!!"
    else
    touch ${AZKABAN_JOB_DIR}/outpeople_table_one_month.job
    echo "type=command" > ${AZKABAN_JOB_DIR}/outpeople_table_one_day.job
    echo "command=sh ${BIN_DIR}/${OUTPEOPLE_TABLE}" >> ${AZKABAN_JOB_DIR}/outpeople_table_one_day.job
fi

if [[ ! -f "${IMSI_BLACKLIST_TABLE}"  ]]; then
    echo "the imsi_blacklist_table_one_day.sh is not exist!!!"
    else
    touch ${AZKABAN_JOB_DIR}/imsi_blacklist_table_one_day.job
    echo "type=command" >> ${AZKABAN_JOB_DIR}/imsi_blacklist_table_one_day.job
    echo "command=sh ${BIN_DIR}/${IMSI_BLACKLIST_TABLE}" >> ${AZKABAN_JOB_DIR}/imsi_blacklist_table_one_day.job
fi

if [[ ! -f "${FUSION_IMSI_TABLE}"  ]]; then
    echo "the fusion_imsi_table_one_day.sh is not exist!!!"
    else
    touch ${AZKABAN_JOB_DIR}/fusion_imsi_table_one_day.job
    echo "type=command" > ${AZKABAN_JOB_DIR}/fusion_imsi_table_one_day.job
    echo "command=sh ${BIN_DIR}/${FUSION_IMSI_TABLE}" >> ${AZKABAN_JOB_DIR}/fusion_imsi_table_one_day.job
    echo "dependencies=imsi_blacklist_table_one_day" >> ${AZKABAN_JOB_DIR}/fusion_imsi_table_one_day.job
fi

if [[ ! -f "${HOUR_COUNT_TABLE}"  ]]; then
    echo "the 24hour_count_table_one_hour.sh is not exist!!!"
    else
    touch ${AZKABAN_JOB_DIR}/24hour_count_table_one_hour.job
    echo "type=command" > ${AZKABAN_JOB_DIR}/24hour_count_table_one_hour.job
    echo "command=sh ${BIN_DIR}/${HOUR_COUNT_TABLE}" >> ${AZKABAN_JOB_DIR}/24hour_count_table_one_hour.job
fi

cd ${AZKABAN_ZIP_DIR}
zip device_recogize_table_one_day.zip ${AZKABAN_JOB_DIR}/device_recogize_table_one_day.job
zip fusion_imsi_table_one_day.zip ${AZKABAN_JOB_DIR}/fusion_imsi_table_one_day.job ${AZKABAN_JOB_DIR}/imsi_blacklist_table_one_day.job
zip 24hour_count_table_one_hour.zip ${AZKABAN_JOB_DIR}/24hour_count_table_one_hour.job
zip newpeople_table_one_month.zip ${AZKABAN_JOB_DIR}/newpeople_table_one_month.job
zip outpeople_table_one_day.zip ${AZKABAN_JOB_DIR}/outpeople_table_one_day.job
