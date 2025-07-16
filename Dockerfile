# 使用轻量级 JRE 作为基础镜像
FROM eclipse-temurin:21-jre

EXPOSE 8100

# 复制静态 FFmpeg 到系统路径
COPY ffmpeg-static/ /usr/local/bin/

# 设置工作目录
WORKDIR /app

# 复制应用程序JAR文件到工作目录
COPY app/mg-esp32-server.jar /app/

# 复制模型文件
COPY app/models /app/models


# 设置启动命令
RUN echo '#!/bin/bash\n\
echo "Starting application version: ${APP_VERSION}"\n\
java -Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -jar /app/mg-esp32-server.jar\n\
' > /app/start.sh && chmod +x /app/start.sh

# 使用 bash 执行启动脚本
CMD ["/bin/bash", "/app/start.sh"]
