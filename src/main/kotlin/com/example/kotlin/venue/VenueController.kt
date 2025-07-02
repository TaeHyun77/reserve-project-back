package com.example.kotlin.venue

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/venue")
@RestController
class VenueController(
    private val venueService: VenueService
) {

    @PostMapping("/register")
    fun registerVenue(@RequestBody venueRequest: VenueRequest): Venue {
        return venueService.registerVenue(venueRequest)
    }

    @GetMapping("/list")
    fun venueList(): List<VenueResponse> {
        return venueService.venueList()
    }

    @DeleteMapping("/delete/{venueId}")
    fun deleteVenue(@PathVariable("venueId") venueId: Long) {
        venueService.deleteVenue(venueId)
    }
}