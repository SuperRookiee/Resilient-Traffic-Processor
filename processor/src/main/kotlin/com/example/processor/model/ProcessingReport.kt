package com.example.processor.model

/**
 * 외부 API 호출 집계 정보를 담는 DTO.
 * - 총 요청 수, 성공/실패, 정확도(%)와 전체 처리 시간을 함께 반환한다.
 * - 테스터가 성능 최적화를 시도할 때 바로 지표를 확인할 수 있도록 설계되었다.
 */
data class ProcessingReport(
    val totalRequests: Int,
    val successCount: Int,
    val failureCount: Int,
    val accuracy: Double,
    val durationMillis: Long,
    val requestResults: List<RequestResult>
)
