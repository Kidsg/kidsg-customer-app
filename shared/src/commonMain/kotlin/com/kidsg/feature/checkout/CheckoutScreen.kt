package com.kidsg.feature.checkout

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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGPrimaryButton
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.domain.model.Address
import com.kidsg.domain.model.UserProfile

/**
 * Checkout Screen (Reference Screen 14)
 * - Stepper: Address -> Payment -> Review
 * - Send Address card with Change link
 * - Delivery Options: Standard (₹30) vs Express (₹60)
 * - Total Amount display
 * - Continue to Payment button
 */
@Composable
fun CheckoutScreen(
    userProfile: UserProfile?,
    subtotal: Double = 260.0,
    onBack: () -> Unit,
    onChangeAddress: () -> Unit,
    onContinueToPayment: (totalAmount: Double, deliverySpeed: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDeliveryOption by remember { mutableStateOf("Standard") }
    val deliveryFee = if (selectedDeliveryOption == "Standard") 30.0 else 60.0
    val totalAmount = subtotal + deliveryFee

    val studentName = userProfile?.studentName?.takeIf { it.isNotBlank() }
        ?: userProfile?.name?.takeIf { it.isNotBlank() }
        ?: "Aarav Kumar"
    val phone = userProfile?.phone?.takeIf { it.isNotBlank() } ?: "9876543210"

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

            Text(
                text = "Checkout",
                style = KidsGTypography.TitleLarge
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(KidsGSpacing.lg)
        ) {
            // Stepper: Address -> Payment -> Review
            CheckoutStepper(currentStep = 1)

            Spacer(modifier = Modifier.height(KidsGSpacing.xl))

            // Send address Header
            Text(
                text = "Send address",
                style = KidsGTypography.TitleSmall.copy(fontSize = 15.sp, color = KidsGColors.TextPrimary)
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.sm))

            // Address Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KidsGShapes.CardRounded)
                    .background(KidsGColors.White)
                    .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.CardRounded)
                    .padding(KidsGSpacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(KidsGColors.AccentYellowLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🏠", fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.width(KidsGSpacing.sm))

                        Column {
                            Text(
                                text = "Home",
                                style = KidsGTypography.TitleSmall.copy(fontSize = 14.sp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "12, Green Park, Bangalore - 560001",
                                style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextSecondary)
                            )
                            Text(
                                text = "$studentName   $phone",
                                style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted)
                            )
                        }
                    }

                    Text(
                        text = "Change",
                        style = KidsGTypography.Caption.copy(
                            color = KidsGColors.OrangePrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable(onClick = onChangeAddress)
                            .padding(KidsGSpacing.xs)
                    )
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.xl))

            // Delivery Options Header
            Text(
                text = "Delivery Options",
                style = KidsGTypography.TitleSmall.copy(fontSize = 15.sp, color = KidsGColors.TextPrimary)
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.sm))

            // Option 1: Standard Delivery
            DeliveryOptionCard(
                title = "Standard Delivery",
                subtitle = "3-5 days",
                price = "₹30",
                isSelected = selectedDeliveryOption == "Standard",
                onSelect = { selectedDeliveryOption = "Standard" }
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.sm))

            // Option 2: Express Delivery
            DeliveryOptionCard(
                title = "Express Delivery",
                subtitle = "1-2 days (Desk Rocket 🚀)",
                price = "₹60",
                isSelected = selectedDeliveryOption == "Express",
                onSelect = { selectedDeliveryOption = "Express" }
            )
        }

        // Bottom Summary & CTA Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KidsGColors.White)
                .border(1.dp, KidsGColors.BorderSubtle)
                .padding(KidsGSpacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount",
                    style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextSecondary)
                )
                Text(
                    text = "₹${totalAmount.toInt()}",
                    style = KidsGTypography.TitleLarge.copy(fontSize = 22.sp, color = KidsGColors.BlackText)
                )
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            KidsGPrimaryButton(
                text = "Continue to Payment",
                onClick = { onContinueToPayment(totalAmount, selectedDeliveryOption) }
            )
        }
    }
}

@Composable
fun CheckoutStepper(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepDot(stepNumber = 1, label = "Address", isActive = currentStep >= 1)
        StepLine(isActive = currentStep >= 2)
        StepDot(stepNumber = 2, label = "Payment", isActive = currentStep >= 2)
        StepLine(isActive = currentStep >= 3)
        StepDot(stepNumber = 3, label = "Review", isActive = currentStep >= 3)
    }
}

@Composable
fun StepDot(stepNumber: Int, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (isActive) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isActive) "✓" else "$stepNumber",
                style = KidsGTypography.Caption.copy(
                    color = if (isActive) KidsGColors.White else KidsGColors.TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = KidsGTypography.Caption.copy(
                fontSize = 11.sp,
                color = if (isActive) KidsGColors.OrangePrimary else KidsGColors.TextSecondary,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

@Composable
fun StepLine(isActive: Boolean) {
    Box(
        modifier = Modifier
            .width(40.dp)
            .height(2.dp)
            .background(if (isActive) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle)
            .padding(horizontal = 4.dp)
    )
}

@Composable
fun DeliveryOptionCard(
    title: String,
    subtitle: String,
    price: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KidsGShapes.CardRounded)
            .background(KidsGColors.White)
            .border(
                1.dp,
                if (isSelected) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle,
                KidsGShapes.CardRounded
            )
            .clickable(onClick = onSelect)
            .padding(KidsGSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = KidsGColors.OrangePrimary,
                        unselectedColor = KidsGColors.BorderSubtle
                    )
                )

                Spacer(modifier = Modifier.width(KidsGSpacing.sm))

                Column {
                    Text(
                        text = title,
                        style = KidsGTypography.TitleSmall.copy(fontSize = 14.sp)
                    )
                    Text(
                        text = subtitle,
                        style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary)
                    )
                }
            }

            Text(
                text = price,
                style = KidsGTypography.TitleSmall.copy(color = KidsGColors.BlackText)
            )
        }
    }
}
