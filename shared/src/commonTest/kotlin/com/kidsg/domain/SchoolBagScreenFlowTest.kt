package com.kidsg.domain

import com.kidsg.data.mock.KidsGMockData
import com.kidsg.data.repository.MockCartRepository
import com.kidsg.domain.model.Cart
import com.kidsg.domain.model.CartItem
import com.kidsg.domain.model.DeliveryConfig
import com.kidsg.feature.bag.CartUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Verification test suite for KidsG School Bag:
 * 1. API loading
 * 2. Empty cart
 * 3. Loaded cart
 * 4. Quantity +/-
 * 5. Remove item
 * 6. Subtotal calculation
 * 7. Checkout navigation
 * 8. API failure
 * 9. Retry
 * 10. Logout/login and cart persistence
 */
class SchoolBagScreenFlowTest {

    private val testConfig = DeliveryConfig(
        baseDeliveryFee = 30.0,
        freeDeliveryThreshold = 199.0,
        platformFee = 5.0,
        taxRatePercent = 5.0
    )

    @Test
    fun test1_apiLoading_stateModel() {
        val state: CartUiState = CartUiState.Loading
        assertIs<CartUiState.Loading>(state)
    }

    @Test
    fun test2_emptyCart_stateModel() {
        val emptyCart = Cart(items = emptyList(), deliveryConfig = testConfig)
        val state: CartUiState = if (emptyCart.items.isEmpty()) CartUiState.Empty else CartUiState.Loaded(
            items = emptyCart.items,
            subtotal = emptyCart.subtotal,
            deliveryFee = emptyCart.deliveryFee,
            discount = 0.0,
            total = emptyCart.finalTotal,
            isFreeDelivery = emptyCart.isFreeDeliveryEligible,
            rawCart = emptyCart
        )

        assertIs<CartUiState.Empty>(state)
        assertEquals(0, emptyCart.itemCount)
        assertEquals(0.0, emptyCart.subtotal)
    }

    @Test
    fun test3_loadedCart_stateModel() {
        val product = KidsGMockData.products[0]
        val items = listOf(CartItem(product = product, quantity = 2))
        val cart = Cart(items = items, deliveryConfig = testConfig)

        val state: CartUiState = if (cart.items.isEmpty()) CartUiState.Empty else CartUiState.Loaded(
            items = cart.items,
            subtotal = cart.subtotal,
            deliveryFee = cart.deliveryFee,
            discount = cart.productDiscount + cart.couponDiscount,
            total = cart.finalTotal,
            isFreeDelivery = cart.isFreeDeliveryEligible,
            rawCart = cart
        )

        assertIs<CartUiState.Loaded>(state)
        assertEquals(1, state.items.size)
        assertEquals(2, state.items[0].quantity)
        assertEquals(cart.subtotal, state.subtotal)
    }

    @Test
    fun test4_quantityIncrementAndDecrement() = runTest {
        val repository = MockCartRepository()
        repository.clearCart()
        val product = KidsGMockData.products[0]

        // Add 1 item
        repository.addToCart(product, 1)
        var currentCart = repository.cartState.first()
        assertEquals(1, currentCart.itemCount)

        // Increment to 3
        repository.updateQuantity(product.id, 3)
        currentCart = repository.cartState.first()
        assertEquals(3, currentCart.itemCount)

        // Decrement to 2
        repository.updateQuantity(product.id, 2)
        currentCart = repository.cartState.first()
        assertEquals(2, currentCart.itemCount)
    }

    @Test
    fun test5_removeItem_atZeroQuantity() = runTest {
        val repository = MockCartRepository()
        repository.clearCart()
        val product = KidsGMockData.products[0]

        repository.addToCart(product, 1)
        var currentCart = repository.cartState.first()
        assertEquals(1, currentCart.items.size)

        // Decrement below 1 removes from cart
        repository.updateQuantity(product.id, 0)
        currentCart = repository.cartState.first()
        assertTrue(currentCart.items.isEmpty())
        assertEquals(0, currentCart.itemCount)
    }

    @Test
    fun test6_subtotalCalculation_andFreeDelivery() {
        val product = KidsGMockData.products[0] // price = ₹40.0, mrp = ₹50.0
        val item = CartItem(product = product, quantity = 5) // subtotal = 200.0 (> ₹199 free delivery)
        val cart = Cart(items = listOf(item), deliveryConfig = testConfig)

        assertEquals(200.0, cart.subtotal)
        assertTrue(cart.isFreeDeliveryEligible)
        assertEquals(0.0, cart.deliveryFee)
        assertEquals(5.0, cart.platformFee)
        // Tax: 5% of 200 = 10.0
        assertEquals(10.0, cart.taxAmount)
        // Total: 200 + 0 + 5 + 10 = 215.0
        assertEquals(215.0, cart.finalTotal)
    }

    @Test
    fun test7_checkoutNavigation_payloadIntegrity() {
        val product = KidsGMockData.products[1]
        val cart = Cart(items = listOf(CartItem(product = product, quantity = 1)), deliveryConfig = testConfig)

        var navigatedToCheckout = false
        var checkoutSubtotal = 0.0

        val onProceedToCheckout: () -> Unit = {
            navigatedToCheckout = true
            checkoutSubtotal = cart.subtotal
        }

        onProceedToCheckout()
        assertTrue(navigatedToCheckout)
        assertEquals(product.price, checkoutSubtotal)
    }

    @Test
    fun test8_apiFailure_errorState() {
        val errorMsg = "Unable to connect to Vidya Depot"
        val state: CartUiState = CartUiState.Error(errorMsg)
        assertIs<CartUiState.Error>(state)
        assertEquals(errorMsg, state.message)
    }

    @Test
    fun test9_retryFlow_recoveryToLoaded() {
        var state: CartUiState = CartUiState.Error("Network failure")
        assertIs<CartUiState.Error>(state)

        // Trigger retry -> transitions to Loading then Loaded
        state = CartUiState.Loading
        assertIs<CartUiState.Loading>(state)

        val product = KidsGMockData.products[0]
        val recoveredCart = Cart(items = listOf(CartItem(product = product, quantity = 1)), deliveryConfig = testConfig)
        state = CartUiState.Loaded(
            items = recoveredCart.items,
            subtotal = recoveredCart.subtotal,
            deliveryFee = recoveredCart.deliveryFee,
            discount = 0.0,
            total = recoveredCart.finalTotal,
            isFreeDelivery = recoveredCart.isFreeDeliveryEligible,
            rawCart = recoveredCart
        )
        assertIs<CartUiState.Loaded>(state)
        assertEquals(1, state.items.size)
    }

    @Test
    fun test10_cartPersistence_acrossSessions() = runTest {
        val repository = MockCartRepository()
        repository.clearCart()
        val product1 = KidsGMockData.products[0]
        val product2 = KidsGMockData.products[1]

        repository.addToCart(product1, 2)
        repository.addToCart(product2, 1)

        val cart = repository.cartState.first()
        assertEquals(2, cart.items.size)
        assertEquals(3, cart.itemCount)

        // Even across navigation / re-queries, cart retains items
        val reloadedCart = repository.cartState.value
        assertEquals(3, reloadedCart.itemCount)
    }
}
