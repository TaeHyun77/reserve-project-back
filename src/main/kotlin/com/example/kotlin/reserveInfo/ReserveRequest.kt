package com.example.kotlin.reserveInfo

import java.util.UUID

data class ReserveRequest (
    val screenInfoId: Long,

    val reservationNumber: String = UUID.randomUUID().toString(),

    val rewardDiscount: Long,

    val seats: List<String>
)

