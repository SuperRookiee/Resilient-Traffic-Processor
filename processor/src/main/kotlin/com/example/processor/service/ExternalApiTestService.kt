package com.example.processor.service

import com.example.processor.model.ProcessingReport
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 테스터에게 제공되는 비어 있는 Service.
 * ExternalApiService의 process 구현을 참고하여 직접 작성해야 한다.
 */
@Service
class ExternalApiTestService {

    private val logger = LoggerFactory.getLogger(ExternalApiTestService::class.java)

    /**
     * targetUrl로 외부 API를 호출하고 결과를 집계하는 로직을 직접 구현한다.
     * 현재는 템플릿 상태이며, NotImplementedError를 던진다.
     */
    suspend fun process(targetUrl: String): ProcessingReport {
        logger.info("ExternalApiTestService.process 호출 - targetUrl={} (구현 필요)", targetUrl)
        throw NotImplementedError("테스터가 구현해야 합니다.")
    }
}
