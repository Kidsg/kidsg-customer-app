package com.kidsg.core.storage

import com.kidsg.domain.model.Address
import com.kidsg.domain.model.CartItem
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
        return platformGetString(KEY_AUTH_TOKEN)
    }

    fun saveAuthToken(token: String?) {
        platformPutString(KEY_AUTH_TOKEN, token)
        if (token != null) {
            platformPutString(KEY_IS_LOGGED_IN, "true")
        }
    }

    fun getUserProfile(): UserProfile? {
        val raw = platformGetString(KEY_USER_PROFILE) ?: return null
        return try {
            json.decodeFromString<UserProfile>(raw)
        } catch (_: Exception) {
            null
        }
    }

    fun saveUserProfile(profile: UserProfile?) {
        if (profile == null) {
            platformRemove(KEY_USER_PROFILE)
        } else {
            try {
                val raw = json.encodeToString(UserProfile.serializer(), profile)
                platformPutString(KEY_USER_PROFILE, raw)
                platformPutString(KEY_IS_LOGGED_IN, "true")
            } catch (_: Exception) {
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return platformGetString(KEY_IS_LOGGED_IN) == "true" && getUserProfile() != null
    }

    fun isOnboardingCompleted(): Boolean {
        return platformGetString(KEY_ONBOARDING_COMPLETED) == "true"
    }

    private const val KEY_SELECTED_CHARACTER = "kidsg_selected_character"

    fun getSelectedCharacter(): String? {
        return platformGetString(KEY_SELECTED_CHARACTER)
    }

    fun saveSelectedCharacter(character: String?) {
        if (character != null) {
            platformPutString(KEY_SELECTED_CHARACTER, character)
        } else {
            platformRemove(KEY_SELECTED_CHARACTER)
        }
    }

    private const val KEY_CURRENT_LOCATION = "kidsg_current_location"

    fun getCurrentLocation(): String? {
        return platformGetString(KEY_CURRENT_LOCATION)
    }

    fun saveCurrentLocation(location: String?) {
        if (location != null) {
            platformPutString(KEY_CURRENT_LOCATION, location)
        } else {
            platformRemove(KEY_CURRENT_LOCATION)
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        if (completed) {
            platformPutString(KEY_ONBOARDING_COMPLETED, "true")
        } else {
            platformRemove(KEY_ONBOARDING_COMPLETED)
        }
    }

    @kotlinx.serialization.Serializable
    private data class OrdersWrapper(
        val orders: List<com.kidsg.domain.model.Order> = emptyList()
    )

    private const val KEY_SAVED_ORDERS = "kidsg_saved_orders"

    fun getSavedOrders(): List<com.kidsg.domain.model.Order> {
        val raw = platformGetString(KEY_SAVED_ORDERS) ?: return emptyList()
        return try {
            json.decodeFromString<OrdersWrapper>(raw).orders
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveOrders(orders: List<com.kidsg.domain.model.Order>) {
        try {
            val raw = json.encodeToString(OrdersWrapper.serializer(), OrdersWrapper(orders))
            platformPutString(KEY_SAVED_ORDERS, raw)
        } catch (_: Exception) {
        }
    }

    fun addOrder(order: com.kidsg.domain.model.Order) {
        val current = getSavedOrders()
        saveOrders(listOf(order) + current.filterNot { it.id == order.id })
    }

    @kotlinx.serialization.Serializable
    private data class CartWrapper(
        val items: List<CartItem> = emptyList()
    )

    private const val KEY_SAVED_CART = "kidsg_saved_cart"

    fun getSavedCartItems(): List<CartItem> {
        val raw = platformGetString(KEY_SAVED_CART) ?: return emptyList()
        return try {
            json.decodeFromString<CartWrapper>(raw).items
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveCartItems(items: List<CartItem>) {
        try {
            val raw = json.encodeToString(CartWrapper.serializer(), CartWrapper(items))
            platformPutString(KEY_SAVED_CART, raw)
        } catch (_: Exception) {
        }
    }

    private const val KEY_SELECTED_ADDRESS = "kidsg_selected_address"

    fun getSelectedAddress(): Address? {
        val raw = platformGetString(KEY_SELECTED_ADDRESS) ?: return null
        return try {
            json.decodeFromString<Address>(raw)
        } catch (_: Exception) {
            null
        }
    }

    fun saveSelectedAddress(address: Address?) {
        if (address == null) {
            platformRemove(KEY_SELECTED_ADDRESS)
        } else {
            try {
                val raw = json.encodeToString(Address.serializer(), address)
                platformPutString(KEY_SELECTED_ADDRESS, raw)
            } catch (_: Exception) {
            }
        }
    }

    fun clearSession() {
        platformRemove(KEY_AUTH_TOKEN)
        platformRemove(KEY_USER_PROFILE)
        platformRemove(KEY_IS_LOGGED_IN)
    }
}
