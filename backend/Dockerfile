FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline -DskipTests

COPY src/ src/

RUN ./mvnw -B clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

ENV QR_UPLOAD_PATH=/app/output

RUN groupadd --system appgroup \
    && useradd --system \
        --gid appgroup \
        --home-dir /app \
        --shell /usr/sbin/nologin \
        appuser \
    && mkdir -p /app/output \
    && chown -R appuser:appgroup /app

COPY --from=build \
     --chown=appuser:appgroup \
     /app/target/*.jar \
     /app/app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=3 \
    CMD curl --fail --silent --show-error http://127.0.0.1:8080/actuator/health/app > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]