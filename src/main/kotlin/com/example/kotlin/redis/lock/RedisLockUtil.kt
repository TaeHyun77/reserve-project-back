package com.example.kotlin.redis.lock

import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

/*
* 특정 비즈니스 로직을 실행하기 전 락을 획득했을 때만 실행되도록 하는 코드
* */
@Component
class RedisLockUtil(
    private val lockManager: LockManager
) {

    init {
        manager = lockManager
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)

        private lateinit var manager: LockManager

        /*
        * 특정 비즈니스 로직에서 Lock 획득을 시도하고 획득하지 못하면 예외를 던짐
        * */
        fun <T> acquireLockAndRun(key: String, block: () -> T): T {

            if (key.isBlank()) {
                log.error("[RedisLockError] key is blank")
                return block()
            }

            // lock 획득 시도
            val acquired = acquiredLock(key)

            return if (acquired) {
                proceedWithLock(key, block)
            } else {
                throw ReserveException(HttpStatus.CONFLICT, ErrorCode.REDIS_FAILED_TO_ACQUIRED_LOCK)
            }
        }

        /*
        * Lock 획득을 시도
        * */
        private fun acquiredLock(key: String): Boolean {
            return try {
                val isAquired = manager.tryMutexLock(key)

                if (isAquired) log.info("Lock 획득 성공")

                isAquired
            } catch (e: Exception) {
                log.error("[RedisLockError] failed to acquire lock. key: $key", e)
                false
            }
        }

        /*
        * 특정 비즈니스 로직을 실행하고, 실행이 끝나면 Lock을 해제
        * */
        private fun <T> proceedWithLock(key: String, block: () -> T): T {
            return try {
                block()
            } catch (e: Exception) {
                throw e
            } finally {
                releaseLock(key)
            }
        }

        /*
        * 획득한 Lock 해제
        * */
        private fun releaseLock(key: String): Boolean {
            return try {
                val isRelease = manager.unlock(key)

                if (isRelease) log.info("Lock 반환 성공")

                isRelease
            } catch (e: Exception) {
                log.error("[RedisLockError] failed to unlock. key: $key", e)
                false
            }
        }
    }
}