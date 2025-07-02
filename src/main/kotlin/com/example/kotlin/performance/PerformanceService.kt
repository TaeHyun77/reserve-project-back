package com.example.kotlin.performance

import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

@Service
class PerformanceService(
    private val performanceRepository: PerformanceRepository,
) {

    @Transactional
    fun registerPerformance(performanceRequest: PerformanceRequest): Performance {
        return try {
            performanceRepository.save(performanceRequest.toPerformance())
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SAVE_DATA)
        }
    }

    @Transactional
    fun deletePerformance(performanceId: Long) {

        val performance = performanceRepository.findById(performanceId)
            .orElseThrow { throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_PERFORMANCE_INFO) }

        val now = LocalDateTime.now()

        val deletableScreenInfos = performance.screenInfoList.filter {
            it.endTime.isBefore(now)
        }

        // 남아 있는 screenInfo가 있으면 삭제 금지
        if (performance.screenInfoList.size != deletableScreenInfos.size) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.CANNOT_DELETE_SOME_SCREENING_HAVE_NOT_YET_ENDED)
        }

        performanceRepository.delete(performance)
        log.info { "performance 삭제 완료" }
    }

    fun performanceList(venueId: Long): List<PerformanceResponse> {

        val performances = performanceRepository.findPerformancesByVenueId(venueId)

        return performances.map { performance ->
            PerformanceResponse(
                id = performance.id,
                type = performance.type,
                title = performance.title,
                duration = performance.duration,
                price = performance.price,
                screenInfoList = performance.screenInfoList.map { screenInfo ->
                    ScreenInfoListResponse(
                        venueId = screenInfo.venue.id,
                        performanceId = screenInfo.performance.id,
                        startTime = screenInfo.startTime
                    )
                }
            )
        }
    }
}

data class ScreenInfoListResponse(
    val venueId: Long?,

    val performanceId: Long?,

    val startTime: LocalDateTime
)