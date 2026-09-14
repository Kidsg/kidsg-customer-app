package com.kidsg.data.repository

import com.kidsg.data.mock.KidsGMockData
import com.kidsg.domain.model.Category
import com.kidsg.domain.model.Coupon
import com.kidsg.domain.model.DeliveryConfig
import com.kidsg.domain.model.IntentModeInfo
import com.kidsg.domain.model.IntentModeType
import com.kidsg.domain.model.OopsEmergencyItem
import com.kidsg.domain.model.Product
import com.kidsg.domain.model.Store
import com.kidsg.domain.repository.ConfigRepository
import com.kidsg.domain.repository.ProductRepository

class MockProductRepository : ProductRepository {
    override suspend fun getPopularProducts(): List<Product> = KidsGMockData.products

    override suspend fun getProductsByCategory(categoryId: String): List<Product> {
        return KidsGMockData.products.filter { it.categoryId == categoryId }
    }

    override suspend fun getProductsByIntentMode(mode: IntentModeType): List<Product> {
        val modeStr = mode.name
        return KidsGMockData.products.filter { it.intentModes.contains(modeStr) }
    }

    override suspend fun getProductById(productId: String): Product? {
        return KidsGMockData.products.find { it.id == productId }
    }

    override suspend fun searchProducts(query: String): List<Product> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return KidsGMockData.products
        return KidsGMockData.products.filter {
            it.name.lowercase().contains(q) ||
            it.brand.lowercase().contains(q) ||
            it.tags.any { tag -> tag.lowercase().contains(q) } ||
            it.categoryId.lowercase().contains(q)
        }
    }

    override suspend fun getCategories(): List<Category> = KidsGMockData.categories

    override suspend fun getIntentModes(): List<IntentModeInfo> = KidsGMockData.intentModes

    override suspend fun getOopsEmergencyItems(): List<OopsEmergencyItem> = KidsGMockData.oopsItems
}

class MockConfigRepository : ConfigRepository {
    override suspend fun getDeliveryConfig(): DeliveryConfig = KidsGMockData.deliveryConfig

    override suspend fun getAvailableCoupons(): List<Coupon> = KidsGMockData.coupons

    override suspend fun getNearestPartnerStore(userPincode: String): Store = KidsGMockData.partnerStore
}
