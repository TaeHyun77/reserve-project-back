package com.example.kotlin.performance

interface PerformanceRepositoryCustom {

    fun findPerformancesByVenueId(venueId: Long): List<Performance>

}