package com.example.kotlin.screenInfo

import com.example.kotlin.performance.Performance
import com.example.kotlin.venue.Venue
import com.example.kotlin.seat.Seat
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class ScreenInfo(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "screen_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    val venue: Venue,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    val performance: Performance,

    @OneToMany(mappedBy = "screenInfo", cascade = [CascadeType.ALL], orphanRemoval = true)
    val seatList: List<Seat> = ArrayList(),

    val screeningDate: LocalDate,

    val startTime: LocalDateTime,

    val endTime: LocalDateTime
)