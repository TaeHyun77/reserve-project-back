package com.example.kotlin.member

import com.example.kotlin.reserveInfo.ReserveInfo
import com.example.kotlin.reserveInfo.ReserveInfoResponse
import java.time.LocalDate
import java.time.LocalDateTime

data class MemberResponse(

    val id: Long? = null,

    val username: String,

    val name: String,

    val role: Role,

    val email: String,

    var last_reward_date: LocalDate?,

    val credit: Long,

    val reward: Long,

    val reserveList: List<ReserveInfoResponse>? = null
)