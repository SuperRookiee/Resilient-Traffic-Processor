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
 * 테스터가 구현해야 하는 핵심 Service.
 * 이번 버전은 정답 코드가 포함되어 있으며, 테스터에게 줄 때는 주석을 읽고 정답을 제거해야 한다.
 */
@Service
class ExternalApiService(
    private val webClientBuilder: WebClient.Builder
) {

    private val logger = LoggerFactory.getLogger(ExternalApiService::class.java)
    private val webClient: WebClient by lazy { webClientBuilder.build() }

    // TODO: 테스터에게 줄 때는 이 정답 코드를 제거하고 NotImplementedError()로 바꿔야 합니다.
    /**
     * targetUrl로 다수의 비동기 GET 요청을 보내고 통계를 생성한다.
     * - 총 요청 수는 service 내부에서 결정하며, 컨트롤러는 로직에 관여하지 않는다.
     * - WebClient + Reactor를 활용하여 높은 동시성으로 호출한다.
     */
    suspend fun process(targetUrl: String): ProcessingReport {
        val totalRequests = 50                    // 한 번 호출 시 발사할 전체 요청 수 (필요에 따라 조절 가능)
        val parallelism = 10                     // 동시에 처리할 요청 수
        val startedAt = System.currentTimeMillis()

        logger.info("targetUrl={} 으로 {}건의 비동기 요청을 시작합니다.", targetUrl, totalRequests)

        // Flux로 총 요청 수만큼 range를 만들고 flatMap으로 비동기 호출 실행
        val results: List<RequestResult> = Flux.range(1, totalRequests)
            .flatMap({ index ->
                performSingleRequest(targetUrl, index)
            }, parallelism)
            .collectList()
            .awaitSingle() // 코루틴 친화적으로 block 없이 기다림

        val duration = System.currentTimeMillis() - startedAt
        val successCount = results.count { it.success }
        val failureCount = results.size - successCount
        val accuracy = if (results.isNotEmpty()) successCount.toDouble() / results.size * 100 else 0.0

        logger.info(
            "총 {}건 중 성공 {}건, 실패 {}건, 정확도 {}% ({}ms)" ,
            results.size, successCount, failureCount, String.format("%.2f", accuracy), duration
        )

        return ProcessingReport(
            totalRequests = results.size,
            successCount = successCount,
            failureCount = failureCount,
            accuracy = accuracy,
            durationMillis = duration,
            requestResults = results
        )
    }

    /**
     * 단일 요청을 수행하고 latency, 상태코드 등을 기록한다.
     * 네트워크 예외가 발생해도 실패 응답으로 감싸어 리스트에 누적된다.
     */
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
