package com.example.kotlin.member

import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
            email = member.email
        )

        return ResponseEntity.ok(result)
    }

    @Transactional
    fun saveMember(memberRequest: MemberRequest) {
        val encodedPassword = passwordEncoder.encode(memberRequest.password)

        try {
            memberRepository.save(memberRequest.toEntity(encodedPassword))
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SAVE_DATA)
        }
    }
}