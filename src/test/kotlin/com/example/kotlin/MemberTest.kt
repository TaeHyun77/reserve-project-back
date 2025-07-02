package com.example.kotlin

import com.example.kotlin.member.CheckUsername
import com.example.kotlin.member.MemberRepository
import com.example.kotlin.member.MemberRequest
import com.example.kotlin.member.MemberService
import org.assertj.core.api.BDDAssertions.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test

@SpringBootTest
class MemberTest {

    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    fun `member 등록 테스트`() {

        // given && when
        val member = memberService.saveMember(createDummyMemberRequest())

        // then
        then(member.name).isEqualTo("test_name")
        then(member.email).isEqualTo("test_email")
        then(member.reward).isEqualTo(0)
    }

    private fun createDummyMemberRequest(): MemberRequest {
        return MemberRequest(
            username = CheckUsername.invoke("@Q12345"),
            password = "test_password",
            name = "test_name",
            email = "test_email",
            reward = 0
        )
    }
}