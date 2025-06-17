package com.example.kotlin.member

import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    fun memberInfo(token: String): ResponseEntity<MemberResponse> {

        val username = jwtUtil.getUsername(token)

        val member: Member

        try {
            member = memberRepository.findByUsername(username)
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.MEMBER_NOT_FOUND)
        }

        val result = MemberResponse(
            id = member.id,
            username = member.username,
            name = member.name,
            role = member.role,
            email = member.email
        )

        return ResponseEntity.ok(result)
    }

    fun saveMember(memberRequest: MemberRequest) {
        val encodedPassword = passwordEncoder.encode(memberRequest.password)

        memberRepository.save(memberRequest.toEntity(encodedPassword))
    }
}