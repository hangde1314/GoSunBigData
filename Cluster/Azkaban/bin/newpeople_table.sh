#!/bin/bash
IP=172.18.18.119
PORT=4000
COUNT=20
 mysql -u root -h ${IP} -P ${PORT} << EOF
use people;

#新增
INSERT INTO people.t_people_new (peopleid,community,month,isconfirm,flag)
SELECT aaa.peopleid,aaa.community,DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m') , 1 ,t_people_recognize.flag FROM
(
	SELECT aa.peopleid,t_people_recognize.community ,COUNT(t_people_recognize.community) AS num  FROM
	(
		SELECT DISTINCT a.peopleid FROM
		(
			SELECT * FROM t_people_recognize WHERE flag=2 AND DATE_FORMAT(capturetime ,'%Y%m') =DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m')
		)AS a ,t_people_recognize
		WHERE a.peopleid=t_people_recognize.peopleid AND t_people_recognize.flag=10
	)AS aa ,t_people_recognize
    WHERE aa.peopleid=t_people_recognize.peopleid AND DATE_FORMAT(t_people_recognize.capturetime ,'%Y%m') =DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m')
	GROUP BY aa.peopleid,t_people_recognize.community
	HAVING COUNT(t_people_recognize.community) >= ${COUNT}
	ORDER BY num DESC
)AS aaa,t_people_recognize
WHERE aaa.peopleid=t_people_recognize.peopleid AND aaa.community=t_people_recognize.community AND DATE_FORMAT(t_people_recognize.capturetime ,'%Y%m') =DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m') AND t_people_recognize.flag=2
GROUP BY aaa.peopleid, aaa.community
ORDER BY peopleid DESC

#实名
INSERT INTO people.t_people_new (peopleid,community,month,isconfirm,flag)
SELECT aaa.peopleid,aaa.community,DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m') ,1 ,t_people_recognize.flag FROM
(
	SELECT *,COUNT(community) FROM t_people_recognize
	WHERE flag=1 AND DATE_FORMAT(capturetime ,'%Y%m') =DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m')
	GROUP BY peopleid,community
	HAVING COUNT(community) >= ${COUNT}
	ORDER BY COUNT(community) DESC
)AS aaa,t_people_recognize
WHERE aaa.peopleid=t_people_recognize.peopleid AND aaa.community=t_people_recognize.community AND DATE_FORMAT(t_people_recognize.capturetime ,'%Y%m') =DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 1 MONTH),'%Y%m')
GROUP BY aaa.peopleid, aaa.community
ORDER BY peopleid DESC
EOF
