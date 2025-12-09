package com.example.processor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
class ProcessorApplication {
    @Bean
    fun webClientBuilder(): WebClient.Builder = WebClient.builder()
}

fun main(args: Array<String>) {
    runApplication<ProcessorApplication>(*args)
}
