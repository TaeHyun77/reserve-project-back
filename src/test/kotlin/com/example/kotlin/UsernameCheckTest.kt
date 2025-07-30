package com.example.kotlin

import com.example.kotlin.member.CheckUsername
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class UsernameCheckTest {

    @ParameterizedTest // 여러 번 반복 실행될 수 있는 테스트를 정의
    @ValueSource(strings = [ // @ParameterizedTest가 실행될 때 사용할 테스트 인자 값들을 직접 제공하는 어노테이션
        "A1@abc",
        "T9%Test",
        "Z1^xyz",
        "@A1B2C",
        "#Q123"
    ])
    fun `유효한 사용자명은 정상 생성`(input: String) {

        // 자동으로 CheckUsername.invoke로 컴파일 됨
        val result = CheckUsername(input)
        println("성공: $result")
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "a1@abc",      // 대문자 없음
        "ABC@def",     // 숫자 없음
        "A1\$abc",     // 허용되지 않은 특수문자
        "abc"          // 전부 없음
    ])
    fun `유효하지 않은 사용자명은 예외가 발생`(input: String) {

        assertThrows<IllegalArgumentException> {
            CheckUsername(input)
            println("실패: $input")
        }
    }
}