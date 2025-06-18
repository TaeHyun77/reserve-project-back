package com.example.kotlin.screenInfo

import com.example.kotlin.performance.Performance
import org.springframework.data.jpa.repository.JpaRepository

interface ScreenInfoRepository: JpaRepository<ScreenInfo, Long> {

}