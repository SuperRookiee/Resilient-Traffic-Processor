# Combined image that can run either the processor service or the k6 load generator
FROM eclipse-temurin:21-jdk AS processor-builder
WORKDIR /processor
COPY processor/ .
RUN ./gradlew clean bootJar --no-daemon

FROM grafana/k6:latest
RUN apk update \
    && apk add --no-cache openjdk21-jdk bash

WORKDIR /app
COPY --from=processor-builder /processor/build/libs/processor-0.0.1-SNAPSHOT.jar /app/processor.jar
COPY third-party/k6-load-test.js /app/k6-load-test.js
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/entrypoint.sh"]
CMD ["processor"]
