package com.kidsg.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val description: String,
    val iconName: String,
    val accentHex: String,
    val itemCount: Int = 0,
    val isHeroCategory: Boolean = false
)
