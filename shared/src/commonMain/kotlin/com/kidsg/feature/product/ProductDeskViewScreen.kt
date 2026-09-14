package com.kidsg.feature.product

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGIcons
import com.kidsg.core.designsystem.KidsGIllustration
import com.kidsg.core.designsystem.KidsGPrimaryButton
import com.kidsg.core.designsystem.KidsGProductVisual
import com.kidsg.core.designsystem.KidsGSecondaryButton
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.domain.model.Product
import com.kidsg.domain.repository.CartRepository

/**
 * Hero Experience 4: Product "DESK VIEW" (Section 16)
 * The stationery item visually sits on the student's creative desk with
 * realistic stationery composition, specifications table, variant chips, and quick buy.
 */
@Composable
fun ProductDeskViewScreen(
    product: Product,
    cartRepository: CartRepository,
    onBack: () -> Unit,
    onGoToBag: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedVariant by remember { mutableStateOf(product.variants.firstOrNull() ?: "Standard") }
    var isWishlisted by remember { mutableStateOf(false) }
    val cart by cartRepository.cartState.collectAsState()
    val itemInCart = cart.items.find { it.product.id == product.id }
    val cartQuantity = itemInCart?.quantity ?: 0

    Box(modifier = modifier.fillMaxSize()) {
        KidsGIllustration.DeskBackground {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // 1. Top Bar with back button and wishlist
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(KidsGShapes.FullPill)
                                .background(KidsGColors.White)
                                .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.FullPill)
                                .clickable(onClick = onBack),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "←", style = KidsGTypography.TitleMedium)
                        }

                        Text(
                            text = "Desk View",
                            style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(KidsGShapes.FullPill)
                                .background(if (isWishlisted) KidsGColors.AccentPinkLight else KidsGColors.White)
                                .border(1.dp, if (isWishlisted) KidsGColors.AccentPink else KidsGColors.BorderSubtle, KidsGShapes.FullPill)
                            .clickable { isWishlisted = !isWishlisted },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isWishlisted) "♥" else "♡",
                                color = if (isWishlisted) KidsGColors.AccentPink else KidsGColors.TextSecondary,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                // 2. Desk Hero View (Product sitting on the desk)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = KidsGSpacing.lg)
                            .height(260.dp)
                            .clip(KidsGShapes.DeskCard)
                            .background(KidsGColors.White)
                            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.DeskCard),
                        contentAlignment = Alignment.Center
                    ) {
                        // Shadow under product
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 30.dp)
                                .size(140.dp, 16.dp)
                                .clip(KidsGShapes.FullPill)
                                .background(KidsGColors.ShadowElevated)
                        )

                        // Central stationery visual
                        KidsGProductVisual(product = product, modifier = Modifier.size(130.dp))

                        // Express Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(KidsGSpacing.md)
                                .clip(KidsGShapes.Small)
                                .background(KidsGColors.OrangeLight)
                                .border(1.dp, KidsGColors.OrangeBorder, KidsGShapes.Small)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⚡ 12-18 MINS", style = KidsGTypography.Tag.copy(color = KidsGColors.OrangeDark))
                            }
                        }

                        // Best Seller Badge
                        if (product.isBestSeller) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(KidsGSpacing.md)
                                    .clip(KidsGShapes.Small)
                                    .background(KidsGColors.AccentYellow)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "★ BESTSELLER", style = KidsGTypography.Tag.copy(color = KidsGColors.BlackText))
                            }
                        }
                    }
                }

                // 3. Product Info Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md)
                            .clip(KidsGShapes.DeskCard)
                            .background(KidsGColors.White)
                            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.DeskCard)
                            .padding(KidsGSpacing.lg)
                    ) {
                        Text(
                            text = product.brand.uppercase(),
                            style = KidsGTypography.Tag,
                            color = KidsGColors.OrangePrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = product.name,
                            style = KidsGTypography.TitleLarge
                        )

                        Spacer(modifier = Modifier.height(KidsGSpacing.sm))

                        // Rating & Reviews
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(KidsGShapes.ExtraSmall)
                                    .background(KidsGColors.AccentMintLight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    KidsGIcons.Star(modifier = Modifier.size(12.dp), color = Color(0xFF065F46))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${product.rating}",
                                        style = KidsGTypography.Caption.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF065F46)
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(KidsGSpacing.sm))
                            Text(
                                text = "${product.reviewCount} verified school ratings",
                                style = KidsGTypography.Caption
                            )
                        }

                        Spacer(modifier = Modifier.height(KidsGSpacing.md))

                        // Pricing Row
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₹${product.price.toInt()}",
                                style = KidsGTypography.DisplayMedium.copy(color = KidsGColors.BlackText)
                            )
                            if (product.mrp > product.price) {
                                Spacer(modifier = Modifier.width(KidsGSpacing.sm))
                                Text(
                                    text = "₹${product.mrp.toInt()}",
                                    style = KidsGTypography.TitleMedium.copy(
                                        textDecoration = TextDecoration.LineThrough,
                                        color = KidsGColors.TextMuted
                                    )
                                )
                                Spacer(modifier = Modifier.width(KidsGSpacing.sm))
                                Box(
                                    modifier = Modifier
                                        .clip(KidsGShapes.ExtraSmall)
                                        .background(KidsGColors.AccentMint)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${product.discountPercentage}% OFF",
                                        style = KidsGTypography.Caption.copy(
                                            color = Color(0xFF065F46),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        // Variants (Ruling / Tip sizes)
                        if (product.variants.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(KidsGSpacing.md))
                            Text(text = "Ruling / Type:", style = KidsGTypography.TitleSmall.copy(fontSize = 13.sp))
                            Spacer(modifier = Modifier.height(KidsGSpacing.xs))
                            Row(horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.sm)) {
                                product.variants.forEach { variant ->
                                    val isSelected = variant == selectedVariant
                                    Box(
                                        modifier = Modifier
                                            .clip(KidsGShapes.Small)
                                            .background(if (isSelected) KidsGColors.OrangePrimary else KidsGColors.SurfaceDesk)
                                            .clickable { selectedVariant = variant }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = variant,
                                            style = KidsGTypography.Caption.copy(
                                                color = if (isSelected) KidsGColors.White else KidsGColors.TextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(KidsGSpacing.lg))

                        // Description
                        Text(text = "Product Details", style = KidsGTypography.TitleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.description,
                            style = KidsGTypography.BodyMedium.copy(lineHeight = 22.sp)
                        )

                        // Specifications table
                        if (product.specifications.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(KidsGSpacing.md))
                            Text(text = "Specifications", style = KidsGTypography.TitleSmall)
                            Spacer(modifier = Modifier.height(KidsGSpacing.xs))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(KidsGShapes.Small)
                                    .background(KidsGColors.SurfaceDesk)
                                    .padding(KidsGSpacing.sm)
                            ) {
                                product.specifications.forEach { (key, value) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = key, style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary))
                                        Text(text = value, style = KidsGTypography.Caption.copy(fontWeight = FontWeight.Bold, color = KidsGColors.TextPrimary))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sticky Bottom CTA Bar
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
                horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add to Bag Button
                KidsGSecondaryButton(
                    text = if (cartQuantity > 0) "In Bag ($cartQuantity)" else "Add to Bag",
                    onClick = {
                        kotlinx.coroutines.runBlocking {
                            cartRepository.addToCart(product, 1, selectedVariant)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Buy Now Button
                KidsGPrimaryButton(
                    text = "Buy Now",
                    onClick = {
                        kotlinx.coroutines.runBlocking {
                            cartRepository.addToCart(product, 1, selectedVariant)
                        }
                        onGoToBag()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
