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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGProductCard
import com.kidsg.core.designsystem.KidsGResourceImage
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.data.mock.KidsGMockData
import com.kidsg.domain.model.IntentModeType
import com.kidsg.domain.model.OopsEmergencyItem
import com.kidsg.domain.model.Product
import com.kidsg.domain.repository.CartRepository
import com.kidsg.domain.repository.ProductRepository

data class DiscoveryCategoryCard(
    val id: String,
    val resName: String,
    val title: String,
    val itemCount: String
)

data class DiscoveryFilterTab(
    val id: String?,
    val label: String
)

/**
 * Stationery Discovery & Categories Screen
 * Pixel-perfect adaptation of the user's designed categories page with:
 * - Top Hero Banner ("Stationery Discovery" + artwork + back button)
 * - Rounded Search Bar with scanner icon
 * - Horizontal Filter Pills ("All", "Notebooks", "Pens & Pencils", "Art & Craft", "Bags", etc.)
 * - "Categories" section with "Find what you love ♡"
 * - 2-Column 8-Card Category Grid with exact illustration cards
 * - Filtered supplies list with product cards & add-to-bag controls
 */
@Composable
fun DiscoveryScreen(
    productRepository: ProductRepository,
    cartRepository: CartRepository,
    initialMode: IntentModeType? = null,
    initialCategoryId: String? = null,
    onProductClick: (Product) -> Unit,
    onBackToHome: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onIncreaseQuantity: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(initialMode) }
    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }
    var searchQuery by remember { mutableStateOf("") }
    val cart by cartRepository.cartState.collectAsState()

    val oopsItems = remember { KidsGMockData.oopsItems }
    val allProducts = remember { KidsGMockData.products }

    val filterTabs = remember {
        listOf(
            DiscoveryFilterTab(null, "All"),
            DiscoveryFilterTab("notebooks", "Notebooks"),
            DiscoveryFilterTab("pens_pencils", "Pens & Pencils"),
            DiscoveryFilterTab("art_craft", "Art & Craft"),
            DiscoveryFilterTab("school_bags", "Bags"),
            DiscoveryFilterTab("geometry", "Geometry"),
            DiscoveryFilterTab("lunch_boxes", "Bottles & Lunch"),
            DiscoveryFilterTab("stationery_sets", "Sets")
        )
    }

    val visualCategoryCards = remember {
        listOf(
            DiscoveryCategoryCard("notebooks", "cat_card_notebooks", "Notebooks", "42+ items"),
            DiscoveryCategoryCard("pens_pencils", "cat_card_pens", "Pens & Pencils", "58+ items"),
            DiscoveryCategoryCard("art_craft", "cat_card_art", "Art & Craft", "35+ items"),
            DiscoveryCategoryCard("school_bags", "cat_card_bags", "School Bags", "28+ items"),
            DiscoveryCategoryCard("geometry", "cat_card_geometry", "Geometry", "19+ items"),
            DiscoveryCategoryCard("lunch_boxes", "cat_card_bottles", "Water Bottles", "24+ items"),
            DiscoveryCategoryCard("lunch_boxes", "cat_card_lunch", "Lunch Boxes", "18+ items"),
            DiscoveryCategoryCard("stationery_sets", "cat_card_sets", "Stationery Sets", "31+ items")
        )
    }

    val displayedProducts = remember(selectedMode, selectedCategoryId, searchQuery) {
        allProducts.filter { product ->
            val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
            val matchesMode = selectedMode == null || product.intentModes.contains(selectedMode!!.name)
            val matchesSearch = searchQuery.isBlank() ||
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.brand.contains(searchQuery, ignoreCase = true) ||
                product.tags.any { it.contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesMode && matchesSearch
        }
    }

    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFCFDFE))
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. TOP HEADER BANNER (Artwork + "Stationery Discovery" + embedded back button)
            item(key = "header_banner", contentType = "header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFFFFFBF5))
                ) {
                    KidsGResourceImage(
                        resName = "cat_header_banner",
                        contentDescription = "Stationery Discovery",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillWidth
                    )

                    // Clickable Back Button over top-left back arrow
                    Box(
                        modifier = Modifier
                            .padding(start = 14.dp, top = 14.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .shadow(2.dp, CircleShape)
                            .clickable(onClick = onBackToHome),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "←",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            // 2. SEARCH BAR WITH SCANNER ICON (Matching Design)
            item(key = "search_bar", contentType = "search") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .shadow(4.dp, RoundedCornerShape(26.dp), spotColor = Color(0x18000000))
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(26.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "🔍", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                textStyle = KidsGTypography.BodyMedium.copy(
                                    color = Color(0xFF0F172A),
                                    fontSize = 14.sp
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search pens, notebooks, geometry...",
                                            style = KidsGTypography.BodyMedium.copy(
                                                color = Color(0xFF94A3B8),
                                                fontSize = 13.sp
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        // Barcode / Scanner Icon on the right
                        Text(
                            text = "⛶",
                            fontSize = 20.sp,
                            color = Color(0xFFEA580C),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. HORIZONTAL FILTER PILLS ("All", "Notebooks", "Pens & Pencils", ...)
            item(key = "filter_chips", contentType = "filter") {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterTabs, key = { it.id ?: "all" }) { tab ->
                        val isSelected = selectedCategoryId == tab.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFFFF6D00) else Color.White)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFFF6D00) else Color(0xFFE2E8F0),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedCategoryId = tab.id }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tab.label,
                                color = if (isSelected) Color.White else Color(0xFF334155),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Dropdown circle arrow button
                    item(key = "dropdown_filter") {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                                .clickable { selectedCategoryId = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "▾", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }

            // 4. OOPS EMERGENCY DRAWER (if in Oops Mode)
            if (selectedMode == IntentModeType.OOPS) {
                item(key = "oops_drawer", contentType = "oops") {
                    OopsEmergencyDrawer(
                        items = oopsItems,
                        onItemClick = { item -> searchQuery = item.targetQuery }
                    )
                }
            }

            // 5. CATEGORIES SECTION HEADER ("Categories" + "Find what you love ♡")
            item(key = "categories_header", contentType = "section_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        style = KidsGTypography.DisplaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = Color(0xFF0F172A)
                        )
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Find what you love  ♡",
                            style = KidsGTypography.Caption.copy(
                                color = Color(0xFFEA580C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // 6. 2-COLUMN GRID OF THE 8 CATEGORY CARDS
            val chunkedCards = visualCategoryCards.chunked(2)
            chunkedCards.forEachIndexed { index, rowCards ->
                item(key = "category_row_$index", contentType = "cat_row") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (card in rowCards) {
                            val isSelected = selectedCategoryId == card.id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(118.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x18000000))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) Color(0xFFFF6D00) else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable {
                                        selectedCategoryId = if (selectedCategoryId == card.id) null else card.id
                                    }
                            ) {
                                KidsGResourceImage(
                                    resName = card.resName,
                                    contentDescription = card.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                        }
                        if (rowCards.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // 7. ACTIVE FILTER / PRODUCTS SECTION
            if (selectedCategoryId != null || searchQuery.isNotBlank()) {
                item(key = "products_header", contentType = "prod_header") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${displayedProducts.size} supplies found",
                            style = KidsGTypography.BodySmall.copy(
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Clear filter ✕",
                            style = KidsGTypography.Caption.copy(
                                color = Color(0xFFEA580C),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable {
                                selectedCategoryId = null
                                searchQuery = ""
                            }
                        )
                    }
                }
            }

            // 8. PRODUCT CARDS (in 2-column rows for matching products)
            val chunkedProducts = displayedProducts.chunked(2)
            chunkedProducts.forEachIndexed { prodRowIndex, prodRow ->
                item(key = "prod_row_$prodRowIndex", contentType = "product_row") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (product in prodRow) {
                            val itemInCart = cart.items.find { it.product.id == product.id }
                            val quantity = itemInCart?.quantity ?: 0

                            Box(modifier = Modifier.weight(1f)) {
                                KidsGProductCard(
                                    product = product,
                                    cartQuantity = quantity,
                                    onProductClick = onProductClick,
                                    onAddToCart = onAddToCart,
                                    onIncreaseQuantity = onIncreaseQuantity,
                                    onDecreaseQuantity = onDecreaseQuantity
                                )
                            }
                        }
                        if (prodRow.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
