package com.example.kotlin.member

import java.time.LocalDate

data class MemberResponse(

    val id: Long? = null,

    val username: String,

    val name: String,

    val role: Role,

    val email: String,

    var last_reward_date: LocalDate?
)