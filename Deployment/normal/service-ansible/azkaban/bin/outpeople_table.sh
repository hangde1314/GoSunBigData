#!/bin/bash
IP=172.18.18.203
PORT=4000
 mysql -u root -h ${IP} -P ${PORT} -pHzgc@123 << EOF
use people;

INSERT INTO t_people_out (peopleid, community, \`month\`, isconfirm)
SELECT id,community ,DATE_FORMAT(NOW(),"%Y%m"),1  FROM (
SELECT DISTINCT community ,time, people.id
FROM (
        SELECT id, community, DATE_FORMAT(lasttime,"%Y%m") AS time
        FROM t_people
        WHERE community is NOT NULL
          AND lasttime = DATE_SUB(now(),INTERVAL 3 MONTH)
) AS people LEFT JOIN t_picture
ON people.id = t_picture.peopleid) AS o1;

EOF
if [ $? != 0 ];then
 echo "`date "+%Y-%m-%d %H:%M:%S"`: exec outpeople sql failed" >> /var/log/mysql.log 2>&1 &
else
 echo "`date "+%Y-%m-%d %H:%M:%S"`: exec outpeople sql success" >> /var/log/mysql.log 2>&1 &
fi
