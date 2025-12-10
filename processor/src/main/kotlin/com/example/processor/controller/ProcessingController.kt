package com.example.processor.controller

import com.example.processor.model.ProcessingReport
import com.example.processor.service.ExternalApiService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * /process 엔드포인트를 노출하는 컨트롤러.
 * targetUrl 파라미터를 받아 Service 단에 위임하고, 계산된 리포트를 그대로 반환한다.
 */
@RestController
class ProcessingController(
    private val externalApiService: ExternalApiService
) {

    /**
     * GET /process
     * targetUrl로 다수의 비동기 GET을 수행한 뒤 집계 리포트를 반환한다.
     */
    @GetMapping("/process")
    suspend fun process(@RequestParam targetUrl: String): ProcessingReport {
        return externalApiService.process(targetUrl)
    }
}
