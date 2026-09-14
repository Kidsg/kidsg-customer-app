package com.kidsg.data.repository

import com.kidsg.core.network.ApiResult
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
            items = emptyList(),
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
        when (val res = cartApi.getCart()) {
            is ApiResult.Success -> {
                val domainItems = res.data.items.map { it.toDomain() }
                _cartState.update { current ->
                    current.copy(items = domainItems)
                }
            }
            is ApiResult.Error -> {
                // Keep current state if network error
            }
        }
    }

    override suspend fun addToCart(product: Product, quantity: Int, variant: String?) {
        // Optimistic local update
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
            current.copy(items = newItems)
        }

        // Authoritative server call
        when (val res = cartApi.addToCart(product.id, quantity, variant)) {
            is ApiResult.Success -> {
                val domainItems = res.data.items.map { it.toDomain() }
                _cartState.update { it.copy(items = domainItems) }
            }
            is ApiResult.Error -> {
                // Fallback kept optimistically
            }
        }
    }

    override suspend fun removeFromCart(productId: String) {
        _cartState.update { current ->
            current.copy(items = current.items.filter { it.product.id !== productId })
        }
        cartApi.removeFromCart(productId)
    }

    override suspend fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }

        _cartState.update { current ->
            current.copy(
                items = current.items.map {
                    if (it.product.id == productId) it.copy(quantity = quantity) else it
                }
            )
        }
        cartApi.updateCartItem(productId, quantity)
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
        _cartState.update { it.copy(items = emptyList(), appliedCoupon = null) }
        cartApi.clearCart()
    }
}
