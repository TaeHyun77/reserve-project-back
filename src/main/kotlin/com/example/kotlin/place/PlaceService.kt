package com.example.kotlin.place

import com.example.kotlin.config.Loggable
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.log

@Service
class PlaceService(
    private val placeRepository: PlaceRepository
): Loggable {

    @Transactional
    fun registerPlace(placeRequest: PlaceRequest) {
        try {
            placeRepository.save(placeRequest.toPlace())
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SAVE_DATA)
        }

    }

    @Transactional
    fun deletePlace(placeId: Long) {

        val place = placeRepository.findById(placeId)
            .orElseThrow { throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.PLACE_NOT_FOUND) }

        val now = LocalDateTime.now()

        val deletablePlaces = place.screenInfoList.filter {
            it.endTime.isBefore(now)
        }

        // 남아 있는 screenInfo가 있으면 삭제 금지
        if (place.screenInfoList.size != deletablePlaces.size) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.CANNOT_DELETE_SOME_SCREENING_HAVE_NOT_YET_ENDED)
        }

        placeRepository.delete(place)
        log.info { "place 삭제 완료" }
    }

    fun placeAllInfo(): List<PlaceResponse> {
        return placeRepository.findAll().map { p ->
            PlaceResponse(
                id = p.id,
                name = p.name,
                location = p.location
            )
        }
    }
}