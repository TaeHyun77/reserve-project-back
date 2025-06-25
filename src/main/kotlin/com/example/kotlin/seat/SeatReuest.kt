package com.example.kotlin.seat

import com.example.kotlin.screenInfo.ScreenInfo

data class SeatRequest(

    val screenInfoId: Long,

    val seatNumber: String?,

    val is_reserved: Boolean? = false,

    val venueId: Long? = null,

    val performanceId: Long? = null,

    val seats: List<String>? = null
) {
    fun toSeat(screenInfo: ScreenInfo): Seat {
        return Seat (
            screenInfo = screenInfo,
            seatNumber = this.seatNumber,
            is_reserved = this.is_reserved
        )
    }
}