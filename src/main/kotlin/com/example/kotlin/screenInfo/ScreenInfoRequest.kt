package com.example.kotlin.screenInfo

import com.example.kotlin.performance.Performance
import com.example.kotlin.venue.Venue
import java.time.LocalDate
import java.time.LocalDateTime

data class ScreenInfoRequest(
    val venueId: Long,

    val performanceId: Long,

    val screeningDate: LocalDate,

    val startTime: LocalDateTime,

    val endTime: LocalDateTime
) {
    fun toScreen(venue: Venue, performance: Performance): ScreenInfo {
        return ScreenInfo (
            venue = venue,
            performance = performance,
            screeningDate = this.screeningDate,
            startTime = this.startTime,
            endTime = this.endTime
        )
    }
}