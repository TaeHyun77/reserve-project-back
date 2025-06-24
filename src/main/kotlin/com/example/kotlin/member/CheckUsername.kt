package com.example.kotlin.member

import com.example.kotlin.config.Loggable
import com.example.kotlin.reserveException.ErrorCode
import com.example.kotlin.reserveException.ReserveException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.springframework.http.HttpStatus
import kotlin.text.contains
import kotlin.text.replace
import kotlin.text.toRegex


@JvmInline
@JsonDeserialize(using = CheckUsernameDeserializer::class) // 커스텀 역직렬화 지정
value class CheckUsername private constructor (val username: String) {

    companion object {
        private val USERNAME_REGEX = Regex("^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@#%*^]+$")

        // 직접 CheckUsername 인스턴스를 생성할 때 사용할 수 있는 invoke 오버로딩
        operator fun invoke(username: String): CheckUsername = CheckUsername(username)
    }

    init {
        validateUsername(username)
    }

    private fun validateUsername(username: String) {
        require(USERNAME_REGEX.matchEntire(username) != null) {
            throw ReserveException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_USERNAME)
        }
    }
}

// 커스텀 역직렬화 클래스 정의
class CheckUsernameDeserializer : JsonDeserializer<CheckUsername>(), Loggable {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CheckUsername {

        val username = p.valueAsString
        log.info { "공백, 하이픈 제거" }

        val cleanedUsername = username.removeSpacesAndHyphens()

        return CheckUsername(cleanedUsername)
    }
}

fun String.removeSpacesAndHyphens(): String {
    if (this.contains(' ') || this.contains('-')) {
        return this.replace("[\\s-]".toRegex(), "")
    }

    return this
}

/*
* companion object 내부에 invoke를 정의하면, 이 invoke 함수는 클래스 자체의 일부분으로 간주되어 private 생성자에 접근할 수 있음
* */