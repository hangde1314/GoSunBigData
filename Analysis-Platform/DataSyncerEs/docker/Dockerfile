#指定镜像
FROM registry.cn-hangzhou.aliyuncs.com/hzgc/java-all:8

#操作文件
COPY docker-entrypoint.sh /
COPY data-syncer.jar /app.jar

#执行命令
RUN chmod +x /docker-entrypoint.sh && /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

#初始化参数并启动Springboot应用
ENTRYPOINT ["/bin/sh","docker-entrypoint.sh"]
