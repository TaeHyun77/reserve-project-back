package com.example.kotlin.performance

import com.example.kotlin.screenInfo.ScreenInfo
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany

@Entity
class Performance(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_id")
    val id: Long? = null,

    val type: String,

    val title: String,

    val duration: String,

    var price: Long,

    @OneToMany(mappedBy = "performance", cascade = [CascadeType.ALL], orphanRemoval = true)
    val screenInfoList: List<ScreenInfo> = ArrayList()
)