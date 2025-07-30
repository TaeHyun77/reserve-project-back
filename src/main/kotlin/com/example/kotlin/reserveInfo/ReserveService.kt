package com.example.kotlin.reserveInfo

import com.example.kotlin.idempotency.IdempotencyService
import com.example.kotlin.config.Loggable
import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.member.Member
import com.example.kotlin.member.MemberRepository
import com.example.kotlin.redis.lock.RedisLockUtil
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import com.example.kotlin.screenInfo.ScreenInfoRepository
import com.example.kotlin.seat.Seat
import com.example.kotlin.seat.SeatRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReserveService (
    private val reserveRepository: ReserveRepository,
    private val memberRepository: MemberRepository,
    private val seatRepository: SeatRepository,
    private val screenInfoRepository: ScreenInfoRepository,
    private val idempotencyService: IdempotencyService,
    private val jwtUtil: JwtUtil,
): Loggable {

    /*
    * 멱등성 로직을 활용한 예약 로직
    * */
    fun reserveSeats(reserveRequest: ReserveRequest, token: String, idempotencyKey: String): ResponseEntity<String> {

        val username = jwtUtil.getUsername(token)

        val member = memberRepository.findByUsername(username)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_MEMBER_INFO)

        return RedisLockUtil.acquireLockAndRun("${reserveRequest.reservationNumber}:${reserveRequest.screenInfoId}:doReserve")
        { idempotencyService.execute(
            key = idempotencyKey,
            url = "/seat/reserve",
            method = "POST",
        ) { doReserveSeats(reserveRequest, member) }
        }
    }

    @Transactional
    fun doReserveSeats(reserveRequest: ReserveRequest, member: Member): String {

        return try {
            val screenInfo = screenInfoRepository.findById(reserveRequest.screenInfoId)
                .orElseThrow {
                    throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_SCREEN_INFO)
                }

            val totalPrice = screenInfo.performance.price * reserveRequest.seats.size
            val rewardDiscount = reserveRequest.rewardDiscount
            val finalPrice = totalPrice - rewardDiscount

            if (finalPrice > member.credit) {
                log.info {"잔액 부족 - 사용자: ${member.username}, 필요 금액: $finalPrice, 보유: ${member.credit}"}
                throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_ENOUGH_CREDIT)
            }

            // 좌석 예약 가능 검증
            val seats = reserveRequest.seats.map { seatNumber ->
                findAndValidateSeat(screenInfo.id, seatNumber, member)
            }

            // 모든 좌석이 예약 가능할 때 비용 차감
            member.updateCreditAndReward(finalPrice, rewardDiscount)
            memberRepository.save(member)

            val reserveInfo = ReserveInfo(
                reservationNumber = reserveRequest.reservationNumber,
                totalPrice = totalPrice,
                rewardDiscount = rewardDiscount,
                finalPrice = finalPrice,
                seats = reserveRequest.seats,
                startTime = screenInfo.startTime,
                endTime = screenInfo.endTime,
                screenInfoId = screenInfo.id,
                member = member
            )
            reserveRepository.save(reserveInfo)

            seats.forEach { seat ->
                seat.is_reserved = true
                seat.reserveInfo = reserveInfo
                seatRepository.save(seat)
            }

            log.info {"예약 성공 - 사용자: ${member.username}, 좌석 수: ${reserveRequest.seats.size}, 총 가격: $totalPrice, 결제 금액: $finalPrice, 포인트 사용: $rewardDiscount"}
            "이미 처리된 요청이거나, 좌석 예약이 완료되었습니다."

        } catch (e: ReserveException) {
            log.info {"예약 실패 - 사용자: ${member.username}, 원인: ${e.errorCode}"}
            throw e
        }
    }

    fun findAndValidateSeat(screenInfoId: Long?, seatNumber: String, member: Member): Seat {
        val seat = seatRepository.findByScreenInfoAndSeatNumber(screenInfoId, seatNumber)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_SEAT_INFO)

        if (seat.is_reserved == true) {
            log.info {"좌석 중복 예약 시도 - 사용자: ${member.username}, 좌석: $seatNumber"}
            throw ReserveException(HttpStatus.CONFLICT, ErrorCode.SEAT_ALREADY_RESERVED)
        }

        return seat
    }

    /*
    * 예약 취소 로직
    * */
    fun deleteReserveInfo(reserveNumber: String, idempotencyKey: String): ResponseEntity<String> {

        val reserveInfo = reserveRepository.findByReservationNumber(reserveNumber)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_RESERVE_INFO)

        val member = memberRepository.findByUsername(reserveInfo.member.username)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_MEMBER_INFO)

        return RedisLockUtil.acquireLockAndRun("${member.username}:${reserveNumber}:doDelete") {
            idempotencyService.execute(
                key = idempotencyKey,
                url = "/reserve/delete",
                method = "DELETE"
            ) {
                doDeleteReserveInfo(reserveNumber, reserveInfo, member)
            }
        }
    }

    /*
    * 좌석 예약 취소 로직
    * */
    @Transactional
    fun doDeleteReserveInfo(reserveNumber: String, reserveInfo: ReserveInfo, member: Member): String {

        return try {

            // 사용자의 금액, 리워드 환불
            member.credit += reserveInfo.finalPrice
            member.reward += reserveInfo.rewardDiscount
            memberRepository.save(member)

            // 좌석 초기화
            reserveInfo.seats.forEach { seatNumber ->

                val seat = seatRepository.findByScreenInfoAndSeatNumber(reserveInfo.screenInfoId, seatNumber)
                    ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_SEAT_INFO)

                seat.is_reserved = false
                seat.reserveInfo = null
                seatRepository.save(seat)
            }

            // 예약 정보 삭제
            reserveRepository.delete(reserveInfo)

            log.info{"예약 취소 완료 - 예약번호: $reserveNumber, 사용자: ${member.username}, 환불 금액: ${reserveInfo.finalPrice}, 리워드 환불: ${reserveInfo.rewardDiscount}"}
            "이미 처리된 요청이거나, 예약이 취소되었습니다."
        } catch (e: ReserveException) {

            log.info { "예약 취소 실패 - 사용자: ${member.username}, 원인: ${e.errorCode}" }
            throw e
        }
    }
}