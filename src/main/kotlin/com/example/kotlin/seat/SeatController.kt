package com.example.kotlin.seat

import com.example.kotlin.config.Loggable
import com.example.kotlin.config.parsingToken
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import com.example.kotlin.screenInfo.ScreenInfo
import io.github.oshai.kotlinlogging.KotlinLogging
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

    @PostMapping("/reserve")
    fun reserveSeats(@RequestBody seatsInfo: SeatRequest, request: HttpServletRequest): ResponseEntity<String> {

        val token = parsingToken(request)

        val idempotencyKey: String = request.getHeader("Idempotency-key")
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_IN_HEADER_IDEMPOTENCY_KEY)

        log.info { "idempotencyKey : $idempotencyKey" }

        return seatService.reserveSeats(seatsInfo, token, idempotencyKey)
    }

    @DeleteMapping("/delete/{seatId}")
    fun deleteSeat(@PathVariable("seatId") seatId: Long) {
        seatService.deleteSeat(seatId)
    }
}


