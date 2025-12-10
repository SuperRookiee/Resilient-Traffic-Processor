# 트래픽 처리 과제 스켈레톤 (정답 포함 버전)

이 저장소는 테스터에게 줄 "대용량 트래픽 처리" 실습용 Spring Boot + Kotlin 스켈레톤입니다. `/process` API가 1분간 k6로 폭발적인 트래픽을 받도록 설계되었으며, **Service 단 구현이 핵심**입니다. 이 버전에는 이해를 돕기 위한 정답 코드가 포함되어 있으니, 테스터에게 전달할 때는 `ExternalApiService`의 정답 구현을 삭제하고 `NotImplementedError()`로 교체해야 합니다.

## 프로젝트 구조
```
src
└ main
    └ kotlin
        └ com.example.processor
            ├ Application.kt
            ├ controller/ProcessingController.kt
            ├ service/ExternalApiService.kt
            ├ model/ProcessingReport.kt
            └ model/RequestResult.kt
```

## 빠른 시작
```bash
./gradlew bootRun
```
- 실행 후 `http://localhost:8080/process?targetUrl=https://example.com` 으로 접근하면 비동기 GET을 여러 번 수행하고 집계 리포트를 JSON으로 돌려줍니다.

## k6 부하 테스트 예시
아래 예시는 1분 동안 300 VU로 `/process` API를 때려서 얼마나 견디는지 확인합니다.
```bash
k6 run --vus 300 --duration 1m - <(cat <<'K6'
import http from 'k6/http';
import { check, sleep } from 'k6';

export default function () {
  const res = http.get('http://localhost:8080/process?targetUrl=https://example.com');
  check(res, { 'status 200': (r) => r.status === 200 });
  sleep(1);
}
K6
)
```

## 과제 안내 (테스터용)
- `/process` 엔드포인트는 초당 다수의 외부 호출을 병렬로 날리고, 성공/실패와 정확도를 `ProcessingReport`로 반환합니다.
- **Service 단 구현이 핵심**이며, 컨트롤러는 파라미터 전달만 담당합니다.
- 정답 코드가 포함된 `ExternalApiService` 상단의 `// TODO` 주석을 참고하여, 테스터에게 전달할 때는 정답 구현을 삭제하고 `NotImplementedError()`로 바꿔주세요.

## 주의
- 코드 전반에 한국어 주석이 포함되어 있어 학습과 수정에 용이합니다.
- k6나 기타 부하 도구로 트래픽을 줄 때, 대상 서버에 적절한 허용을 받았는지 확인하세요.
