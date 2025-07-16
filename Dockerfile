# 使用轻量级 JRE 作为基础镜像
FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 设置启动命令
CMD ["java", "-Xms512m", "-Xmx1024m", "-jar", "/app/mg-esp32-server.jar"]
