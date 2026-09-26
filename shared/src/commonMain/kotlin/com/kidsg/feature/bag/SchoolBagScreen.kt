package com.kidsg.feature.bag

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGAsyncImage
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGProductVisual
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.data.repository.ProductionCartRepository
import com.kidsg.domain.model.Cart
import com.kidsg.domain.model.CartItem
import com.kidsg.domain.repository.CartRepository
import com.kidsg.domain.repository.ConfigRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Cart UI State Hierarchy following MVI/MVVM architecture.
 */
sealed interface CartUiState {
    data object Loading : CartUiState
    data object Empty : CartUiState
    data class Loaded(
        val items: List<CartItem>,
        val subtotal: Double,
        val deliveryFee: Double,
        val discount: Double,
        val total: Double,
        val isFreeDelivery: Boolean,
        val rawCart: Cart
    ) : CartUiState
    data class Error(val message: String) : CartUiState
}

/**
 * Rebuilt KidsG School Bag Screen (Cart)
 * strictly adhering to the KidsG Visual Reference:
 * - Empty state with custom playful yellow school backpack, bear keychain, doodles, CTA & trust card.
 * - Loading state with animated skeleton shimmer product rows & summary.
 * - Loaded state with real backend/cart updates, quantity controls, dynamic bill details & checkout CTA.
 * - Error state with friendly KidsG visual and retry capability.
 */
@Composable
fun SchoolBagScreen(
    cartRepository: CartRepository,
    configRepository: ConfigRepository,
    onProceedToCheckout: () -> Unit,
    onStartShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cart by cartRepository.cartState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isInitialLoading by remember { mutableStateOf(true) }
    var errorState by remember { mutableStateOf<String?>(null) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    var couponMessage by remember { mutableStateOf<String?>(null) }

    // Initial sync simulation with cart API
    LaunchedEffect(Unit) {
        try {
            if (cartRepository is ProductionCartRepository) {
                cartRepository.refreshCart()
            }
            delay(400) // Brief smooth shimmer skeleton transition
        } catch (e: Exception) {
            errorState = e.message ?: "Failed to load School Bag"
        } finally {
            isInitialLoading = false
        }
    }

    val uiState: CartUiState = when {
        isInitialLoading -> CartUiState.Loading
        errorState != null -> CartUiState.Error(errorState ?: "Unknown error")
        cart.items.isEmpty() -> CartUiState.Empty
        else -> CartUiState.Loaded(
            items = cart.items,
            subtotal = cart.subtotal,
            deliveryFee = cart.deliveryFee,
            discount = cart.productDiscount + cart.couponDiscount,
            total = cart.finalTotal,
            isFreeDelivery = cart.isFreeDeliveryEligible,
            rawCart = cart
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KidsGColors.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Header: "My School Bag" + Subtitle
            SchoolBagHeader(
                showMenuButton = uiState !is CartUiState.Error,
                onMenuClick = { showOptionsMenu = true }
            )

            // Dynamic Body Content based on CartUiState
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (uiState) {
                    is CartUiState.Loading -> {
                        LoadingStateContent()
                    }

                    is CartUiState.Empty -> {
                        EmptyStateContent(
                            onExploreClick = onStartShopping
                        )
                    }

                    is CartUiState.Loaded -> {
                        LoadedStateContent(
                            loadedState = uiState,
                            cartRepository = cartRepository,
                            couponMessage = couponMessage,
                            onApplyCoupon = { code ->
                                coroutineScope.launch {
                                    val res = cartRepository.applyCoupon(code)
                                    couponMessage = if (res.isSuccess) "Coupon applied!" else res.exceptionOrNull()?.message
                                }
                            },
                            onRemoveCoupon = {
                                coroutineScope.launch {
                                    cartRepository.removeCoupon()
                                    couponMessage = null
                                }
                            }
                        )
                    }

                    is CartUiState.Error -> {
                        ErrorStateContent(
                            errorMessage = uiState.message,
                            onRetry = {
                                coroutineScope.launch {
                                    isInitialLoading = true
                                    errorState = null
                                    try {
                                        if (cartRepository is ProductionCartRepository) {
                                            cartRepository.refreshCart()
                                        }
                                        delay(500)
                                    } catch (e: Exception) {
                                        errorState = e.message ?: "Could not load bag"
                                    } finally {
                                        isInitialLoading = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Header More Options Dropdown
        if (showOptionsMenu) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 20.dp)
            ) {
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                    modifier = Modifier.background(KidsGColors.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Refresh Bag", style = KidsGTypography.BodyMedium) },
                        onClick = {
                            showOptionsMenu = false
                            coroutineScope.launch {
                                isInitialLoading = true
                                try {
                                    if (cartRepository is ProductionCartRepository) {
                                        cartRepository.refreshCart()
                                    }
                                    delay(400)
                                } catch (e: Exception) {
                                    errorState = e.message
                                } finally {
                                    isInitialLoading = false
                                }
                            }
                        }
                    )
                    if (cart.items.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Empty School Bag", color = KidsGColors.Error, style = KidsGTypography.BodyMedium) },
                            onClick = {
                                showOptionsMenu = false
                                showClearConfirmDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Clear Bag Confirmation Dialog
        if (showClearConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showClearConfirmDialog = false },
                title = { Text("Empty your School Bag?", style = KidsGTypography.TitleMedium) },
                text = { Text("Are you sure you want to remove all stationery items from your bag?", style = KidsGTypography.BodyMedium) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearConfirmDialog = false
                            coroutineScope.launch {
                                cartRepository.clearCart()
                            }
                        }
                    ) {
                        Text("Empty Bag", color = KidsGColors.Error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirmDialog = false }) {
                        Text("Keep Items", color = KidsGColors.TextSecondary)
                    }
                },
                containerColor = KidsGColors.White
            )
        }

        // Sticky Checkout Bottom Bar (When Loaded)
        if (uiState is CartUiState.Loaded) {
            StickyCheckoutBar(
                cart = uiState.rawCart,
                onProceedToCheckout = onProceedToCheckout,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Top Header matching the design reference:
 * "My School Bag" (Bag in KidsG Orange #FF7A00, My School in Black #111111)
 * Subtitle: "All your selected items will appear here"
 * Top-right orange circular button with "···"
 */
@Composable
private fun SchoolBagHeader(
    showMenuButton: Boolean,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(end = if (showMenuButton) 48.dp else 0.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "My School ",
                    style = KidsGTypography.DisplayLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111111)
                    )
                )
                Text(
                    text = "Bag",
                    style = KidsGTypography.DisplayLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KidsGColors.OrangePrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "All your selected items will appear here",
                style = KidsGTypography.BodySmall.copy(
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            )
        }

        if (showMenuButton) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(KidsGColors.OrangePrimary)
                    .clickable(onClick = onMenuClick),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(3.5.dp).clip(CircleShape).background(KidsGColors.White))
                    Box(modifier = Modifier.size(3.5.dp).clip(CircleShape).background(KidsGColors.White))
                    Box(modifier = Modifier.size(3.5.dp).clip(CircleShape).background(KidsGColors.White))
                }
            }
        }
    }
}

/**
 * EMPTY STATE
 * Playful Stationery School Bag Illustration + Bear Keychain + Doodles
 * "Your School Bag is empty"
 * "Explore Stationery →" CTA
 * Trust & service benefits card (Great quality, Fast delivery, Safe & reliable)
 */
@Composable
private fun EmptyStateContent(
    onExploreClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Large Playful Stationery / School Bag Illustration Area
            PlayfulSchoolBagIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Main message
            Text(
                text = "Your School Bag is empty",
                style = KidsGTypography.TitleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Supporting text
            Text(
                text = "Add notebooks, pens, art supplies and more to get started for a brighter day!",
                style = KidsGTypography.BodyMedium.copy(
                    fontSize = 13.5.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 20.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary CTA: "Explore Stationery →"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(KidsGShapes.FullPill)
                    .background(KidsGColors.OrangePrimary)
                    .clickable(onClick = onExploreClick)
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
                        text = "Explore Stationery  →",
                        style = KidsGTypography.TitleSmall.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = KidsGColors.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trust & Service Benefits Card
            TrustBenefitsCard()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Native Compose Vector Illustration of a warm playful school backpack
 * with curved straps, handle, front pocket, cute bear keychain charm,
 * paper airplane flying up-right with dashed flight trail, and twinkling stars.
 */
@Composable
private fun PlayfulSchoolBagIllustration(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bagFloat")

    // Subtle gentle floating bobbing animation
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bagY"
    )

    // Twinkling star animation
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starTwinkle"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { translationY = floatOffset }) {
            val w = size.width
            val h = size.height
            val centerX = w * 0.50f
            val centerY = h * 0.52f

            // 1. Soft warm ambient glow in background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF2E2), Color(0xFFFFF9F0), Color.Transparent),
                    center = Offset(centerX, centerY),
                    radius = w * 0.46f
                ),
                radius = w * 0.46f,
                center = Offset(centerX, centerY)
            )

            // 2. Soft ground shadow under backpack
            drawOval(
                color = Color(0xFFEADBCE).copy(alpha = 0.55f),
                topLeft = Offset(centerX - 75.dp.toPx(), centerY + 58.dp.toPx()),
                size = Size(150.dp.toPx(), 18.dp.toPx())
            )

            // 3. Playful Doodles around the bag

            // Paper airplane flying to top-right
            val planeX = w * 0.82f
            val planeY = h * 0.18f

            // Dashed flight trail looping gracefully
            val trailPath = Path().apply {
                moveTo(centerX + 35.dp.toPx(), centerY - 45.dp.toPx())
                cubicTo(
                    w * 0.70f, h * 0.38f,
                    w * 0.74f, h * 0.26f,
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

            // Playful decorative stars
            drawPlayfulStar(Offset(w * 0.18f, h * 0.28f), 10.dp.toPx(), Color(0xFFFBBF24).copy(alpha = starAlpha))
            drawPlayfulStar(Offset(w * 0.76f, h * 0.32f), 8.dp.toPx(), Color(0xFFC084FC).copy(alpha = starAlpha))
            drawPlayfulStar(Offset(w * 0.14f, h * 0.65f), 9.dp.toPx(), Color(0xFF60A5FA).copy(alpha = starAlpha))
            drawPlayfulStar(Offset(w * 0.84f, h * 0.58f), 11.dp.toPx(), Color(0xFFFBBF24).copy(alpha = starAlpha))

            // Little burst doodles near backpack
            drawLine(
                color = KidsGColors.OrangePrimary,
                start = Offset(centerX + 62.dp.toPx(), centerY - 46.dp.toPx()),
                end = Offset(centerX + 72.dp.toPx(), centerY - 56.dp.toPx()),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = KidsGColors.OrangePrimary,
                start = Offset(centerX + 50.dp.toPx(), centerY - 56.dp.toPx()),
                end = Offset(centerX + 56.dp.toPx(), centerY - 68.dp.toPx()),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 4. Backpack Top Handle
            val handlePath = Path().apply {
                moveTo(centerX - 24.dp.toPx(), centerY - 52.dp.toPx())
                cubicTo(
                    centerX - 24.dp.toPx(), centerY - 88.dp.toPx(),
                    centerX + 24.dp.toPx(), centerY - 88.dp.toPx(),
                    centerX + 24.dp.toPx(), centerY - 52.dp.toPx()
                )
            }
            drawPath(
                path = handlePath,
                color = Color(0xFFEA580C),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // 5. Main Backpack Body
            val bagWidth = 126.dp.toPx()
            val bagHeight = 118.dp.toPx()
            val bagLeft = centerX - (bagWidth / 2f)
            val bagTop = centerY - 54.dp.toPx()

            // Main body gradient (golden amber/yellow)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFA000), Color(0xFFF57C00)),
                    startY = bagTop,
                    endY = bagTop + bagHeight
                ),
                topLeft = Offset(bagLeft, bagTop),
                size = Size(bagWidth, bagHeight),
                cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
            )

            // Zipper Seam Curve outline
            val zipperPath = Path().apply {
                moveTo(bagLeft + 14.dp.toPx(), bagTop + bagHeight - 16.dp.toPx())
                cubicTo(
                    bagLeft + 14.dp.toPx(), bagTop + 14.dp.toPx(),
                    bagLeft + bagWidth - 14.dp.toPx(), bagTop + 14.dp.toPx(),
                    bagLeft + bagWidth - 14.dp.toPx(), bagTop + bagHeight - 16.dp.toPx()
                )
            }
            drawPath(
                path = zipperPath,
                color = Color(0xFFD97706),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 6. Front Pocket (Rounded Pouch)
            val pocketWidth = 104.dp.toPx()
            val pocketHeight = 62.dp.toPx()
            val pocketLeft = centerX - (pocketWidth / 2f)
            val pocketTop = centerY + 2.dp.toPx()

            // Front pocket body with lighter yellow face
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFB74D), Color(0xFFFFA726)),
                    startY = pocketTop,
                    endY = pocketTop + pocketHeight
                ),
                topLeft = Offset(pocketLeft, pocketTop),
                size = Size(pocketWidth, pocketHeight),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
            )

            // Front pocket border
            drawRoundRect(
                color = Color(0xFFB45309).copy(alpha = 0.4f),
                topLeft = Offset(pocketLeft, pocketTop),
                size = Size(pocketWidth, pocketHeight),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Pocket Zipper Line
            val pocketZipperY = pocketTop + 13.dp.toPx()
            drawLine(
                color = Color(0xFFD97706),
                start = Offset(pocketLeft + 14.dp.toPx(), pocketZipperY),
                end = Offset(pocketLeft + pocketWidth - 14.dp.toPx(), pocketZipperY),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Pocket zipper pull tab
            drawRoundRect(
                color = Color(0xFF9E9E9E),
                topLeft = Offset(pocketLeft + 24.dp.toPx(), pocketZipperY - 2.dp.toPx()),
                size = Size(10.dp.toPx(), 5.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // 7. Cute Bear Keychain Charm hanging on the left of front zipper!
            val bearCenterX = pocketLeft + 12.dp.toPx()
            val bearCenterY = pocketTop + 30.dp.toPx()

            // Keychain ring & strap
            drawLine(
                color = Color(0xFF111111),
                start = Offset(pocketLeft + 18.dp.toPx(), pocketZipperY),
                end = Offset(bearCenterX, bearCenterY - 14.dp.toPx()),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(
                color = Color(0xFF9E9E9E),
                radius = 3.dp.toPx(),
                center = Offset(bearCenterX, bearCenterY - 14.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Bear Ears
            drawCircle(color = Color(0xFF8D4925), radius = 5.dp.toPx(), center = Offset(bearCenterX - 9.dp.toPx(), bearCenterY - 9.dp.toPx()))
            drawCircle(color = Color(0xFFD7CCC8), radius = 2.5.dp.toPx(), center = Offset(bearCenterX - 9.dp.toPx(), bearCenterY - 9.dp.toPx()))

            drawCircle(color = Color(0xFF8D4925), radius = 5.dp.toPx(), center = Offset(bearCenterX + 9.dp.toPx(), bearCenterY - 9.dp.toPx()))
            drawCircle(color = Color(0xFFD7CCC8), radius = 2.5.dp.toPx(), center = Offset(bearCenterX + 9.dp.toPx(), bearCenterY - 9.dp.toPx()))

            // Bear Head
            drawCircle(color = Color(0xFF8D4925), radius = 13.dp.toPx(), center = Offset(bearCenterX, bearCenterY))

            // Bear Muzzle
            drawOval(
                color = Color(0xFFFFE0B2),
                topLeft = Offset(bearCenterX - 6.5.dp.toPx(), bearCenterY - 1.dp.toPx()),
                size = Size(13.dp.toPx(), 9.dp.toPx())
            )

            // Bear Nose
            drawCircle(color = Color(0xFF111111), radius = 1.6.dp.toPx(), center = Offset(bearCenterX, bearCenterY + 1.2.dp.toPx()))

            // Bear Eyes with cute white glints
            drawCircle(color = Color(0xFF111111), radius = 1.8.dp.toPx(), center = Offset(bearCenterX - 4.2.dp.toPx(), bearCenterY - 3.dp.toPx()))
            drawCircle(color = Color.White, radius = 0.6.dp.toPx(), center = Offset(bearCenterX - 4.5.dp.toPx(), bearCenterY - 3.5.dp.toPx()))

            drawCircle(color = Color(0xFF111111), radius = 1.8.dp.toPx(), center = Offset(bearCenterX + 4.2.dp.toPx(), bearCenterY - 3.dp.toPx()))
            drawCircle(color = Color.White, radius = 0.6.dp.toPx(), center = Offset(bearCenterX + 3.9.dp.toPx(), bearCenterY - 3.5.dp.toPx()))
        }
    }
}

/**
 * Helper to draw a playful 4-point star on Canvas.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlayfulStar(
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

/**
 * Trust and service benefits card:
 * - Great quality (Pink Heart)
 * - Fast delivery (Green Truck)
 * - Safe & reliable (Amber Shield)
 */
@Composable
private fun TrustBenefitsCard() {
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
            // 1. Great quality
            TrustBenefitItem(
                icon = { HeartBenefitIcon(modifier = Modifier.size(24.dp), color = Color(0xFFFF4B72)) },
                label = "Great\nquality"
            )

            // Divider
            Box(modifier = Modifier.height(28.dp).width(1.dp).background(Color(0xFFF3F4F6)))

            // 2. Fast delivery
            TrustBenefitItem(
                icon = { TruckBenefitIcon(modifier = Modifier.size(24.dp), color = Color(0xFF10B981)) },
                label = "Fast\ndelivery"
            )

            // Divider
            Box(modifier = Modifier.height(28.dp).width(1.dp).background(Color(0xFFF3F4F6)))

            // 3. Safe & reliable
            TrustBenefitItem(
                icon = { ShieldBenefitIcon(modifier = Modifier.size(24.dp), color = Color(0xFFF59E0B)) },
                label = "Safe &\nreliable"
            )
        }
    }
}

@Composable
private fun TrustBenefitItem(
    icon: @Composable () -> Unit,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
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

/**
 * LOADING STATE
 * 3-4 animated skeleton product rows + skeleton summary box with shimmer gradient.
 */
@Composable
private fun LoadingStateContent() {
    val shimmerTransition = rememberInfiniteTransition(label = "skeletonShimmer")
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
        items(3) {
            SkeletonProductCard(shimmerBrush = shimmerBrush)
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonSummaryCard(shimmerBrush = shimmerBrush)
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SkeletonProductCard(shimmerBrush: Brush) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, Color(0xFFF3F4F6), KidsGShapes.Medium)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skeleton Product Image placeholder
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(shimmerBrush)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Product title placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Product subtitle/variant placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
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
                    // Price placeholder
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(shimmerBrush)
                    )

                    // Stepper placeholder
                    Box(
                        modifier = Modifier
                            .width(72.dp)
                            .height(28.dp)
                            .clip(KidsGShapes.FullPill)
                            .background(shimmerBrush)
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonSummaryCard(shimmerBrush: Brush) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, Color(0xFFF3F4F6), KidsGShapes.Medium)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(28.dp)
                    .clip(KidsGShapes.FullPill)
                    .background(shimmerBrush)
            )
        }
    }
}

/**
 * LOADED STATE
 * Real backend/cart products, interactive quantity controls,
 * free delivery goal banner, coupons, accurate bill breakdown & sticky checkout CTA.
 */
@Composable
private fun LoadedStateContent(
    loadedState: CartUiState.Loaded,
    cartRepository: CartRepository,
    couponMessage: String?,
    onApplyCoupon: (String) -> Unit,
    onRemoveCoupon: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val cart = loadedState.rawCart

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Free Delivery Progress / Status Banner
        item {
            FreeDeliveryStatusBanner(
                subtotal = cart.subtotal,
                threshold = cart.deliveryConfig.freeDeliveryThreshold,
                isFreeDelivery = loadedState.isFreeDelivery
            )
        }

        // Real Packed Items in School Bag
        items(cart.items, key = { it.product.id }) { item ->
            ProductCartRowCard(
                item = item,
                onIncrease = {
                    coroutineScope.launch {
                        cartRepository.updateQuantity(item.product.id, item.quantity + 1)
                    }
                },
                onDecrease = {
                    coroutineScope.launch {
                        if (item.quantity <= 1) {
                            cartRepository.removeFromCart(item.product.id)
                        } else {
                            cartRepository.updateQuantity(item.product.id, item.quantity - 1)
                        }
                    }
                },
                onRemove = {
                    coroutineScope.launch {
                        cartRepository.removeFromCart(item.product.id)
                    }
                }
            )
        }

        // Coupon Section
        item {
            StationeryCouponCard(
                appliedCoupon = cart.appliedCoupon,
                onApply = onApplyCoupon,
                onRemove = onRemoveCoupon,
                message = couponMessage
            )
        }

        // Bill Breakdown Details
        item {
            DetailedBillCard(cart = cart)
        }
    }
}

@Composable
private fun FreeDeliveryStatusBanner(
    subtotal: Double,
    threshold: Double,
    isFreeDelivery: Boolean
) {
    val progress = (subtotal / threshold).coerceIn(0.0, 1.0).toFloat()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KidsGShapes.Medium)
            .background(if (isFreeDelivery) Color(0xFFECFDF5) else Color(0xFFFFF9DB))
            .border(
                1.dp,
                if (isFreeDelivery) Color(0xFFA7F3D0) else Color(0xFFFFE082),
                KidsGShapes.Medium
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (isFreeDelivery) "🎉" else "⚡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFreeDelivery) "FREE Delivery unlocked for your bag!" else "Add ₹${(threshold - subtotal).toInt()} more for FREE Delivery",
                        style = KidsGTypography.TitleSmall.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFreeDelivery) Color(0xFF065F46) else Color(0xFF92400E)
                        )
                    )
                }

                Text(
                    text = if (isFreeDelivery) "FREE" else "₹30",
                    style = KidsGTypography.Tag.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isFreeDelivery) Color(0xFF065F46) else Color(0xFF92400E)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(KidsGShapes.FullPill),
                color = if (isFreeDelivery) Color(0xFF10B981) else KidsGColors.OrangePrimary,
                trackColor = KidsGColors.White,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun ProductCartRowCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, Color(0xFFF3F4F6), KidsGShapes.Medium)
            .shadow(1.dp, KidsGShapes.Medium, spotColor = Color(0x06000000))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Visual / Real Image
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = item.product.imageUrl
                if (imageUrl.isNotBlank()) {
                    KidsGAsyncImage(
                        url = imageUrl,
                        contentDescription = item.product.name,
                        modifier = Modifier.size(68.dp),
                        placeholder = {
                            KidsGProductVisual(product = item.product, modifier = Modifier.size(46.dp))
                        }
                    )
                } else {
                    KidsGProductVisual(product = item.product, modifier = Modifier.size(46.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = KidsGTypography.TitleSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111111)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!item.selectedVariant.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Variant: ${item.selectedVariant}",
                        style = KidsGTypography.Caption.copy(
                            fontSize = 11.5.sp,
                            color = Color(0xFF6B7280)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Price & Savings
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${item.totalItemPrice.toInt()}",
                            style = KidsGTypography.TitleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111)
                            )
                        )

                        if (item.product.mrp > item.product.price) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "₹${item.totalItemMrp.toInt()}",
                                style = KidsGTypography.Caption.copy(
                                    fontSize = 12.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = Color(0xFF9CA3AF)
                                )
                            )
                        }
                    }

                    // Native Stepper [-] QTY [+]
                    Row(
                        modifier = Modifier
                            .clip(KidsGShapes.FullPill)
                            .background(Color(0xFFFFF7ED))
                            .border(1.dp, Color(0xFFFED7AA), KidsGShapes.FullPill)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onDecrease),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (item.quantity == 1) "🗑" else "−",
                                style = KidsGTypography.TitleSmall.copy(
                                    color = KidsGColors.OrangePrimary,
                                    fontSize = if (item.quantity == 1) 12.sp else 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = "${item.quantity}",
                            style = KidsGTypography.TitleSmall.copy(
                                color = KidsGColors.OrangePrimary,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onIncrease),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                style = KidsGTypography.TitleSmall.copy(
                                    color = KidsGColors.OrangePrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationeryCouponCard(
    appliedCoupon: com.kidsg.domain.model.Coupon?,
    onApply: (String) -> Unit,
    onRemove: () -> Unit,
    message: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, Color(0xFFF3F4F6), KidsGShapes.Medium)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏷️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (appliedCoupon != null) "Applied: ${appliedCoupon.code}" else "Apply Stationery Coupon",
                        style = KidsGTypography.TitleSmall.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111111)
                        )
                    )
                }

                if (appliedCoupon != null) {
                    Text(
                        text = "Remove",
                        style = KidsGTypography.Tag.copy(
                            fontWeight = FontWeight.Bold,
                            color = KidsGColors.Error
                        ),
                        modifier = Modifier.clickable(onClick = onRemove)
                    )
                }
            }

            if (appliedCoupon == null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CouponQuickChip(code = "KIDSG50", label = "50% OFF", onClick = { onApply("KIDSG50") })
                    CouponQuickChip(code = "EXAMREADY", label = "EXAM READY", onClick = { onApply("EXAMREADY") })
                }
            }

            if (!message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = message,
                    style = KidsGTypography.Caption.copy(
                        color = if (appliedCoupon != null) Color(0xFF10B981) else KidsGColors.Error,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun CouponQuickChip(code: String, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(KidsGShapes.Small)
            .background(Color(0xFFFFF7ED))
            .border(1.dp, Color(0xFFFED7AA), KidsGShapes.Small)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = "$code ($label)",
            style = KidsGTypography.Tag.copy(
                color = KidsGColors.OrangePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun DetailedBillCard(cart: Cart) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, Color(0xFFF3F4F6), KidsGShapes.Medium)
            .padding(16.dp)
    ) {
        Text(
            text = "Bill Summary",
            style = KidsGTypography.TitleSmall.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        BillRowItem(label = "Items Subtotal (${cart.itemCount})", value = "₹${cart.subtotal.toInt()}")

        if (cart.productDiscount > 0) {
            BillRowItem(label = "Product Discount", value = "−₹${cart.productDiscount.toInt()}", valueColor = Color(0xFF10B981))
        }

        if (cart.couponDiscount > 0) {
            BillRowItem(label = "Coupon Discount", value = "−₹${cart.couponDiscount.toInt()}", valueColor = Color(0xFF10B981))
        }

        BillRowItem(
            label = "Delivery Partner Fee",
            value = if (cart.isFreeDeliveryEligible) "FREE" else "₹${cart.deliveryFee.toInt()}",
            valueColor = if (cart.isFreeDeliveryEligible) Color(0xFF10B981) else Color(0xFF111111)
        )

        BillRowItem(label = "Platform & Handling Fee", value = "₹${cart.platformFee.toInt()}")
        BillRowItem(label = "Govt. Taxes (GST 5%)", value = "₹${cart.taxAmount.toInt()}")

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF3F4F6))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total to Pay",
                style = KidsGTypography.TitleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111111)
                )
            )
            Text(
                text = "₹${cart.finalTotal.toInt()}",
                style = KidsGTypography.TitleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF111111)
                )
            )
        }
    }
}

@Composable
private fun BillRowItem(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF111111)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = KidsGTypography.BodySmall.copy(
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )
        )
        Text(
            text = value,
            style = KidsGTypography.BodySmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        )
    }
}

/**
 * Sticky Checkout Bottom Bar:
 * Displays real Total to pay, total savings tag and "Proceed to Checkout →" CTA.
 */
@Composable
private fun StickyCheckoutBar(
    cart: Cart,
    onProceedToCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(KidsGColors.White)
            .border(1.dp, Color(0xFFF3F4F6))
            .shadow(4.dp, spotColor = Color(0x0F000000))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total to Pay",
                    style = KidsGTypography.Caption.copy(
                        fontSize = 11.5.sp,
                        color = Color(0xFF6B7280)
                    )
                )
                Text(
                    text = "₹${cart.finalTotal.toInt()}",
                    style = KidsGTypography.DisplayMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111111)
                    )
                )
                if (cart.totalSavings > 0) {
                    Text(
                        text = "You save ₹${cart.totalSavings.toInt()}!",
                        style = KidsGTypography.Tag.copy(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(48.dp)
                    .clip(KidsGShapes.FullPill)
                    .background(KidsGColors.OrangePrimary)
                    .clickable(onClick = onProceedToCheckout),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Proceed to Checkout →",
                    style = KidsGTypography.TitleSmall.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.White
                    )
                )
            }
        }
    }
}

/**
 * ERROR STATE
 * Friendly illustration of school bag, friendly message, and "Try Again" CTA.
 */
@Composable
private fun ErrorStateContent(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Friendly KidsG Illustration
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFF7ED)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🎒❓", fontSize = 42.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Text(
            text = "Oops! Your school bag got lost.",
            style = KidsGTypography.TitleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111111)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "We couldn't load your bag right now.",
            style = KidsGTypography.BodyMedium.copy(
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Button: Try Again
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
 * Vector Benefit Icons rendered cleanly with native Compose Canvas
 */
@Composable
private fun HeartBenefitIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val heart = Path().apply {
            moveTo(w * 0.5f, h * 0.8f)
            cubicTo(w * 0.15f, h * 0.55f, w * 0.10f, h * 0.25f, w * 0.32f, h * 0.20f)
            cubicTo(w * 0.44f, h * 0.18f, w * 0.50f, h * 0.30f, w * 0.50f, h * 0.32f)
            cubicTo(w * 0.50f, h * 0.30f, w * 0.56f, h * 0.18f, w * 0.68f, h * 0.20f)
            cubicTo(w * 0.90f, h * 0.25f, w * 0.85f, h * 0.55f, w * 0.5f, h * 0.8f)
            close()
        }
        drawPath(heart, color = color)
    }
}

@Composable
private fun TruckBenefitIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Truck body
        val body = Path().apply {
            moveTo(w * 0.15f, h * 0.32f)
            lineTo(w * 0.62f, h * 0.32f)
            lineTo(w * 0.62f, h * 0.48f)
            lineTo(w * 0.82f, h * 0.48f)
            lineTo(w * 0.88f, h * 0.65f)
            lineTo(w * 0.88f, h * 0.75f)
            lineTo(w * 0.15f, h * 0.75f)
            close()
        }
        drawPath(body, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Wheels
        drawCircle(color = color, radius = 2.5.dp.toPx(), center = Offset(w * 0.32f, h * 0.76f))
        drawCircle(color = color, radius = 2.5.dp.toPx(), center = Offset(w * 0.74f, h * 0.76f))
    }
}

@Composable
private fun ShieldBenefitIcon(modifier: Modifier = Modifier, color: Color) {
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
        drawPath(shield, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Checkmark inside shield
        val check = Path().apply {
            moveTo(w * 0.36f, h * 0.50f)
            lineTo(w * 0.46f, h * 0.60f)
            lineTo(w * 0.64f, h * 0.38f)
        }
        drawPath(check, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun ShoppingBagVectorIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Bag Handle
        val handle = Path().apply {
            moveTo(w * 0.35f, h * 0.35f)
            cubicTo(w * 0.35f, h * 0.15f, w * 0.65f, h * 0.15f, w * 0.65f, h * 0.35f)
        }
        drawPath(handle, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

        // Bag Outline
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
