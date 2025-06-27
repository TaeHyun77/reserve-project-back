package com.example.kotlin.seat

import com.example.kotlin.config.IdempotencyManager
import com.example.kotlin.config.Loggable
import com.example.kotlin.idempotency.IdempotencyRepository
import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.member.Member
import com.example.kotlin.member.MemberRepository
import com.example.kotlin.performance.PerformanceRepository
import com.example.kotlin.performance.PerformanceResponse
import com.example.kotlin.redis.RedisLockUtil
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import com.example.kotlin.screenInfo.ScreenInfo
import com.example.kotlin.screenInfo.ScreenInfoRepository
import com.example.kotlin.screenInfo.ScreenInfoResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SeatService(
    private val seatRepository: SeatRepository,
    private val screenInfoRepository: ScreenInfoRepository,
    private val memberRepository: MemberRepository,
    private val performanceRepository: PerformanceRepository,
    private val jwtUtil: JwtUtil,
    private val idempotencyRepository: IdempotencyRepository,
    private val idempotencyManager: IdempotencyManager
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

    /*
    * 멱등성 로직을 활용한 예약 로직
    * */
    fun reserveSeats(reservationRequest: ReservationRequest, token: String, idempotencyKey: String): ResponseEntity<String> {

        val username = jwtUtil.getUsername(token)

        val member = memberRepository.findByUsername(username)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.MEMBER_NOT_FOUND)

        return idempotencyManager.execute(
            key = idempotencyKey,
            url = "/seat/reserve",
            method = "POST",
            failResult = "예약이 실패되었습니다."
        ) {
            RedisLockUtil.acquireLockAndRun(
                "${member.username}:${reservationRequest.screenInfoId}:doReserve"
            ){
                doReserveSeats(reservationRequest, member)
            }
        }
    }

    @Transactional
    fun doReserveSeats(request: ReservationRequest, member: Member): String {

        val screenInfo = screenInfoRepository.findById(request.screenInfoId)
            .orElseThrow { throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.SCREEN_INFO_NOT_FOUND) }

        val seatPrice = screenInfo.performance.price // 좌석 개당 가격
        val seatCount = request.seats.size // 예약한 좌석의 수
        val totalPrice = seatPrice * seatCount // 예약 총 가격

        val usedReward = request.usedReward // 사용한 리워드
        val finalPrice = totalPrice - usedReward // 총 가격 - 리워드

        if (finalPrice > member.credit) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_ENOUGH_CREDIT)
        }

        // 좌석 예약 처리
        request.seats.forEach { seatNumber ->
            val seat = seatRepository.findByScreenInfoAndSeatNumber(screenInfo.id, seatNumber)
                ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.SEAT_NOT_FOUND)

            if (seat.is_reserved == true) {
                throw ReserveException(HttpStatus.CONFLICT, ErrorCode.SEAT_ALREADY_RESERVED)
            }

            seat.is_reserved = true
            seat.member = member
            seatRepository.save(seat)
        }

        // 멤버 정보 업데이트
        member.reward -= usedReward
        member.credit -= finalPrice
        memberRepository.save(member)

        log.info { "예약 성공 - 사용자: ${member.username}, 좌석 수: $seatCount, 총 가격: $totalPrice, 결제 금액: $finalPrice, 포인트 사용: $usedReward" }
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