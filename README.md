# Resilient-Traffic-Processor

Spring Boot service for resilient external request processing with accompanying k6 load testing utilities.

## Project structure

- `processor/`: Kotlin/Spring Boot application using Resilience4j to call external URLs safely.
- `third-party/`: k6-based load generator to exercise the processor service.
- `docker-compose.yml`: Spins up the processor and load test containers together.

## Running with Docker

The repository includes a `docker-compose.yml` file that builds and launches both the processor service and the k6 load generator.

1. Build and start the containers:

   ```bash
   docker compose up --build
   ```

2. Once the stack is running, the processor API is available at `http://localhost:8080`.

3. Stop and clean up the containers when finished:

   ```bash
   docker compose down
   ```
