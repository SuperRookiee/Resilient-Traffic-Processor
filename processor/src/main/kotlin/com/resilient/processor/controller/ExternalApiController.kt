package com.resilient.processor.controller

import com.resilient.processor.service.ExternalApiService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class ExternalApiController(private val externalApiService: ExternalApiService) {

    private val logger = LoggerFactory.getLogger(ExternalApiController::class.java)

    @GetMapping("/process")
    fun process(@RequestParam url: String): Mono<ResponseEntity<Map<String, Any>>> {
        logger.info("Received request to process external URL: {}", url)
        return externalApiService.processUrl(url)
    }
}
