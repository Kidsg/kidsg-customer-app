package com.kidsg.feature.product

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kidsg.core.designsystem.KidsGAsyncImage
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGIcons
import com.kidsg.core.designsystem.KidsGIllustration
import com.kidsg.core.designsystem.KidsGProductCard
import com.kidsg.core.designsystem.KidsGProductVisual
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.core.storage.SessionStorage
import com.kidsg.data.mock.KidsGCatalogue
import com.kidsg.domain.model.Product
import com.kidsg.domain.model.ProductBundle
import com.kidsg.domain.model.ProductQa
import com.kidsg.domain.model.ProductReview
import com.kidsg.domain.repository.CartRepository
import com.kidsg.domain.repository.ProductRepository
import kotlinx.coroutines.launch

enum class DeskViewTab(val label: String) {
    OVERVIEW("Overview"),
    SPECIFICATIONS("Specifications"),
    REVIEWS("Reviews"),
    QA("Q&A")
}

enum class RelatedProductsTab(val label: String) {
    SIMILAR("Similar Products"),
    MORE_BRAND("More from Brand"),
    FOR_STUDENTS("For Your Class")
}

/**
 * Screens 8 through 18: Product Detail — "The Desk View"
 *
 * Implements:
 * - Screen 8: Multi-angle swipeable gallery with page indicator, badges, title, brand tag, ratings, pricing & stock.
 * - Screen 9: Interactive variant selector (Size, Page Count, Ruling) and quantity stepper with mascot message.
 * - Screen 10: Overview tab with eco-friendly badges, chlorine-free highlights and bullet features.
 * - Screen 11: Specifications table with two-column key-value layout.
 * - Screen 12: Product Reviews with 5-star distribution breakdown, filter chips and "Write a Review" dialog.
 * - Screen 13: Product Q&A with verified school expert answers and "Ask a Question" dialog.
 * - Screen 14: Frequently Bought Together bundle with checkboxes and "Add Selected to Bag".
 * - Screen 15: "You May Also Like" Related Products carousel with sub-tabs.
 * - Screen 16: "Popular with Students" horizontal carousel.
 * - Screen 17: "Your Recent Picks" carousel backed by persistent local storage.
 * - Screen 18: Sticky Add to Bag bottom bar with thumbnail, price, wishlist toggle, quantity stepper and primary CTA.
 */
@Composable
fun ProductDeskViewScreen(
    product: Product,
    cartRepository: CartRepository,
    productRepository: ProductRepository? = null,
    onBack: () -> Unit,
    onGoToBag: () -> Unit,
    onProductClick: (Product) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val cart by cartRepository.cartState.collectAsState()
    val itemInCart = cart.items.find { it.product.id == product.id }

    // Persistent storage: save current product to Recently Viewed on launch
    LaunchedEffect(product.id) {
        SessionStorage.addRecentlyViewedProductId(product.id)
    }

    // Wishlist state persisted in SessionStorage
    var isWishlisted by remember(product.id) {
        mutableStateOf(SessionStorage.isProductWishlisted(product.id))
    }

    // Gallery state
    val galleryImages = remember(product) {
        if (product.galleryImages.isNotEmpty()) product.galleryImages
        else listOfNotNull(product.imageUrl).ifEmpty { listOf("mock_angle_front", "mock_angle_open", "mock_angle_back") }
    }
    var selectedImageIndex by remember(product.id) { mutableStateOf(0) }

    // Variant selection
    val availableVariants = remember(product) {
        product.variants.ifEmpty { listOf("Single Line", "Unruled", "Four Line") }
    }
    var selectedRuling by remember(product.id) {
        mutableStateOf(availableVariants.firstOrNull() ?: "Single Line")
    }

    val availableSizes = remember { listOf("A4 (29.7x21cm)", "A5 (21x14.8cm)", "Long Book") }
    var selectedSize by remember(product.id) { mutableStateOf(availableSizes.first()) }

    val availablePageCounts = remember { listOf("172 Pages", "240 Pages", "300 Pages") }
    var selectedPageCount by remember(product.id) { mutableStateOf(availablePageCounts.first()) }

    var selectedQuantity by remember(product.id) { mutableStateOf(1) }

    // Tab state (Overview, Specifications, Reviews, Q&A)
    var selectedTab by remember { mutableStateOf(DeskViewTab.OVERVIEW) }

    // Reviews and Q&A state
    var reviewsList by remember(product.id) {
        mutableStateOf(KidsGCatalogue.getReviews(product.id))
    }
    var qaList by remember(product.id) {
        mutableStateOf(KidsGCatalogue.getQa(product.id))
    }

    // Dialog state
    var showReviewDialog by remember { mutableStateOf(false) }
    var showQaDialog by remember { mutableStateOf(false) }
    var reviewFilter by remember { mutableStateOf("All") }

    // Frequently Bought Together Bundle state
    val bundle: ProductBundle? = remember(product.id) {
        KidsGCatalogue.getFrequentlyBoughtTogether(product.id)
    }
    val selectedBundleItemIds = remember(bundle) {
        mutableStateListOf<String>().apply {
            bundle?.items?.forEach { add(it.id) }
        }
    }

    // Related Products Sub-tabs
    var selectedRelatedTab by remember { mutableStateOf(RelatedProductsTab.SIMILAR) }
    val similarProducts = remember(product.id) { KidsGCatalogue.getSimilarProducts(product) }
    val brandProducts = remember(product.brand) { KidsGCatalogue.getMoreFromBrand(product.brand, product.id) }
    val popularForStudents = remember { KidsGCatalogue.getPopularForStudents() }

    // Recently Viewed Products from SessionStorage
    val recentlyViewedIds = remember(product.id) { SessionStorage.getRecentlyViewedProductIds() }
    val recentlyViewedProducts = remember(recentlyViewedIds) {
        recentlyViewedIds
            .filter { it != product.id }
            .mapNotNull { id -> KidsGCatalogue.products.find { it.id == id } }
            .take(10)
    }

    // Added to bag notification toast banner
    var showAddedToast by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        KidsGIllustration.DeskBackground {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // 1. Top Bar (Back, Desk View Title, Wishlist & Bag shortcut)
                item {
                    DeskTopBar(
                        isWishlisted = isWishlisted,
                        onBack = onBack,
                        onToggleWishlist = {
                            val nowWishlisted = SessionStorage.toggleWishlistProductId(product.id)
                            isWishlisted = nowWishlisted
                        },
                        cartCount = cart.itemCount,
                        onGoToBag = onGoToBag
                    )
                }

                // 2. Section A: Hero Visual & Multi-Angle Image Gallery (Screen 8)
                item {
                    DeskGallerySection(
                        product = product,
                        galleryImages = galleryImages,
                        selectedImageIndex = selectedImageIndex,
                        onSelectImage = { selectedImageIndex = it }
                    )
                }

                // 3. Section B: Title, Brand, Rating & Pricing (Screen 8)
                item {
                    DeskProductSummaryCard(
                        product = product,
                        reviewCount = reviewsList.size
                    )
                }

                // 4. Section C: Interactive Variant Selector & Quantity Stepper (Screen 9)
                item {
                    DeskVariantSection(
                        availableRulings = availableVariants,
                        selectedRuling = selectedRuling,
                        onRulingSelect = { selectedRuling = it },
                        availableSizes = availableSizes,
                        selectedSize = selectedSize,
                        onSizeSelect = { selectedSize = it },
                        availablePageCounts = availablePageCounts,
                        selectedPageCount = selectedPageCount,
                        onPageCountSelect = { selectedPageCount = it },
                        quantity = selectedQuantity,
                        onIncreaseQty = { selectedQuantity++ },
                        onDecreaseQty = { if (selectedQuantity > 1) selectedQuantity-- }
                    )
                }

                // 5. Sections D & E: Tabbed Info Layout (Screens 10, 11, 12, 13)
                item {
                    DeskTabbedSection(
                        product = product,
                        selectedTab = selectedTab,
                        onTabSelect = { selectedTab = it },
                        reviewsList = reviewsList,
                        reviewFilter = reviewFilter,
                        onFilterChange = { reviewFilter = it },
                        onOpenReviewDialog = { showReviewDialog = true },
                        qaList = qaList,
                        onOpenQaDialog = { showQaDialog = true }
                    )
                }

                // 6. Section H: Frequently Bought Together Bundle (Screen 14)
                if (bundle != null && bundle.items.isNotEmpty()) {
                    item {
                        DeskFrequentlyBoughtTogetherSection(
                            bundle = bundle,
                            selectedItemIds = selectedBundleItemIds,
                            onToggleItem = { id ->
                                if (selectedBundleItemIds.contains(id)) {
                                    if (selectedBundleItemIds.size > 1) {
                                        selectedBundleItemIds.remove(id)
                                    }
                                } else {
                                    selectedBundleItemIds.add(id)
                                }
                            },
                            onAddBundleToBag = { itemsToAdd ->
                                coroutineScope.launch {
                                    itemsToAdd.forEach { p ->
                                        cartRepository.addToCart(p, 1)
                                    }
                                    showAddedToast = true
                                }
                            }
                        )
                    }
                }

                // 7. Section I: "You May Also Like" Related Products (Screen 15)
                item {
                    DeskRelatedProductsSection(
                        selectedTab = selectedRelatedTab,
                        onTabSelect = { selectedRelatedTab = it },
                        similarProducts = similarProducts,
                        brandProducts = brandProducts,
                        studentProducts = popularForStudents,
                        cart = cart,
                        onProductClick = onProductClick,
                        onAddToCart = { p ->
                            coroutineScope.launch {
                                cartRepository.addToCart(p, 1)
                            }
                        },
                        onIncreaseQty = { p ->
                            val currentQty = cart.items.find { it.product.id == p.id }?.quantity ?: 0
                            coroutineScope.launch {
                                cartRepository.updateQuantity(p.id, currentQty + 1)
                            }
                        },
                        onDecreaseQty = { p ->
                            val currentQty = cart.items.find { it.product.id == p.id }?.quantity ?: 0
                            coroutineScope.launch {
                                cartRepository.updateQuantity(p.id, currentQty - 1)
                            }
                        }
                    )
                }

                // 8. Section J: "Popular with Students" Carousel (Screen 16)
                if (popularForStudents.isNotEmpty()) {
                    item {
                        DeskPopularCarouselSection(
                            products = popularForStudents,
                            cart = cart,
                            onProductClick = onProductClick,
                            onAddToCart = { p ->
                                coroutineScope.launch {
                                    cartRepository.addToCart(p, 1)
                                }
                            },
                            onIncreaseQty = { p ->
                                val currentQty = cart.items.find { it.product.id == p.id }?.quantity ?: 0
                                coroutineScope.launch {
                                    cartRepository.updateQuantity(p.id, currentQty + 1)
                                }
                            },
                            onDecreaseQty = { p ->
                                val currentQty = cart.items.find { it.product.id == p.id }?.quantity ?: 0
                                coroutineScope.launch {
                                    cartRepository.updateQuantity(p.id, currentQty - 1)
                                }
                            }
                        )
                    }
                }

                // 9. Section K: "Your Recent Picks" Carousel (Screen 17)
                if (recentlyViewedProducts.isNotEmpty()) {
                    item {
                        DeskRecentlyViewedSection(
                            products = recentlyViewedProducts,
                            cart = cart,
                            onProductClick = onProductClick,
                            onAddToCart = { p ->
                                coroutineScope.launch {
                                    cartRepository.addToCart(p, 1)
                                }
                            },
                            onIncreaseQty = { p ->
                                val currentQty = cart.items.find { it.product.id == p.id }?.quantity ?: 0
                                coroutineScope.launch {
                                    cartRepository.updateQuantity(p.id, currentQty + 1)
                                }
                            },
                            onDecreaseQty = { p ->
                                val currentQty = cart.items.find { it.product.id == p.id }?.quantity ?: 0
                                coroutineScope.launch {
                                    cartRepository.updateQuantity(p.id, currentQty - 1)
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }

        // Added to Bag Floating Notification Banner
        AnimatedVisibility(
            visible = showAddedToast,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp, start = 20.dp, end = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(KidsGShapes.Medium)
                    .background(KidsGColors.AccentMint)
                    .border(1.dp, KidsGColors.AccentMintLight, KidsGShapes.Medium)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✔", color = Color(0xFF065F46), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Added to your School Bag successfully!",
                        style = KidsGTypography.BodySmall.copy(
                            color = Color(0xFF065F46),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            LaunchedEffect(showAddedToast) {
                kotlinx.coroutines.delay(2200)
                showAddedToast = false
            }
        }

        // 10. Section L: Sticky Add to Bag Bottom Bar (Screen 18)
        DeskStickyBottomBar(
            product = product,
            selectedVariant = "$selectedRuling • $selectedSize",
            selectedQuantity = selectedQuantity,
            isWishlisted = isWishlisted,
            itemInCartCount = itemInCart?.quantity ?: 0,
            onToggleWishlist = {
                val nowWishlisted = SessionStorage.toggleWishlistProductId(product.id)
                isWishlisted = nowWishlisted
            },
            onIncreaseQty = { selectedQuantity++ },
            onDecreaseQty = { if (selectedQuantity > 1) selectedQuantity-- },
            onAddToCart = {
                coroutineScope.launch {
                    val fullVariant = "$selectedRuling, $selectedSize, $selectedPageCount"
                    cartRepository.addToCart(product, selectedQuantity, fullVariant)
                    showAddedToast = true
                }
            },
            onGoToBag = onGoToBag,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Write a Review Dialog (Screen 12)
        if (showReviewDialog) {
            WriteReviewDialog(
                productId = product.id,
                onDismiss = { showReviewDialog = false },
                onSubmit = { newReview ->
                    KidsGCatalogue.addReview(newReview)
                    reviewsList = KidsGCatalogue.getReviews(product.id)
                    showReviewDialog = false
                }
            )
        }

        // Ask a Question Dialog (Screen 13)
        if (showQaDialog) {
            AskQuestionDialog(
                productId = product.id,
                onDismiss = { showQaDialog = false },
                onSubmit = { question ->
                    KidsGCatalogue.addQuestion(product.id, question)
                    qaList = KidsGCatalogue.getQa(product.id)
                    showQaDialog = false
                }
            )
        }
    }
}

// =========================================================================
// 1. Top App Bar
// =========================================================================
@Composable
private fun DeskTopBar(
    isWishlisted: Boolean,
    onBack: () -> Unit,
    onToggleWishlist: () -> Unit,
    cartCount: Int,
    onGoToBag: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(KidsGColors.White)
                .border(1.dp, KidsGColors.BorderSubtle, CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "←", style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold))
        }

        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(KidsGColors.OrangePrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Student Desk View",
                style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Actions: Wishlist + Bag Shortcut
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isWishlisted) KidsGColors.AccentPinkLight else KidsGColors.White)
                    .border(
                        1.dp,
                        if (isWishlisted) KidsGColors.AccentPink else KidsGColors.BorderSubtle,
                        CircleShape
                    )
                    .clickable(onClick = onToggleWishlist),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isWishlisted) "♥" else "♡",
                    color = if (isWishlisted) KidsGColors.AccentPink else KidsGColors.TextSecondary,
                    fontSize = 20.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(KidsGColors.White)
                    .border(1.dp, KidsGColors.BorderSubtle, CircleShape)
                    .clickable(onClick = onGoToBag),
                contentAlignment = Alignment.Center
            ) {
                Box {
                    KidsGIcons.SchoolBag(modifier = Modifier.size(20.dp), color = KidsGColors.TextPrimary)
                    if (cartCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(KidsGColors.OrangePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$cartCount",
                                color = KidsGColors.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. Section A: Hero Visual & Multi-Angle Image Gallery (Screen 8)
// =========================================================================
@Composable
private fun DeskGallerySection(
    product: Product,
    galleryImages: List<String>,
    selectedImageIndex: Int,
    onSelectImage: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg)
    ) {
        // Main Visual Desk Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(KidsGShapes.DeskCard)
                .background(KidsGColors.White)
                .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.DeskCard),
            contentAlignment = Alignment.Center
        ) {
            // Desk stationery shadow
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                .size(160.dp, 18.dp)
                .clip(KidsGShapes.FullPill)
                .background(KidsGColors.ShadowElevated)
            )

            // Primary stationery illustration/image
            val activeImageUrl = galleryImages.getOrNull(selectedImageIndex) ?: product.imageUrl
            if (!activeImageUrl.isNullOrBlank() && activeImageUrl.startsWith("http")) {
                KidsGAsyncImage(
                    url = activeImageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .size(220.dp)
                        .padding(12.dp),
                    contentScale = ContentScale.Fit,
                    placeholder = {
                        KidsGProductVisual(product = product, modifier = Modifier.size(160.dp))
                    }
                )
            } else {
                KidsGProductVisual(product = product, modifier = Modifier.size(170.dp))
            }

            // Top-left Express Delivery Badge
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
                    Text(
                        text = "⚡ 12-18 MINS",
                        style = KidsGTypography.Tag.copy(color = KidsGColors.OrangeDark, fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Top-right Bestseller or Custom Badge
            val badgeText = product.badgeText ?: if (product.isBestSeller) "★ BESTSELLER" else null
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(KidsGSpacing.md)
                        .clip(KidsGShapes.Small)
                        .background(KidsGColors.AccentYellow)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = KidsGTypography.Tag.copy(color = KidsGColors.BlackText, fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Bottom-right Page counter indicator pill (e.g. "1/4")
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(KidsGSpacing.md)
                    .clip(KidsGShapes.FullPill)
                    .background(KidsGColors.BlackText.copy(alpha = 0.75f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${selectedImageIndex + 1}/${galleryImages.size.coerceAtLeast(1)}",
                    style = KidsGTypography.Caption.copy(
                        color = KidsGColors.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-Angle Thumbnail Selector Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(galleryImages.size) { index ->
                val isSelected = index == selectedImageIndex
                val thumbUrl = galleryImages[index]
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(KidsGShapes.Small)
                        .background(KidsGColors.White)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle,
                            shape = KidsGShapes.Small
                        )
                        .clickable { onSelectImage(index) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (thumbUrl.startsWith("http")) {
                        KidsGAsyncImage(
                            url = thumbUrl,
                            contentDescription = "Thumbnail ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            placeholder = {
                                KidsGProductVisual(product = product, modifier = Modifier.size(36.dp))
                            }
                        )
                    } else {
                        KidsGProductVisual(product = product, modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. Section B: Title, Brand, Rating & Pricing (Screen 8)
// =========================================================================
@Composable
private fun DeskProductSummaryCard(
    product: Product,
    reviewCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md)
            .clip(KidsGShapes.DeskCard)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.DeskCard)
            .padding(KidsGSpacing.lg)
    ) {
        // Brand Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(KidsGShapes.ExtraSmall)
                        .background(KidsGColors.OrangeLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = product.brand.uppercase(),
                        style = KidsGTypography.Tag.copy(
                            color = KidsGColors.OrangePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• Official School Certified",
                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Product Name
        Text(
            text = product.name,
            style = KidsGTypography.TitleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 26.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Rating & Verification Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .clip(KidsGShapes.ExtraSmall)
                    .background(Color(0xFF059669))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${product.rating} ★",
                        style = KidsGTypography.Caption.copy(
                            fontWeight = FontWeight.Bold,
                            color = KidsGColors.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${product.reviewCount.coerceAtLeast(reviewCount)} verified school ratings",
                style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "SKU: ${product.sku.ifBlank { product.id }}",
                style = KidsGTypography.Caption.copy(
                    color = KidsGColors.TextMuted,
                    fontSize = 11.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pricing Row
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "₹${product.price.toInt()}",
                style = KidsGTypography.DisplayMedium.copy(
                    color = KidsGColors.BlackText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                )
            )

            if (product.mrp > product.price) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "₹${product.mrp.toInt()}",
                    style = KidsGTypography.TitleMedium.copy(
                        textDecoration = TextDecoration.LineThrough,
                        color = KidsGColors.TextMuted
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(KidsGShapes.ExtraSmall)
                        .background(KidsGColors.AccentMint)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
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

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Inclusive of all taxes • Delivery in 12-18 mins",
            style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Stock status badge
        if (product.stockQuantity in 1..15) {
            Box(
                modifier = Modifier
                    .clip(KidsGShapes.Small)
                    .background(Color(0xFFFEF2F2))
                    .border(1.dp, Color(0xFFFCA5A5), KidsGShapes.Small)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔥", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Only ${product.stockQuantity} units left in partner dark store!",
                        style = KidsGTypography.Caption.copy(
                            color = Color(0xFFB91C1C),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(KidsGShapes.Small)
                    .background(KidsGColors.AccentMintLight)
                    .border(1.dp, KidsGColors.AccentMint, KidsGShapes.Small)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✔", color = Color(0xFF065F46), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "In Stock & Ready for Rapid Dispatch",
                        style = KidsGTypography.Caption.copy(
                            color = Color(0xFF065F46),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// =========================================================================
// 4. Section C: Interactive Variant Selector & Quantity Stepper (Screen 9)
// =========================================================================
@Composable
private fun DeskVariantSection(
    availableRulings: List<String>,
    selectedRuling: String,
    onRulingSelect: (String) -> Unit,
    availableSizes: List<String>,
    selectedSize: String,
    onSizeSelect: (String) -> Unit,
    availablePageCounts: List<String>,
    selectedPageCount: String,
    onPageCountSelect: (String) -> Unit,
    quantity: Int,
    onIncreaseQty: () -> Unit,
    onDecreaseQty: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.xs)
            .clip(KidsGShapes.DeskCard)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.DeskCard)
            .padding(KidsGSpacing.lg)
    ) {
        Text(
            text = "Customise Your Stationery",
            style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Select format matching your school guidelines",
            style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Ruling / Pattern Chips
        Text(
            text = "Ruling / Style: $selectedRuling",
            style = KidsGTypography.BodySmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            availableRulings.forEach { ruling ->
                val isSelected = ruling == selectedRuling
                Box(
                    modifier = Modifier
                        .clip(KidsGShapes.Small)
                        .background(if (isSelected) KidsGColors.OrangePrimary else KidsGColors.SurfaceDesk)
                        .border(
                            1.dp,
                            if (isSelected) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle,
                            KidsGShapes.Small
                        )
                        .clickable { onRulingSelect(ruling) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = ruling,
                        style = KidsGTypography.Caption.copy(
                            color = if (isSelected) KidsGColors.White else KidsGColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Size Chips
        Text(
            text = "Notebook / Item Size: $selectedSize",
            style = KidsGTypography.BodySmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            availableSizes.forEach { size ->
                val isSelected = size == selectedSize
                Box(
                    modifier = Modifier
                        .clip(KidsGShapes.Small)
                        .background(if (isSelected) KidsGColors.OrangePrimary else KidsGColors.SurfaceDesk)
                        .border(
                            1.dp,
                            if (isSelected) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle,
                            KidsGShapes.Small
                        )
                        .clickable { onSizeSelect(size) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = size,
                        style = KidsGTypography.Caption.copy(
                            color = if (isSelected) KidsGColors.White else KidsGColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Page Count / Density Chips
        Text(
            text = "Page Count: $selectedPageCount",
            style = KidsGTypography.BodySmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            availablePageCounts.forEach { pages ->
                val isSelected = pages == selectedPageCount
                Box(
                    modifier = Modifier
                        .clip(KidsGShapes.Small)
                        .background(if (isSelected) KidsGColors.OrangePrimary else KidsGColors.SurfaceDesk)
                        .border(
                            1.dp,
                            if (isSelected) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle,
                            KidsGShapes.Small
                        )
                        .clickable { onPageCountSelect(pages) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = pages,
                        style = KidsGTypography.Caption.copy(
                            color = if (isSelected) KidsGColors.White else KidsGColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Quantity Stepper & Mascot Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KidsGShapes.Medium)
                .background(KidsGColors.SurfaceDesk)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Select Quantity",
                    style = KidsGTypography.BodySmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Class term bulk discount applies (6+ units)",
                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted, fontSize = 11.sp)
                )
            }

            // Stepper buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(KidsGShapes.FullPill)
                    .background(KidsGColors.White)
                    .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.FullPill)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(KidsGColors.SurfaceDesk)
                        .clickable(onClick = onDecreaseQty),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "−", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Text(
                    text = "$quantity",
                    modifier = Modifier.padding(horizontal = 14.dp),
                    style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(KidsGColors.OrangePrimary)
                        .clickable(onClick = onIncreaseQty),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+", color = KidsGColors.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

// =========================================================================
// 5. Sections D & E: Tabbed Info Layout (Screens 10, 11, 12, 13)
// =========================================================================
@Composable
private fun DeskTabbedSection(
    product: Product,
    selectedTab: DeskViewTab,
    onTabSelect: (DeskViewTab) -> Unit,
    reviewsList: List<ProductReview>,
    reviewFilter: String,
    onFilterChange: (String) -> Unit,
    onOpenReviewDialog: () -> Unit,
    qaList: List<ProductQa>,
    onOpenQaDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md)
            .clip(KidsGShapes.DeskCard)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.DeskCard)
            .padding(KidsGSpacing.lg)
    ) {
        // Tab Header Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DeskViewTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(KidsGShapes.FullPill)
                        .background(if (isSelected) KidsGColors.OrangePrimary else KidsGColors.SurfaceDesk)
                        .clickable { onTabSelect(tab) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (tab) {
                            DeskViewTab.REVIEWS -> "Reviews (${reviewsList.size})"
                            DeskViewTab.QA -> "Q&A (${qaList.size})"
                            else -> tab.label
                        },
                        style = KidsGTypography.Caption.copy(
                            color = if (isSelected) KidsGColors.White else KidsGColors.TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Content of Active Tab
        when (selectedTab) {
            DeskViewTab.OVERVIEW -> {
                DeskOverviewContent(product)
            }
            DeskViewTab.SPECIFICATIONS -> {
                DeskSpecificationsContent(product)
            }
            DeskViewTab.REVIEWS -> {
                DeskReviewsContent(
                    product = product,
                    reviews = reviewsList,
                    activeFilter = reviewFilter,
                    onFilterChange = onFilterChange,
                    onWriteReview = onOpenReviewDialog
                )
            }
            DeskViewTab.QA -> {
                DeskQaContent(
                    qaList = qaList,
                    onAskQuestion = onOpenQaDialog
                )
            }
        }
    }
}

// Tab 1: Overview (Screen 10)
@Composable
private fun DeskOverviewContent(product: Product) {
    Column {
        Text(
            text = "Product Description",
            style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = product.description,
            style = KidsGTypography.BodyMedium.copy(lineHeight = 22.sp, color = KidsGColors.TextPrimary)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Eco-friendly & Paper Quality Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(KidsGShapes.Small)
                    .background(KidsGColors.AccentMintLight)
                    .border(1.dp, KidsGColors.AccentMint, KidsGShapes.Small)
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "🌿 Eco-Friendly", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF065F46))
                    Text(text = "Chlorine-free ozone bleached paper", fontSize = 11.sp, color = Color(0xFF065F46))
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(KidsGShapes.Small)
                    .background(KidsGColors.OrangeLight)
                    .border(1.dp, KidsGColors.OrangeBorder, KidsGShapes.Small)
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "✨ Zero Ink Bleed", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KidsGColors.OrangeDark)
                    Text(text = "70 GSM ultra-white pages", fontSize = 11.sp, color = KidsGColors.OrangeDark)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bullet Features
        Text(
            text = "Key School Highlights",
            style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        val highlights = listOf(
            "Designed and tailored strictly per CBSE, ICSE, and State Board curriculum guidelines.",
            "Firm center-stitch / wire binding that withstands heavy daily school bag pressure.",
            "Includes activity page, teacher signature column, and index sheet on front cover.",
            "Ergonomic rounded corners preventing paper cuts and dog-eared pages."
        )
        highlights.forEach { bullet ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(text = "•", color = KidsGColors.OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = bullet, style = KidsGTypography.BodySmall.copy(lineHeight = 18.sp))
            }
        }
    }
}

// Tab 2: Specifications Table (Screen 11)
@Composable
private fun DeskSpecificationsContent(product: Product) {
    Column {
        Text(
            text = "Detailed Specifications",
            style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(10.dp))

        val specs = mutableMapOf<String, String>()
        specs["Brand"] = product.brand
        specs["SKU Code"] = product.sku.ifBlank { product.id }
        specs["Subcategory"] = product.subcategoryId.ifBlank { "School Stationery" }
        specs["Paper Density"] = "70 GSM Bright White"
        specs["Ruling Type"] = product.variants.firstOrNull() ?: "Single Line (Rule 9mm)"
        specs["Dimensions"] = "24.0 cm x 18.0 cm"
        specs["Cover Finish"] = "Glossy Soft Bound with Poly Wrap"
        specs["Binding"] = "Center-Stitched Reinforced"
        specs["Target Grades"] = "Grade 1 to 12"
        specs["Country of Origin"] = "India"
        specs.putAll(product.specifications)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KidsGShapes.Small)
                .background(KidsGColors.SurfaceDesk)
                .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.Small)
        ) {
            specs.entries.forEachIndexed { index, (key, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 0) KidsGColors.White else KidsGColors.SurfaceDesk)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = key,
                        style = KidsGTypography.Caption.copy(
                            color = KidsGColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = value,
                        style = KidsGTypography.Caption.copy(
                            color = KidsGColors.BlackText,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// Tab 3: Reviews breakdown & list (Screen 12)
@Composable
private fun DeskReviewsContent(
    product: Product,
    reviews: List<ProductReview>,
    activeFilter: String,
    onFilterChange: (String) -> Unit,
    onWriteReview: () -> Unit
) {
    Column {
        // Overall score & distribution breakdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KidsGShapes.Medium)
                .background(KidsGColors.SurfaceDesk)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(
                    text = "${product.rating}",
                    style = KidsGTypography.DisplayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    )
                )
                Text(text = "★★★★★", color = KidsGColors.OrangePrimary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${reviews.size} School Reviews",
                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted, fontSize = 10.sp)
                )
            }

            // Star distribution progress bars
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf(
                    Pair("5★", 0.78f),
                    Pair("4★", 0.14f),
                    Pair("3★", 0.05f),
                    Pair("2★", 0.02f),
                    Pair("1★", 0.01f)
                ).forEach { (star, frac) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = star, fontSize = 10.sp, color = KidsGColors.TextSecondary, modifier = Modifier.width(22.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(KidsGShapes.FullPill)
                                .background(KidsGColors.BorderSubtle)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(frac)
                                    .height(6.dp)
                                    .clip(KidsGShapes.FullPill)
                                    .background(KidsGColors.OrangePrimary)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Write a Review Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Student & Parent Feedback",
                style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Box(
                modifier = Modifier
                    .clip(KidsGShapes.FullPill)
                    .background(KidsGColors.OrangePrimary)
                    .clickable(onClick = onWriteReview)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "+ Write Review",
                    style = KidsGTypography.Caption.copy(
                        color = KidsGColors.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Review Filter Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("All", "5 Star", "With Photos", "Verified").forEach { chip ->
                val isSelected = chip == activeFilter
                Box(
                    modifier = Modifier
                        .clip(KidsGShapes.FullPill)
                        .background(if (isSelected) KidsGColors.BlackText else KidsGColors.SurfaceDesk)
                        .clickable { onFilterChange(chip) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = chip,
                        style = KidsGTypography.Caption.copy(
                            color = if (isSelected) KidsGColors.White else KidsGColors.TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reviews List
        val filtered = reviews.filter {
            when (activeFilter) {
                "5 Star" -> it.rating >= 4.5
                "With Photos" -> it.photoUrls.isNotEmpty()
                "Verified" -> it.isVerifiedPurchase
                else -> true
            }
        }

        if (filtered.isEmpty()) {
            Text(
                text = "No reviews found for this filter.",
                style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filtered.forEach { rev ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(KidsGShapes.Small)
                            .background(KidsGColors.SurfaceDesk)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = rev.authorName,
                                    style = KidsGTypography.BodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (rev.isVerifiedPurchase) {
                                    Box(
                                        modifier = Modifier
                                            .clip(KidsGShapes.ExtraSmall)
                                            .background(KidsGColors.AccentMintLight)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Verified Buyer",
                                            style = KidsGTypography.Caption.copy(
                                                color = Color(0xFF065F46),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${rev.rating} ★",
                                color = KidsGColors.OrangePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "${rev.authorRole} • ${rev.dateText}",
                            style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted, fontSize = 10.sp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = rev.comment,
                            style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextPrimary, lineHeight = 18.sp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "👍 Helpful (${rev.helpfulCount})",
                                style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Tab 4: Questions & Answers (Screen 13)
@Composable
private fun DeskQaContent(
    qaList: List<ProductQa>,
    onAskQuestion: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Questions & Answers",
                    style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "School experts respond in < 15 mins",
                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted)
                )
            }

            Box(
                modifier = Modifier
                    .clip(KidsGShapes.FullPill)
                    .background(KidsGColors.OrangePrimary)
                    .clickable(onClick = onAskQuestion)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "+ Ask Question",
                    style = KidsGTypography.Caption.copy(
                        color = KidsGColors.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (qaList.isEmpty()) {
            Text(
                text = "No questions asked yet. Be the first to ask!",
                style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                qaList.forEach { qa ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(KidsGShapes.Small)
                            .background(KidsGColors.SurfaceDesk)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(text = "Q:", color = KidsGColors.OrangePrimary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = qa.question,
                                style = KidsGTypography.BodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.Top) {
                            Text(text = "A:", color = Color(0xFF059669), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = qa.answer,
                                    style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextPrimary, lineHeight = 18.sp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "By ${qa.answeredBy} • ${qa.answeredDateText}",
                                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted, fontSize = 10.sp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. Section H: Frequently Bought Together Bundle (Screen 14)
// =========================================================================
@Composable
private fun DeskFrequentlyBoughtTogetherSection(
    bundle: ProductBundle,
    selectedItemIds: List<String>,
    onToggleItem: (String) -> Unit,
    onAddBundleToBag: (List<Product>) -> Unit
) {
    val selectedProducts = bundle.items.filter { selectedItemIds.contains(it.id) }
    val originalTotal = selectedProducts.sumOf { it.price }
    val discountMultiplier = 1.0 - (bundle.discountPercent / 100.0)
    val discountedTotal = (originalTotal * discountMultiplier).toInt()
    val savings = (originalTotal - discountedTotal).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.sm)
            .clip(KidsGShapes.DeskCard)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.DeskCard)
            .padding(KidsGSpacing.lg)
    ) {
        // Banner Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Frequently Bought Together",
                    style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Complete your student desk pack",
                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted)
                )
            }

            Box(
                modifier = Modifier
                    .clip(KidsGShapes.Small)
                    .background(KidsGColors.AccentMint)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SAVE ${bundle.discountPercent}%",
                    style = KidsGTypography.Caption.copy(
                        color = Color(0xFF065F46),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Visual Items linked with "+"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            bundle.items.forEachIndexed { index, item ->
                val isChecked = selectedItemIds.contains(item.id)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(KidsGShapes.Medium)
                            .background(KidsGColors.SurfaceDesk)
                            .border(
                                1.5.dp,
                                if (isChecked) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle,
                                KidsGShapes.Medium
                            )
                            .clickable { onToggleItem(item.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        KidsGProductVisual(product = item, modifier = Modifier.size(50.dp))

                        // Selection Checkmark Overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isChecked) KidsGColors.OrangePrimary else KidsGColors.White)
                                .border(1.dp, if (isChecked) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChecked) {
                                Text(text = "✓", color = KidsGColors.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.name,
                        style = KidsGTypography.Caption.copy(fontSize = 10.sp, textAlign = TextAlign.Center),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "₹${item.price.toInt()}",
                        style = KidsGTypography.Caption.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                }

                if (index < bundle.items.lastIndex) {
                    Text(
                        text = "+",
                        style = KidsGTypography.TitleLarge.copy(color = KidsGColors.OrangePrimary),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary and Bundle CTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹$discountedTotal",
                        style = KidsGTypography.TitleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "₹${originalTotal.toInt()}",
                        style = KidsGTypography.Caption.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = KidsGColors.TextMuted
                        )
                    )
                }
                Text(
                    text = "You save ₹$savings on this pack",
                    style = KidsGTypography.Caption.copy(color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                )
            }

            Box(
                modifier = Modifier
                    .clip(KidsGShapes.Medium)
                    .background(KidsGColors.OrangePrimary)
                    .clickable { onAddBundleToBag(selectedProducts) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Add Selected (${selectedProducts.size}) to Bag",
                    style = KidsGTypography.BodySmall.copy(
                        color = KidsGColors.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

// =========================================================================
// 7. Section I: "You May Also Like" Related Products (Screen 15)
// =========================================================================
@Composable
private fun DeskRelatedProductsSection(
    selectedTab: RelatedProductsTab,
    onTabSelect: (RelatedProductsTab) -> Unit,
    similarProducts: List<Product>,
    brandProducts: List<Product>,
    studentProducts: List<Product>,
    cart: com.kidsg.domain.model.Cart,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onIncreaseQty: (Product) -> Unit,
    onDecreaseQty: (Product) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = KidsGSpacing.sm)
    ) {
        Column(modifier = Modifier.padding(horizontal = KidsGSpacing.lg)) {
            Text(
                text = "You May Also Like",
                style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Sub-tabs
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RelatedProductsTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    Box(
                        modifier = Modifier
                            .clip(KidsGShapes.FullPill)
                            .background(if (isSelected) KidsGColors.BlackText else KidsGColors.White)
                            .border(
                                1.dp,
                                if (isSelected) KidsGColors.BlackText else KidsGColors.BorderSubtle,
                                KidsGShapes.FullPill
                            )
                            .clickable { onTabSelect(tab) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tab.label,
                            style = KidsGTypography.Caption.copy(
                                color = if (isSelected) KidsGColors.White else KidsGColors.TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val currentList = when (selectedTab) {
            RelatedProductsTab.SIMILAR -> similarProducts
            RelatedProductsTab.MORE_BRAND -> brandProducts
            RelatedProductsTab.FOR_STUDENTS -> studentProducts
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = KidsGSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(currentList.take(8)) { prod ->
                val qty = cart.items.find { it.product.id == prod.id }?.quantity ?: 0
                KidsGProductCard(
                    product = prod,
                    cartQuantity = qty,
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart,
                    onIncreaseQuantity = onIncreaseQty,
                    onDecreaseQuantity = onDecreaseQty,
                    modifier = Modifier.width(165.dp)
                )
            }
        }
    }
}

// =========================================================================
// 8. Section J: "Popular with Students" Carousel (Screen 16)
// =========================================================================
@Composable
private fun DeskPopularCarouselSection(
    products: List<Product>,
    cart: com.kidsg.domain.model.Cart,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onIncreaseQty: (Product) -> Unit,
    onDecreaseQty: (Product) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = KidsGSpacing.sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KidsGSpacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔥", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Popular with Students",
                        style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = "Most bought in schools this week",
                    style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = KidsGSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products.take(8)) { prod ->
                val qty = cart.items.find { it.product.id == prod.id }?.quantity ?: 0
                KidsGProductCard(
                    product = prod,
                    cartQuantity = qty,
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart,
                    onIncreaseQuantity = onIncreaseQty,
                    onDecreaseQuantity = onDecreaseQty,
                    modifier = Modifier.width(165.dp)
                )
            }
        }
    }
}

// =========================================================================
// 9. Section K: "Your Recent Picks" Carousel (Screen 17)
// =========================================================================
@Composable
private fun DeskRecentlyViewedSection(
    products: List<Product>,
    cart: com.kidsg.domain.model.Cart,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onIncreaseQty: (Product) -> Unit,
    onDecreaseQty: (Product) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = KidsGSpacing.sm)
    ) {
        Column(modifier = Modifier.padding(horizontal = KidsGSpacing.lg)) {
            Text(
                text = "Your Recent Picks",
                style = KidsGTypography.TitleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Stationery items you previously explored",
                style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = KidsGSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { prod ->
                val qty = cart.items.find { it.product.id == prod.id }?.quantity ?: 0
                KidsGProductCard(
                    product = prod,
                    cartQuantity = qty,
                    onProductClick = onProductClick,
                    onAddToCart = onAddToCart,
                    onIncreaseQuantity = onIncreaseQty,
                    onDecreaseQuantity = onDecreaseQty,
                    modifier = Modifier.width(165.dp)
                )
            }
        }
    }
}

// =========================================================================
// 10. Section L: Sticky Add to Bag Bottom Bar (Screen 18)
// =========================================================================
@Composable
private fun DeskStickyBottomBar(
    product: Product,
    selectedVariant: String,
    selectedQuantity: Int,
    isWishlisted: Boolean,
    itemInCartCount: Int,
    onToggleWishlist: () -> Unit,
    onIncreaseQty: () -> Unit,
    onDecreaseQty: () -> Unit,
    onAddToCart: () -> Unit,
    onGoToBag: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle)
            .padding(horizontal = KidsGSpacing.lg, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Price & Thumbnail Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Wishlist Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isWishlisted) KidsGColors.AccentPinkLight else KidsGColors.SurfaceDesk)
                        .border(
                            1.dp,
                            if (isWishlisted) KidsGColors.AccentPink else KidsGColors.BorderSubtle,
                            CircleShape
                        )
                        .clickable(onClick = onToggleWishlist),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isWishlisted) "♥" else "♡",
                        color = if (isWishlisted) KidsGColors.AccentPink else KidsGColors.TextSecondary,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "₹${(product.price * selectedQuantity).toInt()}",
                            style = KidsGTypography.TitleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = KidsGColors.BlackText
                            )
                        )
                        if (product.mrp > product.price) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "₹${(product.mrp * selectedQuantity).toInt()}",
                                style = KidsGTypography.Caption.copy(
                                    textDecoration = TextDecoration.LineThrough,
                                    color = KidsGColors.TextMuted
                                )
                            )
                        }
                    }
                    Text(
                        text = "Qty: $selectedQuantity • ${product.brand}",
                        style = KidsGTypography.Caption.copy(
                            color = KidsGColors.TextSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Actions: Add to Bag Primary CTA
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (itemInCartCount > 0) {
                    // Quick Go to Bag button
                    Box(
                        modifier = Modifier
                            .clip(KidsGShapes.Medium)
                            .background(KidsGColors.SurfaceDesk)
                            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.Medium)
                            .clickable(onClick = onGoToBag)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "View Bag ($itemInCartCount)",
                            style = KidsGTypography.BodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KidsGColors.TextPrimary
                            )
                        )
                    }
                }

                // Add to Bag Button
                Box(
                    modifier = Modifier
                        .clip(KidsGShapes.Medium)
                        .background(KidsGColors.OrangePrimary)
                        .clickable(onClick = onAddToCart)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KidsGIcons.SchoolBag(modifier = Modifier.size(16.dp), color = KidsGColors.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (itemInCartCount > 0) "+ Add More" else "Add to Bag",
                            style = KidsGTypography.BodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KidsGColors.White
                            )
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// Dialogs: Write Review & Ask Question
// =========================================================================
@Composable
private fun WriteReviewDialog(
    productId: String,
    onDismiss: () -> Unit,
    onSubmit: (ProductReview) -> Unit
) {
    var reviewerName by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5.0) }
    var comment by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Student") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KidsGShapes.Large)
                .background(KidsGColors.White)
                .padding(20.dp)
        ) {
            Text(
                text = "Write a School Review",
                style = KidsGTypography.TitleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Help fellow students pick the best stationery",
                style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Rating Stars
            Text(text = "Rating", style = KidsGTypography.Caption.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..5).forEach { star ->
                    Text(
                        text = if (star <= rating) "★" else "☆",
                        fontSize = 28.sp,
                        color = KidsGColors.OrangePrimary,
                        modifier = Modifier.clickable { rating = star.toDouble() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Your Name
            Text(text = "Your Name", style = KidsGTypography.Caption.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            BasicTextField(
                value = reviewerName,
                onValueChange = { reviewerName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KidsGShapes.Small)
                    .background(KidsGColors.SurfaceDesk)
                    .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.Small)
                    .padding(10.dp),
                decorationBox = { innerTextField ->
                    if (reviewerName.isEmpty()) {
                        Text(text = "e.g. Aarav M.", color = KidsGColors.TextMuted, fontSize = 13.sp)
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Role selection
            Text(text = "You are a", style = KidsGTypography.Caption.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Student", "Parent", "Teacher").forEach { r ->
                    val isSelected = r == role
                    Box(
                        modifier = Modifier
                            .clip(KidsGShapes.FullPill)
                            .background(if (isSelected) KidsGColors.OrangePrimary else KidsGColors.SurfaceDesk)
                            .clickable { role = r }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = r,
                            style = KidsGTypography.Caption.copy(
                                color = if (isSelected) KidsGColors.White else KidsGColors.TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comment
            Text(text = "Your Experience / Feedback", style = KidsGTypography.Caption.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            BasicTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(KidsGShapes.Small)
                    .background(KidsGColors.SurfaceDesk)
                    .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.Small)
                    .padding(10.dp),
                decorationBox = { innerTextField ->
                    if (comment.isEmpty()) {
                        Text(text = "Paper smoothness, binding durability, ink bleed...", color = KidsGColors.TextMuted, fontSize = 13.sp)
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Cancel", style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextMuted))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(KidsGShapes.Medium)
                        .background(if (comment.isNotBlank()) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle)
                        .clickable(enabled = comment.isNotBlank()) {
                            onSubmit(
                                ProductReview(
                                    id = "rev_${System.currentTimeMillis()}",
                                    productId = productId,
                                    authorName = reviewerName.ifBlank { "Student Reviewer" },
                                    authorRole = role,
                                    rating = rating,
                                    dateText = "Just now",
                                    comment = comment,
                                    isVerifiedPurchase = true,
                                    helpfulCount = 1
                                )
                            )
                        }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Submit Review",
                        style = KidsGTypography.BodySmall.copy(
                            color = KidsGColors.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AskQuestionDialog(
    productId: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var question by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KidsGShapes.Large)
                .background(KidsGColors.White)
                .padding(20.dp)
        ) {
            Text(
                text = "Ask a Question",
                style = KidsGTypography.TitleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Our verified stationery experts answer promptly",
                style = KidsGTypography.Caption.copy(color = KidsGColors.TextMuted)
            )

            Spacer(modifier = Modifier.height(14.dp))

            BasicTextField(
                value = question,
                onValueChange = { question = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(KidsGShapes.Small)
                    .background(KidsGColors.SurfaceDesk)
                    .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.Small)
                    .padding(10.dp),
                decorationBox = { innerTextField ->
                    if (question.isEmpty()) {
                        Text(text = "e.g. Is this book suitable for 5th grade ICSE syllabus?", color = KidsGColors.TextMuted, fontSize = 13.sp)
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = "Cancel", style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextMuted))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(KidsGShapes.Medium)
                        .background(if (question.isNotBlank()) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle)
                        .clickable(enabled = question.isNotBlank()) {
                            onSubmit(question)
                        }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Submit Question",
                        style = KidsGTypography.BodySmall.copy(
                            color = KidsGColors.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
