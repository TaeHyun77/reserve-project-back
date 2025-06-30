package com.example.kotlin.member

import com.example.kotlin.config.IdempotencyManager
import com.example.kotlin.config.Loggable
import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.redis.RedisLockUtil
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.math.log

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val idempotencyManager: IdempotencyManager
): Loggable {

    fun memberInfo(token: String): ResponseEntity<MemberResponse> {

        val username = jwtUtil.getUsername(token)

        val member = memberRepository.findByUsername(username)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.MEMBER_NOT_FOUND)

        val result = MemberResponse(
            id = member.id,
            username = member.username,
            name = member.name,
            role = member.role,
            email = member.email,
            last_reward_date = member.last_reward_date,
            reward = member.reward
        )

        return ResponseEntity.ok(result)
    }

    @Transactional
    fun saveMember(memberRequest: MemberRequest) {

        val exists = memberRepository.existsByUsername(memberRequest.username)

        if (exists) throw ReserveException(HttpStatus.CONFLICT, ErrorCode.DUPLICATED_USERNAME)

        val encodedPassword = passwordEncoder.encode(memberRequest.password)

        try {
            memberRepository.save(memberRequest.toEntity(encodedPassword))
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SAVE_DATA)
        }
    }

    fun checkUsername(username: CheckUsername): ResponseEntity<UsernameCheckResponse> {

        val exists = memberRepository.existsByUsername(username)

        return if (exists) {
            throw ReserveException(HttpStatus.CONFLICT, ErrorCode.DUPLICATED_USERNAME)
        } else {
            ResponseEntity.ok(
                UsernameCheckResponse(true, "사용 가능한 아이디입니다.")
            )
        }
    }

    fun earnRewardToday(token: String, today: LocalDate, idempotencyKey: String): ResponseEntity<String> {

        val username = jwtUtil.getUsername(token)

        val member = memberRepository.findByUsername(username)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.MEMBER_NOT_FOUND)

        return RedisLockUtil.acquireLockAndRun("${today}:${member.username}:earnReward") {
            idempotencyManager.execute(
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
            } else {
                throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.REWARD_ALREADY_CLAIMED)
            }

            log.info { "리워드 지급 성공 - $today ${member.username}님에게 리워드가 지급되었습니다." }
            memberRepository.save(member)

            return "이미 처리된 요청이거나, $today ${member.name}님에게 리워드가 지급되었습니다."
        } catch (e: ReserveException) {


            when(e.errorCode) {
                ErrorCode.REWARD_ALREADY_CLAIMED -> "오늘 이미 리워드가 지급되었습니다."
                ErrorCode.NOT_EXIST_IN_HEADER_IDEMPOTENCY_KEY -> "IDEMPOTENCY_KEY가 존재하지 않습니다."
                else -> "리워드 지급에 실패하였습니다."
            }.also {
                log.info {"리워드 지급 실패 - 날짜 : ${today}, 사용자: ${member.username}, 원인: ${e.errorCode}"}
            }
        }
    }
}

data class UsernameCheckResponse(
    val available: Boolean,
    val message: String
)