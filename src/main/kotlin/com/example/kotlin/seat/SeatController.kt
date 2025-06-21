package com.example.kotlin.seat

import com.example.kotlin.screenInfo.ScreenInfo
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
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

    @GetMapping("/price/{performanceId}")
    fun seatPrice(@PathVariable("performanceId") performanceId: Long): Long {
        return seatService.seatPrice(performanceId)
    }

    @GetMapping("/list/{placeId}/{performanceId}")
    fun seatList(
        @PathVariable("placeId") placeId: Long,
        @PathVariable("performanceId") performanceId: Long
    ): List<SeatResponse> {

        return seatService.seatList(placeId, performanceId)
    }

    @PostMapping("/reserve")
    fun reserveSeats(@RequestBody seatsInfo: SeatRequest, request: HttpServletRequest): ResponseEntity<String> {

        val authorization = request.getHeader("Authorization")
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val token = authorization.substring(7)

        return seatService.reserveSeats(seatsInfo, token)
    }

    @DeleteMapping("/delete/{seatId}")
    fun deleteSeat(@PathVariable("seatId") seatId: Long) {
        seatService.deleteSeat(seatId)
    }
}


