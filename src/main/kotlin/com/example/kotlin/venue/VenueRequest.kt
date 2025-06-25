package com.example.kotlin.venue

data class VenueRequest(
    val name: String,

    val location: String
) {
    fun toVenue(): Venue {
        return Venue(
            name = this.name,
            location = this.location
        )
    }
}