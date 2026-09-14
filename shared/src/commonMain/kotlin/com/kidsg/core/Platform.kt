package com.kidsg.core

interface Platform {
    val name: String
    val isAndroid: Boolean
    val isIos: Boolean
}

expect fun getPlatform(): Platform
