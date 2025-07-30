package com.example.kotlin.member

import com.example.kotlin.idempotency.IdempotencyService
import com.example.kotlin.config.Loggable
import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.redis.lock.RedisLockUtil
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import com.example.kotlin.reserveInfo.ReserveInfoResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val idempotencyService: IdempotencyService
): Loggable {

    fun memberInfo(token: String): ResponseEntity<MemberResponse> {

        val username = jwtUtil.getUsername(token)

        val member = memberRepository.findByUsername(username)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_MEMBER_INFO)

        val result = MemberResponse(
            id = member.id,
            username = member.username,
            name = member.name,
            role = member.role,
            email = member.email,
            last_reward_date = member.last_reward_date,
            reward = member.reward,
            credit = member.credit,
            reserveList = member.reserveList?.map {
                ReserveInfoResponse(
                    reservationNumber = it.reservationNumber,
                    totalPrice = it.totalPrice,
                    rewardDiscount = it.rewardDiscount,
                    finalPrice = it.finalPrice,
                    createdAt = it.createdAt,
                    seats = it.seats,
                    startTime = it.startTime,
                    endTime = it.endTime
                )
            },
        )

        return ResponseEntity.ok(result)
    }

    @Transactional
    fun saveMember(memberRequest: MemberRequest): Member {

        return try {
            val exists = memberRepository.existsByUsername(memberRequest.username)

            if (exists) throw ReserveException(HttpStatus.CONFLICT, ErrorCode.DUPLICATED_USERNAME)

            val encodedPassword = passwordEncoder.encode(memberRequest.password)

            memberRepository.save(memberRequest.toEntity(encodedPassword))
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SAVE_DATA)
        }
    }

    fun checkUsername(username: CheckUsername): ResponseEntity<UsernameCheckResponse> {

        try {
            val exists = memberRepository.existsByUsername(username)

            return if (exists) {
                throw ReserveException(HttpStatus.CONFLICT, ErrorCode.DUPLICATED_USERNAME)
            } else {
                ResponseEntity.ok(
                    UsernameCheckResponse(true, "사용 가능한 아이디입니다.")
                )
            }
        } catch (e: ReserveException) {
            throw e
        }
    }

    fun earnRewardToday(token: String, today: LocalDate, idempotencyKey: String): ResponseEntity<String> {

        val username = jwtUtil.getUsername(token)

        val member = memberRepository.findByUsername(username)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_MEMBER_INFO)

        return RedisLockUtil.acquireLockAndRun("${today}:${member.username}:earnReward") {
            idempotencyService.execute(
                key = idempotencyKey,
                url = "/member/reward",
                method = "POST",
            ) {
                doPayRewardToday(member, today)
            }
        }
    }

    @Transactional
    fun doPayRewardToday(member: Member, today: LocalDate): String {

        return try {
            if (member.last_reward_date == null || member.last_reward_date != today) {
                member.last_reward_date = today
                member.reward += 200
                memberRepository.save(member)
            } else {
                throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.REWARD_ALREADY_CLAIMED)
            }

            log.info { "리워드 지급 성공 - $today ${member.username}님에게 리워드가 지급되었습니다." }
            "이미 처리된 요청이거나, $today ${member.name}님에게 리워드가 지급되었습니다."
        } catch (e: ReserveException) {

            log.info {"리워드 지급 실패 - 날짜 : ${today}, 사용자: ${member.username}, 원인: ${e.errorCode}"}
            throw e
        }
    }
}

data class UsernameCheckResponse(
    val available: Boolean,
    val message: String
)