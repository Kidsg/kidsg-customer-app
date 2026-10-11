package com.kidsg.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductReview(
    val id: String,
    val productId: String,
    val authorName: String,
    val authorRole: String = "Student", // "Student", "Parent", "Teacher"
    val rating: Double,
    val dateText: String,
    val comment: String,
    val isVerifiedPurchase: Boolean = true,
    val helpfulCount: Int = 0,
    val photoUrls: List<String> = emptyList()
)

@Serializable
data class ProductQa(
    val id: String,
    val productId: String,
    val question: String,
    val answer: String,
    val answeredBy: String = "KidsG Support",
    val answeredDateText: String = "Recently verified"
)

@Serializable
data class ProductBundle(
    val id: String,
    val title: String = "Frequently Bought Together",
    val discountPercent: Int = 10,
    val items: List<Product> = emptyList()
)
