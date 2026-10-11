package com.kidsg.feature.shoppartner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.domain.model.Order
import kotlinx.coroutines.launch

@Composable
fun ShopPartnerDashboardScreen(
    profile: ShopPartnerProfile,
    activeOrders: List<Order>,
    onNavigateToOrders: () -> Unit,
    onNavigateToOrderDetail: (Order) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onToggleShopStatus: () -> Unit
) {
    var metrics by remember { mutableStateOf(ShopMetrics()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            metrics = ShopPartnerService.getDashboardMetrics()
        }
    }

    val displayOrders = activeOrders.take(5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KidsGColors.BackgroundSoft)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // ================================================================
        // TOP HEADER: Greeting + Shop + Notification Bell
        // ================================================================
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Good Morning,",
                    style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextSecondary)
                )
                Text(
                    text = "${profile.ownerName} 👋",
                    style = KidsGTypography.TitleLarge.copy(
                        color = KidsGColors.BlackText,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${profile.shopName}, ${profile.area}",
                    style = KidsGTypography.BodySmall.copy(
                        color = KidsGColors.OrangePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Notification Bell with Unread Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(KidsGColors.White)
                    .clickable { onNavigateToNotifications() },
                contentAlignment = Alignment.Center
            ) {
                Text("🔔", fontSize = 20.sp)
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(KidsGColors.RedPrimary)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ================================================================
        // STORE STATUS TOGGLE PILL
        // ================================================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleShopStatus() },
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B) // Dark navy container from screenshot
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (profile.shopStatus) {
                                    ShopStatus.OPEN -> KidsGColors.GreenFresh
                                    ShopStatus.BUSY -> KidsGColors.GoldYellow
                                    ShopStatus.CLOSED -> KidsGColors.RedPrimary
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = profile.shopStatus.name,
                        style = KidsGTypography.TitleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• Receiving orders",
                        style = KidsGTypography.BodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                    )
                }

                Text(
                    text = "Tap to switch ⚙️",
                    style = KidsGTypography.LabelSmall.copy(color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================================================================
        // METRICS DASHBOARD CARDS
        // ================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Today's Orders
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                color = KidsGColors.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📦", fontSize = 24.sp)
                        Text(
                            text = "${metrics.todayOrders}",
                            style = KidsGTypography.TitleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                color = KidsGColors.BlackText
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Today's Orders",
                        style = KidsGTypography.BodySmall.copy(
                            color = KidsGColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Card 2: Today's Sales
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                color = KidsGColors.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💰", fontSize = 24.sp)
                        Text(
                            text = "₹${metrics.todaySales.toInt()}",
                            style = KidsGTypography.TitleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = KidsGColors.GreenFresh
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Today's Sales",
                        style = KidsGTypography.BodySmall.copy(
                            color = KidsGColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 3: Pending Orders
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                color = KidsGColors.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏳", fontSize = 24.sp)
                        Text(
                            text = "${metrics.pendingOrders}",
                            style = KidsGTypography.TitleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                color = KidsGColors.OrangePrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pending Orders",
                        style = KidsGTypography.BodySmall.copy(
                            color = KidsGColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Card 4: Completed Orders
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                color = KidsGColors.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✅", fontSize = 24.sp)
                        Text(
                            text = "${metrics.completedOrders}",
                            style = KidsGTypography.TitleLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                color = KidsGColors.BlueAction
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Completed",
                        style = KidsGTypography.BodySmall.copy(
                            color = KidsGColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ================================================================
        // RECENT ORDERS SECTION
        // ================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Orders",
                style = KidsGTypography.TitleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = KidsGColors.BlackText
                )
            )

            Text(
                text = "View All >",
                style = KidsGTypography.BodyMedium.copy(
                    color = KidsGColors.OrangePrimary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable { onNavigateToOrders() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (displayOrders.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = KidsGColors.White
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎒", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No orders yet today",
                        style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "When customer orders stationery, they appear here live!",
                        style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextSecondary),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                displayOrders.forEach { order ->
                    ShopOrderCard(
                        order = order,
                        onClick = { onNavigateToOrderDetail(order) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun ShopOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = KidsGColors.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Order Icon Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KidsGColors.OrangePrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🛍️", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${order.displayOrderId}",
                            style = KidsGTypography.TitleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KidsGColors.BlackText
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• 2 mins ago",
                            style = KidsGTypography.LabelSmall.copy(color = KidsGColors.TextSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${order.items.size} items • ₹${order.totalAmount.toInt()}",
                        style = KidsGTypography.BodySmall.copy(
                            color = KidsGColors.BlackText,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Text(
                        text = "📍 ${order.deliveryAddress.city}",
                        style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary)
                    )
                }
            }

            // Status Tag matching design
            val (statusText, statusBg, statusColor) = when (order.status.name) {
                "CONFIRMED", "CREATED" -> Triple("New Order", KidsGColors.OrangePrimary.copy(alpha = 0.12f), KidsGColors.OrangePrimary)
                "ACCEPTED", "STORE_ACCEPTED" -> Triple("Accepted", KidsGColors.Success.copy(alpha = 0.12f), KidsGColors.Success)
                "PREPARING" -> Triple("Preparing", KidsGColors.Warning.copy(alpha = 0.2f), Color(0xFFB45309))
                "READY_FOR_PICKUP" -> Triple("Ready", KidsGColors.AccentSkyBlue.copy(alpha = 0.12f), Color(0xFF0284C7))
                "PICKED_UP", "OUT_FOR_DELIVERY" -> Triple("On Way", KidsGColors.AccentSkyBlue.copy(alpha = 0.12f), Color(0xFF0284C7))
                "DELIVERED" -> Triple("Delivered", KidsGColors.Success.copy(alpha = 0.12f), KidsGColors.Success)
                else -> Triple(order.status.name, KidsGColors.BorderSubtle, KidsGColors.TextSecondary)
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = statusBg
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = KidsGTypography.Caption.copy(
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
