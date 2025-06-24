package com.example.kotlin.screenInfo

import com.example.kotlin.performance.Performance
import com.example.kotlin.performance.PerformanceResponse
import java.time.LocalDate
import java.time.LocalDateTime

data class ScreenInfoResponse(
    val id: Long? = null,

    val performance: PerformanceResponse,

    val screeningDate: LocalDate,

    val startTime: LocalDateTime,

    val endTime: LocalDateTime
)