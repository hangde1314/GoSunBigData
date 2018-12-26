#!/bin/bash

if [ ! -n "${PROFILES_ACTIVE}" ]; then
    echo "Env [PROFILES_ACTIVE] is null"
    exit 0;
elif [ "ftp" = "${PROFILES_ACTIVE}" ]; then
    echo "Env [PROFILES_ACTIVE] is ${PROFILES_ACTIVE}, start ftpserver"
    exec java -jar /app.jar \
    --spring.profiles.active=${PROFILES_ACTIVE} \
    --spring.cloud.config.enabled=false \
    --ftp.ip=${HOST_IP} \
    --ftp.port=${FTP_PORT} \
    --ftp.hostname=${HOST_NAME} \
    --kafka.host=${KAFKA_HOST:-NULL} \
    --home.dir=${HOME_DIR} \
    --seemmo.url=${SEEMMO_URL:-NULL}
elif [ "proxy" = "${PROFILES_ACTIVE}" ]; then
    echo "Env [PROFILES_ACTIVE] is ${PROFILES_ACTIVE}, start ftp proxy"
    java -jar /app.jar \
    --spring.profiles.active=${PROFILES_ACTIVE} \
    --spring.cloud.config.enabled=false \
    --zk.host=${ZOOKEEPER_HOST:-NULL} \
    --ftp.ip=${HOST_IP} \
    --ftp.hostname=${HOST_NAME} \
    --eureka.ip=${EUREKA_IP} \
    --eureka.port=${EUREKA_PORT}
elif [ "local" = "${PROFILES_ACTIVE}" ]; then
    echo "Env [PROFILES_ACTIVE] is ${PROFILES_ACTIVE}, start local ftpserver"
    exec java -jar /app.jar \
    --spring.profiles.active=${PROFILES_ACTIVE} \
    --spring.cloud.config.enabled=false \
    --zk.host=${ZOOKEEPER_HOST:-NULL} \
    --ftp.ip=${HOST_IP} \
    --ftp.hostname=${HOST_NAME} \
    --kafka.host=${KAFKA_HOST:-NULL} \
    --seemmo.url=${SEEMMO_URL:-NULL} \
    --eureka.ip=${EUREKA_IP:-NULL} \
    --eureka.port=${EUREKA_PORT:-NULL}
fi