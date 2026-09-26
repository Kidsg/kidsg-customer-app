package com.kidsg.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGAsyncImage
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGResourceImage
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.data.mock.KidsGMockData
import com.kidsg.domain.model.IntentModeType
import com.kidsg.domain.model.Product
import com.kidsg.domain.model.UserProfile
import com.kidsg.domain.repository.CartRepository
import com.kidsg.domain.repository.ProductRepository

/**
 * KidsG Redesigned Home Screen — Adapted for Kids Stationery Products
 * Adopts the exact high-converting e-commerce layout structure (Top Bar with mascot & weather pill,
 * Hero Banner with overlapping Floating Search, Category Pills, DEAL OF THE DAY, Steal Deals,
 * SHOP BY CATEGORY 12-grid cards, FEATURED COLLECTIONS 6-card banners, FRESH PICKS carousel).
 */
@Composable
fun KidsGDeskHomeScreen(
    productRepository: ProductRepository,
    cartRepository: CartRepository,
    userProfile: UserProfile? = null,
    currentLocationName: String = "Ashok Nagar, Bengaluru",
    onLocationClick: () -> Unit = {},
    onNavigateToDiscovery: (IntentModeType?) -> Unit,
    onNavigateToCategory: ((String) -> Unit)? = null,
    onNavigateToSearch: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onIncreaseQuantity: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    onNavigateToBag: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cart by cartRepository.cartState.collectAsState()
    val mockProducts = remember { KidsGMockData.products }

    var selectedTabId by remember { mutableStateOf("for_you") }

    val tabs = remember {
        listOf(
            TabPill("for_you", "For you"),
            TabPill("school_essentials", "School Essentials"),
            TabPill("notebooks", "Notebooks"),
            TabPill("pens_pencils", "Pens & Pencils"),
            TabPill("art_craft", "Art & Craft"),
            TabPill("school_bags", "School Bags"),
            TabPill("lunch_boxes", "Lunch Box & Bottles"),
            TabPill("geometry", "Geometry"),
            TabPill("exam_essentials", "Exam Essentials")
        )
    }

    // 12 Visual Category Cards matching Image 5
    val categories12 = remember {
        listOf(
            CategoryCard12(
                id = "school_essentials",
                title = "School\nEssentials",
                iconEmoji = "☀️",
                illustrationType = "backpack_yellow",
                bgColor = Color(0xFFFFF4E5),
                accentColor = Color(0xFFF97316)
            ),
            CategoryCard12(
                id = "notebooks",
                title = "Notebooks\n& Notepads",
                iconEmoji = "👑",
                illustrationType = "notebooks_stack",
                bgColor = Color(0xFFF3E8FF),
                accentColor = Color(0xFFA855F7)
            ),
            CategoryCard12(
                id = "pens_pencils",
                title = "Pens\n& Pencils",
                iconEmoji = "💦",
                illustrationType = "pens_cup",
                bgColor = Color(0xFFE0F2FE),
                accentColor = Color(0xFF0284C7)
            ),
            CategoryCard12(
                id = "art_craft",
                title = "Art &\nCraft",
                iconEmoji = "⭐️",
                illustrationType = "art_palette",
                bgColor = Color(0xFFFCE7F3),
                accentColor = Color(0xFFEC4899)
            ),
            CategoryCard12(
                id = "school_bags",
                title = "School\nBags",
                iconEmoji = "🎒",
                illustrationType = "rocket_backpack",
                bgColor = Color(0xFFE0F2FE),
                accentColor = Color(0xFF2563EB)
            ),
            CategoryCard12(
                id = "lunch_boxes",
                title = "Lunch Box\n& Bottles",
                iconEmoji = "❤️",
                illustrationType = "lunchbox_bottle",
                bgColor = Color(0xFFFEF3C7),
                accentColor = Color(0xFFD97706)
            ),
            CategoryCard12(
                id = "geometry",
                title = "Geometry\n& Math Tools",
                iconEmoji = "📐",
                illustrationType = "geometry_box",
                bgColor = Color(0xFFDCFCE7),
                accentColor = Color(0xFF16A34A)
            ),
            CategoryCard12(
                id = "stationery_sets",
                title = "Stationery\nSets",
                iconEmoji = "✨",
                illustrationType = "stationery_kit",
                bgColor = Color(0xFFFFE4E6),
                accentColor = Color(0xFFF43F5E)
            ),
            CategoryCard12(
                id = "books",
                title = "Books",
                iconEmoji = "📚",
                illustrationType = "textbooks_stack",
                bgColor = Color(0xFFFFEDD5),
                accentColor = Color(0xFFEA580C)
            ),
            CategoryCard12(
                id = "exam_essentials",
                title = "Exam\nEssentials",
                iconEmoji = "📋",
                illustrationType = "exam_pouch",
                bgColor = Color(0xFFF3E8FF),
                accentColor = Color(0xFF9333EA)
            ),
            CategoryCard12(
                id = "labels_stickers",
                title = "Labels\n& Stickers",
                iconEmoji = "🏷️",
                illustrationType = "stickers_sheet",
                bgColor = Color(0xFFFCE7F3),
                accentColor = Color(0xFFDB2777)
            ),
            CategoryCard12(
                id = "uniform_accessories",
                title = "Uniform\nAccessories",
                iconEmoji = "🎀",
                illustrationType = "uniform_set",
                bgColor = Color(0xFFE0F2FE),
                accentColor = Color(0xFF0284C7)
            )
        )
    }

    // 6 Feature Banner Cards matching Image 7
    val featureBanners = remember {
        listOf(
            FeatureBannerData(
                badgeTag = "EXAM SEASON",
                badgeBg = Color(0xFFFF7A00),
                title = "Notebooks & Notepads",
                subtitle = "Write, plan and achieve more every day!",
                bgColor = Color(0xFFFFF7ED),
                borderColor = Color(0xFFFFD8A8),
                illustrationType = "feature_notebooks",
                categoryId = "notebooks"
            ),
            FeatureBannerData(
                badgeTag = "PENS & PENCILS",
                badgeBg = Color(0xFFEC4899),
                title = "Creative Essentials",
                subtitle = "For brighter ideas and bigger dreams!",
                bgColor = Color(0xFFFDF2F8),
                borderColor = Color(0xFFFBCFE8),
                illustrationType = "feature_pens",
                categoryId = "pens_pencils"
            ),
            FeatureBannerData(
                badgeTag = "SCHOOL BAGS",
                badgeBg = Color(0xFF2563EB),
                title = "Carry Your World",
                subtitle = "Stylish, durable and ready for every day!",
                bgColor = Color(0xFFF0F9FF),
                borderColor = Color(0xFFBAE6FD),
                illustrationType = "feature_bags",
                categoryId = "school_bags"
            ),
            FeatureBannerData(
                badgeTag = "LUNCH BOX & BOTTLES",
                badgeBg = Color(0xFFD97706),
                title = "Healthy Happier Days",
                subtitle = "Fun lunch boxes and bottles for active kids!",
                bgColor = Color(0xFFFEFCE8),
                borderColor = Color(0xFFFDE68A),
                illustrationType = "feature_lunch",
                categoryId = "lunch_boxes"
            ),
            FeatureBannerData(
                badgeTag = "ART & CRAFT",
                badgeBg = Color(0xFF16A34A),
                title = "Create Without Limits",
                subtitle = "Colors, crafts and tools to express their imagination!",
                bgColor = Color(0xFFF0FDF4),
                borderColor = Color(0xFFBBF7D0),
                illustrationType = "feature_art",
                categoryId = "art_craft"
            ),
            FeatureBannerData(
                badgeTag = "STUDY ESSENTIALS",
                badgeBg = Color(0xFF9333EA),
                title = "Tools for Bright Futures",
                subtitle = "Geometry, calculators, folders & more!",
                bgColor = Color(0xFFF5F3FF),
                borderColor = Color(0xFFDDD6FE),
                illustrationType = "feature_geometry",
                categoryId = "geometry"
            )
        )
    }

    // Fresh Picks products matching Image 8
    val freshPicks = remember {
        listOf(
            FreshPickItem(
                id = "prod_spiral_notebook_a5",
                badgeText = "🔥 Bestseller",
                badgeColor = Color(0xFFFF9000),
                title = "Spiral Notebook A5",
                specs = "(200 Pages)",
                price = 99.0,
                mrp = 120.0,
                rating = 4.8,
                reviews = "2.4k",
                illustrationType = "fresh_spiral",
                imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=80"
            ),
            FreshPickItem(
                id = "prod_camlin_gel_pen_pack",
                badgeText = "20% OFF",
                badgeColor = Color(0xFFF43F5E),
                title = "Gel Pen Set",
                specs = "(10 Colors)",
                price = 159.0,
                mrp = 199.0,
                rating = 4.7,
                reviews = "1.8k",
                illustrationType = "fresh_pens",
                imageUrl = "https://images.unsplash.com/photo-1585336261026-407a50ee7b2c?w=500&auto=format&fit=crop&q=80"
            ),
            FreshPickItem(
                id = "prod_milton_thermosteel_bottle",
                badgeText = "📈 Trending",
                badgeColor = Color(0xFF0284C7),
                title = "Insulated Water Bottle",
                specs = "(600 ml)",
                price = 349.0,
                mrp = 449.0,
                rating = 4.8,
                reviews = "3.1k",
                illustrationType = "fresh_bottle",
                imageUrl = "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500&auto=format&fit=crop&q=80"
            ),
            FreshPickItem(
                id = "prod_kids_bento_snack_box",
                badgeText = "❤️ Popular",
                badgeColor = Color(0xFFDB2777),
                title = "Lunch Box",
                specs = "with 3 Compartments",
                price = 399.0,
                mrp = 499.0,
                rating = 4.6,
                reviews = "1.2k",
                illustrationType = "fresh_lunchbox",
                imageUrl = "https://images.unsplash.com/photo-1590301157890-4810ed352733?w=500&auto=format&fit=crop&q=80"
            ),
            FreshPickItem(
                id = "prod_maped_geometry_box",
                badgeText = "✨ New",
                badgeColor = Color(0xFF16A34A),
                title = "Geometry Box",
                specs = "(8 Tools)",
                price = 249.0,
                mrp = 299.0,
                rating = 4.7,
                reviews = "980",
                illustrationType = "fresh_geometry",
                imageUrl = "https://images.unsplash.com/photo-1588072432836-e10032774350?w=500&auto=format&fit=crop&q=80"
            )
        )
    }

    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. TOP BAR (Mascot avatar + Weather/Delivery pill + Wishlist + Cart)
            item(key = "top_bar", contentType = "top_bar") {
                SlikkStyleTopBar(
                    userProfile = userProfile,
                    currentLocationName = currentLocationName,
                    onLocationClick = onLocationClick,
                    cartItemCount = cart.itemCount,
                    onWishlistClick = { onNavigateToDiscovery(null) },
                    onCartClick = { onNavigateToBag?.invoke() ?: onNavigateToDiscovery(null) }
                )
            }

            // 2. HERO BANNER WITH OVERLAPPING FLOATING SEARCH BAR
            item(key = "hero_banner", contentType = "hero_banner") {
                HeroBannerWithSearch(
                    onShopNow = { onNavigateToDiscovery(null) },
                    onSearchClick = onNavigateToSearch
                )
            }

            // 3. CATEGORY PILLS BAR (Horizontal Tab Bar)
            item(key = "category_pills", contentType = "category_pills") {
                CategoryPillsBar(
                    tabs = tabs,
                    selectedTabId = selectedTabId,
                    onTabSelected = { tabId ->
                        selectedTabId = tabId
                        if (tabId != "for_you") {
                            onNavigateToCategory?.invoke(tabId)
                        }
                    }
                )
            }

            // 4. "DEAL OF THE DAY" SECTION
            item(key = "deal_of_the_day", contentType = "deal_of_the_day") {
                DealOfTheDaySection(
                    onDealClick = { catId -> onNavigateToCategory?.invoke(catId) }
                )
            }

            // 5. PROMO BANNER ("Steal deals")
            item(key = "steal_deals", contentType = "steal_deals") {
                StealDealsBanner(
                    onBannerClick = { onNavigateToDiscovery(null) }
                )
            }

            // 6. "SHOP BY CATEGORY" SECTION (12 Grid Cards matching Image 5 & Image 2)
            item(key = "category_grid", contentType = "category_grid") {
                ShopByCategoryGrid(
                    categories = categories12,
                    onCategoryClick = { catId -> onNavigateToCategory?.invoke(catId) }
                )
            }

            // 7. FEATURED COLLECTIONS SECTION (6 Feature Banners matching Image 7)
            item(key = "featured_collections", contentType = "featured_collections") {
                FeaturedCollectionsSection(
                    banners = featureBanners,
                    onBannerClick = { catId -> onNavigateToCategory?.invoke(catId) }
                )
            }

            // 8. "FRESH PICKS" CAROUSEL SECTION (Matching Image 8)
            item(key = "fresh_picks", contentType = "fresh_picks") {
                FreshPicksSection(
                    items = freshPicks,
                    cart = cart,
                    mockProducts = mockProducts,
                    onAddToCart = onAddToCart,
                    onSeeAllClick = { onNavigateToDiscovery(null) }
                )
            }

            // 9. "BUILD YOUR SCHOOL KIT" BANNER
            item(key = "build_kit", contentType = "build_kit") {
                BuildKitBanner(
                    onCreateKitClick = { onNavigateToDiscovery(null) }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 1: TOP BAR (Mascot avatar + Weather/Location pill + Wishlist + Cart)
// -----------------------------------------------------------------------------
@Composable
private fun SlikkStyleTopBar(
    userProfile: UserProfile?,
    currentLocationName: String,
    onLocationClick: () -> Unit,
    cartItemCount: Int,
    onWishlistClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val studentInitial = userProfile?.studentName?.firstOrNull()?.uppercase()
        ?: userProfile?.name?.firstOrNull()?.uppercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mascot avatar on left (Glasses Bunny in white circle frame or student initial)
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, Color(0xFFE2E8F0), CircleShape)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            if (studentInitial != null) {
                Text(
                    text = studentInitial,
                    style = KidsGTypography.Caption.copy(
                        fontWeight = FontWeight.Black,
                        color = KidsGColors.OrangePrimary,
                        fontSize = 16.sp
                    )
                )
            } else {
                Text(text = "🐰", fontSize = 22.sp)
            }
        }

        // Live location & weather pill in center
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B))
                .clickable(onClick = onLocationClick)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "☁️", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "39 mins",
                    style = KidsGTypography.Caption.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentLocationName,
                    style = KidsGTypography.Caption.copy(
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "▾", color = Color.White, fontSize = 10.sp)
            }
        }

        // Right Action Buttons (Heart wishlist & Cart)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Heart wishlist button (Red heart badge)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF1F2))
                    .border(1.dp, Color(0xFFFECDD3), CircleShape)
                    .clickable(onClick = onWishlistClick),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "❤️", fontSize = 18.sp)
            }

            // Cart button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .clickable(onClick = onCartClick),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛍️", fontSize = 16.sp)
                if (cartItemCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(KidsGColors.OrangePrimary)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$cartItemCount",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 2: HERO BANNER WITH OVERLAPPING FLOATING SEARCH BAR (SLIKK INSPIRED)
// -----------------------------------------------------------------------------
@Composable
private fun HeroBannerWithSearch(
    onShopNow: () -> Unit,
    onSearchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Hero Card with asset-1 kids artwork
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFFFF7ED))
                .clickable(onClick = onShopNow)
        ) {
            // Background Artwork: kids pushing stationery cart with dynamic swirl
            KidsGResourceImage(
                resName = "kidsg_hero_drop",
                contentDescription = "New Drop - Kids Stationery",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            // Top gradient scrim for readability of "New DROP"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )

            // Top Left: Slikk-style "New DROP"
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 20.dp, top = 16.dp)
            ) {
                Text(
                    text = "New",
                    style = KidsGTypography.DisplayMedium.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Normal,
                        fontSize = 32.sp,
                        color = Color.White
                    )
                )
                Text(
                    text = "DROP",
                    style = KidsGTypography.DisplayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 44.sp,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                )
            }

            // Bottom gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
            )

            // Bottom Content: UPTO 70% OFF + BRAND + EXPLORE >
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "UPTO",
                        style = KidsGTypography.Caption.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                    Text(
                        text = "70% OFF",
                        style = KidsGTypography.DisplaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp,
                            lineHeight = 32.sp,
                            color = Color.White
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.25f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "EXPLORE",
                        style = KidsGTypography.Caption.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "›", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Pagination Dots
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.5f))
                )
            }
        }

        // Floating Search Bar overlapping the Hero Banner (matching Slikk Image 1)
        Box(
            modifier = Modifier
                .offset(y = (-24).dp)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .shadow(10.dp, RoundedCornerShape(28.dp), spotColor = Color(0x30000000))
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(28.dp))
                .clickable(onClick = onSearchClick)
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    Text(
                        text = "Search for \"Backpacks\", \"Notebooks\"...",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Vibrant Blue filter pill "Kids" (matching the blue "Men" button in Slikk Image 1)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF007AFF))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Kids",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 3: CATEGORY PILLS BAR
// -----------------------------------------------------------------------------
private data class TabPill(val id: String, val title: String)

@Composable
private fun CategoryPillsBar(
    tabs: List<TabPill>,
    selectedTabId: String,
    onTabSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-8).dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs, key = { it.id }) { tab ->
            val isSelected = tab.id == selectedTabId
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Color(0xFF0F172A) else Color.White)
                    .border(
                        1.dp,
                        if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onTabSelected(tab.id) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = tab.title,
                    color = if (isSelected) Color.White else Color(0xFF334155),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 4: "DEAL OF THE DAY" SECTION (Asset-backed)
// -----------------------------------------------------------------------------
@Composable
private fun DealOfTheDaySection(onDealClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        // Section Header: DEAL of the DAY
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DEAL",
                style = KidsGTypography.DisplayMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B),
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "of the",
                style = KidsGTypography.DisplayMedium.copy(
                    fontSize = 22.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF2563EB)
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "DAY",
                style = KidsGTypography.DisplayMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B),
                    letterSpacing = (-0.5).sp
                )
            )
        }

        // Horizontal Row of 3 Deal of the Day cards from user asset
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "deal_bags") {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x20000000))
                        .clickable { onDealClick("school_bags") }
                ) {
                    KidsGResourceImage(
                        resName = "deal_school_bags",
                        contentDescription = "School Bags Under ₹499",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item(key = "deal_stationery") {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x20000000))
                        .clickable { onDealClick("stationery_sets") }
                ) {
                    KidsGResourceImage(
                        resName = "deal_stationery_sets",
                        contentDescription = "Stationery Sets Under ₹299",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            item(key = "deal_bottles") {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x20000000))
                        .clickable { onDealClick("lunch_boxes") }
                ) {
                    KidsGResourceImage(
                        resName = "deal_water_bottles",
                        contentDescription = "Water Bottles Under ₹349",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 5: PROMO BANNER ("Steal deals")
// -----------------------------------------------------------------------------
@Composable
private fun StealDealsBanner(onBannerClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x20000000))
            .clickable(onClick = onBannerClick)
    ) {
        KidsGResourceImage(
            resName = "kidsg_steal_deals_banner",
            contentDescription = "Steal Deals - Everything They Need UP TO 70% OFF",
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp),
            contentScale = ContentScale.FillWidth
        )
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 6: "SHOP BY CATEGORY" GRID (12 Cards matching Image 5 & Image 2)
// -----------------------------------------------------------------------------
private data class CategoryCard12(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val illustrationType: String,
    val bgColor: Color,
    val accentColor: Color
)

@Composable
private fun ShopByCategoryGrid(
    categories: List<CategoryCard12>,
    onCategoryClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        // Section Header: SHOP BY Category (matching Slikk Image 2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SHOP BY",
                style = KidsGTypography.DisplayMedium.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B),
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Category",
                style = KidsGTypography.DisplayMedium.copy(
                    fontSize = 24.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF7C3AED)
                )
            )
        }

        // Full 12-category illustration grid from asset-2
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(22.dp))
                .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x18000000))
                .clickable { onCategoryClick("school_essentials") }
        ) {
            KidsGResourceImage(
                resName = "kidsg_category_grid",
                contentDescription = "Shop by Category Grid",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(370.dp),
                contentScale = ContentScale.FillWidth
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Horizontal Category Filter Pills for one-tap access
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(cat.bgColor)
                        .border(1.dp, cat.accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable { onCategoryClick(cat.id) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = cat.iconEmoji, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.title.replace("\n", " "),
                            style = KidsGTypography.Caption.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 7: FEATURED COLLECTIONS SECTION (asset-4)
// -----------------------------------------------------------------------------
private data class FeatureBannerData(
    val badgeTag: String,
    val badgeBg: Color,
    val title: String,
    val subtitle: String,
    val bgColor: Color,
    val borderColor: Color,
    val illustrationType: String,
    val categoryId: String
)

@Composable
private fun FeaturedCollectionsSection(
    banners: List<FeatureBannerData>,
    onBannerClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        // Section Header: FEATURED Collections
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FEATURED",
                style = KidsGTypography.DisplayMedium.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B),
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Collections",
                style = KidsGTypography.DisplayMedium.copy(
                    fontSize = 24.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFEC4899)
                )
            )
        }

        // Full 6-Card Feature Collections Graphic (asset-4)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(22.dp))
                .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x18000000))
                .clickable { onBannerClick("stationery_sets") }
        ) {
            KidsGResourceImage(
                resName = "kidsg_feature_collections",
                contentDescription = "Featured Collections",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.FillWidth
            )
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 8: "FRESH PICKS" SECTION (asset-5 & interactive carousel)
// -----------------------------------------------------------------------------
private data class FreshPickItem(
    val id: String,
    val badgeText: String,
    val badgeColor: Color,
    val title: String,
    val specs: String,
    val price: Double,
    val mrp: Double,
    val rating: Double,
    val reviews: String,
    val illustrationType: String,
    val imageUrl: String
)

@Composable
private fun FreshPicksSection(
    items: List<FreshPickItem>,
    cart: com.kidsg.domain.model.Cart,
    mockProducts: List<Product>,
    onAddToCart: (Product) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        // High-res Fresh Picks Graphic Banner from asset-5
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(22.dp))
                .shadow(6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x18000000))
                .clickable(onClick = onSeeAllClick)
        ) {
            KidsGResourceImage(
                resName = "kidsg_fresh_picks",
                contentDescription = "Fresh Picks Showcase",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.FillWidth
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Horizontal Product Carousel with interactive add-to-bag controls
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.id }) { item ->
                val inCartCount = cart.items.find { it.product.id == item.id }?.quantity ?: 0
                FreshPickCardItem(
                    item = item,
                    inCartCount = inCartCount,
                    onAdd = {
                        val prod = mockProducts.find { it.id == item.id }
                            ?: Product(
                                id = item.id,
                                name = item.title,
                                brand = "KidsG",
                                price = item.price,
                                mrp = item.mrp,
                                rating = item.rating,
                                reviewCount = 500,
                                categoryId = "notebooks",
                                description = item.specs,
                                imageUrl = item.imageUrl
                            )
                        onAddToCart(prod)
                    }
                )
            }
        }
    }
}

@Composable
private fun FreshPickCardItem(
    item: FreshPickItem,
    inCartCount: Int,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Image Container with Badge Tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFFFBF5)),
                contentAlignment = Alignment.Center
            ) {
                KidsGAsyncImage(
                    url = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = {
                        val emoji = when (item.illustrationType) {
                            "fresh_spiral" -> "🐰📓"
                            "fresh_pens" -> "🖊️🐻"
                            "fresh_bottle" -> "🚀🍶"
                            "fresh_lunchbox" -> "🍱🚀"
                            "fresh_geometry" -> "📐📏"
                            else -> "✏️"
                        }
                        Text(text = emoji, fontSize = 38.sp)
                    }
                )

                // Top Badge Tag (Bestseller / 20% OFF / Trending)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(item.badgeColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.badgeText,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Specs
            Text(
                text = item.title,
                style = KidsGTypography.BodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.specs,
                style = KidsGTypography.Caption.copy(
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Price & MRP
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "₹${item.price.toInt()}",
                    style = KidsGTypography.TitleMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                )
                if (item.mrp > item.price) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "₹${item.mrp.toInt()}",
                        style = KidsGTypography.Caption.copy(
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Rating & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "★", color = Color(0xFFF59E0B), fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${item.rating} (${item.reviews})",
                        style = KidsGTypography.Caption.copy(
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Add Button (+)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(KidsGColors.OrangePrimary)
                        .clickable(onClick = onAdd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (inCartCount > 0) "$inCartCount" else "+",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (inCartCount > 0) 12.sp else 18.sp
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 9: "BUILD YOUR SCHOOL KIT" BANNER
// -----------------------------------------------------------------------------
@Composable
private fun BuildKitBanner(onCreateKitClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFF2E6))
            .border(1.dp, Color(0xFFFFDAB9), RoundedCornerShape(20.dp))
            .padding(16.dp)
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
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎒", fontSize = 26.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Build Your School Kit",
                        style = KidsGTypography.TitleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Select class & school type for complete stationery list.",
                        style = KidsGTypography.Caption.copy(
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 13.sp
                        )
                    )
                }
            }

            Button(
                onClick = onCreateKitClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Create Kit →",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
