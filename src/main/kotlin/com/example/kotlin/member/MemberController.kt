package com.example.kotlin.member

import com.example.kotlin.config.parsingToken
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RequestMapping("/member")
@RestController
class MemberController(
    private val memberService: MemberService,
) {

    // 사용자 정보 조회
    @GetMapping("/info")
    fun memberInfo(request: HttpServletRequest): ResponseEntity<MemberResponse> {

        val token = parsingToken(request)

        return memberService.memberInfo(token)
    }

    @PostMapping("/save")
    fun saveMember(@RequestBody memberRequest: MemberRequest) {
        memberService.saveMember(memberRequest)
    }

    @PostMapping("/reward/{today}")
    fun setRewardDate(request: HttpServletRequest, @PathVariable("today") today: LocalDate): ResponseEntity<String> {

        val token: String = parsingToken(request)

        println("받은 날짜: $today")

        return memberService.setRewardDate(token, today)
    }
}

