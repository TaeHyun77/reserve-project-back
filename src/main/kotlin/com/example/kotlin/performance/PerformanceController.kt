package com.example.kotlin.performance

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RequestMapping("/performance")
@RestController
class PerformanceController(
    private val performanceService: PerformanceService
) {

    @PostMapping("/register")
    fun registerPerformance(@RequestBody performanceRequest: PerformanceRequest) {
        performanceService.registerPerformance(performanceRequest)
    }

    @GetMapping("/list/{id}")
    fun performanceList(@PathVariable("id") placeId: Long): List<PerformanceResponse> {
        log.info { "요청" }

        return performanceService.performanceList(placeId)
    }
}

data class PerformanceRequest(
    val type: String,

    val title: String,

    val duration: String,

    val price: Long
) {
    fun toPerformance(): Performance {
        return Performance (
            type = this.type,
            title = this.title,
            duration = this.duration,
            price = this.price
        )
    }
}