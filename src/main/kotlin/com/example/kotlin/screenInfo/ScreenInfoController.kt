package com.example.kotlin.screenInfo

import com.example.kotlin.performance.Performance
import com.example.kotlin.venue.Venue
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

    @GetMapping("/list/{venueId}/{performanceId}")
    fun screenInfoList(@PathVariable("venueId") venueId: Long, @PathVariable("performanceId") performanceId: Long): List<ScreenInfoResponse> {
        return screenInfoService.screenList(venueId, performanceId)
    }
}