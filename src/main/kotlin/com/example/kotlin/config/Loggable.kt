package com.example.kotlin.config

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

interface Loggable {
    val log: KLogger
        get() = KotlinLogging.logger(this::class.java.simpleName)
}