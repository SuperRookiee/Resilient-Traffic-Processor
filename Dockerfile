# Build Spring Boot Processor
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /processor

# Copy Gradle wrapper and build files first to leverage Docker layer caching
COPY processor/gradlew ./gradlew
COPY processor/gradle ./gradle
COPY processor/build.gradle.kts processor/settings.gradle.kts ./

# Copy application sources
COPY processor/src ./src

# Normalize potential Windows line endings on the wrapper script before execution
RUN sed -i 's/\r$//' ./gradlew \
    && chmod +x ./gradlew \
    && ./gradlew clean bootJar --no-daemon

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
