#!/bin/bash

# ==============================
# 配置区
# ==============================
DOCKERHUB_USERNAME="pfz16"
APP_NAME="magic-bag-mono"
TAG="${1:-latest}"

# ==============================
# 检查依赖
# ==============================
if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装，请先安装 Docker"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven 未安装，请先安装 Maven（或使用已构建的 JAR）"
    exit 1
fi

# ==============================
# 1. 构建 Spring Boot 项目
# ==============================
echo "📦 正在构建 Spring Boot 项目..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Maven 构建失败！"
    exit 1
fi

JAR_FILE=$(find target -name "*.jar" | head -n 1)
if [ -z "$JAR_FILE" ]; then
    echo "❌ 未找到 JAR 文件，请确保 Maven 构建成功"
    exit 1
fi
echo "✅ 找到 JAR 文件: $JAR_FILE"

# ==============================
# 2. 使用 buildx 构建并直接推送 linux/amd64 镜像
# ==============================
IMAGE_NAME="$DOCKERHUB_USERNAME/$APP_NAME:$TAG"
echo "🚀 正在构建并推送 linux/amd64 镜像: $IMAGE_NAME"

# 确保 buildx 已启用（可选，现代 Docker 通常已支持）
docker buildx create --use --name amd64-builder >/dev/null 2>&1 || true

# 构建并推送（关键：--push 会直接上传，不加载到本地）
docker buildx build \
  --platform linux/amd64 \
  -t "$IMAGE_NAME" \
  --push \
  .

if [ $? -ne 0 ]; then
    echo "❌ 构建或推送失败！"
    exit 1
fi

# ==============================
# 完成
# ==============================
echo "🎉 镜像已成功推送到 Docker Hub！"
echo "🔗 拉取命令: docker pull $IMAGE_NAME"
echo "📌 标签: $TAG"
echo "✅ 该镜像可在 Ubuntu (linux/amd64) 上正常运行"