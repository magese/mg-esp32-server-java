# 使用轻量级 JRE 作为基础镜像
FROM eclipse-temurin:21-jre

# 复制静态 FFmpeg 到系统路径
COPY ffmpeg-static/ /usr/local/bin/

# 验证 FFmpeg 安装
RUN chmod +x /usr/local/bin/ffmpeg && \
    ffmpeg -version

# 设置工作目录
WORKDIR /app

# 设置启动命令
CMD ["java", "-Xms512m", "-Xmx1024m", "-jar", "/app/mg-esp32-server.jar"]
