package com.example.kotlin.performance

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/performance")
@RestController
class PerformanceController(
    private val performanceService: PerformanceService
) {

    @PostMapping("/register")
    fun registerPerformance(@RequestBody performanceRequest: PerformanceRequest) {
        performanceService.registerPerformance(performanceRequest)
    }
}

data class PerformanceRequest(
    val type: String,

    val title: String,

    val duration: String
) {
    fun toPerformance(): Performance {
        return Performance (
            type = this.type,
            title = this.title,
            duration = this.duration
        )
    }
}