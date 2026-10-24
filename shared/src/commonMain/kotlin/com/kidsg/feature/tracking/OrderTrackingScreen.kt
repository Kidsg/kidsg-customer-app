package com.kidsg.feature.tracking

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography

/**
 * Uber-Style Live Order Tracking Screen (Reference Screen 19)
 * Features:
 * - Top Timeline progress stages
 * - Live animated Uber-style Canvas map with route, moving rider scooter, pulsing radar
 * - Floating live ETA countdown
 * - Driver contact card
 */
@Composable
fun OrderTrackingScreen(
    orderId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Rider animation along route (0.0f to 1.0f)
    val infiniteTransition = rememberInfiniteTransition()
    val riderProgress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Pulsing radar ripple
    val radarPulse by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KidsGColors.BackgroundPrimary)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KidsGColors.White)
                .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(KidsGColors.SurfaceElevated)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "←", style = KidsGTypography.TitleSmall)
            }

            Spacer(modifier = Modifier.width(KidsGSpacing.md))

            Column {
                Text(
                    text = "Order Tracking",
                    style = KidsGTypography.TitleLarge
                )
                Text(
                    text = "#$orderId",
                    style = KidsGTypography.Caption.copy(color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Order Status Timeline (10-step BookBuddys State Machine)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KidsGColors.White)
                    .padding(KidsGSpacing.lg)
            ) {
                Text(
                    text = "Order Journey (10-Step Lifecycle)",
                    style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold, color = KidsGColors.BlackText),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                TrackingTimelineItem(
                    title = "1. Order Received",
                    time = "Order created & logged in BookBuddys Bengaluru",
                    isCompleted = true,
                    isCurrent = false
                )
                TrackingTimelineItem(
                    title = "2. Payment Pending",
                    time = "Awaiting UPI payment submission",
                    isCompleted = true,
                    isCurrent = false
                )
                TrackingTimelineItem(
                    title = "3. Payment Submitted",
                    time = "12-digit UTR reference recorded",
                    isCompleted = true,
                    isCurrent = false
                )
                TrackingTimelineItem(
                    title = "4. Payment Verified",
                    time = "Payment confirmed by BookBuddys Founder Desk",
                    isCompleted = true,
                    isCurrent = false
                )
                TrackingTimelineItem(
                    title = "5. Order Accepted",
                    time = "Partner Hub accepted order items",
                    isCompleted = true,
                    isCurrent = false
                )
                TrackingTimelineItem(
                    title = "6. Packing at Hub",
                    time = "Stationery items gathered & sanitized",
                    isCompleted = true,
                    isCurrent = false
                )
                TrackingTimelineItem(
                    title = "7. Ready for Pickup",
                    time = "Package sealed & staged at partner store",
                    isCompleted = true,
                    isCurrent = false
                )
                TrackingTimelineItem(
                    title = "8. Delivery Partner Booked",
                    time = "Hyperlocal Bengaluru courier assigned",
                    isCompleted = true,
                    isCurrent = false
                )
                TrackingTimelineItem(
                    title = "9. Out for Delivery",
                    time = "Live • Rider en route with your package",
                    isCompleted = false,
                    isCurrent = true
                )
                TrackingTimelineItem(
                    title = "10. Delivered to Desk",
                    time = "Expected delivery to student desk in 25 mins",
                    isCompleted = false,
                    isCurrent = false,
                    isLast = true
                )
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // Partner Hub Card & WhatsApp Support Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KidsGSpacing.lg)
                    .clip(KidsGShapes.CardRounded)
                    .background(Color(0xFFFFF7ED))
                    .border(1.dp, Color(0xFFFFD8A8), KidsGShapes.CardRounded)
                    .padding(KidsGSpacing.md)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏪", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Fulfilling Hub: BookBuddys Partner Store",
                            style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Bengaluru Hyperlocal Network • Within 8.0 km radius",
                            style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KidsGShapes.Small)
                        .background(Color(0xFF25D366))
                        .clickable { /* WhatsApp action */ }
                        .padding(vertical = 10.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💬", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WhatsApp Support (+91 9353919178)",
                            style = KidsGTypography.TitleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // 2. Classy Uber-Style Live Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = KidsGSpacing.lg)
                    .clip(KidsGShapes.CardRounded)
                    .background(Color(0xFFE2E8F0))
                    .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.CardRounded)
            ) {
                // Compose Canvas rendering stylized map & animated path
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Background Map Features
                    // Green park zone
                    drawRect(
                        color = Color(0xFFDCFCE7),
                        topLeft = Offset(w * 0.05f, h * 0.1f),
                        size = androidx.compose.ui.geometry.Size(w * 0.35f, h * 0.3f)
                    )

                    // Soft river ribbon
                    val riverPath = Path().apply {
                        moveTo(0f, h * 0.7f)
                        cubicTo(w * 0.3f, h * 0.65f, w * 0.6f, h * 0.85f, w, h * 0.78f)
                    }
                    drawPath(
                        path = riverPath,
                        color = Color(0xFFBAE6FD),
                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                    )

                    // Secondary city roads
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(0f, h * 0.45f),
                        end = Offset(w, h * 0.45f),
                        strokeWidth = 10f
                    )
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(w * 0.3f, 0f),
                        end = Offset(w * 0.3f, h),
                        strokeWidth = 10f
                    )
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(w * 0.7f, 0f),
                        end = Offset(w * 0.7f, h),
                        strokeWidth = 10f
                    )

                    // Delivery Route Path from Depot (w*0.18, h*0.25) to Home (w*0.82, h*0.75)
                    val startX = w * 0.18f
                    val startY = h * 0.28f
                    val endX = w * 0.82f
                    val endY = h * 0.68f

                    val routePath = Path().apply {
                        moveTo(startX, startY)
                        cubicTo(w * 0.25f, h * 0.75f, w * 0.55f, h * 0.2f, endX, endY)
                    }

                    // Draw Glowing Route Path
                    drawPath(
                        path = routePath,
                        color = Color(0xFFFF8A00).copy(alpha = 0.35f),
                        style = Stroke(width = 12f, cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = routePath,
                        color = Color(0xFFFF6B00),
                        style = Stroke(
                            width = 5f,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
                        )
                    )

                    // Draw Store Depot Marker (Start)
                    drawCircle(
                        color = Color(0xFF1E293B),
                        radius = 16f,
                        center = Offset(startX, startY)
                    )
                    drawCircle(
                        color = Color(0xFFFBBF24),
                        radius = 9f,
                        center = Offset(startX, startY)
                    )

                    // Draw Destination Home Marker (End)
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 18f,
                        center = Offset(endX, endY)
                    )
                    drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = 8f,
                        center = Offset(endX, endY)
                    )

                    // Calculate animated Rider coordinates along cubic curve
                    val t = riderProgress
                    val p0 = Offset(startX, startY)
                    val p1 = Offset(w * 0.25f, h * 0.75f)
                    val p2 = Offset(w * 0.55f, h * 0.2f)
                    val p3 = Offset(endX, endY)

                    // Cubic Bezier interpolation: B(t) = (1-t)^3 p0 + 3(1-t)^2 t p1 + 3(1-t) t^2 p2 + t^3 p3
                    val oneMinusT = 1f - t
                    val riderX = (oneMinusT * oneMinusT * oneMinusT * p0.x) +
                            (3 * oneMinusT * oneMinusT * t * p1.x) +
                            (3 * oneMinusT * t * t * p2.x) +
                            (t * t * t * p3.x)
                    val riderY = (oneMinusT * oneMinusT * oneMinusT * p0.y) +
                            (3 * oneMinusT * oneMinusT * t * p1.y) +
                            (3 * oneMinusT * t * t * p2.y) +
                            (t * t * t * p3.y)
                    val riderPos = Offset(riderX, riderY)

                    // Pulsing radar ripple around rider
                    drawCircle(
                        color = Color(0xFFFF6B00).copy(alpha = 0.35f * (1f - (radarPulse / 36f))),
                        radius = radarPulse,
                        center = riderPos
                    )

                    // Rider Marker Circle
                    drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = 18f,
                        center = riderPos
                    )
                    drawCircle(
                        color = Color(0xFFFF6B00),
                        radius = 14f,
                        center = riderPos
                    )
                }

                // Floating Live ETA Badge (Top of Map)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .clip(KidsGShapes.FullPill)
                        .background(KidsGColors.White.copy(alpha = 0.95f))
                        .border(1.dp, KidsGColors.OrangePrimary, KidsGShapes.FullPill)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛵", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Arriving in 12 mins • 1.2 km away",
                            style = KidsGTypography.TitleSmall.copy(
                                fontSize = 13.sp,
                                color = KidsGColors.OrangePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // 3. Driver Contact & Vehicle Card (Reference Screen 19)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KidsGSpacing.lg)
                    .clip(KidsGShapes.CardRounded)
                    .background(KidsGColors.White)
                    .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.CardRounded)
                    .padding(KidsGSpacing.md)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Driver Avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(KidsGColors.AccentYellowLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "👨‍✈️", fontSize = 22.sp)
                            }

                            Spacer(modifier = Modifier.width(KidsGSpacing.md))

                            Column {
                                Text(
                                    text = "Ramesh Kumar",
                                    style = KidsGTypography.TitleSmall.copy(fontSize = 15.sp)
                                )
                                Text(
                                    text = "★ 4.9 • Hero Electric (KA 05 KG 2026)",
                                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary)
                                )
                            }
                        }

                        // Call & Message buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.sm)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7))
                                    .clickable { /* Call rider */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "📞", fontSize = 16.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(KidsGColors.AccentYellowLight)
                                    .clickable { /* Message rider */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "💬", fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(KidsGSpacing.md))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(KidsGColors.BorderSubtle)
                    )

                    Spacer(modifier = Modifier.height(KidsGSpacing.sm))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delivery Instructions: Ring bell & drop at desk",
                            style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted)
                        )
                        Text(
                            text = "Need Help?",
                            style = KidsGTypography.Caption.copy(
                                color = KidsGColors.OrangePrimary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable { /* open help */ }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.xl))
        }
    }
}

@Composable
fun TrackingTimelineItem(
    title: String,
    time: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isLast: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> Color(0xFF10B981)
                            isCurrent -> KidsGColors.OrangePrimary
                            else -> KidsGColors.BorderSubtle
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Text(text = "✓", color = KidsGColors.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                } else if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(KidsGColors.White)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(if (isCompleted) Color(0xFF10B981) else KidsGColors.BorderSubtle)
                )
            }
        }

        Spacer(modifier = Modifier.width(KidsGSpacing.md))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 12.dp)) {
            Text(
                text = title,
                style = KidsGTypography.TitleSmall.copy(
                    fontSize = 14.sp,
                    color = if (isCurrent) KidsGColors.OrangePrimary else KidsGColors.BlackText
                )
            )
            Text(
                text = time,
                style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary)
            )
        }
    }
}
