package com.example.kotlin.screenInfo

import com.example.kotlin.performance.Performance
import com.example.kotlin.performance.ScreenInfoListResponse
import com.example.kotlin.place.Place
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime

@RequestMapping("/screenInfo")
@RestController
class ScreenInfoController(
    private val screenInfoService: ScreenInfoService
) {

    @PostMapping("/register")
    fun registerScreen(@RequestBody screenInfoRequest: ScreenInfoRequest) {
        screenInfoService.registerScreen(screenInfoRequest)
    }

    @GetMapping("/list/{placeId}/{performanceId}")
    fun screenInfoList(@PathVariable("placeId") placeId: Long, @PathVariable("performanceId") performanceId: Long): List<ScreenInfoResponse> {
        return screenInfoService.screenList(placeId, performanceId)
    }
}

data class ScreenInfoRequest(
    val placeId: Long,

    val performanceId: Long,

    val screeningDate: LocalDate,

    val startTime: LocalDateTime,

    val endTime: LocalDateTime
) {
    fun toScreen(place: Place, performance: Performance): ScreenInfo {
        return ScreenInfo (
            place = place,
            performance = performance,
            screeningDate = this.screeningDate,
            startTime = this.startTime,
            endTime = this.endTime
        )
    }
}