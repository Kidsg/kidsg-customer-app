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
            AppEnvironment.DEV -> "https://kidsg-customer-app.vercel.app"
            AppEnvironment.STAGING -> "https://kidsg-customer-app.vercel.app"
            AppEnvironment.PRODUCTION -> "https://kidsg-customer-app.vercel.app"
        }

    val requestTimeoutMs: Long = 15_000L
    val connectTimeoutMs: Long = 10_000L
}
