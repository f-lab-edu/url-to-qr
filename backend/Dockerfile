FROM eclipse-temurin:21-jre

WORKDIR /app

RUN addgroup --system appgroup \
    && adduser --system --ingroup appgroup appuser

COPY ./url-to-qr-0.0.1-SNAPSHOT.jar app.jar

RUN mkdir -p /app/output \
    && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
