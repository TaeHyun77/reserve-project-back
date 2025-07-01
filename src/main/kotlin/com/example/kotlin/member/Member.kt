package com.example.kotlin.member

import com.example.kotlin.BaseTime
import com.example.kotlin.reserveInfo.ReserveInfo
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
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

    var last_reward_date: LocalDate? = null,

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    val reserveList: List<ReserveInfo>? = null

): BaseTime() {

    fun updateCreditAndReward(credit: Long, reward: Long) {
        this.credit -= credit
        this.reward -= reward
    }
}