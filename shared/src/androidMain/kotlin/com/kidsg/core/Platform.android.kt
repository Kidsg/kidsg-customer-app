package com.kidsg.core

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
    override val isAndroid: Boolean = true
    override val isIos: Boolean = false
}

actual fun getPlatform(): Platform = AndroidPlatform()
