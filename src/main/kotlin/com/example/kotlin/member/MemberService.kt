package com.example.kotlin.member

import com.example.kotlin.config.IdempotencyManager
import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
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
    private val idempotencyManager: IdempotencyManager
) {

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

    fun payRewardToday(token: String, today: LocalDate, idempotencyKey: String): ResponseEntity<String> {

        val username = jwtUtil.getUsername(token)

        val member = memberRepository.findByUsername(username)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.MEMBER_NOT_FOUND)

        return idempotencyManager.execute(
            key = idempotencyKey,
            url = "/member/reward",
            method = "POST",
            failResult = "리워드 지급이 실패되었습니다."
        ) {
            doPayRewardToday(member, today)
        }
    }

    @Transactional
    fun doPayRewardToday(member: Member, today: LocalDate): String {

        if (member.last_reward_date == null || member.last_reward_date != today) {
            member.last_reward_date = today
            member.reward += 200
        } else {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.REWARD_ALREADY_CLAIMED)
        }

        memberRepository.save(member)

        return "이미 처리된 요청이거나, $today ${member.name}님에게 리워드가 지급되었습니다."
    }
}

data class UsernameCheckResponse(
    val available: Boolean,
    val message: String
)