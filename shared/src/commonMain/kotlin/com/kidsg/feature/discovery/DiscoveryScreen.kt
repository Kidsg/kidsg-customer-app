package com.kidsg.feature.discovery

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGCategoryCard
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGIcons
import com.kidsg.core.designsystem.KidsGIllustration
import com.kidsg.core.designsystem.KidsGProductCard
import com.kidsg.core.designsystem.KidsGSearchBar
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.domain.model.Category
import com.kidsg.domain.model.IntentModeInfo
import com.kidsg.domain.model.IntentModeType
import com.kidsg.domain.model.OopsEmergencyItem
import com.kidsg.domain.model.Product
import com.kidsg.domain.repository.CartRepository
import com.kidsg.domain.repository.ProductRepository

/**
 * Hero Experience 3: Discovery & Intent Modes (The Stationery Wall & Oops Flow)
 * (Sections 12, 13, 14)
 */
@Composable
fun DiscoveryScreen(
    productRepository: ProductRepository,
    cartRepository: CartRepository,
    initialMode: IntentModeType? = null,
    onProductClick: (Product) -> Unit,
    onBackToHome: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onIncreaseQuantity: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(initialMode) }
    var searchQuery by remember { mutableStateOf("") }
    val cart by cartRepository.cartState.collectAsState()

    val categories = remember { kotlinx.coroutines.runBlocking { productRepository.getCategories() } }
    val intentModes = remember { kotlinx.coroutines.runBlocking { productRepository.getIntentModes() } }
    val oopsItems = remember { kotlinx.coroutines.runBlocking { productRepository.getOopsEmergencyItems() } }
    val allProducts = remember { kotlinx.coroutines.runBlocking { productRepository.getPopularProducts() } }

    val displayedProducts = remember(selectedMode, searchQuery) {
        when {
            searchQuery.isNotBlank() -> allProducts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.brand.contains(searchQuery, ignoreCase = true)
            }
            selectedMode != null -> allProducts.filter { it.intentModes.contains(selectedMode!!.name) }
            else -> allProducts
        }
    }

    KidsGIllustration.DeskBackground(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.md),
            verticalArrangement = Arrangement.spacedBy(KidsGSpacing.md)
        ) {
            // 1. Search Bar & Title
            item(span = { GridItemSpan(2) }) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(KidsGShapes.FullPill)
                                .background(KidsGColors.White)
                                .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.FullPill)
                                .clickable(onClick = onBackToHome),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "←", style = KidsGTypography.TitleSmall)
                        }

                        Spacer(modifier = Modifier.width(KidsGSpacing.md))

                        Text(
                            text = "Stationery Discovery",
                            style = KidsGTypography.TitleLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(KidsGSpacing.md))

                    KidsGSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search pens, notebooks, geometry..."
                    )
                }
            }

            // 2. Intent Modes Quick Filter Tabs
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.sm)
                ) {
                    ModeFilterChip(
                        title = "All Desk",
                        isSelected = selectedMode == null,
                        onClick = { selectedMode = null }
                    )

                    intentModes.forEach { mode ->
                        ModeFilterChip(
                            title = mode.title,
                            isSelected = selectedMode == mode.type,
                            onClick = { selectedMode = mode.type }
                        )
                    }
                }
            }

            // 3. If "Oops Mode" is active, show the OOPS Emergency Drawer!
            if (selectedMode == IntentModeType.OOPS) {
                item(span = { GridItemSpan(2) }) {
                    OopsEmergencyDrawer(
                        items = oopsItems,
                        onItemClick = { item -> searchQuery = item.targetQuery }
                    )
                }
            }

            // 4. Stationery Wall (Categories) when no mode or search filter
            if (selectedMode == null && searchQuery.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "The Stationery Wall",
                        style = KidsGTypography.TitleMedium.copy(fontSize = 18.sp)
                    )
                }

                items(categories) { cat ->
                    KidsGCategoryCard(
                        category = cat,
                        onClick = { category -> searchQuery = category.name }
                    )
                }

                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(KidsGSpacing.lg))
                    Text(
                        text = "Curated For Your Desk",
                        style = KidsGTypography.TitleMedium.copy(fontSize = 18.sp)
                    )
                }
            }

            // 5. Products Grid
            items(displayedProducts) { product ->
                val itemInCart = cart.items.find { it.product.id == product.id }
                val quantity = itemInCart?.quantity ?: 0

                KidsGProductCard(
                    product = product,
                    cartQuantity = quantity,
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart,
                    onIncreaseQuantity = onIncreaseQuantity,
                    onDecreaseQuantity = onDecreaseQuantity
                )
            }

            // Bottom Spacing for navigation bar
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ModeFilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(KidsGShapes.FullPill)
            .background(if (isSelected) KidsGColors.OrangePrimary else KidsGColors.White)
            .border(1.dp, if (isSelected) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle, KidsGShapes.FullPill)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = KidsGTypography.Caption.copy(
                color = if (isSelected) KidsGColors.White else KidsGColors.TextPrimary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
fun OopsEmergencyDrawer(
    items: List<OopsEmergencyItem>,
    onItemClick: (OopsEmergencyItem) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.AccentPinkLight)
            .border(1.5.dp, KidsGColors.AccentPink, KidsGShapes.Medium)
            .padding(KidsGSpacing.md)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🚨", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(KidsGSpacing.sm))
                Column {
                    Text(
                        text = "Oops! Pick what you forgot for tomorrow:",
                        style = KidsGTypography.TitleSmall.copy(color = Color(0xFF9D174D), fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Packed and dispatched in 12-15 minutes.",
                        style = KidsGTypography.BodySmall.copy(color = Color(0xFF831843))
                    )
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.sm))

            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(KidsGShapes.Small)
                        .background(KidsGColors.White)
                        .clickable { onItemClick(item) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = item.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(KidsGSpacing.sm))
                        Column {
                            Text(text = item.title, style = KidsGTypography.TitleSmall.copy(fontSize = 12.sp))
                            Text(text = item.subtitle, style = KidsGTypography.Caption)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(KidsGShapes.FullPill)
                            .background(KidsGColors.OrangeLight)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${item.estimatedMinutes}m",
                            style = KidsGTypography.Tag.copy(color = KidsGColors.OrangeDark)
                        )
                    }
                }
            }
        }
    }
}
