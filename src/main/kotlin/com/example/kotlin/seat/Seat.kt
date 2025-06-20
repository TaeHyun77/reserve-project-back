package com.example.kotlin.seat

import com.example.kotlin.screenInfo.ScreenInfo
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class Seat(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    val screenInfo: ScreenInfo,

    // 좌석은 A1 ~ A5 , B1 ~ B5 까지만 있다고 가정
    val seatNumber: String?,

    var is_reserved: Boolean?
)