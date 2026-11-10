# syntax=docker/dockerfile:1
# --- Stage 1: build ---
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties lombok.config ./
COPY domain ./domain
COPY infrastructure ./infrastructure
COPY application ./application

RUN chmod +x gradlew && ./gradlew :application:app-service:bootJar --no-daemon

# --- Stage 2: runtime (non-root) ---
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/application/app-service/build/libs/*.jar app.jar
USER app

EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["java", "-jar", "app.jar"]
