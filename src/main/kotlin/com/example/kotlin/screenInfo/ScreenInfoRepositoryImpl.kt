package com.example.kotlin.screenInfo

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory

class ScreenInfoRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): ScreenInfoRepositoryCustom {

    override fun findScreenInfoByVenueIdAndPerformanceId(venueId: Long?, performanceId: Long?): ScreenInfoResponse? {
        val screenInfo = QScreenInfo.screenInfo

        return queryFactory
            .select(
                Projections.constructor(
                ScreenInfoResponse::class.java,
                screenInfo.id,
                screenInfo.performance
            ))
            .from(screenInfo)
            .where(
                screenInfo.venue.id.eq(venueId),
                screenInfo.performance.id.eq(performanceId)
            )
            .fetchOne()
    }

    override fun findScreenInfoListByVenueIdAndPerformanceId(
        venueId: Long?, performanceId: Long?
    ): List<ScreenInfo>? {
        val screenInfo = QScreenInfo.screenInfo

        return queryFactory
            .select(screenInfo)
            .from(screenInfo)
            .where(
                screenInfo.venue.id.eq(venueId),
                screenInfo.performance.id.eq(performanceId)
            )
            .fetch()
    }
}