package com.resilient.processor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ResilientTrafficProcessorApplication

fun main(args: Array<String>) {
	runApplication<ResilientTrafficProcessorApplication>(*args)
}
