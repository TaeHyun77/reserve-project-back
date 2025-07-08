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

        // 브라우저가 # 이후는 프래그먼트로 간주하여 서버에 전달하지 않음, 따라서 #은 허용하면 안됨
        private val USERNAME_REGEX = Regex("^(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d@*^]+$")

        // 직접 CheckUsername 인스턴스를 생성할 때 사용할 수 있는 invoke 오버로딩
        // companion object 안에 operator fun invoke(...)를 정의하면 생성자 호출처럼 사용할 수 있음
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
*
* [ 흐름 ]
*
* CheckUsername(값)을 호출하면, 생성자가 private이므로 직접 생성할 수 없고, 대신 invoke 연산자를 통해 CheckUsername 인스턴스가 생성됩니다.
*
* 이 invoke 함수에서 전달된 값은 생성자로 전달되며, @JsonDeserialize(using = CheckUsernameDeserializer::class)에 의해커스텀 역직렬화가 수행됩니다.
* 이 과정에서 removeSpacesAndHyphens()가 실행되어 불필요한 공백이나 특수 문자가 제거됩니다.
*
* 이후 init 블록에서 validateUsername() 함수가 호출되어, 사용자명이 유효한 형식인지 최종 검증됩니다.
*
* */