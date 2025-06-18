package com.example.kotlin.seat

import com.example.kotlin.screenInfo.ScreenInfo
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/seat")
@RestController
class SeatController(
    private val seatService: SeatService
) {

    @PostMapping("/register")
    fun registerSeat(@RequestBody seatRequest: SeatRequest) {
        seatService.registerSeat(seatRequest)
    }

}

data class SeatRequest(

    val screenInfoId: Long,

    val seatNumber: String,

    val is_reserved: Boolean = true
) {
    fun toSeat(screenInfo: ScreenInfo): Seat {
        return Seat (
            screenInfo = screenInfo,
            seatNumber = this.seatNumber,
            is_reserved = this.is_reserved
        )
    }
}
