package com.example.kotlin.reserveInfo

import java.time.LocalDateTime

data class ReserveInfoResponse (

    val reservationNumber: String,

    val totalPrice: Long,

    val rewardDiscount: Long,

    val finalPrice: Long,

    val createdAt: LocalDateTime?,

    val seats: List<String>,

    val startTime: LocalDateTime,

    val endTime: LocalDateTime,
)