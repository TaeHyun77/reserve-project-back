package com.example.kotlin

import com.example.kotlin.performance.PerformanceRepository
import com.example.kotlin.performance.PerformanceRequest
import com.example.kotlin.performance.PerformanceService
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import com.example.kotlin.screenInfo.ScreenInfo
import com.example.kotlin.screenInfo.ScreenInfoRepository
import com.example.kotlin.venue.Venue
import com.example.kotlin.venue.VenueRepository
import com.example.kotlin.venue.VenueRequest
import com.example.kotlin.venue.VenueService
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test

@SpringBootTest
class VenueAndPerformanceTest {

    @Autowired
    private lateinit var venueService: VenueService

    @Autowired
    lateinit var venueRepository: VenueRepository

    @Autowired
    lateinit var screenInfoRepository: ScreenInfoRepository

    @Autowired
    lateinit var performanceService: PerformanceService

    @Autowired
    private lateinit var performanceRepository: PerformanceRepository

    @Test
    fun `venue 등록 테스트`() {

        // given
        val venueTestRequest = VenueRequest(
            name = "test_name",
            location = "test_location"
        )

        // when
        val venue = venueService.registerVenue(venueTestRequest)

        // then
        then(venue.name).isEqualTo("test_name")
        then(venue.location).isEqualTo("test_location")
    }

    @Test
    fun `performance 등록 테스트`() {

        // given
        val testPerformanceRequest = createDummyPerformanceRequest()

        // when
        val performance = performanceService.registerPerformance(testPerformanceRequest)

        then(performance.type).isEqualTo("테스트")
        then(performance.title).isEqualTo("테스트_제목")
        then(performance.duration).isEqualTo("120")
        then(performance.price).isEqualTo(10000)
    }

    @Test
    fun `모든 상영이 종료된 venue 및 performance 삭제 성공`() {
        // given
        val venue = venueRepository.save(Venue(name = "테스트극장", location = "서울"))

        val performance = performanceService.registerPerformance(createDummyPerformanceRequest())

        val screen = screenInfoRepository.save(
            ScreenInfo(
                venue = venue,
                performance = performance,
                screeningDate = LocalDate.now().minusDays(3),
                startTime = LocalDateTime.now().minusDays(3),
                endTime = LocalDateTime.now().minusDays(2)
            )
        )

        // when
        venueService.deleteVenue(venue.id!!)
        performanceService.deletePerformance(performance.id!!)

        // then
        val result = venueRepository.findById(venue.id!!)
        val result2 = performanceRepository.findById(performance.id!!)

        then(result).isEmpty
        then(result2).isEmpty
    }

    @Test
    fun `종료되지 않은 상영이 있는 venue 삭제 실패`() {
        // given
        val venue = venueRepository.save(Venue(name = "테스트극장", location = "서울"))
        val performance = performanceService.registerPerformance(createDummyPerformanceRequest())

        screenInfoRepository.save(
            ScreenInfo(
                venue = venue,
                performance = performance,
                screeningDate = LocalDate.now(),
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusHours(1)
            )
        )

        // when
        val exception = assertThrows<ReserveException> {
            venueService.deleteVenue(venue.id!!)
        }

        // then
        then(exception.errorCode).isEqualTo(ErrorCode.CANNOT_DELETE_SOME_SCREENING_HAVE_NOT_YET_ENDED)
    }

    @Test
    fun `종료되지 않은 상영이 있는 performance 삭제 실패`() {
        // given
        val venue = venueRepository.save(Venue(name = "테스트극장", location = "서울"))
        val performance = performanceService.registerPerformance(createDummyPerformanceRequest())

        screenInfoRepository.save(
            ScreenInfo(
                venue = venue,
                performance = performance,
                screeningDate = LocalDate.now(),
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusHours(1)
            )
        )

        // when
        val exception = assertThrows<ReserveException> {
            performanceService.deletePerformance(performance.id!!)
        }

        // then
        then(exception.errorCode).isEqualTo(ErrorCode.CANNOT_DELETE_SOME_SCREENING_HAVE_NOT_YET_ENDED)
    }

    private fun createDummyPerformanceRequest(): PerformanceRequest {
        return PerformanceRequest(
            type = "테스트",
            title = "테스트_제목",
            duration = "120",
            price = 10000
        )
    }
}