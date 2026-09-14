package com.kidsg.data.repository

import com.kidsg.data.mock.KidsGMockData
import com.kidsg.domain.model.Address
import com.kidsg.domain.model.CartItem
import com.kidsg.domain.model.Order
import com.kidsg.domain.model.OrderStatus
import com.kidsg.domain.model.UserProfile
import com.kidsg.domain.repository.AuthRepository
import com.kidsg.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MockAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<UserProfile?>(KidsGMockData.defaultUser)
    override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    override suspend fun requestOtp(phoneNumber: String): Result<Boolean> {
        return Result.success(true)
    }

    override suspend fun verifyOtp(phoneNumber: String, otpCode: String): Result<UserProfile> {
        if (otpCode.length == 4) {
            val user = KidsGMockData.defaultUser.copy(phone = phoneNumber)
            _currentUser.value = user
            return Result.success(user)
        }
        return Result.failure(IllegalArgumentException("Invalid OTP code. Enter 4 digits."))
    }

    override suspend fun updateProfile(profile: UserProfile): Result<UserProfile> {
        _currentUser.value = profile
        return Result.success(profile)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }
}

class MockOrderRepository : OrderRepository {
    private val orders = MutableStateFlow(
        listOf(
            Order(
                id = "order_kg_457812",
                displayOrderId = "#KG2-457812",
                createdAtEpochMs = 1726251000000L,
                status = OrderStatus.OUT_FOR_DELIVERY,
                items = listOf(
                    CartItem(product = KidsGMockData.products[0], quantity = 2),
                    CartItem(product = KidsGMockData.products[1], quantity = 1),
                    CartItem(product = KidsGMockData.products[2], quantity = 1)
                ),
                store = KidsGMockData.partnerStore,
                deliveryAddress = KidsGMockData.defaultAddresses[0],
                subtotal = 350.0,
                discount = 40.0,
                deliveryFee = 0.0,
                taxes = 15.5,
                totalAmount = 325.5,
                paymentMethod = "UPI",
                riderName = "Venkatesh",
                riderPhone = "+91 98765 43210",
                estimatedDeliveryMinutes = 8
            )
        )
    )

    override fun observeActiveOrders(): Flow<List<Order>> = orders.asStateFlow()

    override suspend fun getOrderById(orderId: String): Order? {
        return orders.value.find { it.id == orderId || it.displayOrderId == orderId }
    }

    override suspend fun createOrder(
        items: List<CartItem>,
        deliveryAddress: Address,
        paymentMethod: String
    ): Result<Order> {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val newOrder = Order(
            id = "order_$now",
            displayOrderId = "#KG2-${(100000..999999).random()}",
            createdAtEpochMs = now,
            status = OrderStatus.CONFIRMED,
            items = items,
            store = KidsGMockData.partnerStore,
            deliveryAddress = deliveryAddress,
            subtotal = items.sumOf { it.totalItemPrice },
            discount = 0.0,
            deliveryFee = 0.0,
            taxes = 15.0,
            totalAmount = items.sumOf { it.totalItemPrice } + 15.0,
            paymentMethod = paymentMethod,
            riderName = "Venkatesh",
            riderPhone = "+91 98765 43210",
            estimatedDeliveryMinutes = 12
        )
        orders.update { listOf(newOrder) + it }
        return Result.success(newOrder)
    }

    override suspend fun cancelOrder(orderId: String): Result<Boolean> {
        orders.update { list ->
            list.map { if (it.id == orderId) it.copy(status = OrderStatus.CANCELLED) else it }
        }
        return Result.success(true)
    }
}
