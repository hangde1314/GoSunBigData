#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    config-service
## Description: 一键配置脚本：执行service的一键配置脚本
## Author:      chenke
## Created:     2018-05-24
################################################################################
#set -x

#----------------------------------------------------------------------------#
#                                定义变量                                    #
#----------------------------------------------------------------------------#

cd `dirname $0`
BIN_DIR=`pwd`                                              ##bin目录：脚本所在目录
cd ..
COMMON_DIR=`pwd`                                           ##common模块部署目录
CONF_COMMON_DIR=$COMMON_DIR/conf                           ##配置文件目录
CONF_FILE=$CONF_COMMON_DIR/project-conf.properties        ##项目配置文件

LOG_DIR=$COMMON_DIR/logs                                   ##log日志目录
LOG_FILE=$LOG_DIR/config-service.log                       ##log日志
cd ..
OBJECT_DIR=`pwd`                                           ##项目根目录

cd /opt/hzgc/bigdata/Hadoop/hadoop/etc/hadoop
HADOOP_CONF_DIR=`pwd`                                      ##hadoop有关配置文件目录
CORE_FILE=$HADOOP_CONF_DIR/core-site.xml
HDFS_FILE=$HADOOP_CONF_DIR/hdfs-site.xml
cd /opt/hzgc/bigdata/HBase/hbase/conf
HBASE_CONF_DIR=`pwd`                                       ##hbase有关配置文件目录
HBASE_FILE=$HBASE_CONF_DIR/hbase-site.xml

SPARK_DIR=$OBJECT_DIR/cluster/spark                        ##spark模块部署目录
SERVICE_DIR=$OBJECT_DIR/service                            ##service模块部署目录
##address模块
ADDRESS_DIR=$SERVICE_DIR/address                                ##address模块目录
ADDRESS_BIN_DIR=$ADDRESS_DIR/bin                                ##address模块脚本存放目录
ADDRESS_START_FILE=$ADDRESS_BIN_DIR/start-address.sh            ##address模块启动脚本
ADDRESS_CONF_DIR=$ADDRESS_DIR/conf                              ##address模块conf目录
ADDRESS_PRO_FILE=$ADDRESS_CONF_DIR/application-pro.properties   ##address模块配置文件
##clustering模块
CLUSTERING_DIR=$SERVICE_DIR/clustering                                ##clustering模块目录
CLUSTERING_BIN_DIR=$CLUSTERING_DIR/bin                                ##clustering模块脚本存放目录
CLUSTERING_START_FILE=$CLUSTERING_BIN_DIR/start-clustering.sh         ##clustering模块启动脚本
CLUSTERING_CONF_DIR=$CLUSTERING_DIR/conf                              ##clustering模块conf目录
CLUSTERING_PRO_FILE=$CLUSTERING_CONF_DIR/application-pro.properties   ##clustering模块配置文件
##dispatch模块
DISPATCH_DIR=$SERVICE_DIR/dispatch                           ##dispatch模块目录
DISPATCH_BIN_DIR=$DISPATCH_DIR/bin                           ##dispatch模块脚本存放目录
DISPATCH_START_FILE=$DISPATCH_BIN_DIR/start-dispatch.sh       ##dispatch模块启动脚本
DISPATCH_CONF_DIR=$DISPATCH_DIR/conf                         ##dispatch模块conf目录
DISPATCH_PRO_FILE=$DISPATCH_CONF_DIR/application-pro.properties   ##dispatch模块配置文件
##dynrepo模块
DYNREPO_DIR=$SERVICE_DIR/dynRepo                           ##dynrepo模块目录
DYNREPO_BIN_DIR=$DYNREPO_DIR/bin                           ##dynrepo模块脚本存放目录
DYNREPO_START_FILE=$DYNREPO_BIN_DIR/start-dynrepo.sh       ##dynrepo模块启动脚本
DYNREPO_CONF_DIR=$DYNREPO_DIR/conf                         ##dynrepo模块conf目录
DYNREPO_PRO_FILE=$DYNREPO_CONF_DIR/application-pro.properties   ##dynrepo模块配置文件
##face模块
FACE_DIR=$SERVICE_DIR/face                           ##face模块目录
FACE_BIN_DIR=$FACE_DIR/bin                           ##face模块脚本存放目录
FACE_START_FILE=$FACE_BIN_DIR/start-face.sh       ##face模块启动脚本
FACE_CONF_DIR=$FACE_DIR/conf                         ##face模块conf目录
FACE_PRO_FILE=$FACE_CONF_DIR/application-pro.properties   ##face模块配置文件
##starepo模块
STAREPO_DIR=$SERVICE_DIR/staRepo                           ##starepo模块目录
STAREPO_BIN_DIR=$STAREPO_DIR/bin                           ##starepo模块脚本存放目录
STAREPO_START_FILE=$STAREPO_BIN_DIR/start-starepo.sh       ##starepo模块启动脚本
STAREPO_CONF_DIR=$STAREPO_DIR/conf                         ##starepo模块conf目录
STAREPO_PRO_FILE=$STAREPO_CONF_DIR/application-pro.properties   ##starepo模块配置文件
##visual模块
VISUAL_DIR=$SERVICE_DIR/visual                           ##visual模块目录
VISUAL_BIN_DIR=$VISUAL_DIR/bin                           ##visual模块脚本存放目录
VISUAL_START_FILE=$VISUAL_BIN_DIR/start-visual.sh       ##visual模块启动脚本
VISUAL_CONF_DIR=$VISUAL_DIR/conf                       ##visual模块conf目录
VISUAL_PRO_FILE=$VISUAL_CONF_DIR/application-pro.properties   ##visual模块配置文件

# 创建日志目录
mkdir -p $LOG_DIR

################################################################################
# 函数名：service_copy
# 描述：将hbase-site、core-site、hdfs-site拷贝至需要的模块conf底下
# 参数：N/A
# 返回值：N/A
# 其他：N/A
################################################################################
function service_copy()
{
    echo "" | tee -a $LOG_FILE
    echo "**************************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "开始将配置文件拷贝至需要的模块下......" | tee -a $LOG_FILE

    scp -r $CORE_FILE $HDFS_FILE $HBASE_FILE $ADDRESS_CONF_DIR
    scp -r $CORE_FILE $HDFS_FILE $HBASE_FILE $DYNREPO_CONF_DIR
    scp -r $CORE_FILE $HDFS_FILE $HBASE_FILE $STAREPO_CONF_DIR
    scp -r $CORE_FILE $HDFS_FILE $HBASE_FILE $CLUSTERING_CONF_DIR
    scp -r $CORE_FILE $HDFS_FILE $HBASE_FILE $VISUAL_CONF_DIR
    scp -r $CORE_FILE $HDFS_FILE $HBASE_FILE $DISPATCH_CONF_DIR
}


################################################################################
# 函数名：distribute_service
# 描述：一键配置service中各个模块的properties文件以及启停脚本
# 参数：N/A
# 返回值：N/A
# 其他：N/A
################################################################################
function distribute_service()
{
    echo "" | tee -a $LOG_FILE
    echo "**************************************************" | tee -a $LOG_FILE
    echo "" | tee -a $LOG_FILE
    echo "开始配置service底下的各个模块......" | tee -a $LOG_FILE

    #单独给静态库配置pro配置文件：
    #从project-conf.properties中读取kafka配置IP
    KAFKA_IP=$(grep kafka_install_node $CONF_FILE | cut -d '=' -f2)
    #将这些分号分割的ip用于放入数组中
    kafka_arr=(${KAFKA_IP//;/ })
    kafkapro=''
    for kafka_host in ${kafka_arr[@]}
    do
      kafkapro=$kafkapro$kafka_host":9092,"
    done
    kafkapro=${kafkapro%?}

    #替换pro文件中的值：
    sed -i "s#^kafka.bootstrap.servers=.*#kafka.bootstrap.servers=${kafkapro}#g" ${STAREPO_PRO_FILE}
	sed -i "s#^kafka.bootstrap.servers=.*#kafka.bootstrap.servers=${kafka_arr[0]:9092}#g" ${STAREPO_START_FILE}
    echo "静态库application-pro文件配置完成......"
	
	
	 #####################KAFKA_HOST#########################
    #替换模块启动脚本中KAFKA_HOST：key=value(替换key字段的值value)
    sed -i "s#^KAFKA_HOST=.*#KAFKA_HOST=${kafkapro}#g" ${STAREPO_START_FILE}
    echo "start-starepo.sh脚本配置kafka完成......"

    #配置es.hosts:
    #从project-conf.properties中读取es所需配置IP
    #根据字段es，查找配置文件，这些值以分号分隔
    ES_IP=$(grep es_service_node $CONF_FILE | cut -d '=' -f2)
    #将这些分号分割的ip用于放入数组中
    es_arr=(${ES_IP//;/ })
    espro=''
    for es_host in ${es_arr[@]}
    do
       espro="$espro$es_host,"
    done
    espro=${espro%?}

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^ES_HOST=.*#ES_HOST=${espro}#g" ${CLUSTERING_START_FILE}
    echo "start-clustering.sh脚本配置es完成......"

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^ES_HOST=.*#ES_HOST=${espro}#g" ${DYNREPO_START_FILE}
    echo "start-dynrepo.sh脚本配置es完成......"

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^ES_HOST=.*#ES_HOST=${espro}#g" ${STAREPO_START_FILE}
    echo "start-starepo.sh脚本配置es完成......"

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^ES_HOST=.*#ES_HOST=${espro}#g" ${VISUAL_START_FILE}
    echo "start-visual.sh脚本配置es完成......"


    #配置zookeeper：
    #从project-conf.properties中读取zookeeper所需配置IP
    #根据字段zookeeper，查找配置文件，这些值以分号分隔
    ZK_HOSTS=$(grep zookeeper_installnode $CONF_FILE | cut -d '=' -f2)
    zk_arr=(${ZK_HOSTS//;/ })
    zkpro=''
    zkpro=$zkpro${zk_arr[0]}":2181"

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^ZOOKEEPER_HOST=.*#ZOOKEEPER_HOST=${zkpro}#g" ${ADDRESS_START_FILE}
    echo "start-address.sh脚本配置zookeeper完成......"

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^ZOOKEEPER_HOST=.*#ZOOKEEPER_HOST=${zkpro}#g" ${DYNREPO_START_FILE}
    echo "start-dynrepo.sh脚本配置zookeeper完成......"

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^ZOOKEEPER_HOST=.*#ZOOKEEPER_HOST=${zk_arr[0]}#g" ${STAREPO_START_FILE}
    echo "start-starepo.sh脚本配置zookeeper完成......"

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^ZOOKEEPER_HOST=.*#ZOOKEEPER_HOST=${zkpro}#g" ${VISUAL_START_FILE}
    echo "start-visual.sh脚本配置zookeeper完成......"


    #配置eureka_node:
    #从project-conf.properties中读取eureka_node所需配置ip
    #根据字段eureka_node，查找配置文件，这些值以分号分隔
    EUREKA_NODE_HOSTS=$(grep spring_cloud_eureka_node $CONF_FILE | cut -d '=' -f2)
    eureka_node_arr=(${EUREKA_NODE_HOSTS//;/ })
    enpro=''
    for en_host in ${eureka_node_arr[@]}
    do
      enpro=${enpro}${en_host}","
    done
    enpro=${enpro%?}

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_IP=.*#EUREKA_IP=${enpro}#g" ${ADDRESS_START_FILE}
    echo "start-address.sh脚本配置eureka_node完成......."

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_IP=.*#EUREKA_IP=${enpro}#g" ${CLUSTERING_START_FILE}
    echo "start-clustering.sh脚本配置eureka_node完成......."

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_IP=.*#EUREKA_IP=${enpro}#g" ${DISPATCH_START_FILE}
    echo "start-dispatch.sh脚本配置eureka_node完成......."

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_IP=.*#EUREKA_IP=${enpro}#g" ${DYNREPO_START_FILE}
    echo "start-dynrepo.sh脚本配置eureka_node完成......."

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_IP=.*#EUREKA_IP=${enpro}#g" ${FACE_START_FILE}
    echo "start-face.sh脚本配置eureka_node完成......."

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_IP=.*#EUREKA_IP=${enpro}#g" ${STAREPO_START_FILE}
    echo "start-starepo.sh脚本配置eureka_node完成......."

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_IP=.*#EUREKA_IP=${enpro}#g" ${VISUAL_START_FILE}
    echo "start-visual.sh脚本配置eureka_node完成......."


    #配置eureka_port:
    #从project-conf.properties中读取eureka_port所需配置port
    #根据字段eureka_port,查找配置文件
    EUREKA_PORT=$(grep spring_cloud_eureka_port $CONF_FILE | cut -d '=' -f2)

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_PORT=.*#EUREKA_PORT=${EUREKA_PORT}#g" ${ADDRESS_START_FILE}
    echo "start-address.sh脚本配置eureka_port完成......."

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_PORT=.*#EUREKA_PORT=${EUREKA_PORT}#g" ${CLUSTERING_START_FILE}
    echo "start-clustering.sh脚本配置eureka_port完成......."

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_PORT=.*#EUREKA_PORT=${EUREKA_PORT}#g" ${DISPATCH_START_FILE}
    echo "start-dispatch.sh脚本配置eureka_port完成......."

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_PORT=.*#EUREKA_PORT=${EUREKA_PORT}#g" ${DYNREPO_START_FILE}
    echo "start-dynrepo.sh脚本配置eureka_port完成......."

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_PORT=.*#EUREKA_PORT=${EUREKA_PORT}#g" ${FACE_START_FILE}
    echo "start-face.sh脚本配置eureka_port完成......."

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_PORT=.*#EUREKA_PORT=${EUREKA_PORT}#g" ${STAREPO_START_FILE}
    echo "start-starepo.sh脚本配置eureka_port完成......."

    #替换模块启动脚本中：key=value(替换key字段的值value)
    sed -i "s#^EUREKA_PORT=.*#EUREKA_PORT=${EUREKA_PORT}#g" ${VISUAL_START_FILE}
    echo "start-visual.sh脚本配置eureka_port完成......."

}

#####################################################################
# 函数名: main
# 描述: 脚本主要业务入口
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################

function main()
{
    service_copy
    distribute_service
}


#---------------------------------------------------------------------#
#                              执行流程                                #
#---------------------------------------------------------------------#

## 打印时间
echo ""
echo ""
echo "==================================================="
echo "$(date "+%Y-%m-%d  %H:%M:%S")"

main


set +x