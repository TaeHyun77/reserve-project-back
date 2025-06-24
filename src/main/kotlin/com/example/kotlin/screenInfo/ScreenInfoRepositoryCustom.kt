package com.example.kotlin.screenInfo

interface ScreenInfoRepositoryCustom {

    fun findScreenInfoByPlaceIdAndPerformanceId(placeId: Long?, performanceId: Long?): ScreenInfoResponse?

    fun findScreenInfoListByPlaceIdAndPerformanceId(placeId: Long?, performanceId: Long?): List<ScreenInfo>?

}