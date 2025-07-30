package com.example.kotlin.member

import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository: JpaRepository<Member, Long> {

    fun findByUsername(username: String): Member?

    fun existsByUsername(username: CheckUsername): Boolean
}