package com.example.kotlin.seat

import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import com.example.kotlin.screenInfo.ScreenInfo
import com.example.kotlin.screenInfo.ScreenInfoRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
class SeatService(
    private val seatRepository: SeatRepository,
    private val screenInfoRepository: ScreenInfoRepository
) {

    fun initSeats(seatRequest: SeatRequest) {

        val screenInfo: ScreenInfo = screenInfoRepository.findById(seatRequest.screenInfoId)
            .orElseThrow { throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.SCREEN_INFO_NOT_FOUND) }

        val seats = mutableListOf<Seat>()

        for (row in 'A'..'E') {
            for (col in 1..5) {
                val seatNumber = "$row$col"

                val seat = Seat(
                    seatNumber = seatNumber,
                    is_reserved = false,
                    screenInfo = screenInfo
                )

                seats.add(seat)
            }
        }

        seatRepository.saveAll(seats)
    }

    @Transactional
    fun reserveSeats(seatsIfo: SeatRequest): ResponseEntity<String> {

        val updatedSeats = mutableListOf<Seat>()

        val screenInfo = screenInfoRepository.findScreenInfoByPlaceIdAndPerformanceId(seatsIfo.placeId, seatsIfo.performanceId)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.SCREEN_INFO_NOT_FOUND)

        seatsIfo.seats?.forEach { seatNumber ->

            val seat = seatRepository.findByScreenInfoAndSeatNumber(screenInfo, seatNumber)
                ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.SEAT_NOT_FOUND)

            if (seat.is_reserved == true) {
                throw ReserveException(HttpStatus.CONFLICT, ErrorCode.SEAT_ALREADY_RESERVED)
            } else {
                seat.is_reserved = true
            }

            updatedSeats.add(seat)
        }

        seatRepository.saveAll(updatedSeats)
        log.info { "예약 성공 !" }
        return ResponseEntity.ok("좌석 예약이 완료되었습니다.")
    }

    fun seatList(placeId: Long, performanceId: Long): List<SeatResponse> {

        try {
            val seats = seatRepository.findSeatByPlaceIdAndPerformanceId(placeId, performanceId)

            return seats.map {
                SeatResponse (
                    seatNumber = it.seatNumber,
                    is_reserved = it.is_reserved
                )
            }
        } catch(e: ReserveException) {
            log.info { "예약 좌석 리스트 반환 실패" }
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_RETURN_RESERVED_SEAT_LIST)
        }
    }

    @Transactional
    fun deleteSeat(seatId: Long) {

        val deleteSeat = seatRepository.findById(seatId)
            .orElseThrow { ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.SEAT_NOT_FOUND) }

        seatRepository.delete(deleteSeat)

    }
}