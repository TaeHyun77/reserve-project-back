package com.example.kotlin.performance

import com.example.kotlin.screenInfo.QScreenInfo
import com.querydsl.jpa.impl.JPAQueryFactory

class PerformanceRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): PerformanceRepositoryCustom {

    // 특정 영화관의 영화 목록 리스트 반환
    override fun findPerformancesByVenueId(venueId: Long): List<Performance> {
        val screenInfo = QScreenInfo.screenInfo

        return queryFactory
            .select(screenInfo.performance)
            .from(screenInfo)
            .where(screenInfo.venue.id.eq(venueId))
            .fetch()
    }
}