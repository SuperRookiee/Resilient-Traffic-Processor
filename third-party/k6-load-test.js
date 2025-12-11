import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 300,
  duration: '60s',
  summaryTrendStats: [], // 기본 summary 제거
};

const targetUrl = 'http://processor:8080/process';

export default function () {
  const delay = Math.random() * 0.5;
  sleep(delay);

  const res = http.get(targetUrl);

  if (Math.random() < 0.05) {
    check(res, {
      'forced error': () => false,
    });
  } else {
    check(res, {
      'status is 2xx': (r) => r.status >= 200 && r.status < 300,
      'response has body': (r) => !!r.body,
    });
  }
}

export function handleSummary(data) {
  const totalSeconds = (data.state.testRunDurationMs / 1000).toFixed(2);
  const metrics = data.metrics ?? {};

  const httpReqs = metrics.http_reqs?.values?.count ?? 0;
  const httpReqsRate = metrics.http_reqs?.values?.rate ?? 0;

  const iterations = metrics.iterations?.values?.count ?? 0;

  const checkPasses = metrics.checks?.passes ?? 0;
  const checkFails = metrics.checks?.fails ?? 0;

  const latency = metrics.http_req_duration?.values ?? {};
  const latencyLine = [
    latency['p(50)'],
    latency['p(90)'],
    latency['p(95)'],
    latency['p(99)'],
  ].some((v) => v !== undefined)
    ? [
        `  p50: ${latency['p(50)']?.toFixed(2) ?? '-'} ms`,
        `  p90: ${latency['p(90)']?.toFixed(2) ?? '-'} ms`,
        `  p95: ${latency['p(95)']?.toFixed(2) ?? '-'} ms`,
        `  p99: ${latency['p(99)']?.toFixed(2) ?? '-'} ms`,
      ].join('\n')
    : '  (데이터 없음)';

  const httpErrors = metrics.http_req_failed?.values ?? {};
  const httpErrorRate = httpErrors.rate ?? 0;

  const header = [
    '██ 테스트 결과 요약',
    `전체 실행 시간: ${totalSeconds}초`,
  ].join('\n');

  const report = [
    header,
    '',
    '📌 주요 지표',
    `- 총 요청 수: ${httpReqs.toLocaleString()}회 (${httpReqsRate.toFixed(2)} req/s)`,
    `- 실행 반복(iterations): ${iterations.toLocaleString()}회`,
    `- 체크 결과: ${checkPasses.toLocaleString()} 성공 / ${checkFails.toLocaleString()} 실패`,
    `- HTTP 오류율: ${(httpErrorRate * 100).toFixed(2)}%`,
    '',
    '⏱ HTTP 응답 지연 (ms)',
    latencyLine,
    '',
    '📄 원본 데이터(JSON)',
    JSON.stringify(data, null, 2),
  ].join('\n');

  return {
    stdout: report,
  };
}