# 使用轻量级 JRE 作为基础镜像
FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 直接从构建上下文复制预构建的 JAR 文件
COPY target/mg-esp32-server-*.jar /app/mg-esp32-server.jar

# 创建模型目录并复制预置模型
RUN mkdir -p /app/models
COPY models/silero_vad.onnx /app/models/silero_vad.onnx

# 复制 Vosk 模型（根据大小参数）
COPY vosk_cache/vosk-model-cn-0.22 /app/models/vosk-model

# 设置启动命令
CMD ["java", "-Xms512m", "-Xmx1024m", "-jar", "/app/mg-esp32-server.jar"]
