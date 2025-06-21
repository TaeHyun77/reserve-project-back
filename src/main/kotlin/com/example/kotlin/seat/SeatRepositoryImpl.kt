package com.example.kotlin.seat

import com.example.kotlin.screenInfo.QScreenInfo
import com.querydsl.jpa.impl.JPAQueryFactory

class SeatRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): SeatRepositoryCustom {

    override fun findSeatByPlaceIdAndPerformanceId(placeId: Long, performanceId: Long): List<Seat> {
        val seat = QSeat.seat
        val screen = QScreenInfo.screenInfo

        return queryFactory
            .selectFrom(seat)
            .join(seat.screenInfo, screen)
            .where(
                screen.place.id.eq(placeId),
                screen.performance.id.eq(performanceId)
            )
            .fetch()
    }

    override fun findByScreenInfoAndSeatNumber(screenInfoId: Long?, seatNumber: String): Seat? {
        val seat = QSeat.seat

        return queryFactory
            .selectFrom(seat)
            .where(
                seat.screenInfo.id.eq(screenInfoId),
                seat.seatNumber.eq(seatNumber)
            )
            .fetchOne()
    }
}