package com.kidsg.feature.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGAsyncImage
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGProductVisual
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.domain.model.Order
import com.kidsg.domain.model.OrderStatus
import com.kidsg.domain.repository.CartRepository
import com.kidsg.domain.repository.OrderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Proper Orders UI State hierarchy following MVI/MVVM architecture.
 */
sealed interface OrdersUiState {
    data object Loading : OrdersUiState
    data object Empty : OrdersUiState
    data class Loaded(val orders: List<Order>) : OrdersUiState
    data class Error(val message: String) : OrdersUiState
}

/**
 * Filter tab categories matching the visual reference.
 */
enum class OrderFilterTab(val label: String) {
    ALL("All"),
    PROCESSING("Processing"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered")
}

/**
 * KidsG "My Orders" Screen
 * Built strictly according to the KidsG Primary Visual Reference:
 * - Empty state with friendly stationery delivery box character, CTA, and trust benefits.
 * - Loading state with animated skeleton shimmer cards.
 * - Loaded state with real backend order data, filter tabs, active delivery tracking progress, and "Buy Again" action.
 * - Error state with retry capability and pull-to-refresh.
 */
@Composable
fun OrdersListScreen(
    orders: List<Order> = emptyList(),
    orderRepository: OrderRepository? = null,
    cartRepository: CartRepository? = null,
    onBack: () -> Unit,
    onOrderClick: (orderId: String) -> Unit,
    onExploreDesk: () -> Unit = onBack,
    onStartShopping: () -> Unit = onExploreDesk,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe active orders from repository if provided, fallback to passed list
    val repositoryOrders by orderRepository?.observeActiveOrders()?.collectAsState(initial = orders)
        ?: remember(orders) { mutableStateOf(orders) }

    var selectedFilter by remember { mutableStateOf(OrderFilterTab.ALL) }
    var isInitialLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Initial load / fetch from backend
    LaunchedEffect(Unit) {
        try {
            orderRepository?.getOrders()
            delay(400) // Brief smooth shimmer skeleton transition
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to fetch orders"
        } finally {
            isInitialLoading = false
        }
    }

    // Refresh function
    val onRefresh: () -> Unit = {
        coroutineScope.launch {
            isRefreshing = true
            errorMessage = null
            try {
                orderRepository?.getOrders()
                delay(400)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isRefreshing = false
            }
        }
    }

    // Determine current UI State
    val uiState: OrdersUiState = when {
        isInitialLoading -> OrdersUiState.Loading
        errorMessage != null && repositoryOrders.isEmpty() -> OrdersUiState.Error(errorMessage ?: "Unknown error")
        repositoryOrders.isEmpty() -> OrdersUiState.Empty
        else -> OrdersUiState.Loaded(orders = repositoryOrders)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KidsGColors.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Navigation Bar
            OrdersTopBar(
                onBack = onBack,
                isRefreshing = isRefreshing,
                onRefreshClick = onRefresh
            )

            // Status Filter Chips: All, Processing, Out for Delivery, Delivered
            OrdersFilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            // Dynamic Body based on OrdersUiState
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (uiState) {
                    is OrdersUiState.Loading -> {
                        OrdersLoadingSkeleton()
                    }

                    is OrdersUiState.Empty -> {
                        OrdersEmptyState(
                            onStartShopping = onStartShopping
                        )
                    }

                    is OrdersUiState.Loaded -> {
                        val filteredOrders = remember(selectedFilter, uiState.orders) {
                            when (selectedFilter) {
                                OrderFilterTab.ALL -> uiState.orders
                                OrderFilterTab.PROCESSING -> uiState.orders.filter {
                                    it.status in listOf(
                                        OrderStatus.CREATED,
                                        OrderStatus.CONFIRMED,
                                        OrderStatus.STORE_ACCEPTED,
                                        OrderStatus.PREPARING,
                                        OrderStatus.READY_FOR_PICKUP
                                    )
                                }
                                OrderFilterTab.OUT_FOR_DELIVERY -> uiState.orders.filter {
                                    it.status in listOf(
                                        OrderStatus.PICKED_UP,
                                        OrderStatus.OUT_FOR_DELIVERY
                                    )
                                }
                                OrderFilterTab.DELIVERED -> uiState.orders.filter {
                                    it.status == OrderStatus.DELIVERED
                                }
                            }
                        }

                        if (filteredOrders.isEmpty()) {
                            // Sub-filter empty state
                            OrdersFilterEmptyState(
                                filterName = selectedFilter.label,
                                onResetFilter = { selectedFilter = OrderFilterTab.ALL }
                            )
                        } else {
                            OrdersLoadedList(
                                orders = filteredOrders,
                                onOrderClick = onOrderClick,
                                onBuyAgain = { order ->
                                    if (cartRepository != null) {
                                        coroutineScope.launch {
                                            order.items.forEach { item ->
                                                cartRepository.addToCart(
                                                    product = item.product,
                                                    quantity = item.quantity,
                                                    variant = item.selectedVariant
                                                )
                                            }
                                            snackbarHostState.showSnackbar("Added ${order.items.size} stationery items back to your bag!")
                                        }
                                    } else {
                                        onStartShopping()
                                    }
                                }
                            )
                        }
                    }

                    is OrdersUiState.Error -> {
                        OrdersErrorState(
                            message = uiState.message,
                            onRetry = {
                                isInitialLoading = true
                                errorMessage = null
                                onRefresh()
                            }
                        )
                    }
                }
            }
        }

        // Notification Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

/**
 * Top App Bar:
 * - Back button (circle with arrow)
 * - "My Orders" title (20sp bold)
 * - Notification bell icon with unread badge dot "2"
 */
@Composable
private fun OrdersTopBar(
    onBack: () -> Unit,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Circular Back Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8F9FA))
                    .border(1.dp, Color(0xFFF1F2F4), CircleShape)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    style = KidsGTypography.TitleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "My Orders",
                style = KidsGTypography.DisplaySmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = KidsGColors.OrangePrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Notification Bell with Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF8F9FA))
                    .border(1.dp, Color(0xFFF1F2F4), CircleShape)
                    .clickable(onClick = onRefreshClick),
                contentAlignment = Alignment.Center
            ) {
                NotificationBellIcon(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF111111)
                )

                // Badge dot "2"
                Box(
                    modifier = Modifier
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(KidsGColors.OrangePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2",
                        style = KidsGTypography.Caption.copy(
                            color = KidsGColors.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Filter Chips Row:
 * "All", "Processing", "Out for Delivery", "Delivered"
 */
@Composable
private fun OrdersFilterRow(
    selectedFilter: OrderFilterTab,
    onFilterSelected: (OrderFilterTab) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(OrderFilterTab.entries) { tab ->
            val isSelected = tab == selectedFilter
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) KidsGColors.OrangePrimary else KidsGColors.White,
                label = "filterBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) KidsGColors.White else Color(0xFF4B5563),
                label = "filterText"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) KidsGColors.OrangePrimary else Color(0xFFE5E7EB),
                label = "filterBorder"
            )

            Box(
                modifier = Modifier
                    .clip(KidsGShapes.FullPill)
                    .background(bgColor)
                    .border(1.dp, borderColor, KidsGShapes.FullPill)
                    .clickable { onFilterSelected(tab) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    style = KidsGTypography.Caption.copy(
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                )
            }
        }
    }
}

/**
 * 1. EMPTY STATE
 * Friendly KidsG delivery package character, "No Orders Yet",
 * "Start Shopping →" primary button, and 3 service benefits.
 */
@Composable
private fun OrdersEmptyState(
    onStartShopping: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Cute KidsG Delivery Box Character Illustration
            KidsGDeliveryBoxIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No Orders Yet",
                style = KidsGTypography.TitleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your stationery orders will appear here with live delivery tracking.",
                style = KidsGTypography.BodyMedium.copy(
                    fontSize = 13.5.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 20.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary CTA: "Start Shopping →"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(KidsGShapes.FullPill)
                    .background(KidsGColors.OrangePrimary)
                    .clickable(onClick = onStartShopping)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ShoppingBagVectorIcon(
                        modifier = Modifier.size(20.dp),
                        color = KidsGColors.White
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Start Shopping  →",
                        style = KidsGTypography.TitleSmall.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KidsGColors.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Three Service Benefits: Fast Delivery, Safe & Reliable, Quality Products
            OrdersServiceBenefitsCard()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Native Compose Vector Illustration of a friendly stationery cardboard delivery box
 * with cute kawaii face, open top flaps, "KidsG" logo, paper airplane, and playful doodles.
 */
@Composable
private fun KidsGDeliveryBoxIllustration(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "boxFloat")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "boxY"
    )

    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starTwinkle"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { translationY = floatOffset }) {
            val w = size.width
            val h = size.height
            val centerX = w * 0.50f
            val centerY = h * 0.54f

            // 1. Soft warm ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF2E2), Color(0xFFFFF9F0), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = w * 0.45f
                ),
                radius = w * 0.45f,
                center = Offset(centerX, centerY)
            )

            // 2. Soft shadow under package
            drawOval(
                color = Color(0xFFEADBCE).copy(alpha = 0.55f),
                topLeft = Offset(centerX - 76.dp.toPx(), centerY + 54.dp.toPx()),
                size = Size(152.dp.toPx(), 18.dp.toPx())
            )

            // 3. Playful Doodles around box

            // Folded paper airplane in top-right
            val planeX = w * 0.82f
            val planeY = h * 0.20f

            // Dashed flight trail looping gracefully
            val trailPath = Path().apply {
                moveTo(centerX + 30.dp.toPx(), centerY - 48.dp.toPx())
                cubicTo(
                    w * 0.70f, h * 0.38f,
                    w * 0.75f, h * 0.28f,
                    planeX - 6.dp.toPx(), planeY + 8.dp.toPx()
                )
            }
            drawPath(
                path = trailPath,
                color = Color(0xFFFDBA74),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
                    cap = StrokeCap.Round
                )
            )

            // Folded paper airplane
            val planePath = Path().apply {
                moveTo(planeX, planeY)
                lineTo(planeX - 22.dp.toPx(), planeY - 6.dp.toPx())
                lineTo(planeX - 8.dp.toPx(), planeY + 14.dp.toPx())
                close()
            }
            drawPath(path = planePath, color = KidsGColors.OrangePrimary)

            val planeFold = Path().apply {
                moveTo(planeX, planeY)
                lineTo(planeX - 8.dp.toPx(), planeY + 14.dp.toPx())
                lineTo(planeX - 16.dp.toPx(), planeY + 2.dp.toPx())
                close()
            }
            drawPath(path = planeFold, color = Color(0xFFE56E00))

            // Playful stars & doodles
            drawPlayfulDoodleStar(Offset(w * 0.16f, h * 0.26f), 10.dp.toPx(), Color(0xFFFBBF24).copy(alpha = starAlpha))
            drawPlayfulDoodleStar(Offset(w * 0.86f, h * 0.38f), 8.dp.toPx(), Color(0xFFC084FC).copy(alpha = starAlpha))
            drawPlayfulDoodleStar(Offset(w * 0.14f, h * 0.62f), 8.dp.toPx(), Color(0xFFFBBF24).copy(alpha = starAlpha))

            // Floating mini hearts
            drawMiniHeart(Offset(w * 0.12f, h * 0.38f), 7.dp.toPx(), Color(0xFFFF8DA1))
            drawMiniHeart(Offset(w * 0.86f, h * 0.30f), 8.dp.toPx(), Color(0xFFFF8DA1))

            // Confetti swirl ribbons
            val ribbon1 = Path().apply {
                moveTo(w * 0.80f, h * 0.50f)
                cubicTo(w * 0.84f, h * 0.46f, w * 0.86f, h * 0.54f, w * 0.90f, h * 0.50f)
            }
            drawPath(path = ribbon1, color = Color(0xFFF59E0B), style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

            // 4. Cardboard Delivery Box
            val boxWidth = 120.dp.toPx()
            val boxHeight = 84.dp.toPx()
            val boxLeft = centerX - (boxWidth / 2f)
            val boxTop = centerY - 32.dp.toPx()

            // Main front face of cardboard box
            drawRoundRect(
                color = Color(0xFFE8BD8C),
                topLeft = Offset(boxLeft, boxTop),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )

            // Left perspective shaded side
            val leftSide = Path().apply {
                moveTo(boxLeft, boxTop + 14.dp.toPx())
                lineTo(boxLeft - 18.dp.toPx(), boxTop - 8.dp.toPx())
                lineTo(boxLeft - 18.dp.toPx(), boxTop + boxHeight - 20.dp.toPx())
                lineTo(boxLeft, boxTop + boxHeight)
                close()
            }
            drawPath(path = leftSide, color = Color(0xFFD29E6A))

            // Open top flaps
            // Left open flap
            val leftFlap = Path().apply {
                moveTo(boxLeft - 18.dp.toPx(), boxTop - 8.dp.toPx())
                lineTo(boxLeft - 6.dp.toPx(), boxTop - 28.dp.toPx())
                lineTo(boxLeft + 36.dp.toPx(), boxTop - 20.dp.toPx())
                lineTo(boxLeft + 20.dp.toPx(), boxTop)
                close()
            }
            drawPath(path = leftFlap, color = Color(0xFFE2B47F))

            // Right open flap
            val rightFlap = Path().apply {
                moveTo(boxLeft + 48.dp.toPx(), boxTop)
                lineTo(boxLeft + 58.dp.toPx(), boxTop - 22.dp.toPx())
                lineTo(boxLeft + 104.dp.toPx(), boxTop - 14.dp.toPx())
                lineTo(boxLeft + 92.dp.toPx(), boxTop)
                close()
            }
            drawPath(path = rightFlap, color = Color(0xFFF0C899))

            // Cute Kawaii Face on front of box
            val faceCenterX = centerX + 4.dp.toPx()
            val faceCenterY = boxTop + 42.dp.toPx()

            // Left Eye
            drawCircle(color = Color(0xFF111111), radius = 3.dp.toPx(), center = Offset(faceCenterX - 18.dp.toPx(), faceCenterY - 4.dp.toPx()))
            drawCircle(color = Color.White, radius = 1.dp.toPx(), center = Offset(faceCenterX - 19.dp.toPx(), faceCenterY - 5.dp.toPx()))

            // Right Eye
            drawCircle(color = Color(0xFF111111), radius = 3.dp.toPx(), center = Offset(faceCenterX + 18.dp.toPx(), faceCenterY - 4.dp.toPx()))
            drawCircle(color = Color.White, radius = 1.dp.toPx(), center = Offset(faceCenterX + 17.dp.toPx(), faceCenterY - 5.dp.toPx()))

            // Cheeks (cute blushing pink)
            drawOval(
                color = Color(0xFFFF8A80).copy(alpha = 0.65f),
                topLeft = Offset(faceCenterX - 28.dp.toPx(), faceCenterY + 2.dp.toPx()),
                size = Size(10.dp.toPx(), 6.dp.toPx())
            )
            drawOval(
                color = Color(0xFFFF8A80).copy(alpha = 0.65f),
                topLeft = Offset(faceCenterX + 18.dp.toPx(), faceCenterY + 2.dp.toPx()),
                size = Size(10.dp.toPx(), 6.dp.toPx())
            )

            // Smiling open mouth
            val mouth = Path().apply {
                moveTo(faceCenterX - 8.dp.toPx(), faceCenterY + 3.dp.toPx())
                cubicTo(
                    faceCenterX - 5.dp.toPx(), faceCenterY + 12.dp.toPx(),
                    faceCenterX + 5.dp.toPx(), faceCenterY + 12.dp.toPx(),
                    faceCenterX + 8.dp.toPx(), faceCenterY + 3.dp.toPx()
                )
                close()
            }
            drawPath(path = mouth, color = Color(0xFF212121))

            // Cute little tongue
            val tongue = Path().apply {
                moveTo(faceCenterX - 4.dp.toPx(), faceCenterY + 8.dp.toPx())
                cubicTo(
                    faceCenterX, faceCenterY + 12.dp.toPx(),
                    faceCenterX + 2.dp.toPx(), faceCenterY + 12.dp.toPx(),
                    faceCenterX + 4.dp.toPx(), faceCenterY + 8.dp.toPx()
                )
            }
            drawPath(path = tongue, color = Color(0xFFFF5252))

            // Barcode sticker on left side of box
            drawRoundRect(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(boxLeft - 14.dp.toPx(), boxTop + 24.dp.toPx()),
                size = Size(11.dp.toPx(), 18.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
            for (i in 0..3) {
                drawLine(
                    color = Color(0xFF333333),
                    start = Offset(boxLeft - 12.dp.toPx() + (i * 2.5f).dp.toPx(), boxTop + 26.dp.toPx()),
                    end = Offset(boxLeft - 12.dp.toPx() + (i * 2.5f).dp.toPx(), boxTop + 38.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // KidsG branding badge on lower right of box
            drawRoundRect(
                color = KidsGColors.OrangePrimary,
                topLeft = Offset(boxLeft + boxWidth - 32.dp.toPx(), boxTop + boxHeight - 20.dp.toPx()),
                size = Size(26.dp.toPx(), 12.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlayfulDoodleStar(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        cubicTo(center.x, center.y, center.x, center.y, center.x + radius, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x, center.y + radius)
        cubicTo(center.x, center.y, center.x, center.y, center.x - radius, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color = color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniHeart(
    center: Offset,
    radius: Float,
    color: Color
) {
    val heart = Path().apply {
        moveTo(center.x, center.y + radius)
        cubicTo(center.x - radius, center.y, center.x - radius, center.y - radius, center.x, center.y - radius * 0.4f)
        cubicTo(center.x + radius, center.y - radius, center.x + radius, center.y, center.x, center.y + radius)
        close()
    }
    drawPath(heart, color = color)
}

/**
 * Three Service Benefits:
 * - Fast Delivery
 * - Safe & Reliable
 * - Quality Products
 */
@Composable
private fun OrdersServiceBenefitsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, Color(0xFFF3F4F6), KidsGShapes.Medium)
            .shadow(1.dp, KidsGShapes.Medium, spotColor = Color(0x0A000000))
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Fast Delivery
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFECFDF5)),
                    contentAlignment = Alignment.Center
                ) {
                    DeliveryTruckIcon(modifier = Modifier.size(18.dp), color = Color(0xFF10B981))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Fast\nDelivery",
                    style = KidsGTypography.Caption.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151),
                        lineHeight = 15.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Box(modifier = Modifier.height(28.dp).width(1.dp).background(Color(0xFFF3F4F6)))

            // 2. Safe & Reliable
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFECFDF5)),
                    contentAlignment = Alignment.Center
                ) {
                    SafeShieldIcon(modifier = Modifier.size(18.dp), color = Color(0xFF10B981))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Safe &\nReliable",
                    style = KidsGTypography.Caption.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151),
                        lineHeight = 15.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Box(modifier = Modifier.height(28.dp).width(1.dp).background(Color(0xFFF3F4F6)))

            // 3. Quality Products
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFFBEB)),
                    contentAlignment = Alignment.Center
                ) {
                    QualityStarIcon(modifier = Modifier.size(18.dp), color = Color(0xFFF59E0B))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Quality\nProducts",
                    style = KidsGTypography.Caption.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151),
                        lineHeight = 15.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 2. LOADING STATE
 * 3-4 animated skeleton order cards with subtle shimmer animation.
 */
@Composable
private fun OrdersLoadingSkeleton() {
    val shimmerTransition = rememberInfiniteTransition(label = "ordersShimmer")
    val translateAnim by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE5E7EB).copy(alpha = 0.6f),
            Color(0xFFF3F4F6),
            Color(0xFFE5E7EB).copy(alpha = 0.6f)
        ),
        start = Offset(translateAnim - 400f, translateAnim - 400f),
        end = Offset(translateAnim, translateAnim)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(4) {
            SkeletonOrderCard(shimmerBrush = shimmerBrush)
        }
    }
}

@Composable
private fun SkeletonOrderCard(shimmerBrush: Brush) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, Color(0xFFF3F4F6), KidsGShapes.Medium)
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Skeleton Product Image
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(shimmerBrush)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(88.dp)
                            .height(24.dp)
                            .clip(KidsGShapes.FullPill)
                            .background(shimmerBrush)
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(28.dp)
                            .clip(KidsGShapes.FullPill)
                            .background(shimmerBrush)
                    )
                }
            }
        }
    }
}

/**
 * 3. LOADED STATE
 * List of real backend orders with interactive status badges,
 * active delivery progress bar, and "Buy Again" button.
 */
@Composable
private fun OrdersLoadedList(
    orders: List<Order>,
    onOrderClick: (orderId: String) -> Unit,
    onBuyAgain: (Order) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(orders, key = { it.id }) { order ->
            ModernOrderCard(
                order = order,
                onClick = { onOrderClick(order.id) },
                onBuyAgain = { onBuyAgain(order) }
            )
        }
    }
}

/**
 * Modern Rounded Order Card:
 * [Product image]  #ORDER_ID             Date
 *                  Product/Order Name
 *                  X items • ₹PRICE
 *                  [Status Badge]        [Buy Again / Arrow]
 *                  [Expanded Delivery Progress if Active]
 */
@Composable
private fun ModernOrderCard(
    order: Order,
    onClick: () -> Unit,
    onBuyAgain: () -> Unit
) {
    val totalItemCount = order.items.sumOf { it.quantity }
    val firstItem = order.items.firstOrNull()
    val orderTitle = firstItem?.product?.name ?: "School Stationery Essentials"
    val dateText = formatOrderDate(order.createdAtEpochMs)
    val isDelivered = order.status == OrderStatus.DELIVERED
    val isActiveDelivery = !order.status.isTerminal

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, Color(0xFFF3F4F6), KidsGShapes.Medium)
            .shadow(1.dp, KidsGShapes.Medium, spotColor = Color(0x06000000))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Product Image Thumbnail
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val imageUrl = firstItem?.product?.imageUrl.orEmpty()
                    if (imageUrl.isNotBlank()) {
                        KidsGAsyncImage(
                            url = imageUrl,
                            contentDescription = orderTitle,
                            modifier = Modifier.size(68.dp),
                            placeholder = {
                                if (firstItem != null) {
                                    KidsGProductVisual(product = firstItem.product, modifier = Modifier.size(46.dp))
                                } else {
                                    Text(text = "📦", fontSize = 28.sp)
                                }
                            }
                        )
                    } else if (firstItem != null) {
                        KidsGProductVisual(product = firstItem.product, modifier = Modifier.size(46.dp))
                    } else {
                        Text(text = "📦", fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Details Column
                Column(modifier = Modifier.weight(1f)) {
                    // Order ID & Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${order.displayOrderId}",
                            style = KidsGTypography.TitleSmall.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111)
                            )
                        )

                        Text(
                            text = dateText,
                            style = KidsGTypography.Caption.copy(
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Order / Product Name
                    Text(
                        text = orderTitle,
                        style = KidsGTypography.TitleSmall.copy(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Item count & Total Price
                    Text(
                        text = "$totalItemCount ${if (totalItemCount == 1) "item" else "items"} • ₹${order.totalAmount.toInt()}",
                        style = KidsGTypography.BodySmall.copy(
                            fontSize = 12.5.sp,
                            color = Color(0xFF6B7280)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Badge & Action Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OrderStatusBadge(status = order.status)

                        if (isDelivered) {
                            // "Buy Again" button
                            Box(
                                modifier = Modifier
                                    .clip(KidsGShapes.FullPill)
                                    .border(1.5.dp, KidsGColors.OrangePrimary, KidsGShapes.FullPill)
                                    .clickable(onClick = onBuyAgain)
                                    .padding(horizontal = 14.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Buy Again",
                                    style = KidsGTypography.Caption.copy(
                                        color = KidsGColors.OrangePrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                )
                            }
                        } else {
                            Text(
                                text = "›",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }

            // 4. ACTIVE DELIVERY ORDER: Expanded Delivery Progress Section
            if (isActiveDelivery) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFF3F4F6))
                Spacer(modifier = Modifier.height(12.dp))

                ActiveDeliveryProgressTimeline(status = order.status)

                if (order.estimatedDeliveryMinutes > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚡ Arriving in ${order.estimatedDeliveryMinutes} mins",
                        style = KidsGTypography.Caption.copy(
                            color = Color(0xFF059669),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Status Badge:
 * - Delivered: Green
 * - Out for Delivery: Blue
 * - Processing: Amber
 */
@Composable
private fun OrderStatusBadge(status: OrderStatus) {
    val (bgColor, textColor, icon) = when (status) {
        OrderStatus.DELIVERED -> Triple(Color(0xFFECFDF5), Color(0xFF059669), "📦 Delivered")
        OrderStatus.OUT_FOR_DELIVERY, OrderStatus.PICKED_UP -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), "🚚 Out for Delivery")
        OrderStatus.CANCELLED, OrderStatus.FAILED, OrderStatus.REFUNDED -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), "✕ Cancelled")
        else -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "⏳ Processing")
    }

    Box(
        modifier = Modifier
            .clip(KidsGShapes.FullPill)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = KidsGTypography.Tag.copy(
                color = textColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

/**
 * 4-Stage Delivery Progress Timeline:
 * Order Placed -> Packed -> Out for Delivery -> Delivered
 */
@Composable
private fun ActiveDeliveryProgressTimeline(status: OrderStatus) {
    val stages = listOf("Order\nPlaced", "Packed", "Out for\nDelivery", "Delivered")
    val currentStep = when (status) {
        OrderStatus.CREATED, OrderStatus.CONFIRMED -> 0
        OrderStatus.STORE_ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP -> 1
        OrderStatus.PICKED_UP, OrderStatus.OUT_FOR_DELIVERY -> 2
        OrderStatus.DELIVERED -> 3
        else -> 0
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val isCompleted = i < currentStep
                val isCurrent = i == currentStep

                // Step Circle
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted || isCurrent -> KidsGColors.OrangePrimary
                                else -> Color(0xFFE5E7EB)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isCompleted -> {
                            Text(text = "✓", color = KidsGColors.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        isCurrent -> {
                            Text(text = if (i == 2) "🚚" else "•", color = KidsGColors.White, fontSize = if (i == 2) 11.sp else 18.sp, fontWeight = FontWeight.Bold)
                        }
                        else -> {
                            Text(text = "•", color = Color(0xFF9CA3AF), fontSize = 14.sp)
                        }
                    }
                }

                // Connecting Line between steps
                if (i < 3) {
                    val isLineCompleted = i < currentStep
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(
                                if (isLineCompleted) KidsGColors.OrangePrimary else Color(0xFFE5E7EB)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Step labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            stages.forEachIndexed { index, label ->
                val isStepActive = index <= currentStep
                Text(
                    text = label,
                    style = KidsGTypography.Caption.copy(
                        fontSize = 10.sp,
                        color = if (isStepActive) Color(0xFF111111) else Color(0xFF9CA3AF),
                        fontWeight = if (isStepActive) FontWeight.Bold else FontWeight.Normal,
                        lineHeight = 12.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(62.dp)
                )
            }
        }
    }
}

/**
 * Sub-filter Empty State:
 * When no orders match the selected filter chip.
 */
@Composable
private fun OrdersFilterEmptyState(
    filterName: String,
    onResetFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🔍", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No $filterName Orders",
            style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "You don't have any orders under $filterName at the moment.",
            style = KidsGTypography.BodySmall.copy(color = Color(0xFF6B7280)),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(KidsGShapes.FullPill)
                .background(Color(0xFFFFF7ED))
                .border(1.dp, Color(0xFFFED7AA), KidsGShapes.FullPill)
                .clickable(onClick = onResetFilter)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Show All Orders",
                style = KidsGTypography.Caption.copy(color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * 9. ERROR STATE
 * Friendly illustration of delivery box, message, and "Try Again" button.
 */
@Composable
private fun OrdersErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF7ED)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📦❓", fontSize = 38.sp)
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Couldn't load your orders",
            style = KidsGTypography.TitleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Something went wrong while fetching your orders.",
            style = KidsGTypography.BodyMedium.copy(
                fontSize = 13.5.sp,
                color = Color(0xFF6B7280)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .width(160.dp)
                .height(46.dp)
                .clip(KidsGShapes.FullPill)
                .background(KidsGColors.OrangePrimary)
                .clickable(onClick = onRetry),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Try Again",
                style = KidsGTypography.TitleSmall.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = KidsGColors.White
                )
            )
        }
    }
}

/**
 * Date formatting helper without crash risk.
 */
private fun formatOrderDate(epochMs: Long): String {
    if (epochMs <= 0L) return "Recent Order"
    return try {
        val instant = Instant.fromEpochMilliseconds(epochMs)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val day = dt.dayOfMonth.toString().padStart(2, '0')
        val month = months.getOrElse(dt.monthNumber - 1) { "Sep" }
        val year = dt.year
        "$day $month $year"
    } catch (_: Exception) {
        "12 Sep 2026"
    }
}

/**
 * Vector Benefit & UI Icons rendered cleanly with native Compose Canvas
 */
@Composable
private fun NotificationBellIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val bell = Path().apply {
            moveTo(w * 0.5f, h * 0.18f)
            cubicTo(w * 0.35f, h * 0.18f, w * 0.28f, h * 0.40f, w * 0.24f, h * 0.65f)
            lineTo(w * 0.18f, h * 0.75f)
            lineTo(w * 0.82f, h * 0.75f)
            lineTo(w * 0.76f, h * 0.65f)
            cubicTo(w * 0.72f, h * 0.40f, w * 0.65f, h * 0.18f, w * 0.5f, h * 0.18f)
            close()
        }
        drawPath(bell, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Clapper
        drawCircle(color = color, radius = 2.dp.toPx(), center = Offset(w * 0.5f, h * 0.82f))
    }
}

@Composable
private fun DeliveryTruckIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val body = Path().apply {
            moveTo(w * 0.12f, h * 0.32f)
            lineTo(w * 0.64f, h * 0.32f)
            lineTo(w * 0.64f, h * 0.48f)
            lineTo(w * 0.82f, h * 0.48f)
            lineTo(w * 0.88f, h * 0.65f)
            lineTo(w * 0.88f, h * 0.75f)
            lineTo(w * 0.12f, h * 0.75f)
            close()
        }
        drawPath(body, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color = color, radius = 2.2.dp.toPx(), center = Offset(w * 0.28f, h * 0.76f))
        drawCircle(color = color, radius = 2.2.dp.toPx(), center = Offset(w * 0.74f, h * 0.76f))
    }
}

@Composable
private fun SafeShieldIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val shield = Path().apply {
            moveTo(w * 0.50f, h * 0.15f)
            lineTo(w * 0.82f, h * 0.26f)
            cubicTo(w * 0.82f, h * 0.62f, w * 0.50f, h * 0.85f, w * 0.50f, h * 0.85f)
            cubicTo(w * 0.50f, h * 0.85f, w * 0.18f, h * 0.62f, w * 0.18f, h * 0.26f)
            close()
        }
        drawPath(shield, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        val check = Path().apply {
            moveTo(w * 0.36f, h * 0.50f)
            lineTo(w * 0.46f, h * 0.60f)
            lineTo(w * 0.64f, h * 0.38f)
        }
        drawPath(check, color = color, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun QualityStarIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val star = Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.62f, h * 0.35f)
            lineTo(w * 0.92f, h * 0.38f)
            lineTo(w * 0.70f, h * 0.58f)
            lineTo(w * 0.76f, h * 0.88f)
            lineTo(w * 0.50f, h * 0.72f)
            lineTo(w * 0.24f, h * 0.88f)
            lineTo(w * 0.30f, h * 0.58f)
            lineTo(w * 0.08f, h * 0.38f)
            lineTo(w * 0.38f, h * 0.35f)
            close()
        }
        drawPath(star, color = color)
    }
}

@Composable
private fun ShoppingBagVectorIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val handle = Path().apply {
            moveTo(w * 0.35f, h * 0.35f)
            cubicTo(w * 0.35f, h * 0.15f, w * 0.65f, h * 0.15f, w * 0.65f, h * 0.35f)
        }
        drawPath(handle, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

        val bag = Path().apply {
            moveTo(w * 0.22f, h * 0.35f)
            lineTo(w * 0.78f, h * 0.35f)
            lineTo(w * 0.84f, h * 0.85f)
            lineTo(w * 0.16f, h * 0.85f)
            close()
        }
        drawPath(bag, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
