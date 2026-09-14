package com.kidsg.domain.repository

import com.kidsg.domain.model.Cart
import com.kidsg.domain.model.Coupon
import com.kidsg.domain.model.DeliveryConfig
import com.kidsg.domain.model.Product
import com.kidsg.domain.model.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val cartState: StateFlow<Cart>
    suspend fun addToCart(product: Product, quantity: Int = 1, variant: String? = null)
    suspend fun removeFromCart(productId: String)
    suspend fun updateQuantity(productId: String, quantity: Int)
    suspend fun applyCoupon(couponCode: String): Result<Coupon>
    suspend fun removeCoupon()
    suspend fun clearCart()
}

/**
 * Dynamic business configurations repository (Rule #5)
 * Controls delivery times, fees, thresholds, taxes, partner stores, and coupons.
 */
interface ConfigRepository {
    suspend fun getDeliveryConfig(): DeliveryConfig
    suspend fun getAvailableCoupons(): List<Coupon>
    suspend fun getNearestPartnerStore(userPincode: String): Store
}
