package com.resilient.processor.controller

import com.resilient.processor.service.ExternalApiService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * 외부 URL을 전달받아 처리 서비스에 위임하는 컨트롤러.
 */
@RestController
class ExternalApiController(private val externalApiService: ExternalApiService) {

    /** 요청 처리 로그를 위한 로거. */
    private val logger = LoggerFactory.getLogger(ExternalApiController::class.java)

    /**
     * /process 엔드포인트에서 전달된 URL을 처리 서비스로 위임한다.
     */
    @GetMapping("/process")
    fun process(@RequestParam url: String): Mono<ResponseEntity<Map<String, Any>>> {
        logger.info("Received request to process external URL: {}", url)
        return externalApiService.processUrl(url)
    }
}
