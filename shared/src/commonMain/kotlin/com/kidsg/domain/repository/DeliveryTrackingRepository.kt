package com.kidsg.domain.repository

import com.kidsg.domain.model.Order
import com.kidsg.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

data class TrackingMilestone(
    val status: OrderStatus,
    val title: String,
    val description: String,
    val timestampEpochMs: Long?,
    val isCompleted: Boolean,
    val isCurrent: Boolean
)

data class LiveDeliverySnapshot(
    val orderId: String,
    val currentStatus: OrderStatus,
    val currentStepIndex: Int,
    val totalSteps: Int,
    val etaMinutes: Int,
    val storeName: String,
    val riderName: String?,
    val milestones: List<TrackingMilestone>
)

/**
 * Real state machine delivery tracking interface (Rule #7 & #8)
 * No fake live GPS coordinates; reflects actual backend state progression.
 */
interface DeliveryTrackingRepository {
    fun observeOrderStatus(orderId: String): Flow<LiveDeliverySnapshot>
    suspend fun getOrderSnapshot(orderId: String): LiveDeliverySnapshot
    suspend fun advanceOrderStateForTesting(orderId: String): OrderStatus
}
