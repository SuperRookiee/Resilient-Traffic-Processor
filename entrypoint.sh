#!/usr/bin/env bash
set -euo pipefail

command=${1:-processor}
shift || true

case "$command" in
  processor)
    exec java -jar /app/processor.jar
    ;;
  k6|third-party|load-generator)
    exec k6 run /app/k6-load-test.js "$@"
    ;;
  *)
    exec "$command" "$@"
    ;;
esac
