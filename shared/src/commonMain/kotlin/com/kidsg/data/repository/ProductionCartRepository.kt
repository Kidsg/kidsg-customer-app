package com.kidsg.data.repository

import com.kidsg.core.config.ApiConfig
import com.kidsg.core.network.ApiResult
import com.kidsg.core.storage.SessionStorage
import com.kidsg.data.remote.api.CartApi
import com.kidsg.data.remote.mapper.toDomain
import com.kidsg.domain.model.*
import com.kidsg.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProductionCartRepository(
    private val cartApi: CartApi = CartApi()
) : CartRepository {

    private val _cartState = MutableStateFlow(
        Cart(
            items = SessionStorage.getSavedCartItems(),
            appliedCoupon = null,
            deliveryConfig = DeliveryConfig(),
            partnerStore = Store(
                id = "store_vidya_depot",
                name = "Vidya Book & Stationery Depot",
                locality = "Koramangala 4th Block",
                distanceKm = 0.8,
                rating = 4.8,
                prepTimeMinutes = 12,
                address = "12th Main Road, Bengaluru"
            )
        )
    )
    override val cartState: StateFlow<Cart> = _cartState.asStateFlow()

    suspend fun refreshCart() {
        if (ApiConfig.authToken.isNullOrBlank()) {
            // Guest or unauthenticated mode: preserve and sync with local storage
            val saved = SessionStorage.getSavedCartItems()
            if (saved.isNotEmpty() && _cartState.value.items.isEmpty()) {
                _cartState.update { it.copy(items = saved) }
            }
            return
        }

        when (val res = cartApi.getCart()) {
            is ApiResult.Success -> {
                val serverItems = res.data.items.map { it.toDomain() }
                _cartState.update { current ->
                    if (serverItems.isNotEmpty()) {
                        SessionStorage.saveCartItems(serverItems)
                        current.copy(items = serverItems)
                    } else if (current.items.isNotEmpty()) {
                        // Preserve active local cart items rather than wiping them out
                        SessionStorage.saveCartItems(current.items)
                        current
                    } else {
                        val saved = SessionStorage.getSavedCartItems()
                        if (saved.isNotEmpty()) {
                            current.copy(items = saved)
                        } else {
                            SessionStorage.saveCartItems(emptyList())
                            current.copy(items = emptyList())
                        }
                    }
                }
                // Push local items to server if server was empty
                val currentItems = _cartState.value.items
                if (serverItems.isEmpty() && currentItems.isNotEmpty()) {
                    for (item in currentItems) {
                        try {
                            cartApi.addToCart(item.product.id, item.quantity, item.selectedVariant)
                        } catch (_: Exception) {
                        }
                    }
                }
            }
            is ApiResult.Error -> {
                // Keep current state and fallback to saved cart on network error
                val saved = SessionStorage.getSavedCartItems()
                if (saved.isNotEmpty() && _cartState.value.items.isEmpty()) {
                    _cartState.update { it.copy(items = saved) }
                }
            }
        }
    }

    override suspend fun addToCart(product: Product, quantity: Int, variant: String?) {
        // Optimistic local update & persistent storage
        _cartState.update { current ->
            val existing = current.items.find { it.product.id == product.id && it.selectedVariant == variant }
            val newItems = if (existing != null) {
                current.items.map {
                    if (it.product.id == product.id && it.selectedVariant == variant) {
                        it.copy(quantity = it.quantity + quantity)
                    } else it
                }
            } else {
                current.items + CartItem(product = product, quantity = quantity, selectedVariant = variant)
            }
            SessionStorage.saveCartItems(newItems)
            current.copy(items = newItems)
        }

        // Authoritative server call if authenticated
        if (!ApiConfig.authToken.isNullOrBlank()) {
            when (val res = cartApi.addToCart(product.id, quantity, variant)) {
                is ApiResult.Success -> {
                    val domainItems = res.data.items.map { it.toDomain() }
                    if (domainItems.isNotEmpty()) {
                        SessionStorage.saveCartItems(domainItems)
                        _cartState.update { it.copy(items = domainItems) }
                    }
                }
                is ApiResult.Error -> {
                    // Fallback kept optimistically
                }
            }
        }
    }

    override suspend fun removeFromCart(productId: String) {
        _cartState.update { current ->
            val updated = current.items.filter { it.product.id != productId }
            SessionStorage.saveCartItems(updated)
            current.copy(items = updated)
        }
        if (!ApiConfig.authToken.isNullOrBlank()) {
            try {
                cartApi.removeFromCart(productId)
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }

        _cartState.update { current ->
            val updated = current.items.map {
                if (it.product.id == productId) it.copy(quantity = quantity) else it
            }
            SessionStorage.saveCartItems(updated)
            current.copy(items = updated)
        }
        if (!ApiConfig.authToken.isNullOrBlank()) {
            try {
                cartApi.updateCartItem(productId, quantity)
            } catch (_: Exception) {
            }
        }
    }

    override suspend fun applyCoupon(couponCode: String): Result<Coupon> {
        return when (val res = cartApi.validateCoupon(couponCode)) {
            is ApiResult.Success -> {
                val coupon = Coupon(
                    code = res.data.code,
                    title = "KidsG Promo",
                    description = res.data.description,
                    discountPercent = 15.0,
                    maxDiscount = res.data.discountAmount,
                    minOrderValue = 199.0,
                    expiryText = "Valid for this order"
                )
                _cartState.update { it.copy(appliedCoupon = coupon) }
                Result.success(coupon)
            }
            is ApiResult.Error -> {
                Result.failure(Exception(res.exception.userFriendlyMessage))
            }
        }
    }

    override suspend fun removeCoupon() {
        _cartState.update { it.copy(appliedCoupon = null) }
    }

    override suspend fun clearCart() {
        SessionStorage.saveCartItems(emptyList())
        _cartState.update { it.copy(items = emptyList(), appliedCoupon = null) }
        if (!ApiConfig.authToken.isNullOrBlank()) {
            try {
                cartApi.clearCart()
            } catch (_: Exception) {
            }
        }
    }
}
