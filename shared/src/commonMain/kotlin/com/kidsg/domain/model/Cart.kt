package com.kidsg.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val product: Product,
    val quantity: Int = 1,
    val selectedVariant: String? = null
) {
    val totalItemPrice: Double
        get() = product.price * quantity

    val totalItemMrp: Double
        get() = product.mrp * quantity

    val totalItemSavings: Double
        get() = (totalItemMrp - totalItemPrice).coerceAtLeast(0.0)
}

@Serializable
data class DeliveryConfig(
    val baseDeliveryFee: Double = 30.0,
    val freeDeliveryThreshold: Double = 199.0,
    val platformFee: Double = 5.0,
    val taxRatePercent: Double = 5.0,
    val minOrderValue: Double = 50.0,
    val expressEtaMinutesMin: Int = 12,
    val expressEtaMinutesMax: Int = 18
)

@Serializable
data class Cart(
    val items: List<CartItem> = emptyList(),
    val appliedCoupon: Coupon? = null,
    val deliveryConfig: DeliveryConfig = DeliveryConfig(),
    val partnerStore: Store? = null
) {
    val itemCount: Int
        get() = items.sumOf { it.quantity }

    val subtotal: Double
        get() = items.sumOf { it.totalItemPrice }

    val totalMrp: Double
        get() = items.sumOf { it.totalItemMrp }

    val productDiscount: Double
        get() = (totalMrp - subtotal).coerceAtLeast(0.0)

    val isFreeDeliveryEligible: Boolean
        get() = subtotal >= deliveryConfig.freeDeliveryThreshold

    val deliveryFee: Double
        get() = if (items.isEmpty() || isFreeDeliveryEligible) 0.0 else deliveryConfig.baseDeliveryFee

    val couponDiscount: Double
        get() {
            val coupon = appliedCoupon ?: return 0.0
            if (subtotal < coupon.minOrderValue) return 0.0
            val discount = (subtotal * coupon.discountPercent) / 100.0
            return discount.coerceAtMost(coupon.maxDiscount)
        }

    val taxAmount: Double
        get() = if (items.isEmpty()) 0.0 else ((subtotal - couponDiscount).coerceAtLeast(0.0) * deliveryConfig.taxRatePercent) / 100.0

    val platformFee: Double
        get() = if (items.isEmpty()) 0.0 else deliveryConfig.platformFee

    val finalTotal: Double
        get() = if (items.isEmpty()) 0.0 else (subtotal - couponDiscount + deliveryFee + taxAmount + platformFee).coerceAtLeast(0.0)

    val totalSavings: Double
        get() = productDiscount + couponDiscount + (if (isFreeDeliveryEligible && items.isNotEmpty()) deliveryConfig.baseDeliveryFee else 0.0)
}
