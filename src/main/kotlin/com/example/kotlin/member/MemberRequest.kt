package com.example.kotlin.member

data class MemberRequest (
    val username: CheckUsername,

    val password: String,

    val name: String,

    val role: Role = Role.MEMBER,

    val email: String
) {
    fun toEntity(password: String): Member {
        return Member(
            username = username.username,
            password = password,
            name = name,
            role = role,
            email = email
        )
    }
}