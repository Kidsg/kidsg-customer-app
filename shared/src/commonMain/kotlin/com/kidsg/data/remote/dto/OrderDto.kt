package com.kidsg.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemDto(
    val id: String,
    val productId: String,
    val productName: String,
    val price: Double,
    val mrp: Double,
    val quantity: Int,
    val variant: String? = null
)

@Serializable
data class AddressDto(
    val id: String = "",
    val userId: String = "",
    val label: String = "Home",
    val name: String,
    val phone: String,
    val addressLine1: String,
    val addressLine2: String = "",
    val city: String,
    val state: String = "Karnataka",
    val postalCode: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val deliveryInstructions: String? = null,
    val isDefault: Boolean = false
)

@Serializable
data class OrderDto(
    val id: String,
    val orderNumber: String,
    val userId: String,
    val storeId: String,
    val storeName: String,
    val status: String,
    val subtotal: Double,
    val discount: Double = 0.0,
    val couponDiscount: Double = 0.0,
    val couponCode: String? = null,
    val deliveryFee: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double,
    val paymentStatus: String,
    val paymentMethod: String,
    val deliveryStatus: String,
    val addressSnapshot: AddressDto,
    val items: List<OrderItemDto>,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class DeliveryStatusStepDto(
    val status: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val completed: Boolean
)

@Serializable
data class OrderTrackingDto(
    val orderId: String,
    val orderStatus: String,
    val deliveryStatus: String,
    val riderName: String? = null,
    val riderPhone: String? = null,
    val storeName: String,
    val storeAddress: String,
    val estimatedDeliveryTime: String,
    val statusHistory: List<DeliveryStatusStepDto> = emptyList()
)

@Serializable
data class PaymentIntentDto(
    val paymentId: String,
    val orderId: String,
    val amount: Double,
    val currency: String = "INR",
    val provider: String,
    val gatewayOrderId: String? = null,
    val clientSecret: String? = null
)

@Serializable
data class PaymentVerificationDto(
    val verified: Boolean,
    val orderId: String,
    val paymentId: String,
    val transactionId: String,
    val orderStatus: String
)

@Serializable
data class NotificationDto(
    val id: String,
    val userId: String,
    val type: String,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: String
)

@Serializable
data class SupportTicketDto(
    val id: String,
    val ticketNumber: String,
    val userId: String,
    val orderId: String? = null,
    val subject: String,
    val message: String,
    val status: String,
    val priority: String = "NORMAL",
    val createdAt: String
)
