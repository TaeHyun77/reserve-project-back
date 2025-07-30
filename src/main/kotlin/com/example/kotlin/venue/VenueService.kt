package com.example.kotlin.venue

import com.example.kotlin.config.Loggable
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class VenueService(
    private val venueRepository: VenueRepository
): Loggable {

    @Transactional
    fun registerVenue(venueRequest: VenueRequest): Venue {
        return try {
            venueRepository.save(venueRequest.toVenue())
        } catch (e: ReserveException) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.FAIL_TO_SAVE_DATA)
        }
    }

    @Transactional
    fun deleteVenue(venueId: Long) {

        val venue = venueRepository.findById(venueId)
            .orElseThrow { throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXIST_PLACE_INFO) }

        val now = LocalDateTime.now()

        val deletableVenue = venue.screenInfoList.filter {
            it.endTime.isBefore(now)
        }

        // 남아 있는 screenInfo가 있으면 삭제 금지
        if (venue.screenInfoList.size != deletableVenue.size) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.CANNOT_DELETE_SOME_SCREENING_HAVE_NOT_YET_ENDED)
        }

        venueRepository.delete(venue)
        log.info { "venue 삭제 완료" }
    }

    fun venueList(): List<VenueResponse> {
        return venueRepository.findAll().map {
            VenueResponse(
                id = it.id,
                name = it.name,
                location = it.location
            )
        }
    }
}