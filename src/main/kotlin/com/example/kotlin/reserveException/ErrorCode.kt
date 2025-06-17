package com.example.kotlin.reserveException

enum class ErrorCode (
    val errorCode: String,
    val message: String
){

    UNKNOWN("000_UNKNOWN", "알 수 없는 에러 발생"),

    ACCESSTOKEN_ISEXPIRED("ACCESSTOKEN_ISEXPIRED", "JWT 토큰 만료"),

    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰"),

    IS_NOT_ACCESSTOKEN("IS_NOT_ACCESSTOKEN", "JWT 토큼이 아닙니다."),

    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "사용자를 찾을 수 없습니다.")

}