package com.resilient.processor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

/**
 * 애플리케이션 진입점과 공용 빈을 정의하는 클래스.
 */
@SpringBootApplication
class ProcessorApplication {
    /** WebClient 빈을 생성하여 외부 요청에 사용한다. */
    @Bean
    fun webClientBuilder(): WebClient.Builder = WebClient.builder()
}

/**
 * 스프링 부트를 실행하는 메인 함수.
 */
fun main(args: Array<String>) {
    runApplication<ProcessorApplication>(*args)
}
