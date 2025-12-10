package com.example.processor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

/**
 * 애플리케이션 진입점.
 * 테스터가 Service 단을 자유롭게 실험할 수 있도록 최소한의 빈만 등록한다.
 */
@SpringBootApplication
class Application {
    /**
     * WebClient.Builder 빈을 등록하여 외부 HTTP 호출 시 재사용한다.
     * 고성능 테스트를 대비해 커넥션을 재활용하도록 builder만 노출한다.
     */
    @Bean
    fun webClientBuilder(): WebClient.Builder = WebClient.builder()
}

/**
 * 스프링부트 메인 함수.
 */
fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
