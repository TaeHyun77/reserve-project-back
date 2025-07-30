package com.example.kotlin.member

data class MemberRequest (
    val username: CheckUsername,

    val password: String,

    val name: String,

    val role: Role = Role.MEMBER,

    val email: String,

    var reward: Long
) {
    fun toEntity(password: String): Member {
        return Member(
            username = this.username.username,
            password = password,
            name = this.name,
            role = this.role,
            email = this.email,
            reward = 0
        )
    }
}