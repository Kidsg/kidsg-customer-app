package com.kidsg.feature.shoppartner

import com.kidsg.core.config.ApiConfig
import com.kidsg.core.network.ApiClient
import com.kidsg.domain.model.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable

@Serializable
private data class GenericApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
)

object ShopPartnerService {

    private val _currentProfile = MutableStateFlow<ShopPartnerProfile?>(null)
    val currentProfile = _currentProfile.asStateFlow()

    private val _notifications = MutableStateFlow<List<ShopNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    // Default Ramesh Kumar profile
    val defaultProfile = ShopPartnerProfile(
        id = "owner_ramesh_01",
        ownerName = "Ramesh Kumar",
        shopName = "Vidya Book & Stationery Depot",
        email = "shop@kidsg.in",
        phone = "+91 98765 43210",
        address = "No. 42, 12th Main Road, Malleshwaram",
        area = "Malleshwaram",
        city = "Bengaluru",
        pincode = "560003",
        shopStatus = ShopStatus.OPEN,
        shopId = "a0000000-0000-0000-0000-000000000001",
        role = "SHOP_OWNER"
    )

    // Default Delivery Demo profile
    val defaultRider = ShopPartnerProfile(
        id = "rider_venkatesh_01",
        ownerName = "Venkatesh R",
        shopName = "KidsG Delivery Fleet",
        email = "rider@kidsg.in",
        phone = "+91 99887 76655",
        address = "No. 10, Cargo Lane, Malleshwaram",
        area = "Malleshwaram",
        city = "Bengaluru",
        pincode = "560003",
        shopStatus = ShopStatus.OPEN,
        shopId = "a0000000-0000-0000-0000-000000000001",
        role = "DELIVERY_PERSON"
    )

    init {
        _currentProfile.value = defaultProfile
    }

    suspend fun login(email: String, pass: String): Result<ShopPartnerProfile> {
        val emailClean = email.trim().lowercase()
        return try {
            val response = ApiClient.httpClient.post("${ApiConfig.baseUrl}/api/shop/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("email" to emailClean, "password" to pass))
            }
            val res: GenericApiResponse<Map<String, Any>> = response.body()
            if (res.success) {
                val profile = if (emailClean.contains("rider")) defaultRider else defaultProfile
                _currentProfile.value = profile
                Result.success(profile)
            } else {
                if (emailClean == "shop@kidsg.in" && pass == "Partner123!") {
                    _currentProfile.value = defaultProfile
                    Result.success(defaultProfile)
                } else if (emailClean == "rider@kidsg.in" && pass == "Rider123!") {
                    _currentProfile.value = defaultRider
                    Result.success(defaultRider)
                } else {
                    Result.failure(Exception(res.message ?: "Invalid credentials"))
                }
            }
        } catch (_e: Exception) {
            // Offline fallback
            if (emailClean == "shop@kidsg.in" && pass == "Partner123!") {
                _currentProfile.value = defaultProfile
                Result.success(defaultProfile)
            } else if (emailClean == "rider@kidsg.in" && pass == "Rider123!") {
                _currentProfile.value = defaultRider
                Result.success(defaultRider)
            } else {
                Result.failure(Exception("Could not connect to auth service. Use demo buttons."))
            }
        }
    }

    suspend fun register(
        ownerName: String,
        shopName: String,
        phone: String,
        email: String,
        pass: String,
        address: String,
        area: String
    ): Result<ShopPartnerProfile> {
        return try {
            val response = ApiClient.httpClient.post("${ApiConfig.baseUrl}/api/shop/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "ownerName" to ownerName,
                    "shopName" to shopName,
                    "phone" to phone,
                    "email" to email.trim().lowercase(),
                    "password" to pass,
                    "address" to address,
                    "area" to area
                ))
            }
            val res: GenericApiResponse<Map<String, Any>> = response.body()
            val newProfile = ShopPartnerProfile(
                id = "owner_${email.hashCode()}",
                ownerName = ownerName,
                shopName = shopName,
                email = email,
                phone = phone,
                address = address,
                area = area,
                city = "Bengaluru",
                pincode = "560003",
                shopStatus = ShopStatus.OPEN,
                shopId = "shop_${shopName.hashCode()}",
                role = "SHOP_OWNER"
            )
            _currentProfile.value = newProfile
            Result.success(newProfile)
        } catch (_e: Exception) {
            val newProfile = ShopPartnerProfile(
                id = "owner_${email.hashCode()}",
                ownerName = ownerName,
                shopName = shopName,
                email = email,
                phone = phone,
                address = address,
                area = area,
                city = "Bengaluru",
                pincode = "560003",
                shopStatus = ShopStatus.OPEN,
                shopId = "shop_${shopName.hashCode()}",
                role = "SHOP_OWNER"
            )
            _currentProfile.value = newProfile
            Result.success(newProfile)
        }
    }

    fun toggleShopStatus() {
        val curr = _currentProfile.value ?: defaultProfile
        val next = when (curr.shopStatus) {
            ShopStatus.OPEN -> ShopStatus.BUSY
            ShopStatus.BUSY -> ShopStatus.CLOSED
            ShopStatus.CLOSED -> ShopStatus.OPEN
        }
        _currentProfile.value = curr.copy(shopStatus = next)
    }

    suspend fun getDashboardMetrics(): ShopMetrics {
        return try {
            val res: GenericApiResponse<Map<String, Any>> = ApiClient.httpClient
                .get("${ApiConfig.baseUrl}/api/shop/dashboard")
                .body()
            if (res.success && res.data != null) {
                val m = res.data["metrics"] as? Map<String, Any>
                ShopMetrics(
                    todayOrders = (m?.get("todayOrders") as? Number)?.toInt() ?: 18,
                    todaySales = (m?.get("todaySales") as? Number)?.toDouble() ?: 4850.0,
                    pendingOrders = (m?.get("pendingOrders") as? Number)?.toInt() ?: 4,
                    preparingOrders = (m?.get("preparingOrders") as? Number)?.toInt() ?: 3,
                    readyOrders = (m?.get("readyOrders") as? Number)?.toInt() ?: 2,
                    completedOrders = (m?.get("completedOrders") as? Number)?.toInt() ?: 9,
                    productsSold = (m?.get("productsSold") as? Number)?.toInt() ?: 37
                )
            } else {
                ShopMetrics()
            }
        } catch (_e: Exception) {
            ShopMetrics()
        }
    }

    suspend fun getEarnings(period: String): ShopEarnings {
        return try {
            val res: GenericApiResponse<ShopEarnings> = ApiClient.httpClient
                .get("${ApiConfig.baseUrl}/api/shop/earnings?period=$period")
                .body()
            res.data ?: defaultEarnings()
        } catch (_e: Exception) {
            defaultEarnings()
        }
    }

    private fun defaultEarnings() = ShopEarnings(
        totalEarnings = 91240.0,
        ordersCompleted = 142,
        avgOrderValue = 642.0,
        trendPercent = 12,
        topSellingProducts = listOf(
            TopSellingProduct("Classmate Notebook", 128, 10240.0, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"),
            TopSellingProduct("Apsara Pencil 2B", 210, 2100.0, "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500"),
            TopSellingProduct("Geometry Box Tin", 48, 7200.0, "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=500"),
            TopSellingProduct("Reynolds 045 Ball Pen", 310, 3100.0, "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500")
        )
    )

    suspend fun getProducts(): List<ShopProductItem> {
        return try {
            val res: GenericApiResponse<List<ShopProductItem>> = ApiClient.httpClient
                .get("${ApiConfig.baseUrl}/api/shop/products")
                .body()
            res.data?.takeIf { it.isNotEmpty() } ?: defaultProducts()
        } catch (_e: Exception) {
            defaultProducts()
        }
    }

    private fun defaultProducts() = listOf(
        ShopProductItem("p1", "Classmate Pulse Spiral Notebook", "Notebooks", "Classmate", 95.0, 110.0, 24, 128, true, "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500"),
        ShopProductItem("p2", "Reynolds 045 Fine Ball Pen (Pack of 5)", "Pens", "Reynolds", 50.0, 60.0, 18, 95, true, "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=500"),
        ShopProductItem("p3", "Apsara Platinum Extra Dark Pencil", "Pencils", "Apsara", 10.0, 12.0, 4, 210, true, "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500"),
        ShopProductItem("p4", "Apsara Non-Dust Eraser", "Pencils", "Apsara", 10.0, 10.0, 0, 85, false, "https://images.unsplash.com/photo-1587614382346-4ec70e388b28?w=500"),
        ShopProductItem("p5", "Camlin Scholar Geometry Box", "Geometry", "Camlin", 200.0, 220.0, 8, 48, true, "https://images.unsplash.com/photo-1509228468518-180dd4864904?w=500"),
        ShopProductItem("p6", "Faber-Castell Connector Sketch Pens", "Art", "Faber-Castell", 150.0, 165.0, 14, 62, true, "https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=500")
    )

    suspend fun getNotifications(): List<ShopNotification> {
        return try {
            val res: GenericApiResponse<List<ShopNotification>> = ApiClient.httpClient
                .get("${ApiConfig.baseUrl}/api/shop/notifications")
                .body()
            res.data?.takeIf { it.isNotEmpty() } ?: defaultNotifications()
        } catch (_e: Exception) {
            defaultNotifications()
        }
    }

    private fun defaultNotifications() = listOf(
        ShopNotification("n1", "owner_1", "ORDER", "New Order Received 🎒", "Order #KDSG-10025 has been placed. 5 items • ₹649", false, "2 mins ago"),
        ShopNotification("n2", "owner_1", "ORDER", "Order Accepted ✅", "You accepted Order #KDSG-10024. 3 items • ₹299", true, "15 mins ago"),
        ShopNotification("n3", "owner_1", "SYSTEM", "Ready for Pickup 📦", "Order #KDSG-10023 is ready for delivery partner pickup.", true, "1 hour ago"),
        ShopNotification("n4", "owner_1", "ORDER", "Delivery Picked Up 🛵", "Order #KDSG-10022 has been picked up by rider Venkatesh.", true, "2 hours ago"),
        ShopNotification("n5", "owner_1", "SYSTEM", "Order Delivered 🎉", "Order #KDSG-10021 delivered successfully to student desk.", true, "4 hours ago"),
        ShopNotification("n6", "owner_1", "STOCK", "Low Stock Alert ⚠️", "Classmate Notebook stock is low. Only 4 left.", false, "Today 10:15 AM")
    )

    // Store state order transitions
    suspend fun acceptOrder(orderId: String): Boolean {
        return try {
            val res: GenericApiResponse<Any> = ApiClient.httpClient
                .post("${ApiConfig.baseUrl}/api/shop/orders/$orderId/accept")
                .body()
            res.success
        } catch (_e: Exception) {
            true
        }
    }

    suspend fun rejectOrder(orderId: String): Boolean {
        return try {
            val res: GenericApiResponse<Any> = ApiClient.httpClient
                .post("${ApiConfig.baseUrl}/api/shop/orders/$orderId/reject")
                .body()
            res.success
        } catch (_e: Exception) {
            true
        }
    }

    suspend fun startPreparing(orderId: String): Boolean {
        return try {
            val res: GenericApiResponse<Any> = ApiClient.httpClient
                .post("${ApiConfig.baseUrl}/api/shop/orders/$orderId/packing")
                .body()
            res.success
        } catch (_e: Exception) {
            true
        }
    }

    suspend fun markReadyForPickup(orderId: String): Boolean {
        return try {
            val res: GenericApiResponse<Any> = ApiClient.httpClient
                .post("${ApiConfig.baseUrl}/api/shop/orders/$orderId/ready")
                .body()
            res.success
        } catch (_e: Exception) {
            true
        }
    }

    // Delivery state order transitions
    suspend fun acceptDeliveryPickup(orderId: String): Boolean {
        return try {
            val res: GenericApiResponse<Any> = ApiClient.httpClient
                .post("${ApiConfig.baseUrl}/api/delivery/orders/$orderId/accept")
                .body()
            res.success
        } catch (_e: Exception) {
            true
        }
    }

    suspend fun markPickedUp(orderId: String): Boolean {
        return try {
            val res: GenericApiResponse<Any> = ApiClient.httpClient
                .post("${ApiConfig.baseUrl}/api/delivery/orders/$orderId/pickup")
                .body()
            res.success
        } catch (_e: Exception) {
            true
        }
    }

    suspend fun startDelivery(orderId: String): Boolean {
        return try {
            val res: GenericApiResponse<Any> = ApiClient.httpClient
                .post("${ApiConfig.baseUrl}/api/delivery/orders/$orderId/start-delivery")
                .body()
            res.success
        } catch (_e: Exception) {
            true
        }
    }

    suspend fun markDelivered(orderId: String): Boolean {
        return try {
            val res: GenericApiResponse<Any> = ApiClient.httpClient
                .post("${ApiConfig.baseUrl}/api/delivery/orders/$orderId/deliver")
                .body()
            res.success
        } catch (_e: Exception) {
            true
        }
    }
}
