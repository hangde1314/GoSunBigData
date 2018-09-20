#!/bin/bash
################################################################################
## Copyright:    HZGOSUN Tech. Co, BigData
## Filename:     create-dynamic-index.sh
## Description:  创建动态库表的所有索引
## Author:       qiaokaifeng
## Created:      2017-11-28
################################################################################

#set -x
#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#

cd `dirname $0`
BIN_DIR=`pwd`                                   ### bin 目录
cd ..
SCRIPT_DIR=`pwd`
CONF_FILE=${SCRIPT_DIR}/../conf/project-conf.properties

#####################################################################
# 函数名: index_es_dynamic
# 描述: index_es的子函数，替换dynamic-index.sh中的节点名
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function index_es_dynamic()
{
    # 判断脚本是否存在，存在才执行
    if [ -f "${BIN_DIR}/dynamic-index.sh" ]; then

        ### 替换dynamic-index.sh中的节点名，共三处
        # 要替换的节点名，如s106
        ES_IP=$(grep es_service_node ${CONF_FILE} | cut -d '=' -f2)
        #ES_Host=$(cat /etc/hosts|grep "$ES_IP" | awk '{print $2}')

        ## 第一处
        # 要查找的目标
        a1="curl -XDELETE '"
        b1="/dynamic?pretty'  -H 'Content-Type: application/json'"
        replace1="curl -XDELETE '${ES_IP}:9200/dynamic?pretty'  -H 'Content-Type: application/json'"
        # ^表示以什么开头，.*a表示以a结尾。替换以a1开头、b1结尾匹配到的字符串为repalce1
        sed -i "s#^${a1}.*${b1}#${replace1}#g" ${BIN_DIR}/dynamic-index.sh

        ## 第二处
        a2="curl -XPUT '"
        b2="/dynamic?pretty' -H 'Content-Type: application/json' -d'"
        replace2="curl -XPUT '${ES_IP}:9200/dynamic?pretty' -H 'Content-Type: application/json' -d'"
        sed -i "s#^${a2}.*${b2}#${replace2}#g" ${BIN_DIR}/dynamic-index.sh

        ## 第三处
        a3="curl -XPUT '"
        b3="/dynamic/_settings' -d '{"
        replace3="curl -XPUT '${ES_IP}:9200/dynamic/_settings' -d '{"
        sed -i "s#^${a3}.*${b3}#${replace3}#g" ${BIN_DIR}/dynamic-index.sh

		##获取分片、副本数
        Shards=$(grep number_of_shards ${CONF_FILE} | cut -d '=' -f2)
        Replicas=$(grep number_of_replicas ${CONF_FILE} | cut -d '=' -f2)
        ##获取模糊查询最小、最大字符数
        Min_Gram=$(grep min_gram ${CONF_FILE} | cut -d '=' -f2)
        Max_Gram=$(grep max_gram ${CONF_FILE} | cut -d '=' -f2)
        ##获取最大分页搜素文档数
        Max_Result_Window=$(grep max_result_window ${CONF_FILE} | cut -d '=' -f2)

        sed -i "s#^\"min_gram\"#\"min_gram\":${Min_Gram}#g" ${BIN_DIR}/dynamic-index.sh
        sed -i "s#^\"max_gram\"#\"max_gram\":${Max_Gram}#g" ${BIN_DIR}/dynamic-index.sh
        sed -i "s#^\"number_of_shards\"#\"number_of_shards\":${Shards}#g" ${BIN_DIR}/dynamic-index.sh
        sed -i "s#^\"number_of_replicas\"#\"number_of_replicas\":${Replicas}#g" ${BIN_DIR}/dynamic-index.sh
        sed -i "s#^\"max_result_window\"#\"max_result_window\":${Max_Result_Window}#g" ${BIN_DIR}/dynamic-index.sh


        sh ${BIN_DIR}/elasticsearch.sh
		if [ $? = 0 ];then
			echo "修改dynamic-index.sh成功并执行......"
		fi
    else
        echo "dynamic-index.sh不存在...."
    fi
}

function main() {
    index_es_dynamic
}

main