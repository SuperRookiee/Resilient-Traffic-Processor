# Resilient-Traffic-Processor

탄력적으로 외부 트래픽을 처리하기 위한 Spring Boot 서비스와 이를 검증하기 위한 k6 부하 테스트 스크립트를 함께 제공합니다. Resilience4j로 재시도·폴백을 적용해 안정적으로 URL을 호출하고, 처리 결과를 한국어 리포트 형태로 반환합니다.

## 프로젝트 구성
- `processor/`: Kotlin 기반 Spring Boot 애플리케이션. 외부 URL 호출 시 재시도와 폴백을 적용하고, 처리 통계를 담은 `ProcessingReport`를 한국어 키로 반환합니다.
- `third-party/`: k6 부하 테스트 스크립트와 Dockerfile. 설정(동시 가상 사용자 수와 테스트 지속 시간)은 JSDoc으로 명시되어 있어 빠르게 조정할 수 있습니다.
- `docker-compose.yml`: 프로세서와 k6 컨테이너를 함께 올려 일관된 환경에서 테스트할 수 있습니다.

## 동작 개요
- **얼마나?** 기본 설정으로 가상 사용자 300명이 동시에 요청을 보냅니다.
- **얼마 동안?** 각 부하 테스트 실행은 60초 동안 지속됩니다.
- **무엇을?** `/process` 엔드포인트에 대상 URL을 전달하여 외부 호출을 수행하고, 성공/실패·재시도 횟수·처리량이 포함된 한국어 `ProcessingReport`를 응답 본문에 포함합니다.

## Docker로 실행하기
1. 컨테이너 빌드 및 실행:
   ```bash
   docker compose up --build
   ```

2. 스택이 올라오면 프로세서 API는 `http://localhost:8080/process?url=<호출할_URL>` 로 접근할 수 있습니다.

3. 종료 및 정리:
   ```bash
   docker compose down
   ```

## 부하 테스트 스크립트 수정하기
`third-party/k6-load-test.js`에서 JSDoc으로 표시된 `options` 설정을 변경하면 부하 강도와 시간을 조정할 수 있습니다. 스크립트에는 한국어 주석이 포함되어 있어 테스트 시나리오를 쉽게 파악할 수 있습니다.
