package com.example.kotlin.performance

import com.example.kotlin.config.Loggable
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/performance")
@RestController
class PerformanceController(
    private val performanceService: PerformanceService
): Loggable {

    @PostMapping("/register")
    fun registerPerformance(@RequestBody performanceRequest: PerformanceRequest): Performance {
        return performanceService.registerPerformance(performanceRequest)
    }

    @DeleteMapping("/delete/{performanceId}")
    fun deletePerformance(@PathVariable("performanceId") performanceId: Long) {
        performanceService.deletePerformance(performanceId)

    }

    @GetMapping("/list/{id}")
    fun performanceList(@PathVariable("id") venueId: Long): List<PerformanceResponse> {

        return performanceService.performanceList(venueId)
    }
}