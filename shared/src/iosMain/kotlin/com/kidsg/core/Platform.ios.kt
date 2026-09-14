package com.kidsg.core

class IOSPlatform : Platform {
    override val name: String = "iOS"
    override val isAndroid: Boolean = false
    override val isIos: Boolean = true
}

actual fun getPlatform(): Platform = IOSPlatform()
