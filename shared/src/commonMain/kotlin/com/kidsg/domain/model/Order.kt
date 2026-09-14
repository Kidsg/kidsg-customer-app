package com.kidsg.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class OrderStatus {
    CREATED,
    CONFIRMED,
    STORE_ACCEPTED,
    PREPARING,
    READY_FOR_PICKUP,
    PICKED_UP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    FAILED,
    REFUNDED;

    val displayTitle: String
        get() = when (this) {
            CREATED -> "Order Received"
            CONFIRMED -> "Order Confirmed"
            STORE_ACCEPTED -> "Store Accepted"
            PREPARING -> "Packing Your Supplies"
            READY_FOR_PICKUP -> "Ready for Pickup"
            PICKED_UP -> "Picked Up by Rider"
            OUT_FOR_DELIVERY -> "On the Way"
            DELIVERED -> "Delivered to Your Desk"
            CANCELLED -> "Order Cancelled"
            FAILED -> "Payment Failed"
            REFUNDED -> "Amount Refunded"
        }

    val isTerminal: Boolean
        get() = this in listOf(DELIVERED, CANCELLED, FAILED, REFUNDED)

    val isEnRoute: Boolean
        get() = this in listOf(PICKED_UP, OUT_FOR_DELIVERY)
}

@Serializable
data class Store(
    val id: String,
    val name: String,
    val locality: String,
    val distanceKm: Double,
    val rating: Double,
    val prepTimeMinutes: Int,
    val address: String,
    val isOpen: Boolean = true
)

@Serializable
data class Coupon(
    val code: String,
    val title: String,
    val description: String,
    val discountPercent: Double,
    val maxDiscount: Double,
    val minOrderValue: Double,
    val expiryText: String
)

@Serializable
data class Address(
    val id: String,
    val label: String, // Home, School, Work, Other
    val recipientName: String,
    val phoneNumber: String,
    val addressLine1: String,
    val addressLine2: String,
    val city: String,
    val pincode: String,
    val deliveryInstructions: String = "",
    val isDefault: Boolean = false
)

@Serializable
data class Order(
    val id: String,
    val displayOrderId: String,
    val createdAtEpochMs: Long,
    val status: OrderStatus,
    val items: List<CartItem>,
    val store: Store,
    val deliveryAddress: Address,
    val subtotal: Double,
    val discount: Double,
    val deliveryFee: Double,
    val taxes: Double,
    val totalAmount: Double,
    val paymentMethod: String,
    val riderName: String? = null,
    val riderPhone: String? = null,
    val estimatedDeliveryMinutes: Int = 15
)

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val studentName: String = "",
    val studentGrade: String = "Class 7",
    val schoolName: String = "National Public School",
    val isParentMode: Boolean = false
)
