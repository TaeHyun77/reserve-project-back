package com.example.kotlin.refresh

import com.example.kotlin.BaseTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Refresh(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column
    val username: String,

    @Column(length = 2048)
    val refresh: String,

    @Column
    val expiration: Long
) : BaseTime() {
}