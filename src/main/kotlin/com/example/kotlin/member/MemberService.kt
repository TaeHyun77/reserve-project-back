package com.example.kotlin.member

import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import jakarta.servlet.http.HttpServletRequest
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
    private val jwtUtil: JwtUtil
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
            last_reward_date = member.last_reward_date
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

    @Transactional
    fun setRewardDate(token: String, today: LocalDate): ResponseEntity<String> {

        val username = jwtUtil.getUsername(token)

        val member = memberRepository.findByUsername(username)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.MEMBER_NOT_FOUND)

        if (member.last_reward_date == null || member.last_reward_date != today) {
            member.last_reward_date = today
            member.reward += 200
        } else {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.REWARD_ALREADY_CLAIMED)
        }

        memberRepository.save(member)

        return ResponseEntity.ok("ok")
    }
}

data class UsernameCheckResponse(
    val available: Boolean,
    val message: String
)