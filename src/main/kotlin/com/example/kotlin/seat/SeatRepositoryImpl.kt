package com.example.kotlin.seat

import com.querydsl.jpa.impl.JPAQueryFactory

class SeatRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): SeatRepositoryCustom {


    override fun findSeatByScreenInfoId(screenInfoId: Long): List<Seat> {
        val seat = QSeat.seat

        return queryFactory
            .selectFrom(seat)
            .where(seat.screenInfo.id.eq(screenInfoId))
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