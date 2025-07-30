package com.example.kotlin.screenInfo

import org.springframework.data.jpa.repository.JpaRepository

interface ScreenInfoRepository: JpaRepository<ScreenInfo, Long>, ScreenInfoRepositoryCustom {
}