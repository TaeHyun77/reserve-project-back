package com.example.kotlin.member

import com.example.kotlin.BaseTime
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val username: String,

    val password: String,

    val name: String,

    val role: Role,

    val email: String,

    val credit: Long = 30000
): BaseTime()