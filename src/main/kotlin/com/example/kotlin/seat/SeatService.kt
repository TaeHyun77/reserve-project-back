package com.example.kotlin.seat

import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import com.example.kotlin.screenInfo.ScreenInfo
import com.example.kotlin.screenInfo.ScreenInfoRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SeatService(
    private val seatRepository: SeatRepository,
    private val screenInfoRepository: ScreenInfoRepository
) {

    @Transactional
    fun registerSeat(seatRequest: SeatRequest) {

        val screenInfo: ScreenInfo = screenInfoRepository.findById(seatRequest.screenInfoId)
            .orElseThrow { throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.SCREEN_INFO_NOT_FOUND) }

        try {
            seatRepository.save(seatRequest.toSeat(screenInfo))
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SAVE_DATA)
        }
    }
}