package com.kidsg.data.repository

import com.kidsg.data.mock.KidsGCatalogue
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
        val cat = KidsGCatalogue.categories.find { it.id == categoryId || it.slug == categoryId }
        val targetCatId = cat?.id ?: categoryId
        return KidsGCatalogue.products.filter {
            it.categoryId == targetCatId || it.categoryId == categoryId || it.subcategoryId == categoryId || it.tags.any { t -> t.equals(categoryId, ignoreCase = true) }
        }
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

    override suspend fun getCategories(): List<Category> = KidsGCatalogue.categories

    override suspend fun getSubcategories(categoryId: String): List<com.kidsg.domain.model.Subcategory> {
        return KidsGCatalogue.subcategories.filter { it.categoryId == categoryId }
    }

    override suspend fun getProductsBySubcategory(categoryId: String, subcategoryId: String): List<Product> {
        return KidsGCatalogue.products.filter { p -> p.subcategoryId == subcategoryId || (p.categoryId == categoryId && subcategoryId.isEmpty()) }
    }

    override suspend fun getIntentModes(): List<IntentModeInfo> = KidsGMockData.intentModes

    override suspend fun getOopsEmergencyItems(): List<OopsEmergencyItem> = KidsGMockData.oopsItems

    override suspend fun getProductReviews(productId: String): List<com.kidsg.domain.model.ProductReview> {
        return KidsGCatalogue.getReviews(productId)
    }

    override suspend fun addProductReview(review: com.kidsg.domain.model.ProductReview): Boolean {
        KidsGCatalogue.addReview(review)
        return true
    }

    override suspend fun getProductQa(productId: String): List<com.kidsg.domain.model.ProductQa> {
        return KidsGCatalogue.getQa(productId)
    }

    override suspend fun addProductQuestion(productId: String, question: String): Boolean {
        KidsGCatalogue.addQuestion(productId, question)
        return true
    }

    override suspend fun getFrequentlyBoughtTogether(productId: String): com.kidsg.domain.model.ProductBundle? {
        return KidsGCatalogue.getFrequentlyBoughtTogether(productId)
    }

    override suspend fun getRelatedProducts(productId: String, type: String): List<Product> {
        return KidsGCatalogue.getRelatedProducts(productId, type)
    }

    override suspend fun getPopularProductsForStudents(): List<Product> {
        return KidsGCatalogue.getPopularForStudents()
    }
}

class MockConfigRepository : ConfigRepository {
    override suspend fun getDeliveryConfig(): DeliveryConfig = KidsGMockData.deliveryConfig

    override suspend fun getAvailableCoupons(): List<Coupon> = KidsGMockData.coupons

    override suspend fun getNearestPartnerStore(userPincode: String): Store = KidsGMockData.partnerStore
}
