package com.example.kotlin.refresh

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ReissueController(
    private val reissueService: ReissueService
) {

    @PostMapping("/reToken")
    fun reToken(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<Any> {
        return reissueService.reToken(request, response)
    }

}