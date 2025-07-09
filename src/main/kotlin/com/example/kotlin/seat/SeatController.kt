package com.example.kotlin.seat

import com.example.kotlin.config.Loggable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RequestMapping("/seat")
@RestController
class SeatController(
    private val seatService: SeatService
): Loggable {

    @PostMapping("/init")
    fun initSeats(@RequestBody seatRequest: SeatRequest) {
        seatService.initSeats(seatRequest)
    }

    @GetMapping("/price/{performanceId}")
    fun seatPrice(@PathVariable("performanceId") performanceId: Long): Long {
        return seatService.seatPrice(performanceId)
    }

    @GetMapping("/list/{screenInfoId}")
    fun seatList(
        @PathVariable("screenInfoId") screenInfoId: Long
    ): List<SeatResponse> {

        return seatService.seatList(screenInfoId)
    }

    @DeleteMapping("/delete/{seatId}")
    fun deleteSeat(@PathVariable("seatId") seatId: Long) {
        seatService.deleteSeat(seatId)
    }
}


