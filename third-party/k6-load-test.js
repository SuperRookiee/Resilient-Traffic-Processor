import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 300,
  duration: '60s',
};

const targetUrl = 'http://processor:8080/process?url=http://example.com';

export default function () {
  const delay = Math.random() * 0.5; // up to 500ms delay
  sleep(delay);

  const res = http.get(targetUrl);

  // Introduce simulated client-side error rate for validation
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
