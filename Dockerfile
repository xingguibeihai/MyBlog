
# 第一阶段：使用 Maven 编译打包
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
# 复制 pom.xml 下载依赖（利用缓存加速）
COPY pom.xml .
RUN mvn dependency:go-offline
# 复制全部源代码（包括你的 html 静态页面）
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：使用轻量级 JRE 运行
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# 设置时区为亚洲/上海
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone
# 提取第一阶段打包好的 jar 包
COPY --from=builder /app/target/*.jar app.jar
# 暴露端口
EXPOSE 8080
# 启动项目
ENTRYPOINT ["java", "-jar", "app.jar"]
