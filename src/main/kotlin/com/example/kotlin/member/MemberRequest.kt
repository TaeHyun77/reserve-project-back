package com.example.kotlin.member

data class MemberRequest (
    val username: String,

    val password: String,

    val name: String,

    val role: Role?,

    val email: String
) {
    fun toEntity(password: String): Member {
        return Member(
            username = username,
            password = password,
            name = name,
            role = role,
            email = email
        )
    }
}