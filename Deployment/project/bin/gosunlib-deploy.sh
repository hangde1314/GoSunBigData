#!/bin/bash
################################################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    gsFaceLibConfig.sh
## Description: 配置算法库.
## Version:     2.0
## Author:      zhangbaolin
## Created:     2018-06-28 
################################################################################
##set -e
#set -x

#---------------------------------------------------------------------#
#                              定义变量                               #
#---------------------------------------------------------------------#

## 进入当前目录
cd `dirname $0`
## 脚本所在目录：../hzgc/service
BIN_DIR=`pwd`
cd ../..
## 脚本根目录：../hzgc
ROOT_HOME=`pwd`
## gsFaceLib 压缩包目录：../hzgc/component/bigdata
GSFACELIB_SOURCE_DIR=${ROOT_HOME}/component/bigdata
## 配置文件目录：../hzgc/conf
CONF_DIR=${ROOT_HOME}/conf
## 安装日记目录
LOG_DIR=${ROOT_HOME}/logs
## 安装日记目录
LOG_FILE=${LOG_DIR}/gsFaceLib.log
## gsFaceLib的安装节点，放入数组中
GSFACELIB_HOSTNAME_LISTS=$(grep GsFaceLib_HostName ${CONF_DIR}/cluster_conf.properties|cut -d '=' -f2)
GSFACELIB_HOSTNAME_ARRY=(${GSFACELIB_HOSTNAME_LISTS//;/ })


if [ ! -d $LOG_DIR ];then
    mkdir -p $LOG_DIR;
fi


#####################################################################
# 函数名: rsync_GsFaceLib
# 描述: 将解压后的GsFaceLib分发到各个节点
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function rsync_GsFaceLib(){
    echo "**********************************************" | tee -a $LOG_FILE
    for hostname in ${GSFACELIB_HOSTNAME_ARRY[@]};do
        echo " GsFaceLib分发到${hostname}........"  | tee -a $LOG_FILE
        #rsync -rvl ${GSFACELIB_SOURCE_DIR}/GsFaceLib   root@${hostname}:/opt  >/dev/null
        scp -r ${GSFACELIB_SOURCE_DIR}/GsFaceLib   root@${hostname}:/opt  >/dev/null
    done

    echo "GsFaceLib 分发完毕 ..."  | tee -a $LOG_FILE  
    echo "" | tee -a $LOG_FILE
}

#####################################################################
# 函数名: add_env
# 描述: 在各个节点上判断/etc/profile中环境变量设置字串存在
#       不存在则添加追加到文件末尾：export LD_LIBRARY_PATH=/opt/GsFaceLib/face_libs
#                              export OPENBLAS_NUM_THREADS=1
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function add_env(){
    echo "**********************************************" | tee -a $LOG_FILE
    echo "please waiting, 添加/etc/profile中的GsFaceLib环境变量 ........"  | tee -a $LOG_FILE
	
	##########原脚本
    #for hostname in ${GSFACELIB_HOSTNAME_ARRY[@]};do
    #    ssh root@${hostname} 'grep "export LD_LIBRARY_PATH=/opt/GsFaceLib/face_libs" /etc/profile; if [ $? -eq 1 ]; then echo 'export LD_LIBRARY_PATH=/opt/GsFaceLib/face_libs' >> /etc/profile; echo "添加成功...";fi'
    #    ssh root@${hostname} "source /etc/profile"
    #done
	
	##########新脚本
	### 增加facelibs环境变量，若先前有配置，要先删除原来的
	### ssh到每个节点，查找etc/profile中是否存在facelibs系统变量行，若存在，则替换；若不存在，则追加。
	LD_LIBRARY_PATH=/opt/GsFaceLib/face_libs
	for hostname in ${GSFACELIB_HOSTNAME_ARRY[@]};do
		facelibshome_exists=$(ssh root@${hostname} 'grep "export LD_LIBRARY_PATH=" /etc/profile')
		facelibopenblas_num_exists=$(ssh root@${hostname} 'grep "export OPENBLAS_NUM_THREADS=" /etc/profile')
		# 存在"export LD_LIBRARY_PATH="这一行：则替换这一行
		if [ "${facelibshome_exists}" != "" ];then
			ssh root@${hostname} "sed -i 's#^export LD_LIBRARY_PATH=.*#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH#g' /etc/profile"
		else
			ssh root@${hostname} "echo '#LD_LIBRARY_PATH'>>/etc/profile ;echo export LD_LIBRARY_PATH=$LD_LIBRARY_PATH >> /etc/profile"
		fi
		# 存在"export OPENBLAS_NUM_THREADS="这一行：则替换这一行
        if [ "${facelibopenblas_num_exists}" != "" ];then
            ssh root@${hostname} "sed -i 's#^export OPENBLAS_NUM_THREADS=.*#export OPENBLAS_NUM_THREADS=1#g' /etc/profile"
        else
            ssh root@${hostname} "echo export OPENBLAS_NUM_THREADS=1 >> /etc/profile"
        fi
	done
}

#####################################################################
# 函数名: remove_snini
# 描述: 删除/opt/GsFaceLib下sn.ini文件
# 参数: N/A
# 返回值: N/A
# 其他: N/A
#####################################################################
function remove_snini(){
    echo "**********************************************" | tee -a $LOG_FILE
    echo "please waiting, 删除/opt/GsFaceLib下sn.ini文件 ........"  | tee -a $LOG_FILE
    for hostname in ${GSFACELIB_HOSTNAME_ARRY[@]};do
        ssh root@${hostname} "rm -rf /opt/GsFaceLib/sn.ini"
    done
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
    #compression_the_tar
    rsync_GsFaceLib
    add_env
    remove_snini
}

#---------------------------------------------------------------------#
#                              执行流程                                #
#---------------------------------------------------------------------#

## 打印时间
echo ""  | tee  -a  $LOG_FILE
echo ""  | tee  -a  $LOG_FILE
echo "==================================================="  | tee -a $LOG_FILE
echo "$(date "+%Y-%m-%d  %H:%M:%S")"                       | tee  -a  $LOG_FILE
main


set +x
