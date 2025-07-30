package com.example.kotlin.seat

import org.springframework.data.jpa.repository.JpaRepository

interface SeatRepository: JpaRepository<Seat, Long>, SeatRepositoryCustom {
}