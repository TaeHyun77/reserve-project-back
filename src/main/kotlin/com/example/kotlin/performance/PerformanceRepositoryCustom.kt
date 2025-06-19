package com.example.kotlin.performance

interface PerformanceRepositoryCustom {

    fun findPerformancesByPlaceId(placeId: Long): List<Performance>

}