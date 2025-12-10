package com.example.processor.model

/**
 * 단일 요청의 결과를 표현하는 DTO.
 * latency, 상태 코드, 에러 메시지 등을 포함하여 k6 결과와 대조하기 쉽다.
 */
data class RequestResult(
    val index: Int,
    val success: Boolean,
    val statusCode: Int?,
    val latencyMillis: Long,
    val errorMessage: String?
)
