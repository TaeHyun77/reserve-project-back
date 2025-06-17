package com.example.kotlin.member

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun saveMember(memberRequest: MemberRequest) {
        val encodedPassword = passwordEncoder.encode(memberRequest.password)

        memberRepository.save(memberRequest.toEntity(encodedPassword))
    }
}