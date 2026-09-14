package com.kidsg.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val slug: String,
    val iconName: String = "category",
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val slug: String,
    val description: String = "",
    val brand: String,
    val categoryId: String,
    val categoryName: String? = null,
    val imageUrl: String,
    val price: Double,
    val mrp: Double,
    val discountPercent: Int = 0,
    val stock: Int = 0,
    val unit: String = "piece",
    val gradeLevel: String = "All",
    val isFeatured: Boolean = false,
    val isActive: Boolean = true,
    val specs: Map<String, String> = emptyMap()
)

@Serializable
data class ProductsPaginationDto(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

@Serializable
data class ProductsResponseDto(
    val products: List<ProductDto>,
    val pagination: ProductsPaginationDto? = null
)

@Serializable
data class StoreDto(
    val id: String,
    val name: String,
    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val deliveryRadiusKm: Double = 5.0,
    val isActive: Boolean = true,
    val distanceKm: Double = 0.8,
    val estimatedDeliveryMinutes: Int = 12,
    val isOpen: Boolean = true,
    val rating: Double = 4.8
)

@Serializable
data class SearchResponseDto(
    val query: String,
    val products: List<ProductDto> = emptyList(),
    val categories: List<CategoryDto> = emptyList()
)
