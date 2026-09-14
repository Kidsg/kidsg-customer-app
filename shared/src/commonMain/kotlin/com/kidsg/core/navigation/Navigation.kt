package com.kidsg.core.navigation

import com.kidsg.domain.model.IntentModeType
import com.kidsg.domain.model.Product

sealed interface Screen {
    data object Splash : Screen
    data object Onboarding : Screen
    data object Auth : Screen
    data object StudentSetup : Screen
    data object Home : Screen
    data class Discovery(val initialMode: IntentModeType? = null, val initialCategoryId: String? = null) : Screen
    data class ProductDetail(val product: Product) : Screen
    data object Bag : Screen
    data class Search(val initialQuery: String = "") : Screen
    data object Checkout : Screen
    data object AddAddress : Screen
    data class PaymentMethod(val totalAmount: Double = 290.0, val deliverySpeed: String = "Standard") : Screen
    data class OrderSuccess(val orderId: String = "KG12345678", val totalAmount: Double = 290.0) : Screen
    data class OrderTracking(val orderId: String = "KG12345678") : Screen
    data object Orders : Screen
    data object Profile : Screen
    data object HelpSupport : Screen
}

