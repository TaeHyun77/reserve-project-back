package com.example.kotlin.seat

interface SeatRepositoryCustom {

    fun findSeatByPlaceIdAndPerformanceId(screenInfoId: Long): List<Seat>

    fun findByScreenInfoAndSeatNumber(screenInfoId: Long?, seatNumber: String): Seat?
}