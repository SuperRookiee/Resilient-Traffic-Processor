# Build Spring Boot Processor
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /processor

# Copy the entire processor project so the Gradle wrapper and sources are available
COPY processor/ ./

# Use sh explicitly to avoid exec issues if the wrapper is missing execute bits
RUN chmod +x ./gradlew \
    && sh ./gradlew clean bootJar --no-daemon

# Final runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy Spring Boot JAR
COPY --from=builder /processor/build/libs/*.jar /app/processor.jar

# Copy K6 script
COPY third-party/k6-load-test.js /app/k6-load-test.js

# Copy entrypoint
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
CMD ["processor"]
