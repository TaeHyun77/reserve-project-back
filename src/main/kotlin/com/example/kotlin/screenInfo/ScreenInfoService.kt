package com.example.kotlin.screenInfo

import com.example.kotlin.performance.Performance
import com.example.kotlin.performance.PerformanceRepository
import com.example.kotlin.place.Place
import com.example.kotlin.place.PlaceRepository
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScreenInfoService(
    private val screenInfoRepository: ScreenInfoRepository,
    private val placeRepository: PlaceRepository,
    private val performanceRepository: PerformanceRepository
) {

    @Transactional
    fun registerScreen(screenInfoRequest: ScreenInfoRequest) {

        val place: Place = placeRepository.findById(screenInfoRequest.placeId)
            .orElseThrow { throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.PLACE_NOT_FOUND) }

        val performance: Performance = performanceRepository.findById(screenInfoRequest.performanceId)
            .orElseThrow { throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.PERFORMANCE_NOT_FOUND) }

        try {
            screenInfoRepository.save(screenInfoRequest.toScreen(place, performance))
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SAVE_DATA)
        }
    }
}