package com.kidsg.core.storage

import platform.Foundation.NSUserDefaults

actual fun platformGetString(key: String): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey(key)
}

actual fun platformPutString(key: String, value: String?) {
    if (value == null) {
        platformRemove(key)
    } else {
        NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
    }
}

actual fun platformRemove(key: String) {
    NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
}

actual fun platformClear() {
    val dict = NSUserDefaults.standardUserDefaults.dictionaryRepresentation()
    dict.keys.forEach { k ->
        NSUserDefaults.standardUserDefaults.removeObjectForKey(k.toString())
    }
}
