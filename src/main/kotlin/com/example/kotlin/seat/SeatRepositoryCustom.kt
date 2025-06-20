package com.example.kotlin.seat

import com.example.kotlin.screenInfo.ScreenInfo

interface SeatRepositoryCustom {

    fun findSeatByPlaceIdAndPerformanceId(placeId: Long, performanceId: Long): List<Seat>

    fun findByScreenInfoAndSeatNumber(screenInfoId: Long, seatNumber: String): Seat?
}