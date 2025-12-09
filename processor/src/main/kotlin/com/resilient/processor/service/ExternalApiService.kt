package com.resilient.processor.service

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import io.github.resilience4j.timelimiter.annotation.TimeLimiter
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class ExternalApiService(private val webClientBuilder: WebClient.Builder) {

    private val logger = LoggerFactory.getLogger(ExternalApiService::class.java)
    private val webClient: WebClient by lazy { webClientBuilder.build() }

    @CircuitBreaker(name = "externalApi", fallbackMethod = "fallbackResponse")
    @Retry(name = "externalApi")
    @TimeLimiter(name = "externalApi")
    fun processUrl(url: String): Mono<ResponseEntity<Map<String, Any>>> {
        logger.info("Processing external request for URL: {}", url)
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { body ->
                ResponseEntity.ok(
                    mapOf(
                        "requestedUrl" to url,
                        "echo" to "Processed request for $url",
                        "responseBody" to body
                    )
                )
            }
    }

    @Suppress("unused")
    private fun fallbackResponse(url: String, ex: Throwable): Mono<ResponseEntity<Map<String, Any>>> {
        logger.error("Fallback triggered for URL: {} due to: {}", url, ex.message)
        return Mono.just(
            ResponseEntity.ok(
                mapOf(
                    "requestedUrl" to url,
                    "echo" to "Processed request for $url",
                    "fallback" to true,
                    "error" to (ex.message ?: "Unknown error")
                )
            )
        )
    }
}
