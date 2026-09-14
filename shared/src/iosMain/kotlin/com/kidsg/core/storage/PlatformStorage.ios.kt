package com.kidsg.core.storage

import platform.Foundation.NSUserDefaults

actual object PlatformStorage {
    private val memoryStorage = mutableMapOf<String, String>()

    actual fun getString(key: String): String? {
        return NSUserDefaults.standardUserDefaults.stringForKey(key) ?: memoryStorage[key]
    }

    actual fun putString(key: String, value: String?) {
        if (value == null) {
            remove(key)
        } else {
            memoryStorage[key] = value
            NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
        }
    }

    actual fun remove(key: String) {
        memoryStorage.remove(key)
        NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
    }

    actual fun clear() {
        memoryStorage.clear()
        val appDomain = platform.Foundation.NSBundle.mainBundle.bundleIdentifier
        if (appDomain != null) {
            NSUserDefaults.standardUserDefaults.removePersistentDomainForName(appDomain)
        }
    }
}
