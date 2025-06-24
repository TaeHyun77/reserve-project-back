package com.example.kotlin.place

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

    @GetMapping("/list")
    fun placeAllInfo(): List<PlaceResponse> {
        return placeService.placeAllInfo()
    }

    @DeleteMapping("/delete/{placeId}")
    fun deletePlace(@PathVariable("placeId") placeId: Long) {
        placeService.deletePlace(placeId
        )
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