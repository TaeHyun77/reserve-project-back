package com.example.kotlin.seat

import com.example.kotlin.screenInfo.ScreenInfoResponse

data class SeatResponse(
    val seatNumber: String?,

    val is_reserved: Boolean?,

    val screenInfo: ScreenInfoResponse
)