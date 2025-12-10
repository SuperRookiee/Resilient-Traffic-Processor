package com.resilient.processor.service

import com.resilient.processor.model.ProcessingReport
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.timelimiter.TimeLimiterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit

/**
 * 외부 API 호출을 수행하고 처리 결과를 리포트로 변환하는 서비스.
 */
@Service
class ExternalApiService(
    private val webClientBuilder: WebClient.Builder,
    @param:Value("\${processor.target-url}") private val targetUrl: String,
    circuitBreakerRegistry: CircuitBreakerRegistry,
    timeLimiterRegistry: TimeLimiterRegistry
) {

    /** 외부 호출 시 로그를 남기기 위한 로거. */
    private val logger = LoggerFactory.getLogger(ExternalApiService::class.java)

    /** 지연 초기화된 WebClient 인스턴스. */
    private val webClient: WebClient by lazy { webClientBuilder.build() }

    /** 회로 차단기와 타임리미터 인스턴스. */
    private val circuitBreaker = circuitBreakerRegistry.circuitBreaker("externalApi")
    private val timeLimiter = timeLimiterRegistry.timeLimiter("externalApi")

    /**
     * 설정된 기본 URL을 호출하여 응답 본문과 처리 리포트를 반환한다.
     * 재시도 횟수, 성공/실패 횟수, 처리 시간과 속도를 포함한 리포트를 생성한다.
     */
    fun processConfiguredUrl(): Mono<ResponseEntity<Map<String, Any>>> {
        val url = targetUrl
        // 처리 시간을 계산하기 위한 시작 시간
        val startTime = System.nanoTime()
        // 재시도, 성공, 실패 횟수를 추적하기 위한 카운터
        val retryAttempts = AtomicInteger(0)
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)
        val timeoutDuration = timeLimiter.timeLimiterConfig.timeoutDuration

        logger.info("외부 요청 처리 시작 - URL: {}", url)
        if (!circuitBreaker.tryAcquirePermission()) {
            failureCount.incrementAndGet()
            val notPermitted = CallNotPermittedException.createCallNotPermittedException(circuitBreaker)
            return buildFallbackResponse(url, notPermitted, startTime, retryAttempts, successCount, failureCount)
        }

        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
            // 성공할 때마다 성공 카운터 증가
            .doOnSuccess { successCount.incrementAndGet() }
            // 오류 발생 시 실패 카운터 증가
            .doOnError { failureCount.incrementAndGet() }
            // 설정된 타임리미터로 요청 시간을 제한
            .timeout(timeoutDuration)
            // 회로 차단기에 성공/실패 이벤트 전파
            .doOnSuccess {
                val elapsed = Duration.ofNanos(System.nanoTime() - startTime)
                circuitBreaker.onSuccess(elapsed.toNanos(), TimeUnit.NANOSECONDS)
            }
            .doOnError { ex ->
                val elapsed = Duration.ofNanos(System.nanoTime() - startTime)
                circuitBreaker.onError(elapsed.toNanos(), TimeUnit.NANOSECONDS, ex)
            }
            // 최대 3회까지 백오프 재시도하며 재시도 카운터 기록
            .retryWhen(
                Retry.backoff(3, Duration.ofMillis(300)).doBeforeRetry { signal ->
                    val attempt = retryAttempts.incrementAndGet()
                    logger.warn("재시도 {}회차 진행 - 이유: {}", attempt, signal.failure().message)
                }
            )
            // 성공적으로 응답을 받으면 리포트와 함께 ResponseEntity 반환
            .map { body -> buildSuccessResponse(url, body, startTime, retryAttempts, successCount, failureCount) }
            // 모든 재시도가 실패하면 폴백 응답 생성
            .onErrorResume { ex -> buildFallbackResponse(url, ex, startTime, retryAttempts, successCount, failureCount) }
    }

    /**
     * 성공 응답을 구성하고 처리 리포트를 생성한다.
     */
    private fun buildSuccessResponse(
        url: String,
        body: String,
        startTime: Long,
        retryAttempts: AtomicInteger,
        successCount: AtomicInteger,
        failureCount: AtomicInteger
    ): ResponseEntity<Map<String, Any>> {
        val durationMillis = Duration.ofNanos(System.nanoTime() - startTime).toMillis()
        val dataSizeBytes = body.toByteArray().size
        val throughput = calculateThroughput(dataSizeBytes, durationMillis)
        val report = ProcessingReport(
            requestedUrl = url,
            dataSizeBytes = dataSizeBytes,
            durationMillis = durationMillis,
            throughputBytesPerSecond = throughput,
            retryAttempts = retryAttempts.get(),
            successCount = successCount.get(),
            failureCount = failureCount.get(),
            fallbackUsed = false,
            circuitBreakerState = circuitBreaker.state.name
        )

        logger.info("성공 처리 리포트: {}", report)

        return ResponseEntity.ok(
            mapOf(
                "요청_URL" to url,
                "요청_메시지" to "요청을 성공적으로 처리했습니다: $url",
                "응답_본문" to body,
                "처리_리포트" to report
            )
        )
    }

    /**
     * 모든 재시도 실패 시 폴백 응답과 리포트를 생성한다.
     */
    private fun buildFallbackResponse(
        url: String,
        ex: Throwable,
        startTime: Long,
        retryAttempts: AtomicInteger,
        successCount: AtomicInteger,
        failureCount: AtomicInteger
    ): Mono<ResponseEntity<Map<String, Any>>> {
        val durationMillis = Duration.ofNanos(System.nanoTime() - startTime).toMillis()
        val report = ProcessingReport(
            requestedUrl = url,
            dataSizeBytes = 0,
            durationMillis = durationMillis,
            throughputBytesPerSecond = 0.0,
            retryAttempts = retryAttempts.get(),
            successCount = successCount.get(),
            failureCount = failureCount.get(),
            fallbackUsed = true,
            circuitBreakerState = circuitBreaker.state.name
        )

        logger.error("폴백 실행 - URL: {} / 이유: {}", url, ex.message)
        logger.info("실패 처리 리포트: {}", report)

        return Mono.just(
            ResponseEntity.ok(
                mapOf(
                    "요청_URL" to url,
                    "요청_메시지" to "요청 처리 중 문제가 발생했습니다: $url",
                    "폴백" to true,
                    "에러" to (ex.message ?: "알 수 없는 오류"),
                    "처리_리포트" to report
                )
            )
        )
    }

    /**
     * 데이터 크기와 처리 시간을 기반으로 초당 처리량(B/s)을 계산한다.
     */
    private fun calculateThroughput(dataSizeBytes: Int, durationMillis: Long): Double {
        if (durationMillis <= 0) return dataSizeBytes.toDouble()
        return dataSizeBytes / (durationMillis / 1000.0)
    }
}
