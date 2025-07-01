package com.example.kotlin.reserveInfo

import org.springframework.data.jpa.repository.JpaRepository

interface ReserveRepository: JpaRepository<ReserveInfo, Long> {

    fun findByReservationNumber(reservationNumber: String): ReserveInfo?

}