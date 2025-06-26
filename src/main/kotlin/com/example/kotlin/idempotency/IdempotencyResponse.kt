package com.example.kotlin.idempotency

data class IdempotencyResponse (

    val statusCode: Int,

    val responseBody: String? = null,

)