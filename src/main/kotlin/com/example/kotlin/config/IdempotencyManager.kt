package com.example.kotlin.config

import com.example.kotlin.idempotency.Idempotency
import com.example.kotlin.idempotency.IdempotencyRepository
import com.example.kotlin.idempotency.IdempotencyResponse
import com.example.kotlin.reserveException.ReserveException
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class IdempotencyManager(
    private val idempotencyRepository: IdempotencyRepository,
): Loggable {

    fun execute(
        key: String,
        url: String,
        method: String,
        failResult: String,
        process: () -> String
    ): ResponseEntity<String> {

        val now = LocalDateTime.now()
        val idempotency = idempotencyRepository.findByIdempotencyKey(key)

        if (idempotency != null && idempotency.expires_at.isAfter(now)) {

            log.info { "동일한 Idempotent 요청 감지됨 - 저장된 이전 응답 반환" }

            val idempotencyRes = IdempotencyResponse(
                statusCode = idempotency.statusCode,
                responseBody = idempotency.responseBody
            )

            return ResponseEntity
                .status(idempotencyRes.statusCode)
                .body(idempotencyRes.responseBody)
        }

        try {
            val result = process()

            idempotencyRepository.save(
                Idempotency(
                    idempotencyKey = key,
                    url = url,
                    httpMethod = method,
                    responseBody = result,
                    statusCode = 200,
                    expires_at = now.plusMinutes(10)
                )
            )

            return ResponseEntity
                .status(200)
                .body(result)

        } catch (e: ReserveException) {

            val failResult = failResult

            idempotencyRepository.save(
                Idempotency(
                    idempotencyKey = key,
                    url = url,
                    httpMethod = method,
                    responseBody = failResult,
                    statusCode = e.status.value(),
                    expires_at = now.plusMinutes(10)
                )
            )
            throw e
        }
    }
}