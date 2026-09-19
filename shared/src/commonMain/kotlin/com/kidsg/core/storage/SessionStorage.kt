package com.kidsg.core.storage

import com.kidsg.domain.model.UserProfile
import kotlinx.serialization.json.Json

object SessionStorage {
    private const val KEY_AUTH_TOKEN = "kidsg_auth_token"
    private const val KEY_USER_PROFILE = "kidsg_user_profile"
    private const val KEY_IS_LOGGED_IN = "kidsg_is_logged_in"
    private const val KEY_ONBOARDING_COMPLETED = "kidsg_onboarding_completed"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun getAuthToken(): String? {
        return PlatformStorage.getString(KEY_AUTH_TOKEN)
    }

    fun saveAuthToken(token: String?) {
        PlatformStorage.putString(KEY_AUTH_TOKEN, token)
        if (token != null) {
            PlatformStorage.putString(KEY_IS_LOGGED_IN, "true")
        }
    }

    fun getUserProfile(): UserProfile? {
        val raw = PlatformStorage.getString(KEY_USER_PROFILE) ?: return null
        return try {
            json.decodeFromString<UserProfile>(raw)
        } catch (_: Exception) {
            null
        }
    }

    fun saveUserProfile(profile: UserProfile?) {
        if (profile == null) {
            PlatformStorage.remove(KEY_USER_PROFILE)
        } else {
            try {
                val raw = json.encodeToString(UserProfile.serializer(), profile)
                PlatformStorage.putString(KEY_USER_PROFILE, raw)
                PlatformStorage.putString(KEY_IS_LOGGED_IN, "true")
            } catch (_: Exception) {
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return PlatformStorage.getString(KEY_IS_LOGGED_IN) == "true" && getUserProfile() != null
    }

    fun isOnboardingCompleted(): Boolean {
        return PlatformStorage.getString(KEY_ONBOARDING_COMPLETED) == "true"
    }

    private const val KEY_SELECTED_CHARACTER = "kidsg_selected_character"

    fun getSelectedCharacter(): String? {
        return PlatformStorage.getString(KEY_SELECTED_CHARACTER)
    }

    fun saveSelectedCharacter(character: String?) {
        if (character != null) {
            PlatformStorage.putString(KEY_SELECTED_CHARACTER, character)
        } else {
            PlatformStorage.remove(KEY_SELECTED_CHARACTER)
        }
    }

    private const val KEY_CURRENT_LOCATION = "kidsg_current_location"

    fun getCurrentLocation(): String? {
        return PlatformStorage.getString(KEY_CURRENT_LOCATION)
    }

    fun saveCurrentLocation(location: String?) {
        if (location != null) {
            PlatformStorage.putString(KEY_CURRENT_LOCATION, location)
        } else {
            PlatformStorage.remove(KEY_CURRENT_LOCATION)
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        if (completed) {
            PlatformStorage.putString(KEY_ONBOARDING_COMPLETED, "true")
        } else {
            PlatformStorage.remove(KEY_ONBOARDING_COMPLETED)
        }
    }

    @kotlinx.serialization.Serializable
    data class AccountRecord(
        val email: String,
        val passwordHash: String,
        val profile: UserProfile
    )

    @kotlinx.serialization.Serializable
    private data class AccountsWrapper(
        val accounts: Map<String, AccountRecord> = emptyMap()
    )

    @kotlinx.serialization.Serializable
    private data class OrdersWrapper(
        val orders: List<com.kidsg.domain.model.Order> = emptyList()
    )

    private const val KEY_ACCOUNTS_MAP = "kidsg_accounts_map"
    private const val KEY_SAVED_ORDERS = "kidsg_saved_orders"

    fun getAccounts(): Map<String, AccountRecord> {
        val raw = PlatformStorage.getString(KEY_ACCOUNTS_MAP) ?: return emptyMap()
        return try {
            json.decodeFromString<AccountsWrapper>(raw).accounts
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun saveAccount(account: AccountRecord) {
        val current = getAccounts().toMutableMap()
        current[account.email.trim().lowercase()] = account
        try {
            val raw = json.encodeToString(AccountsWrapper.serializer(), AccountsWrapper(current))
            PlatformStorage.putString(KEY_ACCOUNTS_MAP, raw)
        } catch (_: Exception) {
        }
    }

    fun getAccount(email: String): AccountRecord? {
        return getAccounts()[email.trim().lowercase()]
    }

    fun getSavedOrders(): List<com.kidsg.domain.model.Order> {
        val raw = PlatformStorage.getString(KEY_SAVED_ORDERS) ?: return emptyList()
        return try {
            json.decodeFromString<OrdersWrapper>(raw).orders
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveOrders(orders: List<com.kidsg.domain.model.Order>) {
        try {
            val raw = json.encodeToString(OrdersWrapper.serializer(), OrdersWrapper(orders))
            PlatformStorage.putString(KEY_SAVED_ORDERS, raw)
        } catch (_: Exception) {
        }
    }

    fun addOrder(order: com.kidsg.domain.model.Order) {
        val current = getSavedOrders()
        saveOrders(listOf(order) + current.filterNot { it.id == order.id })
    }

    private const val KEY_PENDING_PASSWORD = "kidsg_pending_password"

    fun savePendingPassword(password: String?) {
        if (password != null) {
            PlatformStorage.putString(KEY_PENDING_PASSWORD, password)
        } else {
            PlatformStorage.remove(KEY_PENDING_PASSWORD)
        }
    }

    fun getPendingPassword(): String? {
        return PlatformStorage.getString(KEY_PENDING_PASSWORD)
    }

    fun clearSession() {
        PlatformStorage.remove(KEY_AUTH_TOKEN)
        PlatformStorage.remove(KEY_USER_PROFILE)
        PlatformStorage.remove(KEY_IS_LOGGED_IN)
        PlatformStorage.remove(KEY_PENDING_PASSWORD)
    }
}
