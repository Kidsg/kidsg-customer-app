package com.kidsg.core.storage

expect object PlatformStorage {
    fun getString(key: String): String?
    fun putString(key: String, value: String?)
    fun remove(key: String)
    fun clear()
}
