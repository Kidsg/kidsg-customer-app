package com.kidsg.core.config

enum class AppEnvironment(val displayName: String) {
    DEV("Development"),
    STAGING("Staging"),
    PRODUCTION("Production")
}

object ApiConfig {
    var currentEnvironment: AppEnvironment = AppEnvironment.DEV
    var isMockBackend: Boolean = false // Toggle between MockBackend and ProductionBackend
    var authToken: String? = "dev-token-user_dev_default"

    val baseUrl: String
        get() = when (currentEnvironment) {
            AppEnvironment.DEV -> "http://10.0.2.2:3000" // Standard Android emulator loopback
            AppEnvironment.STAGING -> "https://kidsg-api-staging.vercel.app"
            AppEnvironment.PRODUCTION -> "https://api.kidsg.in"
        }

    val requestTimeoutMs: Long = 15_000L
    val connectTimeoutMs: Long = 10_000L
}
