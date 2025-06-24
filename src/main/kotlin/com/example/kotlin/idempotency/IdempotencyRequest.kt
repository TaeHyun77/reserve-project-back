package com.example.kotlin.idempotency

import java.time.LocalDateTime

data class IdempotencyRequest (

    val idempotencyKey: String,

    // 요청 url
    val url: String,

    // HTTP 요청 메서드
    val httpMethod: String,

) {
    fun toIdempotency(): Idempotency {
        return Idempotency(
            idempotencyKey = this.idempotencyKey,
            url = this.url,
            httpMethod = this.httpMethod,
            expires_at = LocalDateTime.now().plusMinutes(10)
        )
    }
}