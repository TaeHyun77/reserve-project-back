package com.example.kotlin.member

data class MemberResponse(

    val id: Long? = null,

    val username: String,

    val name: String,

    val role: Role,

    val email: String
)