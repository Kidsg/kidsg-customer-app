package com.kidsg.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CartItemDto(
    val id: String,
    val productId: String,
    val quantity: Int,
    val selectedVariant: String? = null,
    val product: ProductDto? = null
)

@Serializable
data class CartResponseDto(
    val items: List<CartItemDto> = emptyList(),
    val subtotal: Double = 0.0,
    val totalItems: Int = 0
)

@Serializable
data class CouponDto(
    val id: String,
    val code: String,
    val description: String,
    val discountType: String,
    val discountValue: Double,
    val minOrderValue: Double = 0.0,
    val maxDiscountAmount: Double? = null,
    val validUntil: String,
    val isActive: Boolean = true
)

@Serializable
data class CouponValidationResponseDto(
    val valid: Boolean,
    val code: String,
    val discountAmount: Double,
    val description: String,
    val message: String
)

@Serializable
data class CheckoutPreviewDto(
    val items: List<CartItemDto> = emptyList(),
    val subtotal: Double,
    val discount: Double = 0.0,
    val couponDiscount: Double = 0.0,
    val couponCode: String? = null,
    val deliveryFee: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double,
    val freeDeliveryThreshold: Double = 199.0,
    val freeDeliveryUnlocked: Boolean = false,
    val amountNeededForFreeDelivery: Double = 0.0
)

@Serializable
data class CheckoutCreateResponseDto(
    val orderId: String,
    val orderNumber: String,
    val total: Double,
    val paymentMethod: String,
    val order: OrderDto? = null
)

@Serializable
data class AddToCartRequestDto(
    val productId: String,
    val quantity: Int = 1,
    val selectedVariant: String? = null
)

@Serializable
data class UpdateCartItemRequestDto(
    val quantity: Int
)

@Serializable
data class ValidateCouponRequestDto(
    val code: String
)

@Serializable
data class CheckoutPreviewRequestDto(
    val couponCode: String? = null
)

@Serializable
data class CheckoutCreateRequestDto(
    val addressId: String,
    val paymentMethod: String = "UPI",
    val couponCode: String? = null
)

