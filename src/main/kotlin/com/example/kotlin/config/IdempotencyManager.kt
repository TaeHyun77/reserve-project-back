package com.example.kotlin.config

import com.example.kotlin.idempotency.Idempotency
import com.example.kotlin.idempotency.IdempotencyRepository
import com.example.kotlin.idempotency.IdempotencyResponse
import com.example.kotlin.reserveException.ErrorCode
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
        process: () -> String
    ): ResponseEntity<String> {

        val now = LocalDateTime.now()
        val idempotency = idempotencyRepository.findByIdempotencyKey(key)

        if (idempotency != null && idempotency.expires_at.isAfter(now)) {

            log.info { "동일한 Idempotent 요청 감지됨 - 저장된 이전 응답 반환" }
            log.info { "⇒ ${idempotency.responseBody}" }

            val idempotencyRes = IdempotencyResponse(
                statusCode = idempotency.statusCode,
                responseBody = idempotency.responseBody
            )

            return ResponseEntity
                .status(idempotencyRes.statusCode)
                .body(idempotencyRes.responseBody)
        }

        // 예약이 성공했을 때
        try {
            val resultMessage = process()  // 성공 결과 메시지 or 예외

            idempotencyRepository.save(
                Idempotency(
                    idempotencyKey = key,
                    url = url,
                    httpMethod = method,
                    responseBody = resultMessage,
                    statusCode = 200,
                    expires_at = now.plusMinutes(10)
                )
            )

            log.info{"멱등성 키 저장 (성공 요청) - key: $key, message: $resultMessage"}

            return ResponseEntity
                .status(200)
                .body(resultMessage)

        // 예약이 실패했을 때
        } catch (e: ReserveException) {

            val failMessage = when (e.errorCode) {
                ErrorCode.NOT_ENOUGH_CREDIT -> "잔액이 부족하여 예약에 실패했습니다."
                ErrorCode.SEAT_ALREADY_RESERVED -> "이미 예약된 좌석이 포함되어 있어 예약에 실패했습니다."
                ErrorCode.SCREEN_INFO_NOT_FOUND -> "상영 정보를 찾을 수 없습니다."
                ErrorCode.SEAT_NOT_FOUND -> "선택한 좌석 정보를 찾을 수 없습니다."
                ErrorCode.REWARD_ALREADY_CLAIMED -> "오늘 이미 리워드가 지급되었습니다."
                ErrorCode.NOT_EXIST_IN_HEADER_IDEMPOTENCY_KEY -> "IDEMPOTENCY_KEY가 존재하지 않습니다."
                else -> "요청에 실패했습니다."
            }

            idempotencyRepository.save(
                Idempotency(
                    idempotencyKey = key,
                    url = url,
                    httpMethod = method,
                    responseBody = failMessage,
                    statusCode = e.status.value(),
                    expires_at = now.plusMinutes(10)
                )
            )

            log.info{"멱등성 키 저장 (실패 요청) - key: $key, message: $failMessage"}

            return ResponseEntity
                .status(e.status)
                .body(failMessage)
        }
    }
}