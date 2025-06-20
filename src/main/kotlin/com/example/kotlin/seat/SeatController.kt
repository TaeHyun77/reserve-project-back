package com.example.kotlin.seat

import com.example.kotlin.screenInfo.ScreenInfo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/seat")
@RestController
class SeatController(
    private val seatService: SeatService
) {

    @PostMapping("/init")
    fun initSeats(@RequestBody seatRequest: SeatRequest) {
        seatService.initSeats(seatRequest)
    }

    @GetMapping("/list/{placeId}/{performanceId}")
    fun seatList(
        @PathVariable("placeId") placeId: Long,
        @PathVariable("performanceId") performanceId: Long): List<SeatResponse> {

        return seatService.seatList(placeId, performanceId)
    }

    @PostMapping("/reserve")
    fun reserveSeats(@RequestBody seatsInfo: SeatRequest): ResponseEntity<String> {

        return seatService.reserveSeats(seatsInfo)
    }

    @DeleteMapping("/delete/{seatId}")
    fun deleteSeat(@PathVariable("seatId") seatId: Long) {
        seatService.deleteSeat(seatId)
    }
}


