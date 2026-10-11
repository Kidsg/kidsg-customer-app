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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGIcons
import com.kidsg.core.designsystem.KidsGProductCard
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.core.storage.SessionStorage
import com.kidsg.data.mock.KidsGCatalogue
import com.kidsg.domain.model.Product
import com.kidsg.domain.model.Subcategory
import com.kidsg.domain.repository.CartRepository
import com.kidsg.domain.repository.ProductRepository
import kotlinx.coroutines.launch

enum class ProductSortOption(val displayName: String) {
    RECOMMENDED("Recommended"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    BESTSELLER("Bestselling"),
    HIGHEST_RATED("Highest Rated"),
    NEW_ARRIVALS("New Arrivals")
}

data class FilterState(
    val sortOption: ProductSortOption = ProductSortOption.RECOMMENDED,
    val selectedSizes: Set<String> = emptySet(),
    val selectedPageRanges: Set<String> = emptySet(),
    val selectedRulings: Set<String> = emptySet(),
    val selectedBrands: Set<String> = emptySet(),
    val inStockOnly: Boolean = false
) {
    val activeCount: Int
        get() = (if (sortOption != ProductSortOption.RECOMMENDED) 1 else 0) +
                selectedSizes.size +
                selectedPageRanges.size +
                selectedRulings.size +
                selectedBrands.size +
                (if (inStockOnly) 1 else 0)
}

/**
 * Screens 6 & 7: Product Listing Screen with Responsive Two-Column Grid and Filter & Sort Modal
 */
@Composable
fun ProductListingScreen(
    categoryId: String,
    initialSubcategoryId: String? = null,
    initialQuery: String = "",
    productRepository: ProductRepository,
    cartRepository: CartRepository,
    onProductClick: (Product) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val cart by cartRepository.cartState.collectAsState()

    val currentCategory = remember(categoryId) {
        KidsGCatalogue.categories.find { it.id == categoryId || it.slug == categoryId }
            ?: KidsGCatalogue.categories.first()
    }

    val subcategories = remember(currentCategory) {
        KidsGCatalogue.subcategories.filter { it.categoryId == currentCategory.id }
    }

    var selectedSubcategoryId by remember {
        mutableStateOf(initialSubcategoryId ?: subcategories.firstOrNull()?.id)
    }

    var searchQuery by remember { mutableStateOf(initialQuery) }
    var filterState by remember { mutableStateOf(FilterState()) }
    var showFilterSheet by remember { mutableStateOf(false) }

    // Wishlist local state backed by SessionStorage
    var wishlistIds by remember {
        mutableStateOf(SessionStorage.getWishlistProductIds())
    }

    // Category / Subcategory Products
    val baseProducts = remember(currentCategory, selectedSubcategoryId) {
        KidsGCatalogue.products.filter { p ->
            val matchCategory = p.categoryId == currentCategory.id || p.categoryId == currentCategory.slug
            val matchSubcategory = selectedSubcategoryId == null || p.subcategoryId == selectedSubcategoryId
            matchCategory && (selectedSubcategoryId == null || matchSubcategory)
        }.ifEmpty {
            // Fallback to all products matching subcategory
            KidsGCatalogue.products.filter { it.subcategoryId == selectedSubcategoryId }
        }
    }

    // Filtered and Sorted Products
    val displayedProducts = remember(baseProducts, searchQuery, filterState) {
        var list = baseProducts

        // Search Filter
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                        it.brand.lowercase().contains(q) ||
                        it.sku.lowercase().contains(q) ||
                        it.description.lowercase().contains(q)
            }
        }

        // Brand Filter
        if (filterState.selectedBrands.isNotEmpty()) {
            list = list.filter { filterState.selectedBrands.contains(it.brand) }
        }

        // Ruling Filter
        if (filterState.selectedRulings.isNotEmpty()) {
            list = list.filter { p ->
                val ruling = p.specifications["Ruling Type"] ?: p.specifications["Ruling"] ?: ""
                filterState.selectedRulings.any { r ->
                    ruling.contains(r, ignoreCase = true) || p.variants.any { v -> v.contains(r, ignoreCase = true) }
                }
            }
        }

        // Size Filter
        if (filterState.selectedSizes.isNotEmpty()) {
            list = list.filter { p ->
                val size = p.specifications["Size"] ?: ""
                filterState.selectedSizes.any { s -> size.contains(s, ignoreCase = true) || p.name.contains(s, ignoreCase = true) }
            }
        }

        // In Stock Filter
        if (filterState.inStockOnly) {
            list = list.filter { it.isAvailable && it.stockQuantity > 0 }
        }

        // Sorting
        when (filterState.sortOption) {
            ProductSortOption.RECOMMENDED -> list
            ProductSortOption.PRICE_LOW_HIGH -> list.sortedBy { it.price }
            ProductSortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
            ProductSortOption.BESTSELLER -> list.sortedByDescending { if (it.isBestSeller) 1 else 0 }
            ProductSortOption.HIGHEST_RATED -> list.sortedByDescending { it.rating }
            ProductSortOption.NEW_ARRIVALS -> list.reversed()
        }
    }

    val selectedSubcatName = remember(selectedSubcategoryId) {
        subcategories.find { it.id == selectedSubcategoryId }?.name ?: currentCategory.name
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KidsGColors.BackgroundPrimary)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Breadcrumb Top Bar
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
                        .clip(KidsGShapes.FullPill)
                        .background(KidsGColors.SurfaceDesk)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "←", style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Categories → ${currentCategory.name}",
                        style = KidsGTypography.Tag.copy(color = KidsGColors.TextMuted, fontSize = 11.sp)
                    )
                    Text(
                        text = selectedSubcatName,
                        style = KidsGTypography.TitleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = KidsGColors.BlackText
                        )
                    )
                }
            }

            // 2. Subcategory Pills Strip
            if (subcategories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KidsGColors.White)
                        .padding(bottom = KidsGSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = KidsGSpacing.lg)
                ) {
                    items(subcategories) { subcat ->
                        val isSelected = subcat.id == selectedSubcategoryId
                        Box(
                            modifier = Modifier
                                .clip(KidsGShapes.FullPill)
                                .background(if (isSelected) KidsGColors.OrangePrimary else KidsGColors.SurfaceDesk)
                                .border(
                                    1.dp,
                                    if (isSelected) KidsGColors.OrangeDark else KidsGColors.BorderSubtle,
                                    KidsGShapes.FullPill
                                )
                                .clickable { selectedSubcategoryId = subcat.id }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = subcat.name,
                                style = KidsGTypography.Caption.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) KidsGColors.White else KidsGColors.TextPrimary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // 3. Search Bar + Filter/Sort Trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(KidsGShapes.FullPill)
                        .background(KidsGColors.White)
                        .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.FullPill)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        KidsGIcons.Pencil(modifier = Modifier.size(16.dp), color = KidsGColors.OrangePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = KidsGTypography.BodyMedium.copy(fontSize = 13.sp, color = KidsGColors.BlackText),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search in $selectedSubcatName...",
                                        style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextMuted, fontSize = 13.sp)
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "✕",
                                modifier = Modifier
                                    .clickable { searchQuery = "" }
                                    .padding(4.dp),
                                color = KidsGColors.TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Filter & Sort Trigger Button
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(KidsGShapes.FullPill)
                        .background(if (filterState.activeCount > 0) KidsGColors.OrangeLight else KidsGColors.White)
                        .border(
                            1.dp,
                            if (filterState.activeCount > 0) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle,
                            KidsGShapes.FullPill
                        )
                        .clickable { showFilterSheet = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚙ Filters",
                            style = KidsGTypography.Caption.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (filterState.activeCount > 0) KidsGColors.OrangePrimary else KidsGColors.TextPrimary
                            )
                        )
                        if (filterState.activeCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(KidsGColors.OrangePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${filterState.activeCount}",
                                    style = KidsGTypography.Tag.copy(color = KidsGColors.White, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Products Count & Subcategory Tagline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KidsGSpacing.lg, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${displayedProducts.size} Products available",
                    style = KidsGTypography.Tag.copy(color = KidsGColors.TextSecondary, fontSize = 11.sp)
                )
                Text(
                    text = "⚡ 12-18 Mins Express",
                    style = KidsGTypography.Tag.copy(color = KidsGColors.OrangeDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                )
            }

            // 5. Responsive Two-Column Grid (Screen 6 in Storyboard)
            if (displayedProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(KidsGSpacing.xl),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎒", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No stationery found",
                            style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing filters or search for another item.",
                            style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(KidsGShapes.FullPill)
                                .background(KidsGColors.OrangePrimary)
                                .clickable {
                                    searchQuery = ""
                                    filterState = FilterState()
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(text = "Clear All Filters", style = KidsGTypography.Caption.copy(color = KidsGColors.White, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(KidsGSpacing.md)
                ) {
                    items(displayedProducts, key = { it.id }) { product ->
                        val itemInCart = cart.items.find { it.product.id == product.id }
                        val cartQuantity = itemInCart?.quantity ?: 0
                        val isWishlisted = wishlistIds.contains(product.id)

                        KidsGProductCard(
                            product = product,
                            cartQuantity = cartQuantity,
                            isWishlisted = isWishlisted,
                            onProductClick = { onProductClick(product) },
                            onToggleWishlist = {
                                val nowWishlisted = SessionStorage.toggleWishlistProductId(product.id)
                                wishlistIds = SessionStorage.getWishlistProductIds()
                            },
                            onAddToCart = {
                                coroutineScope.launch {
                                    cartRepository.addToCart(product, 1)
                                }
                            },
                            onIncreaseQuantity = {
                                coroutineScope.launch {
                                    cartRepository.updateQuantity(product.id, cartQuantity + 1)
                                }
                            },
                            onDecreaseQuantity = {
                                coroutineScope.launch {
                                    cartRepository.updateQuantity(product.id, (cartQuantity - 1).coerceAtLeast(0))
                                }
                            }
                        )
                    }

                    item(span = { GridItemSpan(2) }) {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // 6. Filter & Sort Modal / Sheet (Screen 7 in Storyboard)
        if (showFilterSheet) {
            FilterSortDialog(
                currentFilter = filterState,
                totalResults = displayedProducts.size,
                onDismiss = { showFilterSheet = false },
                onApply = { newFilters ->
                    filterState = newFilters
                    showFilterSheet = false
                },
                onClear = {
                    filterState = FilterState()
                    showFilterSheet = false
                }
            )
        }
    }
}

/**
 * Screen 7: Interactive Filters & Sort Dialog matching exact storyboard
 */
@Composable
private fun FilterSortDialog(
    currentFilter: FilterState,
    totalResults: Int,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit,
    onClear: () -> Unit
) {
    var sortOption by remember { mutableStateOf(currentFilter.sortOption) }
    var selectedSizes by remember { mutableStateOf(currentFilter.selectedSizes) }
    var selectedRulings by remember { mutableStateOf(currentFilter.selectedRulings) }
    var selectedBrands by remember { mutableStateOf(currentFilter.selectedBrands) }
    var inStockOnly by remember { mutableStateOf(currentFilter.inStockOnly) }

    val sizes = listOf("A4", "A5", "Long Book", "Pocket", "B5")
    val rulings = listOf("Single Line", "Double Line", "Unruled", "Four Line", "Square", "Graph")
    val brands = listOf("Classmate", "Navneet", "Camlin", "DOMS", "Apsara", "Faber-Castell", "Hauser", "Pilot")

    // Overlay scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x77000000))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(KidsGColors.White)
                .clickable(enabled = false) {}
                .padding(KidsGSpacing.lg)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters & Sort",
                    style = KidsGTypography.TitleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Clear All",
                    style = KidsGTypography.Caption.copy(
                        color = KidsGColors.OrangePrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable(onClick = onClear)
                )
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // Sort By Options
            Text(text = "Sort By", style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ProductSortOption.values()) { opt ->
                    val isSelected = opt == sortOption
                    FilterChip(
                        label = opt.displayName,
                        isSelected = isSelected,
                        onClick = { sortOption = opt }
                    )
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // Size Options
            Text(text = "Size", style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sizes) { size ->
                    val isSelected = selectedSizes.contains(size)
                    FilterChip(
                        label = size,
                        isSelected = isSelected,
                        onClick = {
                            selectedSizes = if (isSelected) selectedSizes - size else selectedSizes + size
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // Ruling Type Options
            Text(text = "Ruling Type", style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rulings) { ruling ->
                    val isSelected = selectedRulings.contains(ruling)
                    FilterChip(
                        label = ruling,
                        isSelected = isSelected,
                        onClick = {
                            selectedRulings = if (isSelected) selectedRulings - ruling else selectedRulings + ruling
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // Brand Options
            Text(text = "Brand", style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(brands) { brand ->
                    val isSelected = selectedBrands.contains(brand)
                    FilterChip(
                        label = brand,
                        isSelected = isSelected,
                        onClick = {
                            selectedBrands = if (isSelected) selectedBrands - brand else selectedBrands + brand
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // Stock Availability Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { inStockOnly = !inStockOnly }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (inStockOnly) KidsGColors.OrangePrimary else KidsGColors.SurfaceDesk)
                        .border(1.dp, KidsGColors.BorderStrong, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (inStockOnly) {
                        Text(text = "✓", color = KidsGColors.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "In Stock Only (Fast Express Delivery)", style = KidsGTypography.BodyMedium)
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.lg))

            // Apply Filters Button (KidsG Orange)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(KidsGShapes.FullPill)
                    .background(KidsGColors.OrangePrimary)
                    .clickable {
                        onApply(
                            FilterState(
                                sortOption = sortOption,
                                selectedSizes = selectedSizes,
                                selectedRulings = selectedRulings,
                                selectedBrands = selectedBrands,
                                inStockOnly = inStockOnly
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Apply Filters",
                    style = KidsGTypography.ButtonText.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(KidsGShapes.Small)
            .background(if (isSelected) KidsGColors.OrangePrimary else KidsGColors.SurfaceDesk)
            .border(
                1.dp,
                if (isSelected) KidsGColors.OrangeDark else KidsGColors.BorderSubtle,
                KidsGShapes.Small
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            style = KidsGTypography.Caption.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) KidsGColors.White else KidsGColors.TextPrimary,
                fontSize = 12.sp
            )
        )
    }
}
