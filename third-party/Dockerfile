FROM grafana/k6:latest
WORKDIR /src
COPY k6-load-test.js ./k6-load-test.js
ENTRYPOINT ["k6", "run", "/src/k6-load-test.js"]
