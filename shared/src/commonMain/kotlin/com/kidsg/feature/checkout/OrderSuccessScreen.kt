package com.kidsg.feature.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGOutlineButton
import com.kidsg.core.designsystem.KidsGPrimaryButton
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography

/**
 * Order Placed Successfully Screen (Reference Screen 17)
 */
@Composable
fun OrderSuccessScreen(
    orderId: String,
    totalAmount: Double = 290.0,
    onViewOrder: () -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KidsGColors.White)
            .padding(KidsGSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.7f))

        // Cute smiling package box character with confetti
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Confetti dots around box
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF38BDF8))
                    .align(Alignment.TopStart)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFBBF24))
                    .align(Alignment.TopEnd)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFA855F7))
                    .align(Alignment.CenterStart)
            )
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34D399))
                    .align(Alignment.BottomEnd)
            )
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF43F5E))
                    .align(Alignment.BottomStart)
            )

            // Smiling 3D box character
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFFBBF24))
                    .border(3.dp, Color(0xFFF59E0B), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Two eyes
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Cute smile curve
                    Text(
                        text = "‿",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(KidsGSpacing.xl))

        // Title
        Text(
            text = "Order Placed\nSuccessfully!",
            style = KidsGTypography.DisplayMedium.copy(
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(KidsGSpacing.md))

        // Order ID Badge
        Box(
            modifier = Modifier
                .clip(KidsGShapes.FullPill)
                .background(KidsGColors.AccentYellowLight)
                .border(1.dp, KidsGColors.AccentYellow, KidsGShapes.FullPill)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Order #$orderId",
                style = KidsGTypography.TitleSmall.copy(
                    fontSize = 14.sp,
                    color = KidsGColors.OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(KidsGSpacing.md))

        Text(
            text = "We'll notify you when your items are on the way to your desk.",
            style = KidsGTypography.BodyMedium.copy(
                color = KidsGColors.TextSecondary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = KidsGSpacing.md)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Buttons
        KidsGPrimaryButton(
            text = "View Order",
            onClick = onViewOrder
        )

        Spacer(modifier = Modifier.height(KidsGSpacing.md))

        KidsGOutlineButton(
            text = "Continue Shopping",
            onClick = onContinueShopping
        )

        Spacer(modifier = Modifier.height(KidsGSpacing.lg))
    }
}
