#!/bin/bash
IP=172.18.18.203
PORT=4000
COUNT=20
 mysql -u root -h ${IP} -P ${PORT} -pHzgc@123 << EOF
use people;

#新增
INSERT INTO people.t_people_new (peopleid,community,month,isconfirm,flag)
SELECT aaa.peopleid,aaa.community,DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m') , 1 ,t_recognize_record.flag FROM
(
	SELECT aa.peopleid,t_recognize_record.community ,COUNT(t_recognize_record.community) AS num  FROM
	(
		SELECT DISTINCT a.peopleid FROM
		(
			SELECT peopleid FROM t_recognize_record WHERE DATE_FORMAT(capturetime ,'%Y%m') = DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m') AND flag=2
		)AS a ,t_recognize_record
		WHERE a.peopleid=t_recognize_record.peopleid AND t_recognize_record.flag=10
	)AS aa ,t_recognize_record
    WHERE aa.peopleid=t_recognize_record.peopleid AND DATE_FORMAT(t_recognize_record.capturetime ,'%Y%m') =DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m')
	GROUP BY aa.peopleid,t_recognize_record.community
	HAVING COUNT(t_recognize_record.community) >= ${COUNT}
	ORDER BY num DESC
)AS aaa,t_recognize_record
WHERE aaa.peopleid=t_recognize_record.peopleid AND aaa.community=t_recognize_record.community AND DATE_FORMAT(t_recognize_record.capturetime ,'%Y%m') =DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m') AND t_recognize_record.flag=2
GROUP BY aaa.peopleid, aaa.community
ORDER BY peopleid DESC;

#实名
INSERT INTO people.t_people_new (peopleid,community,month,isconfirm,flag)
SELECT  aa.peopleid,aa.community,DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m') ,1 ,aa.flag FROM
(
    SELECT a.peopleid,a.community,t_recognize_record.flag FROM
    (
    	SELECT peopleid,community,COUNT(community) FROM t_recognize_record
    	WHERE DATE_FORMAT(capturetime ,'%Y%m') =DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m') AND flag=1
    	GROUP BY peopleid,community
    	HAVING COUNT(community) >= ${COUNT}
    )AS a,t_recognize_record
    WHERE a.peopleid=t_recognize_record.peopleid AND a.community=t_recognize_record.community AND DATE_FORMAT(t_recognize_record.capturetime ,'%Y%m') =DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m')
    GROUP BY a.peopleid, a.community
)AS aa , t_people
WHERE aa.peopleid = t_people.id AND (aa.community != t_people.community OR aa.community IS NULL)
ORDER BY peopleid DESC;
EOF
if [ $? != 0 ];then
 echo "`date "+%Y-%m-%d %H:%M:%S"`: exec newpeople sql failed" >> /var/log/mysql.log 2>&1 &
else
 echo "`date "+%Y-%m-%d %H:%M:%S"`: exec newpeople sql success" >> /var/log/mysql.log 2>&1 &
fi
