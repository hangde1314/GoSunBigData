#!/bin/bash
IP=172.18.18.119
PORT=4000
 mysql -u root -h ${IP} -P ${PORT} << EOF
use people;

INSERT INTO t_people_out (peopleid, community, \`month\`, isconfirm)
SELECT t1.id, t1.community ,time, 1
FROM(
    SELECT community ,time, people.id
    FROM (
        SELECT id, community, DATE_FORMAT(lasttime,"%Y%m") AS time
        FROM t_people
        WHERE lasttime <= DATE_SUB(now(),INTERVAL 1 DAY)
          AND community is NOT NULL
    ) AS people LEFT JOIN t_picture
    ON people.id = t_picture.peopleid
)AS t1 LEFT JOIN t_people_out
ON t1.id != t_people_out.peopleid
AND time = t_people_out.\`month\`;

EOF
