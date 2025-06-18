package com.example.kotlin.place

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/place")
@RestController
class PlaceController(
    private val placeService: PlaceService
) {

    @PostMapping("/register")
    fun registerPlace(@RequestBody placeRequest: PlaceRequest) {
        placeService.registerPlace(placeRequest)
    }

}

data class PlaceRequest(
    val name: String,

    val location: String
) {
    fun toPlace(): Place {
        return Place(
            name = this.name,
            location = this.location
        )
    }
}