package com.kidsg.data.repository

import com.kidsg.core.config.ApiConfig
import com.kidsg.domain.repository.*

object RepositoryProvider {

    // Production backend instances
    private val productionProductRepository by lazy { ProductionProductRepository() }
    private val productionCartRepository by lazy { ProductionCartRepository() }
    private val productionOrderRepository by lazy { ProductionOrderRepository() }
    private val productionAuthRepository by lazy { ProductionAuthRepository() }
    private val mockConfigRepository by lazy { MockConfigRepository() }

    // Mock backend instances
    private val mockProductRepository by lazy { MockProductRepository() }
    private val mockCartRepository by lazy { MockCartRepository() }
    private val mockOrderRepository by lazy { MockOrderRepository() }
    private val mockAuthRepository by lazy { MockAuthRepository() }

    val productRepository: ProductRepository
        get() = if (ApiConfig.isMockBackend) mockProductRepository else productionProductRepository

    val cartRepository: CartRepository
        get() = if (ApiConfig.isMockBackend) mockCartRepository else productionCartRepository

    val orderRepository: OrderRepository
        get() = if (ApiConfig.isMockBackend) mockOrderRepository else productionOrderRepository

    val authRepository: AuthRepository
        get() = if (ApiConfig.isMockBackend) mockAuthRepository else productionAuthRepository

    val configRepository: ConfigRepository
        get() = mockConfigRepository
}
