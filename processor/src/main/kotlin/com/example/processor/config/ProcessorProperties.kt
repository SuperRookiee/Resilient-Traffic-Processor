package com.example.processor.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ProcessorProperties::class)
class ProcessorPropertiesConfiguration

@ConfigurationProperties(prefix = "processor")
data class ProcessorProperties(
    val targetUrl: String = ""
)
