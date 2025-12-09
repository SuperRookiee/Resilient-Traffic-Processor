package com.resilient.processor.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 외부 API 처리 결과를 요약하는 리포트 데이터 클래스.
 * 요청 URL, 처리한 데이터 크기, 처리 속도, 재시도 횟수 및 성공/실패 횟수를 담는다.
 */
data class ProcessingReport(
    @JsonProperty("요청_URL")
    val requestedUrl: String,

    @JsonProperty("데이터_크기_바이트")
    val dataSizeBytes: Int,

    @JsonProperty("처리_시간_밀리초")
    val durationMillis: Long,

    @JsonProperty("초당_처리량_Bps")
    val throughputBytesPerSecond: Double,

    @JsonProperty("재시도_횟수")
    val retryAttempts: Int,

    @JsonProperty("성공_횟수")
    val successCount: Int,

    @JsonProperty("실패_횟수")
    val failureCount: Int,

    @JsonProperty("폴백_사용")
    val fallbackUsed: Boolean
)
