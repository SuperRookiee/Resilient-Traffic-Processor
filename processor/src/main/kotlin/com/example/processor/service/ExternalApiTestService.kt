package com.example.processor.service

import com.example.processor.model.ProcessingReport
import com.example.processor.model.RequestResult
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * k6 부하 테스트를 손쉽게 실행할 수 있는 샘플 Service.
 * ExternalApiService의 로직을 단순화해 기본 흐름만 남기고, 목표 시간/정확도 체크 로깅을 추가했다.
 */
@Service
class ExternalApiTestService(
    private val webClientBuilder: WebClient.Builder
) {

    private val logger = LoggerFactory.getLogger(ExternalApiTestService::class.java)
    private val webClient: WebClient by lazy { webClientBuilder.build() }

    private val defaultRequestCount = 200          // k6에서 만들어낼 요청 수를 넉넉히 확보
    private val parallelism = 20                   // 동시에 처리할 병렬 갯수
    private val goalDurationMillis = 2_000L        // 목표 시간(ms) - 초과 시 경고
    private val accuracyTargetPercent = 99.0       // 성공률 목표 - 미만 시 경고

    /**
     * targetUrl로 외부 API를 호출하고 결과를 집계하는 샘플 구현.
     * - 요청 수와 병렬도는 내부 상수(defaultRequestCount, parallelism)를 조정해 k6 부하를 쉽게 늘릴 수 있다.
     * - 실행이 오래 걸리거나 오류가 발생하는 상황을 로깅으로 바로 확인할 수 있다.
     * - 목표 시간, 정확도 기준을 만족하는지 체크하여 k6 시나리오가 "터지는" 상태를 직관적으로 보여준다.
     */
    suspend fun process(targetUrl: String): ProcessingReport {
        val startedAt = System.currentTimeMillis()
        logger.info(
            "k6 부하 샘플 실행 - targetUrl={} | 요청 {}건, 병렬 {}건 (목표: {}ms, 정확도 {}%)",
            targetUrl, defaultRequestCount, parallelism, goalDurationMillis, accuracyTargetPercent
        )

        val results: List<RequestResult> = Flux.range(1, defaultRequestCount)
            .flatMap({ index -> performSingleRequest(targetUrl, index) }, parallelism)
            .collectList()
            .awaitSingle()

        val duration = System.currentTimeMillis() - startedAt
        val successCount = results.count { it.success }
        val failureCount = results.size - successCount
        val accuracy = if (results.isNotEmpty()) successCount.toDouble() / results.size * 100 else 0.0

        if (duration > goalDurationMillis) {
            logger.warn(
                "요청 처리 시간이 목표({}ms)를 초과했습니다. 실제 부하에서는 타임아웃/장애 가능성이 높습니다.",
                goalDurationMillis
            )
        } else {
            logger.info("목표 시간({}ms) 내에 응답을 모두 수집했습니다.", goalDurationMillis)
        }

        if (accuracy < accuracyTargetPercent) {
            logger.warn(
                "정확도가 {}% 미만입니다 (현재: {}%). 응답 누락 또는 오류가 발생했을 수 있습니다.",
                accuracyTargetPercent, String.format("%.2f", accuracy)
            )
        } else {
            logger.info("정확도 {}% 충족 (현재: {}%).", accuracyTargetPercent, String.format("%.2f", accuracy))
        }

        return ProcessingReport(
            totalRequests = results.size,
            successCount = successCount,
            failureCount = failureCount,
            accuracy = accuracy,
            durationMillis = duration,
            requestResults = results
        )
    }

    private fun performSingleRequest(targetUrl: String, index: Int): Mono<RequestResult> {
        val startedAt = System.nanoTime()
        return webClient.get()
            .uri(targetUrl)
            .exchangeToMono { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("")
                    .map {
                        val latency = Duration.ofNanos(System.nanoTime() - startedAt).toMillis()
                        RequestResult(
                            index = index,
                            success = response.statusCode().is2xxSuccessful,
                            statusCode = response.statusCode().value(),
                            latencyMillis = latency,
                            errorMessage = null
                        )
                    }
            }
            .onErrorResume { ex ->
                val latency = Duration.ofNanos(System.nanoTime() - startedAt).toMillis()
                logger.warn("요청 #{} 실패 - 이유: {}", index, ex.message)
                Mono.just(
                    RequestResult(
                        index = index,
                        success = false,
                        statusCode = null,
                        latencyMillis = latency,
                        errorMessage = ex.message
                    )
                )
            }
    }
}
