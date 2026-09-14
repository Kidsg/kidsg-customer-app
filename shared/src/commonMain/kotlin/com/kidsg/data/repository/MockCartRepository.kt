package com.kidsg.data.repository

import com.kidsg.data.mock.KidsGMockData
import com.kidsg.domain.model.Cart
import com.kidsg.domain.model.CartItem
import com.kidsg.domain.model.Coupon
import com.kidsg.domain.model.Product
import com.kidsg.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MockCartRepository : CartRepository {

    private val _cartState = MutableStateFlow(
        Cart(
            items = emptyList(),
            appliedCoupon = null,
            deliveryConfig = KidsGMockData.deliveryConfig,
            partnerStore = KidsGMockData.partnerStore
        )
    )
    override val cartState: StateFlow<Cart> = _cartState.asStateFlow()

    override suspend fun addToCart(product: Product, quantity: Int, variant: String?) {
        _cartState.update { current ->
            val existingIndex = current.items.indexOfFirst { it.product.id == product.id && it.selectedVariant == variant }
            val newItems = current.items.toMutableList()
            if (existingIndex >= 0) {
                val existing = newItems[existingIndex]
                newItems[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
            } else {
                newItems.add(CartItem(product = product, quantity = quantity, selectedVariant = variant))
            }
            current.copy(items = newItems)
        }
    }

    override suspend fun removeFromCart(productId: String) {
        _cartState.update { current ->
            val newItems = current.items.filterNot { it.product.id == productId }
            current.copy(items = newItems)
        }
    }

    override suspend fun updateQuantity(productId: String, quantity: Int) {
        _cartState.update { current ->
            if (quantity <= 0) {
                current.copy(items = current.items.filterNot { it.product.id == productId })
            } else {
                val newItems = current.items.map { item ->
                    if (item.product.id == productId) item.copy(quantity = quantity) else item
                }
                current.copy(items = newItems)
            }
        }
    }

    override suspend fun applyCoupon(couponCode: String): Result<Coupon> {
        val coupon = KidsGMockData.coupons.find { it.code.equals(couponCode.trim(), ignoreCase = true) }
            ?: return Result.failure(IllegalArgumentException("Invalid coupon code '$couponCode'"))

        val currentCart = _cartState.value
        if (currentCart.subtotal < coupon.minOrderValue) {
            return Result.failure(IllegalStateException("Add ₹${(coupon.minOrderValue - currentCart.subtotal).toInt()} more to apply ${coupon.code}"))
        }

        _cartState.update { it.copy(appliedCoupon = coupon) }
        return Result.success(coupon)
    }

    override suspend fun removeCoupon() {
        _cartState.update { it.copy(appliedCoupon = null) }
    }

    override suspend fun clearCart() {
        _cartState.update { it.copy(items = emptyList(), appliedCoupon = null) }
    }
}
