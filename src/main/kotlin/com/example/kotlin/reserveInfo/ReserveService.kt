package com.example.kotlin.reserveInfo

import com.example.kotlin.config.IdempotencyManager
import com.example.kotlin.config.Loggable
import com.example.kotlin.member.Member
import com.example.kotlin.member.MemberRepository
import com.example.kotlin.redis.RedisLockUtil
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
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
    private val idempotencyManager: IdempotencyManager
): Loggable {

    fun deleteReserveInfo(reserveNumber: String, idempotencyKey: String): ResponseEntity<String> {

        val reserveInfo = reserveRepository.findByReservationNumber(reserveNumber)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_RESERVE_INFO)

        val member = memberRepository.findByUsername(reserveInfo.member.username)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_MEMBER_INFO)

        return RedisLockUtil.acquireLockAndRun("${member.username}:${reserveNumber}:doDelete") {
            idempotencyManager.execute(
                key = idempotencyKey,
                url = "/reserve/delete",
                method = "DELETE"
            ) {
                doDeleteReserveInfo(reserveNumber, reserveInfo, member)
            }
        }
    }

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