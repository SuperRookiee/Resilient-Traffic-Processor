# Resilient Traffic Processor

This repository contains the Kotlin/Spring Boot **processor** service and the **third-party** k6 load generator used to exercise it. Use `docker-compose.yml` at the root to build and run the two containers together.

## Structure
- `processor/`: Spring Boot WebFlux service with Resilience4j-protected `/process` endpoint.
- `third-party/`: k6 load script and container definition that targets the processor service.
- `docker-compose.yml`: Compose file that builds both images and wires them on the default network.
