#!/bin/bash
IP=172.18.18.203
PORT=4000
COUNT=100
 mysql -u root -h ${IP} -P ${PORT} -pHzgc@123 << EOF
use people;
INSERT INTO t_imsi (peopleid,imsi)
SELECT DISTINCT final.peopleid,final.imsi FROM
(
    SELECT t1.imsi,t1.peopleid ,MAX(counted) FROM
    (
        SELECT imsi,peopleid,receivetime,COUNT(DISTINCT peopleid,imsi,receivetime) AS counted
        FROM t_fusion_imsi
        WHERE DATE_FORMAT(receivetime,'%Y%m') = DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m')
        GROUP BY imsi,peopleid
        HAVING counted > ${COUNT}
    )AS t1
    GROUP BY imsi
) AS final ,t_imsi
WHERE final.imsi != t_imsi.imsi;
EOF
if [ $? != 0 ];then
 echo "`date "+%Y-%m-%d %H:%M:%S"`: exec fusion sql failed" >> /var/log/mysql.log 2>&1 &
else
 echo "`date "+%Y-%m-%d %H:%M:%S"`: exec fusion sql success" >> /var/log/mysql.log 2>&1 &
fi
