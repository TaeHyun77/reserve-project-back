package com.example.kotlin.member

import com.example.kotlin.config.Loggable
import com.example.kotlin.util.parsingToken
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
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
): Loggable {

    // 로그인 사용자 정보 조회
    @GetMapping("/info")
    fun memberInfo(request: HttpServletRequest): ResponseEntity<MemberResponse> {

        val token = parsingToken(request)

        return memberService.memberInfo(token)
    }

    // 사용자 회원가입
    @PostMapping("/save")
    fun saveMember(@RequestBody memberRequest: MemberRequest): Member {
        return memberService.saveMember(memberRequest)
    }

    // 아이디 검증 로직
    @GetMapping("/check/validation/{username}")
    fun checkUsername(@PathVariable("username") username: CheckUsername): ResponseEntity<UsernameCheckResponse> {
        return memberService.checkUsername(username)
    }

    // 하루 한 번 리워드 지급 로직
    @PostMapping("/reward/{today}")
    fun earnRewardToday(request: HttpServletRequest, @PathVariable("today") today: LocalDate): ResponseEntity<String> {

        val token: String = parsingToken(request)

        val idempotencyKey: String = request.getHeader("Idempotency-key")
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_IN_HEADER_IDEMPOTENCY_KEY)

        log.info { "idempotencyKey : $idempotencyKey" }

        return memberService.earnRewardToday(token, today, idempotencyKey)
    }
}

