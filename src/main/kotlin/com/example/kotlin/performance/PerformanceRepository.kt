package com.example.kotlin.performance

import org.springframework.data.jpa.repository.JpaRepository

interface PerformanceRepository: JpaRepository<Performance, Long> {
}