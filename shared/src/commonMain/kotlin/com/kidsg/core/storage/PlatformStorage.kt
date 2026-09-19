package com.kidsg.core.storage

expect fun platformGetString(key: String): String?
expect fun platformPutString(key: String, value: String?)
expect fun platformRemove(key: String)
expect fun platformClear()
