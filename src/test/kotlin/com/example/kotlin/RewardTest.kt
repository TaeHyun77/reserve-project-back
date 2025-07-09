package com.example.kotlin

import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.member.MemberRepository
import com.example.kotlin.member.MemberService
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import org.assertj.core.api.BDDAssertions.then
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
class RewardTest {

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @Autowired
    private lateinit var memberService: MemberService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    private val today: LocalDate = LocalDate.now()

    @Test
    fun `사용자는 하루에 리워드를 한 번만 받을 수 있다`() {

        val token = jwtUtil.createToken(
            username = "@JTest04",
            name = "@JTest04",
            email = "@JTest04",
            role = "0",
            category = "access",
            expired = 600_000L,
        )

        val idempotencyKey1 = UUID.randomUUID().toString()
        val idempotencyKey2 = UUID.randomUUID().toString()

        // 첫 시도 - 성공
        val response1 = memberService.earnRewardToday(token, today, idempotencyKey1)
        println("응답 1: ${response1.body}")

        // 두 번째 시도 - 실패 기대
        val response2 = memberService.earnRewardToday(token, today, idempotencyKey2)

        then(response2.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)

        // 리워드가 실제로 한 번만 증가했는지 확인
        val member = memberRepository.findByUsername("@JTest04")!!
        assertEquals(today, member.last_reward_date)
        assertEquals(200, member.reward)
    }
}