package com.kidsg.core.services

import com.kidsg.domain.model.Address
import kotlinx.coroutines.flow.Flow

/**
 * Masked rider calling/chatting interface (Rule #7)
 */
interface RiderContactService {
    suspend fun callRider(orderId: String): Result<Boolean>
    suspend fun sendDeliveryNote(orderId: String, note: String): Result<Boolean>
}

/**
 * System notification service interface
 */
interface NotificationService {
    suspend fun requestNotificationPermission(): Boolean
    fun observeNotifications(): Flow<List<AppNotification>>
    suspend fun markAsRead(notificationId: String)
}

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestampEpochMs: Long,
    val type: String,
    val isRead: Boolean = false,
    val deeplink: String? = null
)

/**
 * Device location service interface
 */
interface LocationService {
    suspend fun getCurrentLocation(): Result<GeoLocation>
    suspend fun getNearbyAddresses(query: String): List<Address>
}

data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val addressLabel: String,
    val locality: String
)

/**
 * Vendor-agnostic analytics tracking (Rule #7 & Section 45)
 */
interface AnalyticsService {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap())
    fun setUserProperty(name: String, value: String)
}
