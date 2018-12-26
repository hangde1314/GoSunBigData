#!/bin/bash
#set -x

#cd `dirname $0`
#:pwd
INSTALL_HOME=/opt/hzgc/bigdata

#zookeeper home
export ZOOKEEPER_HOME=${INSTALL_HOME}/zookeeper
export PATH=$PATH:$ZOOKEEPER_HOME/bin

#hadoop home 
export HADOOP_HOME=${INSTALL_HOME}/hadoop
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin

#spark home
export SPARK_HOME=${INSTALL_HOME}/spark
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin

#kafka home
export KAFKA_HOME=${INSTALL_HOME}/kafka
export PATH=$PATH:$KAFKA_HOME/bin

#java home
export JAVA_HOME=${INSTALL_HOME}/jdk
export PATH=$PATH:$JAVA_HOME/bin

#es home
export ES_HOME=${INSTALL_HOME}/elastic
export PATH=$PATH:$ES_HOME/bin

set +x
