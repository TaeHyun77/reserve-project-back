package com.example.kotlin.screenInfo

import com.example.kotlin.performance.Performance
import com.example.kotlin.place.Place
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/screenInfo")
@RestController
class ScreenInfoController(
    private val screenInfoService: ScreenInfoService
) {

    @PostMapping("/register")
    fun registerScreen(@RequestBody screenInfoRequest: ScreenInfoRequest) {
        screenInfoService.registerScreen(screenInfoRequest)
    }
}

data class ScreenInfoRequest(
    val placeId: Long,

    val performanceId: Long,

    val startTime: String,

    val endTime: String
) {
    fun toScreen(place: Place, performance: Performance): ScreenInfo {
        return ScreenInfo (
            place = place,
            performance = performance,
            startTime = this.startTime,
            endTime = this.startTime
        )
    }
}