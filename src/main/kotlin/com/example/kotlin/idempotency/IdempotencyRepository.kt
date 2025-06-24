package com.example.kotlin.idempotency

import org.springframework.data.jpa.repository.JpaRepository

interface IdempotencyRepository: JpaRepository<Idempotency, Long> {

    fun findByIdempotencyKey(idempotencyKey: String): Idempotency?

}