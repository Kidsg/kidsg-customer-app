package com.kidsg.core.storage

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri

private var appContext: Context? = null
private val memoryFallback = mutableMapOf<String, String>()

private val prefs: SharedPreferences?
    get() = appContext?.getSharedPreferences("kidsg_session_prefs", Context.MODE_PRIVATE)

actual fun platformGetString(key: String): String? {
    return try {
        prefs?.getString(key, null) ?: memoryFallback[key]
    } catch (_: Exception) {
        memoryFallback[key]
    }
}

actual fun platformPutString(key: String, value: String?) {
    if (value == null) {
        platformRemove(key)
    } else {
        memoryFallback[key] = value
        try {
            prefs?.edit()?.putString(key, value)?.apply()
        } catch (_: Exception) {
        }
    }
}

actual fun platformRemove(key: String) {
    memoryFallback.remove(key)
    try {
        prefs?.edit()?.remove(key)?.apply()
    } catch (_: Exception) {
    }
}

actual fun platformClear() {
    memoryFallback.clear()
    try {
        prefs?.edit()?.clear()?.apply()
    } catch (_: Exception) {
    }
}

/**
 * Auto-initializes application context before any Activity or Composable starts.
 */
class KidsGStorageProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        appContext = context?.applicationContext
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
