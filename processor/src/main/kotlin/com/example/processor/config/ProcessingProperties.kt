package com.example.processor.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ProcessingProperties::class)
class ProcessingPropertiesConfiguration

@ConfigurationProperties(prefix = "processing")
data class ProcessingProperties(
    val totalRequests: Int = 50,
    val parallelism: Int = 10
)
