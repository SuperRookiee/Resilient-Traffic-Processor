# Resilient-Traffic-Processor

Spring Boot service for resilient external request processing with accompanying k6 load testing utilities.

## Project structure

- `processor/`: Kotlin/Spring Boot application using Resilience4j to call external URLs safely.
- `third-party/`: k6-based load generator to exercise the processor service.
- `docker-compose.yml`: Spins up the processor and load test containers together.
