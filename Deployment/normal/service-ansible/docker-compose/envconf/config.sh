#!/bin/bash
#########################################################
## Copyright:   HZGOSUN Tech. Co, BigData
## Filename:    config.sh
## Description: 一键配置env服务模块
## Author:      yinhang
## Created:     2018-12-13
##########################################################
#set -x  ## 用于调试用，不用的时候可以注释掉
#set -e

cd `dirname $0`
## 脚本所在目录
ENV_CONF_DIR=`pwd`
ENV_FILE=${BIN_DIR}/.env
cd ../..
GOSUN_DIR=`pwd`
FILE_PATHS=$(find ${GOSUN_DIR} -name "docker-compose.yml")
i=0
for file in ${FILE_PATHS}
do
  filelist[$i]="$file"
  ((i++))
done
cd ${ENV_CONF_DIR}
for value in ${filelist[*]}
do
  file_path=${value%/*}
  echo ${file_path}
  cp .env ${file_path}
done
