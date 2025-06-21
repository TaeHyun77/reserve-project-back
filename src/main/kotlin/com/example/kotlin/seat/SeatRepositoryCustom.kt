package com.example.kotlin.seat

interface SeatRepositoryCustom {

    fun findSeatByPlaceIdAndPerformanceId(placeId: Long, performanceId: Long): List<Seat>

    fun findByScreenInfoAndSeatNumber(screenInfoId: Long?, seatNumber: String): Seat?
}