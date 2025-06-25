package com.example.kotlin.seat

import com.example.kotlin.config.Loggable
import com.example.kotlin.idempotency.Idempotency
import com.example.kotlin.idempotency.IdempotencyRepository
import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.member.MemberRepository
import com.example.kotlin.performance.PerformanceRepository
import com.example.kotlin.performance.PerformanceResponse
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ErrorCodeDto
import com.example.kotlin.reserveException.ReserveException
import com.example.kotlin.screenInfo.ScreenInfo
import com.example.kotlin.screenInfo.ScreenInfoRepository
import com.example.kotlin.screenInfo.ScreenInfoResponse
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SeatService(
    private val seatRepository: SeatRepository,
    private val screenInfoRepository: ScreenInfoRepository,
    private val memberRepository: MemberRepository,
    private val performanceRepository: PerformanceRepository,
    private val jwtUtil: JwtUtil,
    private val idempotencyRepository: IdempotencyRepository,
    private val objectMapper: ObjectMapper,

    ): Loggable {

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

    @Transactional
    fun reserveSeats(seatRequest: SeatRequest, token: String, idempotencyKey: String): ResponseEntity<String> {
        return checkIdempotencyOrProceed(idempotencyKey, "/seat/reserve", "POST") {
            doReserveSeats(seatRequest, token)
        }
    }

    fun checkIdempotencyOrProceed(
        idempotencyKey: String, url: String, method: String, process: () -> String
    ): ResponseEntity<String> {

        val idempotency = idempotencyRepository.findByIdempotencyKey(idempotencyKey)

        val now = LocalDateTime.now()

        // 최초 요청이 아니고, idempotency의 유효기간이 지나지 않은 경우
        // ⇒ 저장되어 있던 응답을 그대로 반환해야 함, 예매 로직 실행되지 않음
        if (idempotency != null && idempotency.expires_at.isAfter(now)) {
            log.info { "이전 Idempotent 요청 감지됨 - 이전 응답 반환" }

            return ResponseEntity
                .status(idempotency.statusCode)
                .body(idempotency.responseBody)
        }

        // 최초 요청인 경우 예매 로직 실행 → 예매 로직의 응답 값을 Idempotency의 응답 데이터로 설정함
        try {
            val result = process()

            val newIdempotency = Idempotency(
                idempotencyKey = idempotencyKey,
                url = url,
                httpMethod = method,
                responseBody = objectMapper.writeValueAsString(result),
                statusCode = 200,
                expires_at = LocalDateTime.now().plusMinutes(10)
            )

            idempotencyRepository.save(newIdempotency)

            return ResponseEntity
                .status(200)
                .body(result)

        } catch (e: ReserveException) {

            val failResult = "예약이 실패되었습니다."

            idempotencyRepository.save(
                Idempotency(
                    idempotencyKey = idempotencyKey,
                    url = url,
                    httpMethod = method,
                    responseBody = failResult,
                    statusCode = e.status.value(),
                    expires_at = LocalDateTime.now().plusMinutes(10)
                )
            )

            throw e
        }
    }

    @Transactional
    fun doReserveSeats(seatRequest: SeatRequest, token: String): String {
        val username = jwtUtil.getUsername(token)

        val member = memberRepository.findByUsername(username)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.MEMBER_NOT_FOUND)

        val screenInfo = screenInfoRepository.findById(seatRequest.screenInfoId)
            .orElseThrow { throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.SCREEN_INFO_NOT_FOUND) }

        val totalSeatCount = (seatRequest.seats as List<String>).size
        val totalPrice = screenInfo.performance.price * totalSeatCount

        if (totalPrice > member.credit) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_ENOUGH_CREDIT)
        }

        seatRequest.seats.forEach { seatNumber ->
            val seat = seatRepository.findByScreenInfoAndSeatNumber(screenInfo.id, seatNumber)
                ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.SEAT_NOT_FOUND)

            if (seat.is_reserved == true) {
                throw ReserveException(HttpStatus.CONFLICT, ErrorCode.SEAT_ALREADY_RESERVED)
            }

            seat.is_reserved = true
            seat.member = member
            seatRepository.save(seat)
        }

        member.credit -= totalPrice
        memberRepository.save(member)

        log.info { "예약 성공!" }
        return "이미 처리된 요청이거나, 좌석 예약이 완료되었습니다."
    }

    /*
    * 특정 영화관에서 상영 중인 영화의 좌석 목록 조회
    * */
    fun seatList(screenInfoId: Long): List<SeatResponse> {
        try {
            val seats = seatRepository.findSeatByPerformanceId(screenInfoId)

            return seats.map {
                SeatResponse (
                    seatNumber = it.seatNumber,
                    is_reserved = it.is_reserved,
                    screenInfo = ScreenInfoResponse(
                        performance = PerformanceResponse(
                            type = it.screenInfo.performance.type,
                            title = it.screenInfo.performance.title,
                            duration = it.screenInfo.performance.duration,
                            price = it.screenInfo.performance.price
                        ),
                        screeningDate = it.screenInfo.screeningDate,
                        startTime = it.screenInfo.startTime,
                        endTime = it.screenInfo.endTime
                    )
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