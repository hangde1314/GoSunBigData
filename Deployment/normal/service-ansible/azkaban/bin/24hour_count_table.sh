#!/bin/bash
IP=172.18.18.203
PORT=4000
 mysql -u root -h ${IP} -P ${PORT} -pHzgc@123 << EOF
use people;

INSERT INTO t_24hour_count (peopleid, hour, count, community)
SELECT peopleid, DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 HOUR),'%Y%m%d%H'),COUNT(peopleid),community
FROM t_recognize_record
WHERE DATE_FORMAT(capturetime,'%Y%m%d%H')= DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 HOUR),'%Y%m%d%H') AND type=1
GROUP BY community,peopleid;
EOF
if [ $? != 0 ];then
 echo "`date "+%Y-%m-%d %H:%M:%S"`: exec 24 sql failed" >> /var/log/mysql.log 2>&1 &
else
 echo "`date "+%Y-%m-%d %H:%M:%S"`: exec 24 sql success" >> /var/log/mysql.log 2>&1 &
fi
