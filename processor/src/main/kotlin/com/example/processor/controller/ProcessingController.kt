package com.example.processor.controller

import com.example.processor.config.ProcessorProperties
import com.example.processor.model.ProcessingReport
import com.example.processor.service.ExternalApiService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * /process 엔드포인트를 노출하는 컨트롤러.
 * 설정된 기본 targetUrl을 Service 단에 넘겨 집계 리포트를 반환한다.
 */
@RestController
class ProcessingController(
    private val externalApiService: ExternalApiService,
    private val processorProperties: ProcessorProperties
) {

    /**
     * GET /process
     * 설정 파일의 targetUrl로 비동기 GET을 수행한 뒤 집계 리포트를 반환한다.
     */
    @GetMapping("/process")
    suspend fun process(): ProcessingReport =
        externalApiService.process(processorProperties.targetUrl)
}
