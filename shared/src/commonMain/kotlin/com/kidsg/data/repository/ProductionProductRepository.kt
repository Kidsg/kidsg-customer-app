package com.kidsg.data.repository

import com.kidsg.core.network.ApiResult
import com.kidsg.data.mock.KidsGCatalogue
import com.kidsg.data.mock.KidsGMockData
import com.kidsg.data.remote.api.ProductApi
import com.kidsg.data.remote.mapper.toDomain
import com.kidsg.domain.model.*
import com.kidsg.domain.repository.ProductRepository

class ProductionProductRepository(
    private val productApi: ProductApi = ProductApi()
) : ProductRepository {

    override suspend fun getPopularProducts(): List<Product> {
        return when (val result = productApi.getFeaturedProducts()) {
            is ApiResult.Success -> result.data.map { it.toDomain() }
            is ApiResult.Error -> KidsGMockData.products.filter { it.isBestSeller }
        }
    }

    override suspend fun getProductsByCategory(categoryId: String): List<Product> {
        return when (val result = productApi.getProducts(categoryId = categoryId)) {
            is ApiResult.Success -> result.data.products.map { it.toDomain() }
            is ApiResult.Error -> KidsGMockData.products.filter { it.categoryId == categoryId }
        }
    }

    override suspend fun getProductsByIntentMode(mode: IntentModeType): List<Product> {
        val all = when (val result = productApi.getProducts()) {
            is ApiResult.Success -> result.data.products.map { it.toDomain() }
            is ApiResult.Error -> KidsGMockData.products
        }
        val modeStr = mode.name
        return all.filter { it.intentModes.contains(modeStr) }
            .ifEmpty { all.take(6) }
    }

    override suspend fun getProductById(productId: String): Product? {
        return when (val result = productApi.getProductById(productId)) {
            is ApiResult.Success -> result.data.toDomain()
            is ApiResult.Error -> KidsGMockData.products.find { it.id == productId }
        }
    }

    override suspend fun searchProducts(query: String): List<Product> {
        return when (val result = productApi.search(query)) {
            is ApiResult.Success -> result.data.products.map { it.toDomain() }
            is ApiResult.Error -> KidsGMockData.products.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.brand.contains(query, ignoreCase = true)
            }
        }
    }

    override suspend fun getCategories(): List<Category> {
        return when (val result = productApi.getCategories()) {
            is ApiResult.Success -> result.data.map { it.toDomain() }
            is ApiResult.Error -> KidsGCatalogue.categories
        }
    }

    override suspend fun getSubcategories(categoryId: String): List<Subcategory> {
        return KidsGCatalogue.subcategories.filter { sub -> sub.categoryId == categoryId }
    }

    override suspend fun getProductsBySubcategory(categoryId: String, subcategoryId: String): List<Product> {
        return KidsGCatalogue.products.filter { p -> p.subcategoryId == subcategoryId || (p.categoryId == categoryId && subcategoryId.isEmpty()) }
    }

    override suspend fun getIntentModes(): List<IntentModeInfo> {
        return KidsGMockData.intentModes
    }

    override suspend fun getOopsEmergencyItems(): List<OopsEmergencyItem> {
        return KidsGMockData.oopsItems
    }

    override suspend fun getProductReviews(productId: String): List<ProductReview> {
        return KidsGCatalogue.getReviews(productId)
    }

    override suspend fun addProductReview(review: ProductReview): Boolean {
        KidsGCatalogue.addReview(review)
        return true
    }

    override suspend fun getProductQa(productId: String): List<ProductQa> {
        return KidsGCatalogue.getQa(productId)
    }

    override suspend fun addProductQuestion(productId: String, question: String): Boolean {
        KidsGCatalogue.addQuestion(productId, question)
        return true
    }

    override suspend fun getFrequentlyBoughtTogether(productId: String): ProductBundle? {
        return KidsGCatalogue.getFrequentlyBoughtTogether(productId)
    }

    override suspend fun getRelatedProducts(productId: String, type: String): List<Product> {
        return KidsGCatalogue.getRelatedProducts(productId, type)
    }

    override suspend fun getPopularProductsForStudents(): List<Product> {
        return KidsGCatalogue.getPopularForStudents()
    }
}
