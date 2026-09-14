package com.kidsg.core.navigation

import com.kidsg.domain.model.IntentModeType
import com.kidsg.domain.model.Product

sealed interface Screen {
    data object Splash : Screen
    data object Home : Screen
    data class Discovery(val initialMode: IntentModeType? = null) : Screen
    data class ProductDetail(val product: Product) : Screen
    data object Bag : Screen
    data class Search(val initialQuery: String = "") : Screen
    data object Checkout : Screen
    data class OrderTracking(val orderId: String) : Screen
    data object Profile : Screen
}
