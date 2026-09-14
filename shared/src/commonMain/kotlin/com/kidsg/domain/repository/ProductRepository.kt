package com.kidsg.domain.repository

import com.kidsg.domain.model.Category
import com.kidsg.domain.model.IntentModeInfo
import com.kidsg.domain.model.IntentModeType
import com.kidsg.domain.model.OopsEmergencyItem
import com.kidsg.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getPopularProducts(): List<Product>
    suspend fun getProductsByCategory(categoryId: String): List<Product>
    suspend fun getProductsByIntentMode(mode: IntentModeType): List<Product>
    suspend fun getProductById(productId: String): Product?
    suspend fun searchProducts(query: String): List<Product>
    suspend fun getCategories(): List<Category>
    suspend fun getIntentModes(): List<IntentModeInfo>
    suspend fun getOopsEmergencyItems(): List<OopsEmergencyItem>
}
