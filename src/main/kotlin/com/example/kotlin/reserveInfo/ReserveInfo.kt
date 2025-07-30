package com.example.kotlin.reserveInfo

import com.example.kotlin.BaseTime
import com.example.kotlin.member.Member
import com.example.kotlin.seat.Seat
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import org.springframework.cglib.core.Local
import java.time.LocalDateTime

@Entity
class ReserveInfo(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="reserve_id")
    val id: Long? = null,

    val reservationNumber: String,

    val totalPrice: Long,

    val rewardDiscount: Long,

    val finalPrice: Long,

    val seats: List<String>,

    val startTime: LocalDateTime,

    val endTime: LocalDateTime,

    val screenInfoId: Long?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member,
): BaseTime()