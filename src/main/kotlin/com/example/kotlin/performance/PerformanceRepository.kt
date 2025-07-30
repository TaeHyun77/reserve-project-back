package com.example.kotlin.performance

import org.springframework.data.jpa.repository.JpaRepository

interface PerformanceRepository: JpaRepository<Performance, Long>, PerformanceRepositoryCustom {

    // @Query("SELECT s.performance FROM ScreenInfo s WHERE s.place.id = :placeId")
    // fun findPerformancesByPlaceId(@Param("placeId") placeId: Long): List<Performance>


}