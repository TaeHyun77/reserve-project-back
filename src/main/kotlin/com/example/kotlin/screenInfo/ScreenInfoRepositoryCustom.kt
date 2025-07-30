package com.example.kotlin.screenInfo

interface ScreenInfoRepositoryCustom {

    fun findScreenInfoByVenueIdAndPerformanceId(venueId: Long?, performanceId: Long?): ScreenInfoResponse?

    fun findScreenInfoListByVenueIdAndPerformanceId(venueId: Long?, performanceId: Long?): List<ScreenInfo>?

}