package com.example.kotlin.screenInfo

import com.querydsl.jpa.impl.JPAQueryFactory

class ScreenInfoRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): ScreenInfoRepositoryCustom {

    override fun findScreenInfoByPlaceIdAndPerformanceId(placeId: Long?, performanceId: Long?): ScreenInfoResponse? {
        val screenInfo = QScreenInfo.screenInfo

        return queryFactory
            .select(
                com.querydsl.core.types.Projections.constructor(
                ScreenInfoResponse::class.java,
                screenInfo.id,
                screenInfo.performance
            ))
            .from(screenInfo)
            .where(
                screenInfo.place.id.eq(placeId),
                screenInfo.performance.id.eq(performanceId)
            )
            .fetchOne()
    }
}