package com.example.kotlin.screenInfo

import com.example.kotlin.performance.Performance
import com.example.kotlin.performance.PerformanceRepository
import com.example.kotlin.performance.PerformanceResponse
import com.example.kotlin.venue.Venue
import com.example.kotlin.venue.VenueRepository
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScreenInfoService(
    private val screenInfoRepository: ScreenInfoRepository,
    private val venueRepository: VenueRepository,
    private val performanceRepository: PerformanceRepository
) {

    @Transactional
    fun registerScreen(screenInfoRequest: ScreenInfoRequest) {

        val venue: Venue = venueRepository.findById(screenInfoRequest.venueId)
            .orElseThrow { throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_PLACE_INFO) }

        val performance: Performance = performanceRepository.findById(screenInfoRequest.performanceId)
            .orElseThrow { throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_PERFORMANCE_INFO) }

        try {
            screenInfoRepository.save(screenInfoRequest.toScreen(venue, performance))
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SAVE_DATA)
        }
    }

    fun screenList(venueId: Long, performanceId: Long): List<ScreenInfoResponse> {

        val screenInfos = screenInfoRepository.findScreenInfoListByVenueIdAndPerformanceId(venueId, performanceId)
            ?: throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_SCREEN_INFO)

        return screenInfos.map {
            val performance = PerformanceResponse(
                id = it.performance.id,
                type = it.performance.type,
                title = it.performance.title,
                duration = it.performance.duration,
                price = it.performance.price
            )

            ScreenInfoResponse(
                id = it.id,
                performance = performance,
                screeningDate = it.screeningDate,
                startTime = it.startTime,
                endTime = it.endTime
            )
        }
    }
}
