#!/bin/bash
IP=172.18.18.203
PORT=4000
 mysql -u root -h ${IP} -P ${PORT} -pHzgc@123 << EOF
use people;

INSERT INTO t_device_recognize (peopleid,deviceid,currenttime,count,flag,community)
SELECT peopleid,deviceid,DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 DAY),'%Y%m%d') as time,COUNT(peopleid),type,community
FROM t_recognize_record
WHERE DATE_FORMAT(capturetime,'%Y%m%d') = DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 DAY),'%Y%m%d')
GROUP BY peopleid,deviceid,type;
EOF
if [ $? != 0 ];then
 echo "`date "+%Y-%m-%d %H:%M:%S"`: exec device sql failed" >> /var/log/mysql.log 2>&1 &
else
 echo "`date "+%Y-%m-%d %H:%M:%S"`: exec device sql success" >> /var/log/mysql.log 2>&1 &
fi
