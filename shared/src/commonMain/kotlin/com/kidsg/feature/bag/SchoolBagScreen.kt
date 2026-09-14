package com.kidsg.feature.bag

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGEmptyState
import com.kidsg.core.designsystem.KidsGIcons
import com.kidsg.core.designsystem.KidsGIllustration
import com.kidsg.core.designsystem.KidsGPrimaryButton
import com.kidsg.core.designsystem.KidsGProductVisual
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.domain.model.CartItem
import com.kidsg.domain.repository.CartRepository
import com.kidsg.domain.repository.ConfigRepository

/**
 * Hero Experience 5: "YOUR SCHOOL BAG" Cart (Section 17)
 * Products visually packed into a student's school backpack.
 * Driven entirely by dynamic DeliveryConfig (Rule #5).
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
    var couponInput by remember { mutableStateOf("") }
    var couponMessage by remember { mutableStateOf<String?>(null) }

    if (cart.items.isEmpty()) {
        KidsGEmptyState(
            title = "Your School Bag is suspiciously empty",
            subtitle = "Pencils, notebooks and art supplies are waiting to jump in!",
            actionButtonText = "Pack My School Bag",
            onActionClick = onStartShopping,
            illustration = { KidsGIcons.SchoolBag(modifier = Modifier.size(72.dp), color = KidsGColors.OrangePrimary) },
            modifier = modifier.fillMaxSize().background(KidsGColors.Background)
        )
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        KidsGIllustration.DeskBackground {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // 1. School Bag Visual Header
                item {
                    SchoolBagHeader(
                        itemCount = cart.itemCount,
                        partnerStoreName = cart.partnerStore?.name ?: "Nearby Partner Store"
                    )
                }

                // 2. Free Delivery Goal Progress Bar
                item {
                    FreeDeliveryBanner(
                        subtotal = cart.subtotal,
                        threshold = cart.deliveryConfig.freeDeliveryThreshold
                    )
                }

                // 3. Packed Stationery Items in School Bag
                items(cart.items) { item ->
                    PackedItemCard(
                        item = item,
                        onIncrease = {
                            kotlinx.coroutines.runBlocking {
                                cartRepository.updateQuantity(item.product.id, item.quantity + 1)
                            }
                        },
                        onDecrease = {
                            kotlinx.coroutines.runBlocking {
                                cartRepository.updateQuantity(item.product.id, item.quantity - 1)
                            }
                        }
                    )
                }

                // 4. Coupon Section
                item {
                    CouponSection(
                        appliedCouponCode = cart.appliedCoupon?.code,
                        couponDiscount = cart.couponDiscount,
                        onApplyCoupon = { code ->
                            val res = kotlinx.coroutines.runBlocking { cartRepository.applyCoupon(code) }
                            couponMessage = if (res.isSuccess) "Coupon applied!" else res.exceptionOrNull()?.message
                        },
                        onRemoveCoupon = {
                            kotlinx.coroutines.runBlocking { cartRepository.removeCoupon() }
                            couponMessage = null
                        },
                        couponMessage = couponMessage
                    )
                }

                // 5. Bill Details (Strictly computed from domain models - Rule #5)
                item {
                    BillDetailsSection(cart = cart)
                }
            }
        }

        // Sticky Checkout Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
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
                Column {
                    Text(
                        text = "Total to Pay",
                        style = KidsGTypography.Caption
                    )
                    Text(
                        text = "₹${cart.finalTotal.toInt()}",
                        style = KidsGTypography.DisplayMedium.copy(fontSize = 22.sp, color = KidsGColors.BlackText)
                    )
                    if (cart.totalSavings > 0) {
                        Text(
                            text = "You save ₹${cart.totalSavings.toInt()}!",
                            style = KidsGTypography.Tag.copy(color = KidsGColors.Success)
                        )
                    }
                }

                KidsGPrimaryButton(
                    text = "Proceed to Checkout →",
                    onClick = onProceedToCheckout,
                    modifier = Modifier.width(210.dp)
                )
            }
        }
    }
}

@Composable
fun SchoolBagHeader(itemCount: Int, partnerStoreName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle)
            .padding(KidsGSpacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual School Bag with open zipper
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(KidsGShapes.Small)
                    .background(KidsGColors.OrangeLight),
                contentAlignment = Alignment.Center
            ) {
                KidsGIcons.SchoolBag(modifier = Modifier.size(32.dp), color = KidsGColors.OrangePrimary)
            }

            Spacer(modifier = Modifier.width(KidsGSpacing.md))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Your School Bag",
                        style = KidsGTypography.TitleLarge
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(KidsGShapes.FullPill)
                            .background(KidsGColors.OrangePrimary)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$itemCount ITEMS",
                            style = KidsGTypography.Caption.copy(
                                color = KidsGColors.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                Text(
                    text = "Preparing from $partnerStoreName",
                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary)
                )
            }
        }
    }
}

@Composable
fun FreeDeliveryBanner(subtotal: Double, threshold: Double) {
    val progress = (subtotal / threshold).coerceIn(0.0, 1.0).toFloat()
    val isFree = subtotal >= threshold

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.sm)
            .clip(KidsGShapes.Small)
            .background(if (isFree) KidsGColors.AccentMintLight else KidsGColors.AccentYellowLight)
            .border(1.dp, if (isFree) KidsGColors.AccentMint else KidsGColors.AccentYellow, KidsGShapes.Small)
            .padding(KidsGSpacing.md)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFree) "🎉 FREE delivery unlocked for your bag!" else "Add ₹${(threshold - subtotal).toInt()} more for FREE Delivery",
                    style = KidsGTypography.TitleSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (isFree) "Free" else "₹30",
                    style = KidsGTypography.Tag.copy(color = if (isFree) Color(0xFF065F46) else KidsGColors.TextPrimary)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(KidsGShapes.FullPill),
                color = if (isFree) KidsGColors.Success else KidsGColors.OrangePrimary,
                trackColor = KidsGColors.White,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun PackedItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg, vertical = 4.dp)
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.Medium)
            .padding(KidsGSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Packed stationery item visual
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(KidsGShapes.Small)
                    .background(KidsGColors.SurfaceDesk),
                contentAlignment = Alignment.Center
            ) {
                KidsGProductVisual(product = item.product, modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.width(KidsGSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = KidsGTypography.TitleSmall.copy(fontSize = 13.sp),
                    maxLines = 2
                )
                if (item.selectedVariant != null) {
                    Text(
                        text = "Type: ${item.selectedVariant}",
                        style = KidsGTypography.Caption
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${item.totalItemPrice.toInt()}",
                        style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    if (item.product.mrp > item.product.price) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${item.totalItemMrp.toInt()}",
                            style = KidsGTypography.Caption.copy(textDecoration = TextDecoration.LineThrough)
                        )
                    }
                }
            }

            // Quantity stepper
            Row(
                modifier = Modifier
                    .clip(KidsGShapes.Small)
                    .background(KidsGColors.OrangeLight)
                    .border(1.dp, KidsGColors.OrangeBorder, KidsGShapes.Small)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier.size(22.dp).clip(KidsGShapes.FullPill).clickable(onClick = onDecrease),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "−", style = KidsGTypography.TitleSmall.copy(color = KidsGColors.OrangeDark))
                }

                Text(
                    text = "${item.quantity}",
                    style = KidsGTypography.TitleSmall.copy(color = KidsGColors.OrangeDark, fontSize = 13.sp)
                )

                Box(
                    modifier = Modifier.size(22.dp).clip(KidsGShapes.FullPill).clickable(onClick = onIncrease),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+", style = KidsGTypography.TitleSmall.copy(color = KidsGColors.OrangeDark))
                }
            }
        }
    }
}

@Composable
fun CouponSection(
    appliedCouponCode: String?,
    couponDiscount: Double,
    onApplyCoupon: (String) -> Unit,
    onRemoveCoupon: () -> Unit,
    couponMessage: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.sm)
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.Medium)
            .padding(KidsGSpacing.md)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏷️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (appliedCouponCode != null) "Applied: $appliedCouponCode" else "Apply Stationery Coupon",
                        style = KidsGTypography.TitleSmall.copy(fontSize = 13.sp)
                    )
                }

                if (appliedCouponCode != null) {
                    Text(
                        text = "Remove",
                        style = KidsGTypography.Tag.copy(color = KidsGColors.Error),
                        modifier = Modifier.clickable(onClick = onRemoveCoupon)
                    )
                }
            }

            if (appliedCouponCode == null) {
                Spacer(modifier = Modifier.height(KidsGSpacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.sm)
                ) {
                    // Quick apply chip for KIDSG50
                    Box(
                        modifier = Modifier
                            .clip(KidsGShapes.Small)
                            .background(KidsGColors.OrangeLight)
                            .border(1.dp, KidsGColors.OrangeBorder, KidsGShapes.Small)
                            .clickable { onApplyCoupon("KIDSG50") }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "KIDSG50 (50% OFF)",
                            style = KidsGTypography.Tag.copy(color = KidsGColors.OrangeDark)
                        )
                    }

                    // Quick apply chip for EXAMREADY
                    Box(
                        modifier = Modifier
                            .clip(KidsGShapes.Small)
                            .background(KidsGColors.AccentYellowLight)
                            .border(1.dp, KidsGColors.AccentYellow, KidsGShapes.Small)
                            .clickable { onApplyCoupon("EXAMREADY") }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "EXAMREADY",
                            style = KidsGTypography.Tag.copy(color = KidsGColors.BlackText)
                        )
                    }
                }
            }

            if (couponMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = couponMessage,
                    style = KidsGTypography.Caption.copy(
                        color = if (appliedCouponCode != null) KidsGColors.Success else KidsGColors.Error
                    )
                )
            }
        }
    }
}

@Composable
fun BillDetailsSection(cart: com.kidsg.domain.model.Cart) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.sm)
            .clip(KidsGShapes.DeskCard)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.DeskCard)
            .padding(KidsGSpacing.lg)
    ) {
        Text(text = "Bill Breakdown", style = KidsGTypography.TitleSmall)
        Spacer(modifier = Modifier.height(KidsGSpacing.sm))

        BillRow(label = "Items Total (${cart.itemCount})", value = "₹${cart.subtotal.toInt()}")

        if (cart.productDiscount > 0) {
            BillRow(label = "Product Discount", value = "−₹${cart.productDiscount.toInt()}", valueColor = KidsGColors.Success)
        }

        if (cart.couponDiscount > 0) {
            BillRow(label = "Coupon Discount", value = "−₹${cart.couponDiscount.toInt()}", valueColor = KidsGColors.Success)
        }

        BillRow(
            label = "Delivery Partner Fee",
            value = if (cart.isFreeDeliveryEligible) "FREE" else "₹${cart.deliveryFee.toInt()}",
            valueColor = if (cart.isFreeDeliveryEligible) KidsGColors.Success else KidsGColors.TextPrimary
        )

        BillRow(label = "Platform & Handling Fee", value = "₹${cart.platformFee.toInt()}")
        BillRow(label = "Govt. Taxes (GST 5%)", value = "₹${cart.taxAmount.toInt()}")

        HorizontalDivider(modifier = Modifier.padding(vertical = KidsGSpacing.sm), color = KidsGColors.BorderSubtle)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "To Pay", style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))
            Text(text = "₹${cart.finalTotal.toInt()}", style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun BillRow(label: String, value: String, valueColor: Color = KidsGColors.TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = KidsGTypography.BodyMedium.copy(fontSize = 13.sp, color = KidsGColors.TextSecondary))
        Text(text = value, style = KidsGTypography.BodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor))
    }
}
