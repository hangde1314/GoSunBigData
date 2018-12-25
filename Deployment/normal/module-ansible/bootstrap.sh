#!/bin/bash

cd `dirname $0`
BIN_DIR=`pwd`
VARS_FILE=${BIN_DIR}/roles/vars/main.yml
BIGDATADIR=$(grep 'BigdataDir:' ${VARS_FILE} | cut -d ' ' -f2)
ENV_FILE=${BIN_DIR}/../env_bigdata.sh

sed -i "s#AnsibleDir: .*#AnsibleDir: ${BIN_DIR}#g" ${VARS_FILE}
sed -i "s#INSTALL_HOME=.*#INSTALL_HOME=${BIGDATADIR}#g" ${ENV_FILE}

###### 配置文件路径 ######
mainyml=./roles/vars/main.yml
slave_spark_tem=./roles/templates/slaves.spark.j2
slave_hadoop_tem=./roles/templates/slaves.hadoop.j2
zoo_cfg_j2=./roles/templates/zoo.cfg.j2
es_tem=./roles/templates/elasticsearch.yml.j2
sysctl_conf=/etc/sysctl.conf
limits_conf=/etc/security/limits.conf
###### 节点行数  ######
mline=$(grep -n master hosts | tail -1 | cut -d : -f 1)
fline=$(grep -n follower hosts | tail -1 | cut -d : -f 1)
master=`sed -n "${mline}, ${fline}p" hosts| grep [0-9][0-9]`
followers=`sed -n "${fline}, $(cat hosts| wc -l)p" hosts | grep [0-9][0-9]`
zkline=$(grep -n zk_servers hosts | tail -1 | cut -d : -f 1)
esline=$(grep -n es_servers hosts | tail -1 | cut -d : -f 1)
zkli=`sed -n "${zkline}, ${esline}p" hosts| grep [0-9][0-9] | awk '{print $1}'`
zklist=${master}:2181
bootstraplist=${master}:9092

function modify(){

###### 修改es配置文件 ######
grep -q vm.max_map_count* $sysctl_conf
if [ ! "$?" -eq "0" ]  ;then
   echo "vm.max_map_count=655360" >> $sysctl_conf
fi

grep -q 65536 $limits_conf
if [ ! "$?" -eq "0" ]  ;then
   echo "* soft nofile 65536" >> $limits_conf
fi

grep -q 131072 $limits_conf
if [ ! "$?" -eq "0" ]  ;then
   echo "* hard nofile 131072" >> $limits_conf
fi

grep -q 2048 $limits_conf
if [ ! "$?" -eq "0" ]  ;then
   echo "* soft nproc 2048" >> $limits_conf
fi

grep -q 4096 $limits_conf
if [ ! "$?" -eq "0" ]  ;then
   echo "* hard nproc 4096" >> $limits_conf
fi

k=1
es=""
master_01=${master}
for f in ${followers[@]}
do
    grep -q cluster_f$k $mainyml
    if [ ! "$?" -eq "0" ]  ;then
        num=$(grep -n cluster_master $mainyml | tail -1 | cut -d : -f 1)
        sed -i "$[${num}+k]i cluster_f$k: \"$f\"" $mainyml
        else
        sed -i "s/cluster_f$k:.*/cluster_f$k: \"$f\"/g" $mainyml
    fi
    ((k++))
    ###### es配置参数 #######
    es=\"${f}\",${es}

    master_01=${master_01},${f}
    zklist=${zklist},${f}:2181
    ###### bootstarp参数 ######
    bootstraplist=${bootstraplist},${f}:9092
done

sed -i "s/bootstrap_servers:.*/bootstrap_servers: $bootstraplist/g" $mainyml

zk_hosts=""
zk_num=1
for z in ${zkli[@]}
do
    zk_hosts=${z}:2181,${zk_hosts}
    grep "server.${zk_num}=${z}:2888:3888" ${zoo_cfg_j2}
    if [ $? -eq 0 ]; then
        sed -i "s#server.${zk_num}=${z}:2888:3888#server.${zk_num}=${z}:2888:3888#g" ${zoo_cfg_j2}
    else
        echo "server.${zk_num}=${z}:2888:3888" >>  ${zoo_cfg_j2}
    fi
    ((zk_num++))
done

zk_hosts=${zk_hosts%?}
sed -i "s/zk_cluster:.*/zk_cluster: $zk_hosts/g" $mainyml
es=\[\"$master\",${es%?}\]
sed -i "s/discovery.zen.ping.unicast.hosts:.*/discovery.zen.ping.unicast.hosts: $es/g" $es_tem
nodelist=(${master_01//,/ })
i=1

for n in ${nodelist[@]}
do
    grep -q server${i}_hostname $mainyml
    if [ "$?" -eq "0" ]  ;then
        sed -i "s/server${i}_hostname:.*/server${i}_hostname: ${n}/g" $mainyml
        else
        sed -i "${i}i server${i}_hostname: ${n}" $mainyml
    fi

    grep -q server${i}_hostname $slave_spark_tem
    if [ ! "$?" -eq "0" ]  ;then
        sed -i "$(cat $slave_spark_tem| wc -l)i \{\{\server${i}_hostname\}\}" $slave_spark_tem
    fi
    
    grep -q server${i}_hostname $slave_hadoop_tem
    if [ ! "$?" -eq "0" ]  ;then
        sed -i "$(cat $slave_hadoop_tem| wc -l)i \{\{\server${i}_hostname\}\}" $slave_hadoop_tem
    fi
    
    ((i++))
done
    
    sed -i "s/master_ip:.*/master_ip: $master/g" $mainyml
    sed -i "s/node_master:.*/node_master: `hostname`/g" $mainyml
    sed -i "s/cluster_master:.*/cluster_master: \"$master\"/g" $mainyml
    sed -i "s#BigdataDir:.*#BigdataDir: $1#g" $mainyml
    sed -i "s/tidb_hostname:.*/tidb_hostname: $master/g" $mainyml
    sed -i "s/namenode1_hostname:.*/namenode1_hostname: $master/g" $mainyml
    sed -i "s/namenode2_hostname:.*/namenode2_hostname: ${nodelist[1]}/g" $mainyml
}

echo $#
if [[ $# -lt 1 ]] ; then
        echo "USAGE: $0 path "
        echo " e.g.: $0 /opt/"
        exit 1;
else
    echo $#
    modify $1
fi
