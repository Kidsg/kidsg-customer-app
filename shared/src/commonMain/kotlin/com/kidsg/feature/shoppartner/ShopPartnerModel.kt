package com.kidsg.feature.shoppartner

import kotlinx.serialization.Serializable

@Serializable
enum class ShopStatus {
    OPEN,
    CLOSED,
    BUSY
}

@Serializable
enum class ShopPartnerTab {
    HOME,
    ORDERS,
    PRODUCTS,
    DELIVERY,
    EARNINGS
}

@Serializable
data class ShopPartnerProfile(
    val id: String,
    val ownerName: String,
    val shopName: String,
    val email: String,
    val phone: String,
    val address: String,
    val area: String,
    val city: String,
    val pincode: String,
    val shopStatus: ShopStatus = ShopStatus.OPEN,
    val shopId: String,
    val role: String = "SHOP_OWNER"
)

@Serializable
data class ShopMetrics(
    val todayOrders: Int = 18,
    val todaySales: Double = 4850.0,
    val pendingOrders: Int = 4,
    val preparingOrders: Int = 3,
    val readyOrders: Int = 2,
    val completedOrders: Int = 9,
    val productsSold: Int = 37
)

@Serializable
data class ShopProductItem(
    val id: String,
    val name: String,
    val categoryName: String,
    val brand: String,
    val price: Double,
    val mrp: Double,
    val stock: Int,
    val soldCount: Int = 0,
    val isActive: Boolean = true,
    val imageUrl: String,
    val unit: String = "piece"
)

@Serializable
data class TopSellingProduct(
    val name: String,
    val quantitySold: Int,
    val revenue: Double,
    val imageUrl: String
)

@Serializable
data class ShopEarnings(
    val period: String = "today",
    val totalEarnings: Double = 91240.0,
    val ordersCompleted: Int = 142,
    val avgOrderValue: Double = 642.0,
    val trendPercent: Int = 12,
    val topSellingProducts: List<TopSellingProduct> = emptyList()
)

@Serializable
data class ShopNotification(
    val id: String,
    val userId: String,
    val type: String, // ORDER, STOCK, SYSTEM
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: String
)

@Serializable
data class DeliveryRiderInfo(
    val id: String = "rider_venkatesh_01",
    val name: String = "Venkatesh R",
    val phone: String = "+91 99887 76655",
    val vehicleNumber: String = "KA 04 EL 4521",
    val status: String = "ONLINE"
)
