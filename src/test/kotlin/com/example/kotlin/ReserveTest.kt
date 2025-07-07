package com.example.kotlin

import com.example.kotlin.jwt.JwtUtil
import com.example.kotlin.reserveException.ReserveException
import com.example.kotlin.reserveInfo.ReserveRequest
import com.example.kotlin.reserveInfo.ReserveService
import com.example.kotlin.seat.SeatRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.test.Test

@SpringBootTest
class ReserveTest {

    @Autowired
    private lateinit var reserveService: ReserveService

    @Autowired
    private lateinit var seatRepository: SeatRepository

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    @Test
    fun `여러 사용자가 동시에 좌석 예약을 시도하면 하나만 성공`() {

        // given
        val threadCount = 5
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        val screenInfoId = 2L
        val seatNumber = "D2"
        val tokenList = (1..threadCount).map { jwtUtil.createToken(
                username = "@JTest0$it",
                name = "@JTest0$it",
                email = "@JTest0$it",
                role = "0",
                category = "access",
                expired = 600_000L,
            )
        }

        val request = ReserveRequest(
            screenInfoId = screenInfoId,
            seats = listOf(seatNumber),
            rewardDiscount = 0
        )

        // when
        repeat(threadCount) { index ->
            executor.submit {
                try {
                    val idempotencyKey = UUID.randomUUID().toString()
                    reserveService.reserveSeats(request, tokenList[index], idempotencyKey)
                } catch (e: ReserveException) {
                    throw e
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        // then
        val reservedSeat = seatRepository.findByScreenInfoAndSeatNumber(screenInfoId, seatNumber)
        assertEquals(true, reservedSeat?.is_reserved)

        val reservedUsername = reservedSeat?.reserveInfo?.member?.username
        println("좌석 예약자: $reservedUsername")
    }
}