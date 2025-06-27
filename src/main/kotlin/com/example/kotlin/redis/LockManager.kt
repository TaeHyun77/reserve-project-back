package com.example.kotlin.redis

import com.example.kotlin.config.Loggable
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class LockManager(
    private val redisTemplate: RedisTemplate<String, String>
) {

    // setIfAbsent : Redis의 SETNX 명령 → 해당 key가 Redis에 없을 때만 값을 설정
    // 키가 이미 존재하면 false, 없으면 true를 반환, 예외가 발생하면 null 반환, TTL은 3초로
    // 뮤텍스 방식으로 구현 :
    fun tryMutexLock(
        key: String, maxWaitMillis: Long = 3000, retryDelayMillis: Long = 100
    ): Boolean {
        val start = System.currentTimeMillis()

        while (System.currentTimeMillis() - start < maxWaitMillis) {
            val success = redisTemplate.opsForValue().setIfAbsent(key, "lock", Duration.ofSeconds(5)) == true

            if (success) {
                return true // 락 획득 성공
            }

            Thread.sleep(retryDelayMillis) // 기다렸다가 다시 시도 ( Blocking Polling )
        }

        return false // 최대 대기 시간 초과
    }

    fun unlock(key: String): Boolean = redisTemplate.delete(key)
}