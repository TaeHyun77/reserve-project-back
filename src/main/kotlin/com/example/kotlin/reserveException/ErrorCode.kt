package com.example.kotlin.reserveException

enum class ErrorCode (
    val errorCode: String,
    val message: String
){

    UNKNOWN("000_UNKNOWN", "알 수 없는 에러 발생"),

    ACCESSTOKEN_ISEXPIRED("ACCESSTOKEN_ISEXPIRED", "JWT 토큰 만료"),

    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰"),

    IS_NOT_ACCESSTOKEN("IS_NOT_ACCESSTOKEN", "JWT 토큼이 아닙니다."),

    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "사용자를 찾을 수 없습니다."),

    PLACE_NOT_FOUND("PLACE_NOT_FOUND", "장소를 찾을 수 없습니다."),

    PERFORMANCE_NOT_FOUND("PERFORMANCE_NOT_FOUND", "공연을 찾을 수 없습니다."),

    SCREEN_INFO_NOT_FOUND("SCREEN_INFO_NOT_FOUND", "상영 정보를 찾을 수 없습니다."),

    SEAT_NOT_FOUND("SEAT_NOT_FOUND", "좌석 정보를 찾을 수 없습니다."),

    SEAT_ALREADY_RESERVED("SEAT_ALREADY_RESERVED", "이미 예약된 좌석입니다."),

    FAIL_TO_SAVE_DATA("FAIL_TO_SAVE_DATA", "데이터 저장 실패"),

    FAIL_TO_RETURN_RESERVED_SEAT_LIST("FAIL_TO_RETURN_RESERVED_SEAT_LIST", "예약 좌석 리스트 반환 실해")

}