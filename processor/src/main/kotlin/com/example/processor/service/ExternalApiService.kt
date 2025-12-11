package com.example.processor.service

import com.example.processor.config.ProcessingProperties
import com.example.processor.model.ProcessingReport
import com.example.processor.model.RequestResult
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class ExternalApiService(
    private val webClient: WebClient,
    private val processingProperties: ProcessingProperties
) {

    private val logger = LoggerFactory.getLogger(ExternalApiService::class.java)

    suspend fun process(targetUrl: String): ProcessingReport {
        val totalRequests = processingProperties.totalRequests
        val parallelism = processingProperties.parallelism
        val startedAt = System.currentTimeMillis()

        logger.info("targetUrl={} 으로 {}건의 비동기 요청을 시작합니다. (parallelism={})", targetUrl, totalRequests, parallelism)

        val results: List<RequestResult> = Flux.range(1, totalRequests)
            .onBackpressureBuffer(totalRequests)
            .flatMap({ index -> performSingleRequest(targetUrl, index) }, parallelism)
            .collectList()
            .awaitSingle()

        val duration = System.currentTimeMillis() - startedAt
        val successCount = results.count { it.success }
        val failureCount = results.size - successCount
        val accuracy = if (results.isNotEmpty()) successCount.toDouble() / results.size * 100 else 0.0

        logger.info(
            "총 {}건 중 성공 {}건, 실패 {}건, 정확도 {}% ({}ms)",
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

    private fun performSingleRequest(targetUrl: String, index: Int): Mono<RequestResult> {
        val startedAt = System.nanoTime()
        return webClient.get()
            .uri(targetUrl)
            .exchangeToMono { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("")
                    .timeout(Duration.ofSeconds(10))
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
