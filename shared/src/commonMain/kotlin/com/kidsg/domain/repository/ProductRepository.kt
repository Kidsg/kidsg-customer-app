package com.kidsg.domain.repository

import com.kidsg.domain.model.Category
import com.kidsg.domain.model.IntentModeInfo
import com.kidsg.domain.model.IntentModeType
import com.kidsg.domain.model.OopsEmergencyItem
import com.kidsg.domain.model.Product
import com.kidsg.domain.model.ProductBundle
import com.kidsg.domain.model.ProductQa
import com.kidsg.domain.model.ProductReview
import com.kidsg.domain.model.Subcategory
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getPopularProducts(): List<Product>
    suspend fun getProductsByCategory(categoryId: String): List<Product>
    suspend fun getProductsBySubcategory(categoryId: String, subcategoryId: String): List<Product>
    suspend fun getProductsByIntentMode(mode: IntentModeType): List<Product>
    suspend fun getProductById(productId: String): Product?
    suspend fun searchProducts(query: String): List<Product>
    suspend fun getCategories(): List<Category>
    suspend fun getSubcategories(categoryId: String): List<Subcategory>
    suspend fun getIntentModes(): List<IntentModeInfo>
    suspend fun getOopsEmergencyItems(): List<OopsEmergencyItem>
    suspend fun getProductReviews(productId: String): List<ProductReview>
    suspend fun addProductReview(review: ProductReview): Boolean
    suspend fun getProductQa(productId: String): List<ProductQa>
    suspend fun addProductQuestion(productId: String, question: String): Boolean
    suspend fun getFrequentlyBoughtTogether(productId: String): ProductBundle?
    suspend fun getRelatedProducts(productId: String, type: String = "similar"): List<Product>
    suspend fun getPopularProductsForStudents(): List<Product>
}
