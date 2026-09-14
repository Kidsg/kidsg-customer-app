package com.kidsg.domain.repository

import com.kidsg.domain.model.Address
import com.kidsg.domain.model.Order
import com.kidsg.domain.model.OrderStatus
import com.kidsg.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface OrderRepository {
    fun observeActiveOrders(): Flow<List<Order>>
    suspend fun getOrderById(orderId: String): Order?
    suspend fun createOrder(
        items: List<com.kidsg.domain.model.CartItem>,
        deliveryAddress: Address,
        paymentMethod: String
    ): Result<Order>
    suspend fun cancelOrder(orderId: String): Result<Boolean>
}

interface AuthRepository {
    val currentUser: StateFlow<UserProfile?>
    suspend fun requestOtp(phoneNumber: String): Result<Boolean>
    suspend fun verifyOtp(phoneNumber: String, otpCode: String): Result<UserProfile>
    suspend fun updateProfile(profile: UserProfile): Result<UserProfile>
    suspend fun logout()
}
