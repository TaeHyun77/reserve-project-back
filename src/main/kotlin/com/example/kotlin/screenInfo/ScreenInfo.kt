package com.example.kotlin.screenInfo

import com.example.kotlin.performance.Performance
import com.example.kotlin.place.Place
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class ScreenInfo(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "screen_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    val place: Place,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    val performance: Performance,

    val startTime: String,

    val endTime: String
)