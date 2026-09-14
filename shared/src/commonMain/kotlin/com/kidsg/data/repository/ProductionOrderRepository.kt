package com.kidsg.data.repository

import com.kidsg.core.network.ApiResult
import com.kidsg.data.remote.api.CheckoutApi
import com.kidsg.data.remote.api.OrderApi
import com.kidsg.data.remote.mapper.toDomain
import com.kidsg.domain.model.Address
import com.kidsg.domain.model.CartItem
import com.kidsg.domain.model.Order
import com.kidsg.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProductionOrderRepository(
    private val orderApi: OrderApi = OrderApi(),
    private val checkoutApi: CheckoutApi = CheckoutApi()
) : OrderRepository {

    private val ordersFlow = MutableStateFlow<List<Order>>(emptyList())

    override fun observeActiveOrders(): Flow<List<Order>> {
        return ordersFlow.asStateFlow()
    }

    override suspend fun getOrderById(orderId: String): Order? {
        return when (val res = orderApi.getOrderById(orderId)) {
            is ApiResult.Success -> res.data.toDomain()
            is ApiResult.Error -> ordersFlow.value.find { it.id == orderId }
        }
    }

    override suspend fun createOrder(
        items: List<CartItem>,
        deliveryAddress: Address,
        paymentMethod: String
    ): Result<Order> {
        val addressId = deliveryAddress.id.ifBlank { "addr_dev_default" }
        return when (val res = checkoutApi.createOrder(addressId = addressId, paymentMethod = paymentMethod)) {
            is ApiResult.Success -> {
                val createdOrder = res.data.order?.toDomain() ?: Order(
                    id = res.data.orderId,
                    displayOrderId = res.data.orderNumber,
                    createdAtEpochMs = 1718000000000L,
                    status = com.kidsg.domain.model.OrderStatus.CONFIRMED,
                    items = items,
                    store = com.kidsg.domain.model.Store(
                        id = "store_vidya_depot",
                        name = "Vidya Book & Stationery Depot",
                        locality = "Koramangala 4th Block",
                        distanceKm = 0.8,
                        rating = 4.8,
                        prepTimeMinutes = 12,
                        address = "12th Main Road, Bengaluru"
                    ),
                    deliveryAddress = deliveryAddress,
                    subtotal = items.sumOf { it.totalItemPrice },
                    discount = 0.0,
                    deliveryFee = 0.0,
                    taxes = 0.0,
                    totalAmount = res.data.total,
                    paymentMethod = paymentMethod
                )

                ordersFlow.update { listOf(createdOrder) + it }
                Result.success(createdOrder)
            }
            is ApiResult.Error -> {
                Result.failure(Exception(res.exception.userFriendlyMessage))
            }
        }
    }

    override suspend fun cancelOrder(orderId: String): Result<Boolean> {
        return when (val res = orderApi.cancelOrder(orderId)) {
            is ApiResult.Success -> {
                ordersFlow.update { current ->
                    current.map {
                        if (it.id == orderId) it.copy(status = com.kidsg.domain.model.OrderStatus.CANCELLED) else it
                    }
                }
                Result.success(true)
            }
            is ApiResult.Error -> {
                Result.failure(Exception(res.exception.userFriendlyMessage))
            }
        }
    }
}
