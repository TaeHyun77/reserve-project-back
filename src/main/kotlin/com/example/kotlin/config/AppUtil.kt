package com.example.kotlin.config

import jakarta.servlet.http.Cookie

fun createCookie(key: String, value: String): Cookie {

    return Cookie(key, value).apply {
        maxAge = 12 * 60 * 60
        isHttpOnly = false
        path = "/"
    }

}