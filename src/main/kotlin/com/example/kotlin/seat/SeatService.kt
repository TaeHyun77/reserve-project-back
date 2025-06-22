package com.example.kotlin.seat

import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.member.MemberRepository
import com.example.kotlin.performance.PerformanceRepository
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
    private val screenInfoRepository: ScreenInfoRepository,
    private val memberRepository: MemberRepository,
    private val performanceRepository: PerformanceRepository,
    private val jwtUtil: JwtUtil,
) {

    @Transactional
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

    /*
    * 특정 영화관의 영화 예매
    * */
    @Transactional
    fun reserveSeats(seatsIfo: SeatRequest, token: String): ResponseEntity<String> {

        val username = jwtUtil.getUsername(token)

        val member = memberRepository.findByUsername(username)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.MEMBER_NOT_FOUND)

        log.info { "member name : ${member.name}" }

        val updatedSeats = mutableListOf<Seat>()

        val screenInfo = screenInfoRepository.findScreenInfoByPlaceIdAndPerformanceId(seatsIfo.placeId, seatsIfo.performanceId)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.SCREEN_INFO_NOT_FOUND)

        val totalSeatCount = (seatsIfo.seats as List<String>).size
        val totalPrice = screenInfo.performance.price * totalSeatCount

        if (totalPrice > member.credit ) {
            log.info { "사용자의 보유 금액이 부족합니다." }
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_ENOUGH_CREDIT)
        }

        seatsIfo.seats.forEach { seatNumber ->

            val seat = seatRepository.findByScreenInfoAndSeatNumber(screenInfo.id, seatNumber)
                ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.SEAT_NOT_FOUND)

            if (seat.is_reserved == true) {
                throw ReserveException(HttpStatus.CONFLICT, ErrorCode.SEAT_ALREADY_RESERVED)
            } else {
                seat.is_reserved = true
                seat.member = member
                member.credit -= totalPrice
            }

            updatedSeats.add(seat)
        }

        memberRepository.save(member)
        seatRepository.saveAll(updatedSeats)
        log.info { "예약 성공 !" }
        return ResponseEntity.ok("좌석 예약이 완료되었습니다.")
    }

    /*
    * 특정 영화관에서 상영 중인 영화의 좌석 목록 조회
    * */
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

    fun seatPrice(performanceId: Long): Long {

        val performance = performanceRepository.findById(performanceId)
            .orElseThrow{throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.PERFORMANCE_NOT_FOUND) }

        return performance.price
    }

    /*
    * 좌석 삭제
    * */
    @Transactional
    fun deleteSeat(seatId: Long) {

        val deleteSeat = seatRepository.findById(seatId)
            .orElseThrow { ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.SEAT_NOT_FOUND) }

        seatRepository.delete(deleteSeat)

    }
}