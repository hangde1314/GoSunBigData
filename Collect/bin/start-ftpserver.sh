#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    start-consumer.sh
## Description: to start consumer
## Author:      liushanbin
## Created:     2018-01-08
################################################################################
#set -x  ## 用于调试用，不用的时候可以注释掉

#---------------------------------------------------------------------#
#                              定义变量                                #
#---------------------------------------------------------------------#
cd `dirname $0`
BIN_DIR=`pwd`                                          ### bin目录
cd ..
OBJECT_DIR=`pwd`                                       ### 项目根目录
LOG_DIR=${OBJECT_DIR}/logs                             ## log 日记目录
LOG_FILE=${LOG_DIR}/ftpserver.log                      ##  log 日记文件
CONF_DIR=$OBJECT_DIR/conf                              ### 配置文件目录
LIB_DIR=$OBJECT_DIR/lib                                ## Jar 包目录
BIN_DIR=${OBJECT_DIR}/bin
FTP_PORT=`sed '/ftp.port/!d;s/.*=//' ${CONF_DIR}/collect.properties | tr -d '\r'`
FTP_PNAME=`sed '/ftp.process.name/!d;s/.*=//' ${CONF_DIR}/collect.properties | tr -d '\r'`
LIB_JARS=`ls $LIB_DIR|grep .jar|awk '{print "'$LIB_DIR'/"$0}'|tr "\n" ":"`    ## jar 包位置以及第三方依赖jar包，绝对路径
LIB_JARS=${LIB_JARS}

FTPDATA=`sed '/com.hzgc.ftpserver.user.admin.homedirectory/!d;s/.*=//' ${CONF_DIR}/users.properties | tr -d '\r'`

if [ ! -d ${FTPDATA} ]; then
    mkdir ${FTPDATA}
fi

if [ -n "${FTP_PNAME}" ];then
    PNAME_COUNT=`ps -ef | grep ${FTP_PNAME} | wc -l`
    if [ ${PNAME_COUNT} -gt 1 ];then
        echo "ftp process name ${FTP_PNAME} alread exists, stop ftpserver first"
        exit 1
    fi
fi

if [ -n "$FTP_PORT" ]; then
    SERVER_PORT_COUNT=`netstat -tln | grep $FTP_PORT | wc -l`
    if [ $SERVER_PORT_COUNT -gt 0 ]; then
        echo "ftp port $FTP_PORT already used, start ftp failed"
        exit 1
    fi
fi

#####################################################################
# 函数名: start_ftpserver
# 描述: 启动ftp
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function start_ftpserver()
{
    if [ ! -d $LOG_DIR ]; then
        mkdir $LOG_DIR;
    fi
    nohup java -server -Xms2g -Xmx4g -classpath $CONF_DIR:$LIB_JARS com.hzgc.collect.service.FTP > ${LOG_FILE} 2>&1 &
    echo "ftpserver started"
}

#####################################################################
# 函数名: drop_caches
# 描述: ftp 启动的时候，在每个ftp 服务器起一个清楚缓存的定时任务
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function drop_caches()
{
    boolDropCaches=$(grep drop_caches /etc/crontab | wc  -l)
    if [ "$boolDropCaches" == "1" ];then
        echo "drop_caches already exist"
    else
        echo "drop_caches not exist, add drop_caches into crontab"
        echo "* */1 * * * root sync;echo 3 > /proc/sys/vm/drop_caches" >> /etc/crontab
        service crond restart
    fi
}

#####################################################################
# 函数名: ftp_protect
# 描述: ftp 启动的时候，为FTP添加定时检测服务是否挂掉脚本，保证FTP挂掉后可以重新拉起
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function ftp_protect()
{
    isExistProtest=$(grep ftp_protect /etc/crontab | wc -l)
    if [ "${isExistProtest=}" == "1" ];then
        echo "ftp_protect already exist"
    else
        echo "ftp_protect not exist, add ftp_protect into crontab"
        echo "* */1 * * * root sync;sh ${BIN_DIR}/ftp_protect.sh" >> /etc/crontab
        service crond restart
    fi
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
    drop_caches
    ftp_protect
    start_ftpserver
}

## 脚本主要业务入口
main
