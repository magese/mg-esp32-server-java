# 使用轻量级 JRE 作为基础镜像
FROM eclipse-temurin:21-jre

EXPOSE 8100

# 复制静态 FFmpeg 到系统路径
COPY ffmpeg-static/ /usr/local/bin/

# 验证 FFmpeg 安装
RUN chmod +x /usr/local/bin/ffmpeg && \
    ffmpeg -version

# 设置工作目录
WORKDIR /app

# 复制应用程序JAR文件到工作目录
COPY app/mg-esp32-server.jar /app/

# 复制模型文件
COPY app/models /app/models

# 设置启动命令
CMD ["java", "-Xms512m", "-Xmx1024m", "-jar", "/app/mg-esp32-server.jar"]
