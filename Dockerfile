# 构建阶段：使用存在的 Maven 镜像
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# 复制 pom.xml 并预下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B


# 运行阶段：轻量 JRE 镜像
FROM eclipse-temurin:21-jre-jammy

# 设置新加坡时区
ENV TZ=Asia/Singapore
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# 从构建阶段复制 jar
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
