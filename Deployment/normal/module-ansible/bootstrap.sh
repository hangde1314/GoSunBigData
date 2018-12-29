#!/bin/bash
#set -x

cd `dirname $0`
BIN_DIR=`pwd`

###### 配置文件路径 ######
env_bigdata=../env_bigdata.sh
mainyml=./roles/vars/main.yml
slave_spark_tem=./roles/templates/slaves.spark.j2
slave_hadoop_tem=./roles/templates/slaves.hadoop.j2
host_tem=./roles/templates/hosts.j2
hdfs_j2=./roles/templates/hdfs-site.xml.j2
zoo_cfg_j2=./roles/templates/zoo.cfg.j2
es_tem=./roles/templates/elasticsearch.yml.j2
sysctl_conf=/etc/sysctl.conf
limits_conf=/etc/security/limits.conf
ntp_conf_master_j2=./roles/templates/ntp.conf.master.j2
ntp_conf_worker_j2=./roles/templates/ntp.conf.worker.j2
###### 节点行数  #####
tidbline=$(grep -n tidb_servers hosts | tail -1 | cut -d : -f 1)
zkline=$(grep -n zk_servers hosts | tail -1 | cut -d : -f 1)
esline=$(grep -n es_servers hosts | tail -1 | cut -d : -f 1)
kibanaline=$(grep -n kibana_servers hosts | tail -1 | cut -d : -f 1)
kafkaline=$(grep -n kafka_servers hosts | tail -1 | cut -d : -f 1)
hadoopline=$(grep -n hadoop_servers hosts | tail -1 | cut -d : -f 1)
sparkline=$(grep -n spark_servers hosts | tail -1 | cut -d : -f 1)
azkabanline=$(grep -n azkaban_servers hosts | tail -1 | cut -d : -f 1)
zkmaster=`sed -n "${zkline},${esline}p" hosts | grep zk_master=true | awk '{print $1}'`
zknodes=`sed -n "${zkline},${esline}p" hosts | grep [0-9][0-9] | awk -F " " '{print $1}'`
kafkanodes=`sed -n "${kafkaline},${hadoopline}p" hosts | grep [0-9][0-9] | awk -F " " '{print $1}'`
esnodes=`sed -n "${esline},${kibanaline}p" hosts | grep [0-9][0-9]`
hadoopnodes=`sed -n "${hadoopline},${sparkline}p" hosts | grep [0-9][0-9] | awk -F " " '{print $1}'`
sshnodes=`sed -n "2,${tidbline}p" hosts | grep [0-9][0-9] | awk -F " " '{print $1}'`
sparknodes=`sed -n "${sparkline},${azkabanline}p" hosts | grep [0-9][0-9]`
tidbmaster=`sed -n "${tidbline},${zkline}p" hosts | grep ntp_server=true | awk '{print $1}'`
namenode1=`sed -n "${hadoopline},${sparkline}p" hosts | grep namenode_active=true | awk -F " " '{print $1}'`
namenode2=`sed -n "${hadoopline},${sparkline}p" hosts | grep namenode_standby=true | awk -F " " '{print $1}'`
BigDataDir=$1

if [ ! -n "${BigDataDir}" ];then
   BigDataDir=/opt/hzgc/bigdata
else
   sed -i "s#INSTALL_HOME=.*#INSTALL_HOME=${BigDataDir}#g" ${env_bigdata}
fi

sed -i "s#AnsibleDir: .*#AnsibleDir: ${BIN_DIR}#g" ${mainyml}

###### 安装ansible ######
echo `ansible --version` >> /opt/test.txt
grep "ansible 2.5.0" /opt/test.txt
if [ ! "$?" -eq "0" ]; then
   echo "准备安装ansible"
   tar -xvf ./roles/files/ansible.tar.gz
   sh ./roles/files/ansible/ansible-denpendency/install_ansible_system_rpms.sh
   sh ./roles/files/ansible/ansible-install/install_ansible.sh
   echo "安装ansible完成..."
fi
rm -rf /opt/test.txt

###### 配置ssh免密 ######
ansible-playbook -i hosts yml/sshKey.yml

###### 安装ntp ######
function ntp(){
###### 修改ntp-server配置文件 ######
IP=$(grep ntp_server=true ./hosts)
LOCAL_IP=$(echo ${IP} | cut -d ' ' -f1)
GATEWAY=$(echo ${IP} | cut -d ' ' -f1 | awk -F '.' '{print $1"."$2"."$3".0"}')
grep "restrict ${GATEWAY} mask 255.255.255.0 nomodify notrap" ${ntp_conf_master_j2}
if [ ! "$?" -eq "0" ]; then
   sed -i -e "/restrict ::1/{s|$|\nrestrict ${GATEWAY} mask 255.255.255.0 nomodify notrap|}" ${ntp_conf_master_j2}
fi
###### 修改ntp-client端配置文件 ######
grep "server ${LOCAL_IP} iburst" ${ntp_conf_worker_j2}
if [ ! "$?" -eq "0" ]; then
   sed -i -e "/#server 3.centos.pool.ntp.org iburst/{s|$|\nserver ${LOCAL_IP} iburst|}" ${ntp_conf_worker_j2}
fi
}

function modify(){

###### 修改es配置文件 ######
grep -q vm.max_map_count* $sysctl_conf
if [ ! "$?" -eq "0" ]  ;then
   echo "vm.max_map_count=655360" >> $sysctl_conf
fi
grep -q net.ipv4.tcp_syncookies* $sysctl_conf
if [ ! "$?" -eq "0" ]  ;then
   echo "net.ipv4.tcp_syncookies=1" >> $sysctl_conf
fi
grep -q net.ipv4.tcp_tw_reuse* $sysctl_conf
if [ ! "$?" -eq "0" ]  ;then
   echo "net.ipv4.tcp_tw_reuse=1" >> $sysctl_conf
fi
grep -q net.ipv4.tcp_tw_recycle* $sysctl_conf
if [ ! "$?" -eq "0" ]  ;then
   echo "net.ipv4.tcp_tw_recycle=1" >> $sysctl_conf
fi
grep -q net.ipv4.tcp_fin_timeout* $sysctl_conf
if [ ! "$?" -eq "0" ]  ;then
   echo "net.ipv4.tcp_fin_timeout=30" >> $sysctl_conf
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

es=""
bootstraplist=""
for f in ${esnodes[@]}
do
 ###### es配置参数 #######
  es=\"${f}\",${es}
done

es=\[${es%?}\]
sed -i "s/discovery.zen.ping.unicast.hosts:.*/discovery.zen.ping.unicast.hosts: $es/g" $es_tem

for j in ${kafkanodes[@]}
do
 ##### kafka配置参数 ########
   bootstraplist=${j}:9092,${bootstraplist}
done

bootstraplist=${bootstraplist%?}
sed -i "s/bootstrap_servers:.*/bootstrap_servers: $bootstraplist/g" $mainyml

zklist=""
zk_num=1
sed -i "/server/d" ${zoo_cfg_j2}
for k in ${zknodes[@]}
do
  #### zookeeper配置参数 ####
  zklist=${k}:2181,${zklist}
  grep "server.${zk_num}=${k}:2888:3888" ${zoo_cfg_j2}
  if [ $? -eq 0 ]; then
     sed -i "s#server.${zk_num}=${k}:2888:3888#server.${zk_num}=${k}:2888:3888#g" ${zoo_cfg_j2}
  else
     echo "server.${zk_num}=${k}:2888:3888" >>  ${zoo_cfg_j2}
  fi 
  ((zk_num++))
done

zklist=${zklist%?}
sed -i "s/zk_cluster:.*/zk_cluster: $zklist/g" $mainyml

hadoop_num=1
hadooplist=""
for a in ${hadoopnodes[@]}
do
  hadooplist=${a}:8485\;${hadooplist}
  #### hadoop 配置参数 ####
  if [ "${hadoop_num}" -eq "1" ];then
    echo "${a}" > $slave_spark_tem
  else
    echo "${a}" >> $slave_spark_tem
  fi
((hadoop_num++))
done

sed -i "s#qjournal:.*#qjournal://${hadooplist}/hzgc</value>#g" ${hdfs_j2}

spark_num=1
for b in ${sparknodes[@]}
do
  #### spark 配置参数 ####
  if [ "${spark_num}" -eq "1"  ];then
    echo "${b}" > $slave_hadoop_tem
  else
    echo "${b}" >> $slave_hadoop_tem
  fi
  ((spark_num++))
done

    sed -i "s/master_ip:.*/master_ip: $zkmaster/g" $mainyml
    sed -i "s/node_master:.*/node_master: `hostname`/g" $mainyml
    sed -i "s#BigdataDir:.*#BigdataDir: $BigDataDir#g" $mainyml
    sed -i "s/tidb_hostname:.*/tidb_hostname: $tidbmaster/g" $mainyml
    sed -i "s/namenode1_hostname:.*/namenode1_hostname: $namenode1/g" $mainyml
    sed -i "s/namenode2_hostname:.*/namenode2_hostname: $namenode2/g" $mainyml


}

function host(){

hostname=""
ssh_num=1
for l in ${sshnodes[@]}
do

  if [ "${ssh_num}" -eq "1"  ];then
     hostname=`ssh root@${l} "hostname"`
     echo "127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4" > ${host_tem}
     echo "::1         localhost localhost.localdomain localhost6 localhost6.localdomain6" >> ${host_tem}
     echo "${l} ${hostname} hzgc" >> ${host_tem}
  else
    hostname=`ssh root@${l} "hostname"`
    echo "${l} ${hostname}" >> ${host_tem}
  fi
  ((ssh_num++))
done


}

function main(){
   ntp
   modify
   host
}


main
