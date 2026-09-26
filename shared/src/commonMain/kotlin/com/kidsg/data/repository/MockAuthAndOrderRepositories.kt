package com.kidsg.data.repository

import com.kidsg.core.storage.SessionStorage
import com.kidsg.data.mock.KidsGMockData
import com.kidsg.domain.model.Address
import com.kidsg.domain.model.CartItem
import com.kidsg.domain.model.Order
import com.kidsg.domain.model.OrderStatus
import com.kidsg.domain.model.UserProfile
import com.kidsg.domain.repository.AuthRepository
import com.kidsg.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MockAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<UserProfile?>(KidsGMockData.defaultUser)
    override val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val registeredUsers = mutableMapOf<String, UserProfile>(
        "student@kidsg.in" to KidsGMockData.defaultUser,
        "aarav@kidsg.in" to KidsGMockData.defaultUser
    )
    private val userPasswords = mutableMapOf<String, String>(
        "student@kidsg.in" to "KidsGSecure2026!",
        "aarav@kidsg.in" to "KidsGSecure2026!"
    )
    private var lastOtp: String? = null

    override suspend fun checkEmail(email: String): Result<Pair<Boolean, String?>> {
        val clean = email.trim().lowercase()
        val user = registeredUsers[clean]
        return if (user != null) {
            Result.success(Pair(true, user.studentName.ifBlank { user.name.split(" ").firstOrNull() ?: "Student" }))
        } else {
            Result.success(Pair(false, null))
        }
    }

    override suspend fun loginWithPassword(email: String, password: String): Result<UserProfile> {
        val clean = email.trim().lowercase()
        val storedPass = userPasswords[clean]
        if (storedPass == null || storedPass != password) {
            return Result.failure(IllegalArgumentException("Invalid email or password"))
        }
        val user = registeredUsers[clean] ?: KidsGMockData.defaultUser.copy(email = clean)
        _currentUser.value = user
        SessionStorage.saveUserProfile(user)
        return Result.success(user)
    }

    override suspend fun signup(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
        selectedClass: String,
        selectedSchool: String
    ): Result<UserProfile> {
        val clean = email.trim().lowercase()
        if (registeredUsers.containsKey(clean)) {
            return Result.failure(IllegalArgumentException("An account with this email already exists"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        val newUser = UserProfile(
            id = "user_${clean.replace("[^a-zA-Z0-9]".toRegex(), "_")}",
            name = "$firstName $lastName".trim(),
            phone = phone,
            email = clean,
            studentName = firstName,
            studentGrade = selectedClass,
            schoolName = selectedSchool
        )
        registeredUsers[clean] = newUser
        userPasswords[clean] = password
        _currentUser.value = newUser
        SessionStorage.saveUserProfile(newUser)
        return Result.success(newUser)
    }

    override suspend fun requestOtp(phoneNumber: String): Result<String> {
        lastOtp = (100000..999999).random().toString()
        return Result.success("Verification code sent to $phoneNumber (Code: $lastOtp)")
    }

    override suspend fun verifyOtp(phoneNumber: String, otpCode: String): Result<UserProfile> {
        val clean = phoneNumber.trim().lowercase()
        // Must match either last generated OTP or valid 6-digit code
        if (lastOtp != null && otpCode.trim() != lastOtp) {
            return Result.failure(IllegalArgumentException("Invalid verification code. Please check and try again."))
        }
        val existing = registeredUsers[clean]
        val user = existing ?: KidsGMockData.defaultUser.copy(email = clean, phone = phoneNumber)
        _currentUser.value = user
        SessionStorage.saveUserProfile(user)
        return Result.success(user)
    }

    override suspend fun updateProfile(profile: UserProfile): Result<UserProfile> {
        _currentUser.value = profile
        SessionStorage.saveUserProfile(profile)
        return Result.success(profile)
    }

    override suspend fun logout() {
        _currentUser.value = null
        SessionStorage.clearSession()
    }
}

class MockOrderRepository : OrderRepository {
    private val orders: MutableStateFlow<List<Order>> = MutableStateFlow(SessionStorage.getSavedOrders())

    override fun observeActiveOrders(): Flow<List<Order>> = orders.asStateFlow()

    override suspend fun getOrderById(orderId: String): Order? {
        return orders.value.find { it.id == orderId || it.displayOrderId == orderId }
    }

    override suspend fun createOrder(
        items: List<CartItem>,
        deliveryAddress: Address,
        paymentMethod: String
    ): Result<Order> {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val newOrder = Order(
            id = "order_$now",
            displayOrderId = "#KG2-${(100000..999999).random()}",
            createdAtEpochMs = now,
            status = OrderStatus.CONFIRMED,
            items = items,
            store = KidsGMockData.partnerStore,
            deliveryAddress = deliveryAddress,
            subtotal = items.sumOf { it.totalItemPrice },
            discount = 0.0,
            deliveryFee = 0.0,
            taxes = 15.0,
            totalAmount = items.sumOf { it.totalItemPrice } + 15.0,
            paymentMethod = paymentMethod,
            riderName = "Venkatesh",
            riderPhone = "+91 98765 43210",
            estimatedDeliveryMinutes = 12
        )
        orders.update { listOf(newOrder) + it }
        SessionStorage.addOrder(newOrder)
        return Result.success(newOrder)
    }

    override suspend fun cancelOrder(orderId: String): Result<Boolean> {
        orders.update { list ->
            list.map { if (it.id == orderId) it.copy(status = OrderStatus.CANCELLED) else it }
        }
        return Result.success(true)
    }

    override suspend fun getOrders(): Result<List<Order>> {
        val current = SessionStorage.getSavedOrders()
        orders.value = current
        return Result.success(current)
    }
}
