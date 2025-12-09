package com.resilient.processor.model

/**
 * 외부 API 처리 결과를 요약하는 리포트 데이터 클래스.
 * 요청 URL, 처리한 데이터 크기, 처리 속도, 재시도 횟수 및 성공/실패 횟수를 담는다.
 */
data class ProcessingReport(
    val requestedUrl: String,
    val dataSizeBytes: Int,
    val durationMillis: Long,
    val throughputBytesPerSecond: Double,
    val retryAttempts: Int,
    val successCount: Int,
    val failureCount: Int,
    val fallbackUsed: Boolean
)
