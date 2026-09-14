package com.kidsg.core.storage

import com.kidsg.domain.model.UserProfile
import kotlinx.serialization.json.Json

object SessionStorage {
    private const val KEY_AUTH_TOKEN = "kidsg_auth_token"
    private const val KEY_USER_PROFILE = "kidsg_user_profile"
    private const val KEY_IS_LOGGED_IN = "kidsg_is_logged_in"

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

    fun clearSession() {
        PlatformStorage.remove(KEY_AUTH_TOKEN)
        PlatformStorage.remove(KEY_USER_PROFILE)
        PlatformStorage.remove(KEY_IS_LOGGED_IN)
    }
}
