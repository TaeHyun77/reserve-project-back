package com.example.kotlin.reserveException

enum class ErrorCode (
    val errorCode: String,
    val message: String
){
    UNKNOWN("000_UNKNOWN", "알 수 없는 에러 발생"),


    // NOT EXIST
    NOT_EXIST_MEMBER_INFO("NOT_EXIST_MEMBER_INFO", "사용자 정보를 찾을 수 없습니다."),

    NOT_EXIST_RESERVE_INFO("NOT_EXIST_RESERVE_INFO", "예약 정보를 찾을 수 없습니다."),

    NOT_EXIST_PLACE_INFO("NOT_EXIST_PLACE_INFO", "장소 정보를 찾을 수 없습니다."),

    NOT_EXIST_PERFORMANCE_INFO("NOT_EXIST_PERFORMANCE_INFO", "공연 정보를 찾을 수 없습니다."),

    NOT_EXIST_SCREEN_INFO("NOT_EXIST_SCREEN_INFO", "상영 정보를 찾을 수 없습니다."),

    NOT_EXIST_SEAT_INFO("NOT_EXIST_SEAT_INFO", "좌석 정보를 찾을 수 없습니다."),

    NOT_EXIST_AUTHORIZATION_IN_HEADER("NOT_EXIST_AUTHORIZATION_IN_HEADER", "Header에 Authorization이 존재하지 않습니다."),

    NOT_EXIST_IN_HEADER_IDEMPOTENCY_KEY("NOT_EXIST_IN_HEADER_IDEMPOTENCY_KEY", "Idempotency-Key 헤더 누락"),

    NOT_EXIST_IDEMPOTENCY_KEY_INFO("NOT_EXIST_IDEMPOTENCY_KEY_INFO", "Idempotency-Key 정보를 찾을 수 없습니다."),


    // TOKEN
    ACCESSTOKEN_ISEXPIRED("ACCESSTOKEN_ISEXPIRED", "JWT 토큰 만료"),

    INVALID_TOKEN("INVALID_TOKEN", "유효하지 않은 토큰"),

    IS_NOT_ACCESSTOKEN("IS_NOT_ACCESSTOKEN", "JWT 토큰이 아닙니다."),


    // RESERVE
    SEAT_ALREADY_RESERVED("SEAT_ALREADY_RESERVED", "이미 예약된 좌석입니다."),

    FAIL_TO_RETURN_RESERVED_SEAT_LIST("FAIL_TO_RETURN_RESERVED_SEAT_LIST", "예약 좌석 리스트 반환 실패"),

    NOT_ENOUGH_CREDIT("NOT_ENOUGH_CREDIT", "보유 금액이 부족합니다."),


    // JOIN
    INVALID_USERNAME("INVALID_USERNAME", "유효하지 않은 username 값 입니다."),

    DUPLICATED_USERNAME("DUPLICATED_USERNAME", "중복된 username 값 입니다."),


    // REWARD
    REWARD_ALREADY_CLAIMED("REWARD_ALREADY_CLAIMED", "오늘 이미 리워드를 지급받았습니다."),


    // ECT
    FAIL_TO_SAVE_DATA("FAIL_TO_SAVE_DATA", "데이터 저장 실패"),

    CANNOT_DELETE_SOME_SCREENING_HAVE_NOT_YET_ENDED("CANNOT_DELETE_SOME_SCREENING_HAVE_NOT_YET_ENDED", "아직 종료되지 않은 상영 정보가 있어 삭제할 수 없습니다."),

    REDIS_FAILED_TO_ACQUIRED_LOCK("REDIS_FAILED_TO_ACQUIRED_LOCK", "레디스에서 Lock을 획득 실패")
}