# 使用轻量级 JRE 作为基础镜像
FROM eclipse-temurin:21-jre

EXPOSE 8100

ARG APP_NAME=mg-esp32-server

# 复制静态 FFmpeg 到系统路径
COPY ffmpeg-static/ /usr/local/bin/

# 设置工作目录
WORKDIR /app

# 复制应用程序JAR文件到工作目录
COPY app/${APP_NAME}.jar /app/

# 复制模型文件
COPY app/models /app/models

# 设置启动命令
RUN echo '#!/bin/bash\n\
echo "Starting application: ${APP_NAME}"\n\
java -Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -jar /app/${APP_NAME}.jar\n\
' > /app/start.sh && chmod +x /app/start.sh

# 使用 bash 执行启动脚本
CMD ["/bin/bash", "/app/start.sh"]
