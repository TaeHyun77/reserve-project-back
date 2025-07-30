package com.example.kotlin.util

import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus

fun createCookie(key: String, value: String): Cookie {

    return Cookie(key, value).apply {
        maxAge = 12 * 60 * 60
        isHttpOnly = false
        path = "/"
    }
}

fun parsingToken(request: HttpServletRequest): String {

    val authorization = request.getHeader("Authorization")
        ?: throw ReserveException(HttpStatus.UNAUTHORIZED, ErrorCode.NOT_EXIST_AUTHORIZATION_IN_HEADER)

    if (!authorization.startsWith("Bearer ")) {
        throw ReserveException(HttpStatus.UNAUTHORIZED, ErrorCode.NOT_EXIST_AUTHORIZATION_IN_HEADER)
    }

    val token = authorization.substring(7)

    return token
}