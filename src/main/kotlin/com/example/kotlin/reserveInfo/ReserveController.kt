package com.example.kotlin.reserveInfo

import com.example.kotlin.config.Loggable
import com.example.kotlin.util.parsingToken
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/reserve")
@RestController
class ReserveController(
    private val reserveService: ReserveService
): Loggable {

    // 예약
    @PostMapping("/reserve")
    fun reserveSeats(@RequestBody reserveRequest: ReserveRequest, request: HttpServletRequest): ResponseEntity<String> {

        val token = parsingToken(request)

        val idempotencyKey: String = request.getHeader("Idempotency-key")
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_IN_HEADER_IDEMPOTENCY_KEY)

        log.info { "idempotencyKey : $idempotencyKey" }

        return reserveService.reserveSeats(reserveRequest, token, idempotencyKey)
    }

    // 예약 취소
    @DeleteMapping("/delete/{reserveNumber}")
    fun deleteReserveInfo(@PathVariable("reserveNumber") reserveNumber: String, request: HttpServletRequest) {

        val idempotencyKey: String = request.getHeader("Idempotency-key")
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_IN_HEADER_IDEMPOTENCY_KEY)

        log.info { "idempotencyKey: $idempotencyKey" }

        reserveService.deleteReserveInfo(reserveNumber, idempotencyKey)

    }

}