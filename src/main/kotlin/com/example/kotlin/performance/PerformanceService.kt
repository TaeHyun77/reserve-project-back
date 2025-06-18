package com.example.kotlin.performance

import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PerformanceService(
    private val performanceRepository: PerformanceRepository
) {

    @Transactional
    fun registerPerformance(performanceRequest: PerformanceRequest) {
        try {
            performanceRepository.save(performanceRequest.toPerformance())
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SAVE_DATA)
        }
    }
}