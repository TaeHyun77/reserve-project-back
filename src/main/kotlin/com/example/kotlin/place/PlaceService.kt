package com.example.kotlin.place

import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceService(
    private val placeRepository: PlaceRepository
) {

    @Transactional
    fun registerPlace(placeRequest: PlaceRequest) {
        try {
            placeRepository.save(placeRequest.toPlace())
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SAVE_DATA)
        }

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