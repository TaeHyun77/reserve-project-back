package com.example.kotlin.member

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/member")
@RestController
class MemberController(
    private val memberService: MemberService,
) {

    // 사용자 정보 조회
    @GetMapping("/info")
    fun memberInfo(request: HttpServletRequest): ResponseEntity<MemberResponse> {

        val authorization = request.getHeader("Authorization")
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val token = authorization.substring(7)

        return memberService.memberInfo(token)
    }

    @PostMapping("/save")
    fun saveMember(@RequestBody memberRequest: MemberRequest) {
        memberService.saveMember(memberRequest)
    }
}