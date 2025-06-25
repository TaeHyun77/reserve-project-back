package com.example.kotlin.venue

import org.springframework.data.jpa.repository.JpaRepository

interface VenueRepository: JpaRepository<Venue, Long> {
}