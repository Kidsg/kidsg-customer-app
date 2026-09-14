package com.kidsg.core.storage

import android.content.Context
import android.content.SharedPreferences

actual object PlatformStorage {
    private const val PREFS_NAME = "kidsg_session_prefs"
    private var prefs: SharedPreferences? = null
    private val memoryFallback = mutableMapOf<String, String>()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            // Migrate any in-memory values stored prior to init
            memoryFallback.forEach { (k, v) ->
                prefs?.edit()?.putString(k, v)?.apply()
            }
            memoryFallback.clear()
        }
    }

    actual fun getString(key: String): String? {
        return prefs?.getString(key, null) ?: memoryFallback[key]
    }

    actual fun putString(key: String, value: String?) {
        if (value == null) {
            remove(key)
        } else {
            memoryFallback[key] = value
            prefs?.edit()?.putString(key, value)?.apply()
        }
    }

    actual fun remove(key: String) {
        memoryFallback.remove(key)
        prefs?.edit()?.remove(key)?.apply()
    }

    actual fun clear() {
        memoryFallback.clear()
        prefs?.edit()?.clear()?.apply()
    }
}
