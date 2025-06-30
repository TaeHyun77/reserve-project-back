package com.example.kotlin.member

import com.example.kotlin.BaseTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDate

@Entity
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true)
    val username: String,

    val password: String,

    val name: String,

    val role: Role,

    val email: String,

    var credit: Long = 30000,

    var reward: Long,

    var last_reward_date: LocalDate? = null
): BaseTime() {

    fun updateCreditAndReward(credit: Long, reward: Long) {
        this.credit -= credit
        this.reward -= reward
    }
}