package com.example.kotlin.member

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/member")
@RestController
class MemberController(
    private val memberService: MemberService,
) {

    @PostMapping("/save")
    fun saveMember(@RequestBody memberRequest: MemberRequest) {
        memberService.saveMember(memberRequest)
    }
}