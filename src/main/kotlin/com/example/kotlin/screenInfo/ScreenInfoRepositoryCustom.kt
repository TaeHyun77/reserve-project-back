package com.example.kotlin.screenInfo

interface ScreenInfoRepositoryCustom {

    fun findScreenInfoByPlaceIdAndPerformanceId(placeId: Long?, performanceId: Long?): ScreenInfoResponse?

}