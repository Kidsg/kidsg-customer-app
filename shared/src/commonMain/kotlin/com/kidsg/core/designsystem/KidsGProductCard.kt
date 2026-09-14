package com.kidsg.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.domain.model.Product

/**
 * KidsG Stationery Product Card
 * Tactile stationery composition with brand tag, MRP strike-through,
 * discount pill, and interactive Add / Quantity stepper.
 */
@Composable
fun KidsGProductCard(
    product: Product,
    cartQuantity: Int,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onIncreaseQuantity: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    modifier: Modifier = Modifier,
    isWishlisted: Boolean = false,
    onToggleWishlist: (Product) -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(KidsGShapes.DeskCard)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.DeskCard)
            .clickable { onProductClick(product) }
            .padding(KidsGSpacing.md)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Row: Brand & Wishlist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.brand.uppercase(),
                    style = KidsGTypography.Tag,
                    color = KidsGColors.OrangePrimary
                )

                // Heart Wishlist Icon
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(KidsGShapes.FullPill)
                        .background(if (isWishlisted) KidsGColors.AccentPinkLight else KidsGColors.SurfaceDesk)
                        .clickable { onToggleWishlist(product) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isWishlisted) "♥" else "♡",
                        color = if (isWishlisted) KidsGColors.AccentPink else KidsGColors.TextMuted,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.xs))

            // Product Visual Container (Stationery Desk placement)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(KidsGShapes.Small)
                    .background(KidsGColors.SurfaceDesk),
                contentAlignment = Alignment.Center
            ) {
                // Stationery product illustration
                KidsGProductVisual(product = product)

                // Best Seller or Discount Badge
                if (product.discountPercentage > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .clip(KidsGShapes.ExtraSmall)
                            .background(KidsGColors.AccentMint)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${product.discountPercentage}% OFF",
                            style = KidsGTypography.Caption.copy(
                                fontSize = 9.sp,
                                color = Color(0xFF065F46)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.sm))

            // Product Name
            Text(
                text = product.name,
                style = KidsGTypography.TitleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.xs))

            // Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                KidsGIcons.Star(modifier = Modifier.size(12.dp), color = KidsGColors.AccentYellow)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${product.rating}",
                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextPrimary)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "(${product.reviewCount})",
                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted)
                )
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.sm))

            // Price & Add / Quantity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price Column
                Column {
                    Text(
                        text = "₹${product.price.toInt()}",
                        style = KidsGTypography.TitleMedium.copy(color = KidsGColors.BlackText)
                    )
                    if (product.mrp > product.price) {
                        Text(
                            text = "₹${product.mrp.toInt()}",
                            style = KidsGTypography.Caption.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = KidsGColors.TextMuted
                            )
                        )
                    }
                }

                // Add or Quantity Stepper
                AnimatedContent(targetState = cartQuantity > 0, label = "cartStepper") { hasItem ->
                    if (!hasItem) {
                        // KidsG Add Button
                        Box(
                            modifier = Modifier
                                .clip(KidsGShapes.Small)
                                .background(KidsGColors.OrangePrimary)
                                .clickable { onAddToCart(product) }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ADD +",
                                style = KidsGTypography.ButtonText.copy(fontSize = 12.sp)
                            )
                        }
                    } else {
                        // Interactive Stepper
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
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(KidsGShapes.FullPill)
                                    .clickable { onDecreaseQuantity(product) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "−", style = KidsGTypography.TitleSmall.copy(color = KidsGColors.OrangeDark))
                            }

                            Text(
                                text = "$cartQuantity",
                                style = KidsGTypography.TitleSmall.copy(color = KidsGColors.OrangeDark, fontSize = 13.sp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(KidsGShapes.FullPill)
                                    .clickable { onIncreaseQuantity(product) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "+", style = KidsGTypography.TitleSmall.copy(color = KidsGColors.OrangeDark))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Procedural Stationery Visual for products based on category/name
 */
@Composable
fun KidsGProductVisual(product: Product, modifier: Modifier = Modifier.size(60.dp)) {
    when {
        product.name.contains("Notebook", ignoreCase = true) || product.categoryId == "notebooks" -> {
            KidsGIcons.Notebook(modifier = modifier, color = KidsGColors.OrangePrimary)
        }
        product.name.contains("Pen", ignoreCase = true) || product.name.contains("Pencil", ignoreCase = true) || product.categoryId == "pens_pencils" -> {
            KidsGIcons.Pencil(modifier = modifier, color = KidsGColors.OrangePrimary)
        }
        product.name.contains("Color", ignoreCase = true) || product.name.contains("Paint", ignoreCase = true) || product.categoryId == "art_craft" -> {
            KidsGIcons.ArtPalette(modifier = modifier, color = KidsGColors.AccentPurple)
        }
        product.name.contains("Geometry", ignoreCase = true) || product.name.contains("Compass", ignoreCase = true) || product.categoryId == "geometry" -> {
            KidsGIcons.Geometry(modifier = modifier, color = KidsGColors.AccentSkyBlue)
        }
        else -> {
            KidsGIcons.SchoolBag(modifier = modifier, color = KidsGColors.OrangePrimary)
        }
    }
}
