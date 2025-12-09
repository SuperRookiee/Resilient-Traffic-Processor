import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * k6 실행 옵션.
 * @type {{vus: number, duration: string}}
 * - vus: 동시에 동작할 가상 사용자의 수.
 * - duration: 테스트를 유지할 전체 시간.
 */
export const options = {
  vus: 300,
  duration: '60s',
};

const targetUrl = 'http://processor:8080/process?url=http://resilient.com';

export default function () {
  const delay = Math.random() * 0.5; // 최대 500ms까지 지연을 줘서 실제 사용자 변동을 흉내
  sleep(delay);

  const res = http.get(targetUrl);

  // 검증을 위해 클라이언트 오류를 일부러 발생시키는 구간
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

/**
 * textSummary 제거 버전
 * Docker / 기본 k6에서 100% 정상 동작
 */
export function handleSummary(data) {
  const totalSeconds = (data.state.testRunDurationMs / 1000).toFixed(2);

  const header = [
    '█ 총 결과',
    `전체 실행 시간: ${totalSeconds}초`,
  ].join('\n');

  // 기본 요약 JSON 출력
  const json = JSON.stringify(data, null, 2);

  return {
    stdout: `${header}\n\n요약(JSON):\n${json}`,
  };
}