package com.example.kotlin

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import kotlin.test.Test

@SpringBootTest
class RedisConnectTest{

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @DisplayName("레디스 기본 테스트")
    @Test
    fun basic_redis_test() {
        val valueOperations = redisTemplate.opsForValue()

        val key = "testKey"
        val value = "testValue"

        valueOperations.set(key, value)

        val result = valueOperations.get(key)

        assertThat(result).isEqualTo(value)
    }
}