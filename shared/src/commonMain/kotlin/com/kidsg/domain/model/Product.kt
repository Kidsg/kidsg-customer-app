package com.kidsg.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val price: Double,
    val mrp: Double,
    val rating: Double = 4.8,
    val reviewCount: Int = 120,
    val categoryId: String,
    val description: String,
    val imageUrl: String,
    val specifications: Map<String, String> = emptyMap(),
    val variants: List<String> = emptyList(),
    val isAvailable: Boolean = true,
    val stockQuantity: Int = 50,
    val isBestSeller: Boolean = false,
    val tags: List<String> = emptyList(),
    val intentModes: List<String> = emptyList()
) {
    val discountPercentage: Int
        get() = if (mrp > price) (((mrp - price) / mrp) * 100).toInt() else 0

    val savingsAmount: Double
        get() = (mrp - price).coerceAtLeast(0.0)
}
