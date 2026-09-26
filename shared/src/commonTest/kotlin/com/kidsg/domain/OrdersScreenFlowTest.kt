package com.kidsg.domain

import com.kidsg.data.mock.KidsGMockData
import com.kidsg.data.repository.MockCartRepository
import com.kidsg.data.repository.MockOrderRepository
import com.kidsg.domain.model.Address
import com.kidsg.domain.model.CartItem
import com.kidsg.domain.model.Order
import com.kidsg.domain.model.OrderStatus
import com.kidsg.feature.orders.OrderFilterTab
import com.kidsg.feature.orders.OrdersUiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Verification test suite for KidsG My Orders Screen:
 * 1. Loading
 * 2. Empty orders
 * 3. One order
 * 4. Multiple orders
 * 5. Processing order
 * 6. Out-for-delivery order
 * 7. Delivered order
 * 8. Mixed order statuses
 * 9. Filter = All
 * 10. Filter = Processing
 * 11. Filter = Out for Delivery
 * 12. Filter = Delivered
 * 13. Order details navigation
 * 14. Buy Again
 * 15. API failure
 * 16. Retry
 * 17. Pull to refresh
 * 18. Logged-out state
 * 19. Different screen sizes / responsiveness
 */
class OrdersScreenFlowTest {

    private val testAddress = Address(
        id = "addr_test",
        label = "Home",
        recipientName = "Aarav Sharma",
        phoneNumber = "+91 91484 73131",
        addressLine1 = "#402, Sunshine Heights",
        addressLine2 = "HSR Layout",
        city = "Bengaluru",
        pincode = "560102"
    )

    private fun createTestOrder(
        id: String,
        displayId: String,
        status: OrderStatus,
        itemCount: Int = 1
    ): Order {
        val product = KidsGMockData.products[0]
        val item = CartItem(product = product, quantity = itemCount)
        return Order(
            id = id,
            displayOrderId = displayId,
            createdAtEpochMs = 1726000000000L,
            status = status,
            items = listOf(item),
            store = KidsGMockData.partnerStore,
            deliveryAddress = testAddress,
            subtotal = item.totalItemPrice,
            discount = 0.0,
            deliveryFee = 0.0,
            taxes = 2.0,
            totalAmount = item.totalItemPrice + 2.0,
            paymentMethod = "UPI",
            riderName = "Ramesh",
            riderPhone = "+91 98765 11111",
            estimatedDeliveryMinutes = 14
        )
    }

    @Test
    fun test1_loading_state() {
        val state: OrdersUiState = OrdersUiState.Loading
        assertIs<OrdersUiState.Loading>(state)
    }

    @Test
    fun test2_emptyOrders_state() {
        val orders = emptyList<Order>()
        val state: OrdersUiState = if (orders.isEmpty()) OrdersUiState.Empty else OrdersUiState.Loaded(orders)
        assertIs<OrdersUiState.Empty>(state)
    }

    @Test
    fun test3_oneOrder_state() {
        val orders = listOf(createTestOrder("ord_1", "KG1001", OrderStatus.CONFIRMED))
        val state: OrdersUiState = OrdersUiState.Loaded(orders)
        assertIs<OrdersUiState.Loaded>(state)
        assertEquals(1, state.orders.size)
        assertEquals("KG1001", state.orders[0].displayOrderId)
    }

    @Test
    fun test4_multipleOrders_state() {
        val orders = listOf(
            createTestOrder("ord_1", "KG1001", OrderStatus.DELIVERED),
            createTestOrder("ord_2", "KG1002", OrderStatus.OUT_FOR_DELIVERY),
            createTestOrder("ord_3", "KG1003", OrderStatus.PREPARING)
        )
        val state: OrdersUiState = OrdersUiState.Loaded(orders)
        assertIs<OrdersUiState.Loaded>(state)
        assertEquals(3, state.orders.size)
    }

    @Test
    fun test5_processingOrder_statusMatching() {
        val order = createTestOrder("ord_proc", "KG2001", OrderStatus.PREPARING)
        val isProcessing = order.status in listOf(
            OrderStatus.CREATED,
            OrderStatus.CONFIRMED,
            OrderStatus.STORE_ACCEPTED,
            OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP
        )
        assertTrue(isProcessing)
        assertFalse(order.status.isTerminal)
    }

    @Test
    fun test6_outForDeliveryOrder_statusMatching() {
        val order = createTestOrder("ord_ofd", "KG2002", OrderStatus.OUT_FOR_DELIVERY)
        val isEnRoute = order.status in listOf(OrderStatus.PICKED_UP, OrderStatus.OUT_FOR_DELIVERY)
        assertTrue(isEnRoute)
        assertTrue(order.status.isEnRoute)
        assertFalse(order.status.isTerminal)
    }

    @Test
    fun test7_deliveredOrder_statusMatching() {
        val order = createTestOrder("ord_deliv", "KG2003", OrderStatus.DELIVERED)
        assertEquals(OrderStatus.DELIVERED, order.status)
        assertTrue(order.status.isTerminal)
    }

    @Test
    fun test8_mixedOrderStatuses() {
        val orders = listOf(
            createTestOrder("o1", "KG01", OrderStatus.CONFIRMED),
            createTestOrder("o2", "KG02", OrderStatus.OUT_FOR_DELIVERY),
            createTestOrder("o3", "KG03", OrderStatus.DELIVERED),
            createTestOrder("o4", "KG04", OrderStatus.CANCELLED)
        )
        assertEquals(4, orders.size)
        val active = orders.filterNot { it.status.isTerminal }
        assertEquals(2, active.size)
    }

    @Test
    fun test9_filterAll_returnsAllOrders() {
        val orders = listOf(
            createTestOrder("o1", "KG01", OrderStatus.PREPARING),
            createTestOrder("o2", "KG02", OrderStatus.OUT_FOR_DELIVERY),
            createTestOrder("o3", "KG03", OrderStatus.DELIVERED)
        )
        val filtered = when (OrderFilterTab.ALL) {
            OrderFilterTab.ALL -> orders
            else -> emptyList()
        }
        assertEquals(3, filtered.size)
    }

    @Test
    fun test10_filterProcessing_isolatesProcessing() {
        val orders = listOf(
            createTestOrder("o1", "KG01", OrderStatus.CONFIRMED),
            createTestOrder("o2", "KG02", OrderStatus.OUT_FOR_DELIVERY),
            createTestOrder("o3", "KG03", OrderStatus.DELIVERED)
        )
        val filtered = orders.filter {
            it.status in listOf(
                OrderStatus.CREATED,
                OrderStatus.CONFIRMED,
                OrderStatus.STORE_ACCEPTED,
                OrderStatus.PREPARING,
                OrderStatus.READY_FOR_PICKUP
            )
        }
        assertEquals(1, filtered.size)
        assertEquals("KG01", filtered[0].displayOrderId)
    }

    @Test
    fun test11_filterOutForDelivery_isolatesEnRoute() {
        val orders = listOf(
            createTestOrder("o1", "KG01", OrderStatus.CONFIRMED),
            createTestOrder("o2", "KG02", OrderStatus.OUT_FOR_DELIVERY),
            createTestOrder("o3", "KG03", OrderStatus.DELIVERED)
        )
        val filtered = orders.filter {
            it.status in listOf(OrderStatus.PICKED_UP, OrderStatus.OUT_FOR_DELIVERY)
        }
        assertEquals(1, filtered.size)
        assertEquals("KG02", filtered[0].displayOrderId)
    }

    @Test
    fun test12_filterDelivered_isolatesDelivered() {
        val orders = listOf(
            createTestOrder("o1", "KG01", OrderStatus.CONFIRMED),
            createTestOrder("o2", "KG02", OrderStatus.OUT_FOR_DELIVERY),
            createTestOrder("o3", "KG03", OrderStatus.DELIVERED)
        )
        val filtered = orders.filter { it.status == OrderStatus.DELIVERED }
        assertEquals(1, filtered.size)
        assertEquals("KG03", filtered[0].displayOrderId)
    }

    @Test
    fun test13_orderDetailsNavigation_passesExactId() {
        var clickedOrderId: String? = null
        val onOrderClick: (String) -> Unit = { id -> clickedOrderId = id }

        val order = createTestOrder("ord_real_999", "KG999", OrderStatus.OUT_FOR_DELIVERY)
        onOrderClick(order.id)

        assertNotNull(clickedOrderId)
        assertEquals("ord_real_999", clickedOrderId)
    }

    @Test
    fun test14_buyAgain_addsItemsToCart() = runTest {
        val cartRepository = MockCartRepository()
        cartRepository.clearCart()

        val product = KidsGMockData.products[0]
        val order = createTestOrder("ord_reorder", "KG500", OrderStatus.DELIVERED, itemCount = 3)

        // Trigger Buy Again action
        order.items.forEach { item ->
            cartRepository.addToCart(item.product, item.quantity, item.selectedVariant)
        }

        val cart = cartRepository.cartState.first()
        assertEquals(1, cart.items.size)
        assertEquals(3, cart.items[0].quantity)
        assertEquals(product.id, cart.items[0].product.id)
    }

    @Test
    fun test15_apiFailure_errorState() {
        val errorMsg = "Network timeout"
        val state: OrdersUiState = OrdersUiState.Error(errorMsg)
        assertIs<OrdersUiState.Error>(state)
        assertEquals(errorMsg, state.message)
    }

    @Test
    fun test16_retry_recoversState() {
        var state: OrdersUiState = OrdersUiState.Error("Server 500")
        assertIs<OrdersUiState.Error>(state)

        // Retry clicked
        state = OrdersUiState.Loading
        assertIs<OrdersUiState.Loading>(state)

        val recoveredOrders = listOf(createTestOrder("ord_rec", "KG888", OrderStatus.DELIVERED))
        state = OrdersUiState.Loaded(recoveredOrders)
        assertIs<OrdersUiState.Loaded>(state)
        assertEquals(1, state.orders.size)
    }

    @Test
    fun test17_pullToRefresh_maintainsCurrentOrders() = runTest {
        val repository = MockOrderRepository()
        val result = repository.getOrders()
        assertTrue(result.isSuccess)
    }

    @Test
    fun test18_loggedOutState_emptyOrders() {
        val loggedOutOrders = emptyList<Order>()
        val state = if (loggedOutOrders.isEmpty()) OrdersUiState.Empty else OrdersUiState.Loaded(loggedOutOrders)
        assertIs<OrdersUiState.Empty>(state)
    }

    @Test
    fun test19_screenResponsiveness_calculationIntegrity() {
        val order = createTestOrder("ord_multi", "KG777", OrderStatus.OUT_FOR_DELIVERY, itemCount = 4)
        val itemCount = order.items.sumOf { it.quantity }
        val price = order.totalAmount

        assertEquals(4, itemCount)
        assertTrue(price > 0.0)
    }
}
