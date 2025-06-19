package com.example.kotlin.performance

import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
class PerformanceService(
    private val performanceRepository: PerformanceRepository,
) {

    @Transactional
    fun registerPerformance(performanceRequest: PerformanceRequest) {
        try {
            performanceRepository.save(performanceRequest.toPerformance())
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SAVE_DATA)
        }
    }

    fun performanceList(placeId: Long): List<PerformanceResponse> {
        val performances = performanceRepository.findPerformancesByPlaceId(placeId)

        log.info { performances[0].type }

        return performances.map {
            PerformanceResponse(
                id = it.id,
                type = it.type,
                title = it.title,
                duration = it.duration
            )
        }
    }
}