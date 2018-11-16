#!/bin/bash
IP=${IP}
PORT=${PORT}
 mysql -u root -h ${IP} -P ${PORT} -pHzgc@123 << EOF
use people;

INSERT INTO t_24hour_count (peopleid, hour, count, community)
SELECT peopleid, DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 HOUR),'%Y%m%d%H'),COUNT(peopleid),community
FROM t_people_recognize
WHERE DATE_FORMAT(capturetime,'%Y%m%d%H')= DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 HOUR),'%Y%m%d%H')
GROUP BY community,peopleid;
EOF
if [ $? != 0 ];then
 echo "`date "+%Y-%m-%d %H:%M:%S"`: exec 24 sql failed" >> /var/log/mysql.log 2>&1 &
else
 echo "`date "+%Y-%m-%d %H:%M:%S"`: exec 24 sql success" >> /var/log/mysql.log 2>&1 &
fi
