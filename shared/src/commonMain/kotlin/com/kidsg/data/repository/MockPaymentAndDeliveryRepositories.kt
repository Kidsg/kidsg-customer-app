package com.kidsg.data.repository

import com.kidsg.core.payment.PaymentInitiationResponse
import com.kidsg.core.payment.PaymentMethodType
import com.kidsg.core.payment.PaymentService
import com.kidsg.core.payment.PaymentVerificationStatus
import com.kidsg.data.mock.KidsGMockData
import com.kidsg.domain.model.OrderStatus
import com.kidsg.domain.repository.DeliveryTrackingRepository
import com.kidsg.domain.repository.LiveDeliverySnapshot
import com.kidsg.domain.repository.TrackingMilestone
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MockPaymentService (Rule #6)
 * Simulates backend-mediated payment flow:
 * 1. App initiates payment -> gets backend transaction session.
 * 2. App requests verification -> backend verifies with gateway/webhook.
 */
class MockPaymentService : PaymentService {
    override suspend fun initiatePayment(
        orderId: String,
        amount: Double,
        method: PaymentMethodType
    ): Result<PaymentInitiationResponse> {
        delay(400) // Realistic network roundtrip
        val randomNum = kotlin.random.Random.nextInt(10000, 99999)
        val txId = "TXN_${orderId}_$randomNum"
        return Result.success(
            PaymentInitiationResponse(
                transactionId = txId,
                paymentGatewayToken = "token_${txId}",
                requiresSdkLaunch = method != PaymentMethodType.CASH_ON_DELIVERY
            )
        )
    }

    override suspend fun verifyPayment(
        orderId: String,
        transactionId: String
    ): Result<PaymentVerificationStatus> {
        delay(600) // Verification call to backend authority
        val refNum = kotlin.random.Random.nextInt(100000, 999999)
        val refId = "REF_GATEWAY_$refNum"
        return Result.success(
            PaymentVerificationStatus(
                isSuccess = true,
                transactionId = transactionId,
                gatewayReferenceId = refId
            )
        )
    }
}

/**
 * MockDeliveryTrackingRepository (Rule #8)
 * Clean backend order state machine:
 * CREATED -> CONFIRMED -> STORE_ACCEPTED -> PREPARING -> READY_FOR_PICKUP -> PICKED_UP -> OUT_FOR_DELIVERY -> DELIVERED.
 */
class MockDeliveryTrackingRepository : DeliveryTrackingRepository {

    private val orderStates = MutableStateFlow(
        OrderStatus.OUT_FOR_DELIVERY
    )

    override fun observeOrderStatus(orderId: String): Flow<LiveDeliverySnapshot> {
        return MutableStateFlow(buildSnapshot(orderId, orderStates.value)).asStateFlow()
    }

    override suspend fun getOrderSnapshot(orderId: String): LiveDeliverySnapshot {
        return buildSnapshot(orderId, orderStates.value)
    }

    override suspend fun advanceOrderStateForTesting(orderId: String): OrderStatus {
        val next = when (orderStates.value) {
            OrderStatus.CREATED -> OrderStatus.CONFIRMED
            OrderStatus.CONFIRMED -> OrderStatus.STORE_ACCEPTED
            OrderStatus.STORE_ACCEPTED -> OrderStatus.PREPARING
            OrderStatus.PREPARING -> OrderStatus.READY_FOR_PICKUP
            OrderStatus.READY_FOR_PICKUP -> OrderStatus.PICKED_UP
            OrderStatus.PICKED_UP -> OrderStatus.OUT_FOR_DELIVERY
            OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.DELIVERED
            OrderStatus.DELIVERED -> OrderStatus.DELIVERED
            else -> OrderStatus.DELIVERED
        }
        orderStates.value = next
        return next
    }

    private fun buildSnapshot(orderId: String, currentStatus: OrderStatus): LiveDeliverySnapshot {
        val allFlow = listOf(
            OrderStatus.CONFIRMED,
            OrderStatus.STORE_ACCEPTED,
            OrderStatus.PREPARING,
            OrderStatus.PICKED_UP,
            OrderStatus.OUT_FOR_DELIVERY,
            OrderStatus.DELIVERED
        )
        val currentIndex = allFlow.indexOf(currentStatus).coerceAtLeast(0)

        val milestones = allFlow.mapIndexed { idx, status ->
            TrackingMilestone(
                status = status,
                title = status.displayTitle,
                description = when (status) {
                    OrderStatus.CONFIRMED -> "Order placed with Vidya Depot"
                    OrderStatus.STORE_ACCEPTED -> "Store accepted & preparing invoice"
                    OrderStatus.PREPARING -> "Items being packed into your School Bag"
                    OrderStatus.PICKED_UP -> "Rider Venkatesh picked up your stationery"
                    OrderStatus.OUT_FOR_DELIVERY -> "Rider is 500m away, arriving at your gate"
                    OrderStatus.DELIVERED -> "Delivered safely"
                    else -> ""
                },
                timestampEpochMs = 1726250000000L + idx * 180000L,
                isCompleted = idx <= currentIndex,
                isCurrent = idx == currentIndex
            )
        }

        val eta = when (currentStatus) {
            OrderStatus.CONFIRMED -> 15
            OrderStatus.STORE_ACCEPTED -> 13
            OrderStatus.PREPARING -> 10
            OrderStatus.PICKED_UP -> 8
            OrderStatus.OUT_FOR_DELIVERY -> 4
            OrderStatus.DELIVERED -> 0
            else -> 12
        }

        return LiveDeliverySnapshot(
            orderId = orderId,
            currentStatus = currentStatus,
            currentStepIndex = currentIndex,
            totalSteps = allFlow.size,
            etaMinutes = eta,
            storeName = KidsGMockData.partnerStore.name,
            riderName = "Venkatesh (KidsG Rider)",
            milestones = milestones
        )
    }
}
