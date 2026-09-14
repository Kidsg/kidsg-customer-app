package com.kidsg.core.payment

enum class PaymentMethodType(val title: String, val subtitle: String) {
    UPI("UPI", "Google Pay, PhonePe, Paytm, BHIM"),
    CARD("Credit / Debit Card", "Visa, MasterCard, RuPay, Maestro"),
    NET_BANKING("Net Banking", "All major Indian banks supported"),
    WALLET("Wallets", "Paytm, Amazon Pay, Mobikwik"),
    CASH_ON_DELIVERY("Cash on Delivery", "Pay when your supplies arrive at your desk")
}

sealed interface PaymentState {
    data object Idle : PaymentState
    data class Initiating(val orderId: String, val amount: Double) : PaymentState
    data class Processing(val orderId: String, val transactionId: String) : PaymentState
    data class Verified(val orderId: String, val transactionId: String, val gatewayRef: String) : PaymentState
    data class Failed(val orderId: String, val errorMessage: String, val canRetry: Boolean = true) : PaymentState
    data class Cancelled(val orderId: String) : PaymentState
}

data class PaymentInitiationResponse(
    val transactionId: String,
    val paymentGatewayToken: String,
    val requiresSdkLaunch: Boolean
)

data class PaymentVerificationStatus(
    val isSuccess: Boolean,
    val transactionId: String,
    val gatewayReferenceId: String?,
    val failureReason: String? = null
)

/**
 * Payment Abstraction (Rule #6 & #7)
 * The customer app delegates transaction handling to the backend/gateway.
 * The client never assumes success until the authoritative verification responds.
 */
interface PaymentService {
    suspend fun initiatePayment(orderId: String, amount: Double, method: PaymentMethodType): Result<PaymentInitiationResponse>
    suspend fun verifyPayment(orderId: String, transactionId: String): Result<PaymentVerificationStatus>
}
