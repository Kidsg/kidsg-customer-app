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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.kidsg.core.storage.SessionStorage
import com.kidsg.domain.model.Address
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Payment Method Screen (Reference Screen 16)
 */
@Composable
fun PaymentMethodScreen(
    totalAmount: Double = 290.0,
    deliverySpeed: String = "Standard",
    deliveryAddress: Address? = null,
    onBack: () -> Unit,
    onPaymentSuccess: (orderId: String) -> Unit,
    orderRepository: com.kidsg.domain.repository.OrderRepository = remember { com.kidsg.data.repository.RepositoryProvider.orderRepository },
    cartRepository: com.kidsg.domain.repository.CartRepository = remember { com.kidsg.data.repository.RepositoryProvider.cartRepository },
    modifier: Modifier = Modifier
) {
    var selectedMethod by remember { mutableStateOf("UPI") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

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
                text = "Payment Method",
                style = KidsGTypography.TitleLarge
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(KidsGSpacing.lg)
        ) {
            // Stepper: Step 2 Active
            CheckoutStepper(currentStep = 2)

            Spacer(modifier = Modifier.height(KidsGSpacing.xl))

            // Payment Option 1: UPI
            PaymentMethodCard(
                icon = "⚡",
                title = "UPI",
                subtitle = "Pay with any UPI app (GPay, PhonePe, Paytm)",
                isSelected = selectedMethod == "UPI",
                onSelect = { selectedMethod = "UPI" }
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // Payment Option 2: Card
            PaymentMethodCard(
                icon = "💳",
                title = "Card",
                subtitle = "Debit / Credit Card (Visa, Master, RuPay)",
                isSelected = selectedMethod == "Card",
                onSelect = { selectedMethod = "Card" }
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // Payment Option 3: Net Banking
            PaymentMethodCard(
                icon = "🏦",
                title = "Net Banking",
                subtitle = "SBI, HDFC, ICICI, Axis & more",
                isSelected = selectedMethod == "NetBanking",
                onSelect = { selectedMethod = "NetBanking" }
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // Payment Option 4: Wallets
            PaymentMethodCard(
                icon = "👛",
                title = "Wallets",
                subtitle = "PhonePe, Paytm, Amazon Pay",
                isSelected = selectedMethod == "Wallets",
                onSelect = { selectedMethod = "Wallets" }
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // Payment Option 5: Cash on Delivery
            PaymentMethodCard(
                icon = "💵",
                title = "Cash on Delivery",
                subtitle = "Pay with cash or QR at your doorstep",
                isSelected = selectedMethod == "COD",
                onSelect = { selectedMethod = "COD" }
            )
        }

        // Bottom CTA & Summary
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

            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(KidsGShapes.FullPill)
                        .background(KidsGColors.OrangePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = KidsGColors.White,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Securing Payment...",
                            style = KidsGTypography.TitleSmall.copy(color = KidsGColors.White)
                        )
                    }
                }
            } else {
                KidsGPrimaryButton(
                    text = "🔒 Pay ₹${totalAmount.toInt()}",
                    onClick = {
                        isProcessing = true
                        errorMessage = null
                        coroutineScope.launch {
                            val currentItems = cartRepository.cartState.value.items.ifEmpty {
                                SessionStorage.getSavedCartItems()
                            }
                            val addr = deliveryAddress
                                ?: SessionStorage.getSelectedAddress()
                                ?: Address(
                                    id = "addr_default",
                                    label = "Home",
                                    recipientName = "Student Desk",
                                    phoneNumber = "+91 98765 43210",
                                    addressLine1 = "KidsG Desk Delivery",
                                    addressLine2 = "",
                                    city = "Bengaluru",
                                    pincode = "560034",
                                    isDefault = true
                                )
                            val result = orderRepository.createOrder(
                                items = currentItems,
                                deliveryAddress = addr,
                                paymentMethod = selectedMethod
                            )
                            isProcessing = false
                            result.onSuccess { order ->
                                onPaymentSuccess(order.displayOrderId)
                            }.onFailure { err ->
                                errorMessage = err.message ?: "Failed to process payment and place order."
                            }
                        }
                    }
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(KidsGSpacing.sm))
                Text(
                    text = errorMessage ?: "",
                    style = KidsGTypography.BodySmall.copy(color = Color(0xFFDC2626)),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔒 100% Secure Payment Powered by KidsG",
                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary, fontSize = 11.sp)
                )
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    icon: String,
    title: String,
    subtitle: String,
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                    .background(if (isSelected) KidsGColors.AccentYellowLight else KidsGColors.SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(KidsGSpacing.md))

                Column {
                    Text(
                        text = title,
                        style = KidsGTypography.TitleSmall.copy(fontSize = 15.sp)
                    )
                    Text(
                        text = subtitle,
                        style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary)
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = KidsGColors.OrangePrimary,
                    unselectedColor = KidsGColors.BorderSubtle
                )
            )
        }
    }
}
