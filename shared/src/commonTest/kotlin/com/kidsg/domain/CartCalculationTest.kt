package com.kidsg.domain

import com.kidsg.data.mock.KidsGMockData
import com.kidsg.domain.model.Cart
import com.kidsg.domain.model.CartItem
import com.kidsg.domain.model.Coupon
import com.kidsg.domain.model.DeliveryConfig
import com.kidsg.domain.model.OrderStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CartCalculationTest {

    private val testConfig = DeliveryConfig(
        baseDeliveryFee = 30.0,
        freeDeliveryThreshold = 199.0,
        platformFee = 5.0,
        taxRatePercent = 5.0
    )

    @Test
    fun testEmptyCart_hasZeroTotals() {
        val cart = Cart(items = emptyList(), deliveryConfig = testConfig)
        assertEquals(0, cart.itemCount)
        assertEquals(0.0, cart.subtotal)
        assertEquals(0.0, cart.deliveryFee)
        assertEquals(0.0, cart.finalTotal)
    }

    @Test
    fun testSubtotalBelowThreshold_incursDeliveryFee() {
        // Classmate notebook: price ₹60 * 2 = ₹120 (< ₹199 threshold)
        val item = CartItem(product = KidsGMockData.products[0], quantity = 2)
        val cart = Cart(items = listOf(item), deliveryConfig = testConfig)

        assertEquals(2, cart.itemCount)
        assertEquals(120.0, cart.subtotal)
        assertFalse(cart.isFreeDeliveryEligible)
        assertEquals(30.0, cart.deliveryFee)

        // Tax: 5% of 120 = 6.0
        // Platform fee: 5.0
        // Final: 120 + 30 + 5 + 6 = 161.0
        assertEquals(161.0, cart.finalTotal)
    }

    @Test
    fun testSubtotalAboveThreshold_unlocksFreeDelivery() {
        // Camlin Gel Pen Set: ₹180 * 2 = ₹360 (> ₹199 threshold)
        val item = CartItem(product = KidsGMockData.products[1], quantity = 2)
        val cart = Cart(items = listOf(item), deliveryConfig = testConfig)

        assertEquals(360.0, cart.subtotal)
        assertTrue(cart.isFreeDeliveryEligible)
        assertEquals(0.0, cart.deliveryFee)
    }

    @Test
    fun testCouponDiscount_appliesAccuratelyWithinCap() {
        val coupon = Coupon(
            code = "KIDSG50",
            title = "50% OFF",
            description = "50% off up to 100",
            discountPercent = 50.0,
            maxDiscount = 100.0,
            minOrderValue = 149.0,
            expiryText = "Today"
        )

        // Subtotal = 360.0 -> 50% is 180, but capped at 100.0
        val item = CartItem(product = KidsGMockData.products[1], quantity = 2)
        val cart = Cart(items = listOf(item), appliedCoupon = coupon, deliveryConfig = testConfig)

        assertEquals(100.0, cart.couponDiscount)
    }

    @Test
    fun testOrderStateProgression_isSequential() {
        val initial = OrderStatus.CONFIRMED
        assertEquals("Order Confirmed", initial.displayTitle)
        assertFalse(initial.isTerminal)

        val delivered = OrderStatus.DELIVERED
        assertTrue(delivered.isTerminal)
        assertEquals("Delivered to Your Desk", delivered.displayTitle)
    }
}
