package com.kidsg.feature.home

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGIcons
import com.kidsg.core.designsystem.KidsGIllustration
import com.kidsg.core.designsystem.KidsGProductCard
import com.kidsg.core.designsystem.KidsGSearchBar
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.core.designsystem.parseColorHex
import com.kidsg.domain.model.IntentModeInfo
import com.kidsg.domain.model.IntentModeType
import com.kidsg.domain.model.Product
import com.kidsg.domain.model.Store
import com.kidsg.domain.repository.CartRepository
import com.kidsg.domain.repository.ProductRepository

/**
 * Hero Experience 2: "THE KIDSG DESK" Home Screen (Section 10 & 11)
 * Visually feels like an interactive student workspace:
 * - Dynamic Time-Aware Greeting
 * - "What do you need today?" Desk Search
 * - Stationery Desk Composition
 * - Intent Modes (Sticky Notes on desk)
 * - Nearby Partner Store Availability badge
 * - Popular Desk Supplies
 */
@Composable
fun KidsGDeskHomeScreen(
    productRepository: ProductRepository,
    cartRepository: CartRepository,
    userProfile: com.kidsg.domain.model.UserProfile? = null,
    onNavigateToDiscovery: (IntentModeType?) -> Unit,
    onNavigateToCategory: ((String) -> Unit)? = null,
    onNavigateToSearch: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onIncreaseQuantity: (Product) -> Unit,
    onDecreaseQuantity: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val cart by cartRepository.cartState.collectAsState()
    val popularProducts = androidx.compose.runtime.remember { kotlinx.coroutines.runBlocking { productRepository.getPopularProducts() } }
    val intentModes = androidx.compose.runtime.remember { kotlinx.coroutines.runBlocking { productRepository.getIntentModes() } }
    val store = cart.partnerStore

    KidsGIllustration.DeskBackground(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Desk Top Bar (Dynamic Student Greeting & Quick Categories)
            item {
                DeskHeader(
                    userProfile = userProfile,
                    store = store,
                    onSearchClick = onNavigateToSearch,
                    onCategoryClick = { catId ->
                        if (onNavigateToCategory != null) {
                            onNavigateToCategory(catId)
                        } else {
                            onNavigateToDiscovery(null)
                        }
                    }
                )
            }

            // 2. Desk Inspiration Banner (Matching Reference Screen 9)
            item {
                Spacer(modifier = Modifier.height(KidsGSpacing.md))
                DeskInspirationBanner(onExploreClick = { onNavigateToDiscovery(null) })
            }

            // 3. Urgent / Sticky Notes Intent Modes ("The KidsG Desk")
            item {
                Spacer(modifier = Modifier.height(KidsGSpacing.md))
                StickyNotesSection(
                    intentModes = intentModes,
                    onModeSelected = { mode -> onNavigateToDiscovery(mode.type) }
                )
            }

            // 4. Popular Desk Supplies (Classmate, Camlin, Doms)
            item {
                Spacer(modifier = Modifier.height(KidsGSpacing.lg))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = KidsGSpacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Popular Picks",
                            style = KidsGTypography.TitleLarge
                        )
                        Text(
                            text = "Essentials loved by students in ${userProfile?.schoolName?.takeIf { it.isNotBlank() } ?: "school"}",
                            style = KidsGTypography.BodySmall
                        )
                    }

                    Text(
                        text = "See All →",
                        style = KidsGTypography.TitleSmall.copy(color = KidsGColors.OrangePrimary),
                        modifier = Modifier.clickable { onNavigateToDiscovery(null) }
                    )
                }

                Spacer(modifier = Modifier.height(KidsGSpacing.md))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = KidsGSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.md)
                ) {
                    items(popularProducts.take(8)) { product ->
                        val itemInCart = cart.items.find { it.product.id == product.id }
                        val quantity = itemInCart?.quantity ?: 0

                        KidsGProductCard(
                            product = product,
                            cartQuantity = quantity,
                            onProductClick = onProductClick,
                            onAddToCart = onAddToCart,
                            onIncreaseQuantity = onIncreaseQuantity,
                            onDecreaseQuantity = onDecreaseQuantity,
                            modifier = Modifier.width(170.dp)
                        )
                    }
                }
            }

            // 5. Partner Store Dispatch Card
            item {
                Spacer(modifier = Modifier.height(KidsGSpacing.xl))
                store?.let {
                    NearbyStoreCard(store = it)
                }
            }
        }
    }
}

@Composable
fun DeskHeader(
    userProfile: com.kidsg.domain.model.UserProfile?,
    store: Store?,
    onSearchClick: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    val studentName = userProfile?.studentName?.takeIf { it.isNotBlank() }
        ?: userProfile?.name?.takeIf { it.isNotBlank() }
        ?: "Student"
    val studentGrade = userProfile?.studentGrade?.takeIf { it.isNotBlank() } ?: "Class 1"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle)
            .padding(KidsGSpacing.lg)
    ) {
        // Location row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KidsGIcons.Pencil(modifier = Modifier.size(16.dp), color = KidsGColors.OrangePrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Bengaluru",
                            style = KidsGTypography.TitleSmall.copy(fontSize = 13.sp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "▾", color = KidsGColors.OrangePrimary, fontSize = 11.sp)
                    }
                    Text(
                        text = "Delivering to $studentName's Desk",
                        style = KidsGTypography.Caption
                    )
                }
            }

            // Student avatar badge with child initial
            Box(
                modifier = Modifier
                    .clip(KidsGShapes.FullPill)
                    .background(KidsGColors.AccentYellowLight)
                    .border(1.dp, KidsGColors.AccentYellow, KidsGShapes.FullPill)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$studentName • $studentGrade",
                    style = KidsGTypography.Tag.copy(color = KidsGColors.BlackText)
                )
            }
        }

        Spacer(modifier = Modifier.height(KidsGSpacing.md))

        // Dynamic Student Greeting (Matching Reference Screen 9)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hi, $studentName! 👋",
                    style = KidsGTypography.DisplayMedium.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Ready to learn today?",
                    style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextSecondary)
                )
            }

            // Circular Student Avatar Graphic
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(KidsGShapes.FullPill)
                    .background(KidsGColors.AccentYellow)
                    .border(2.dp, KidsGColors.OrangePrimary, KidsGShapes.FullPill),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = studentName.firstOrNull()?.uppercase() ?: "S",
                    style = KidsGTypography.TitleLarge.copy(color = KidsGColors.White, fontWeight = FontWeight.Black)
                )
            }
        }

        Spacer(modifier = Modifier.height(KidsGSpacing.md))

        // Interactive Search Bar (Matching Reference Screen 9)
        KidsGSearchBar(
            query = "",
            onQueryChange = {},
            isClickableOnly = true,
            onClick = onSearchClick,
            placeholder = "Search for products..."
        )

        Spacer(modifier = Modifier.height(KidsGSpacing.md))

        // Quick Category Row (Matching Reference Screen 9: Books, Writing, Art & Craft, Bags)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickCategoryChip(icon = "📚", title = "Books", onClick = { onCategoryClick("notebooks") })
            QuickCategoryChip(icon = "✏️", title = "Writing", onClick = { onCategoryClick("pens_pencils") })
            QuickCategoryChip(icon = "🎨", title = "Art & Craft", onClick = { onCategoryClick("art_craft") })
            QuickCategoryChip(icon = "🎒", title = "Bags", onClick = { onCategoryClick("school_bags") })
        }
    }
}

@Composable
fun QuickCategoryChip(
    icon: String,
    title: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(KidsGShapes.MediumRounded)
            .clickable(onClick = onClick)
            .padding(KidsGSpacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(KidsGShapes.CardRounded)
                .background(KidsGColors.SurfaceElevated)
                .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.CardRounded),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = KidsGTypography.Caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        )
    }
}

/**
 * Sticky Notes Intent Modes on the KidsG Desk (Section 12)
 * Tactile stationery composition with slight rotations and pastel tints.
 */
@Composable
fun StickyNotesSection(
    intentModes: List<IntentModeInfo>,
    onModeSelected: (IntentModeInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "What's the mission?",
                style = KidsGTypography.TitleMedium
            )
            Text(
                text = "5 Intent Modes",
                style = KidsGTypography.Caption
            )
        }

        Spacer(modifier = Modifier.height(KidsGSpacing.sm))

        // First Row: School Mode & Exam Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.md)
        ) {
            intentModes.getOrNull(0)?.let { schoolMode ->
                StickyNoteCard(
                    mode = schoolMode,
                    rotationDegrees = -1.5f,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeSelected(schoolMode) }
                )
            }

            intentModes.getOrNull(1)?.let { examMode ->
                StickyNoteCard(
                    mode = examMode,
                    rotationDegrees = 1.8f,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeSelected(examMode) }
                )
            }
        }

        Spacer(modifier = Modifier.height(KidsGSpacing.md))

        // Second Row: Create Mode & New Term
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.md)
        ) {
            intentModes.getOrNull(2)?.let { createMode ->
                StickyNoteCard(
                    mode = createMode,
                    rotationDegrees = 1.2f,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeSelected(createMode) }
                )
            }

            intentModes.getOrNull(3)?.let { newTermMode ->
                StickyNoteCard(
                    mode = newTermMode,
                    rotationDegrees = -1.2f,
                    modifier = Modifier.weight(1f),
                    onClick = { onModeSelected(newTermMode) }
                )
            }
        }

        Spacer(modifier = Modifier.height(KidsGSpacing.md))

        // Prominent Hero: OOPS MODE! (Forgot something urgently?)
        intentModes.find { it.type == IntentModeType.OOPS }?.let { oopsMode ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KidsGShapes.Medium)
                    .background(KidsGColors.AccentPinkLight)
                    .border(1.5.dp, KidsGColors.AccentPink, KidsGShapes.Medium)
                    .clickable { onModeSelected(oopsMode) }
                    .padding(KidsGSpacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🚨", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(KidsGSpacing.md))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "OOPS — I forgot something!",
                                    style = KidsGTypography.TitleSmall.copy(
                                        color = Color(0xFF9D174D),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(KidsGShapes.FullPill)
                                        .background(KidsGColors.AccentPink)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "12 MINS",
                                        style = KidsGTypography.Caption.copy(
                                            color = KidsGColors.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "Forgot pen, notebook, or chart paper for tomorrow?",
                                style = KidsGTypography.BodySmall.copy(color = Color(0xFF831843))
                            )
                        }
                    }

                    Text(text = "KidsG it! →", style = KidsGTypography.Tag.copy(color = Color(0xFF9D174D)))
                }
            }
        }
    }
}

@Composable
fun StickyNoteCard(
    mode: IntentModeInfo,
    rotationDegrees: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = try {
        Color(parseColorHex(mode.accentColorHex)).copy(alpha = 0.22f)
    } catch (_: Exception) {
        KidsGColors.AccentYellowLight
    }

    Box(
        modifier = modifier
            .rotate(rotationDegrees)
            .clip(KidsGShapes.StickyNote)
            .background(bg)
            .border(1.dp, Color(parseColorHex(mode.accentColorHex)).copy(alpha = 0.4f), KidsGShapes.StickyNote)
            .clickable(onClick = onClick)
            .padding(KidsGSpacing.md)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Little tape pin
                Box(
                    modifier = Modifier
                        .size(16.dp, 6.dp)
                        .clip(KidsGShapes.ExtraSmall)
                        .background(KidsGColors.BorderStrong.copy(alpha = 0.5f))
                )

                Box(
                    modifier = Modifier
                        .clip(KidsGShapes.FullPill)
                        .background(KidsGColors.White)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = mode.badge,
                        style = KidsGTypography.Tag.copy(fontSize = 9.sp, color = KidsGColors.TextPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mode.title,
                style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = mode.subtitle,
                style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextSecondary, fontSize = 11.sp),
                maxLines = 1
            )
        }
    }
}

@Composable
fun DeskInspirationBanner(onExploreClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg)
            .clip(KidsGShapes.DeskCard)
            .background(KidsGColors.OrangePrimary)
            .clickable(onClick = onExploreClick)
            .padding(KidsGSpacing.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Big ideas start with small supplies.",
                    style = KidsGTypography.TitleMedium.copy(
                        color = KidsGColors.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Trusted brands delivered to your doorstep in 15 mins.",
                    style = KidsGTypography.BodySmall.copy(color = KidsGColors.White.copy(alpha = 0.9f))
                )
            }

            Spacer(modifier = Modifier.width(KidsGSpacing.md))

            KidsGIllustration.Mascot(modifier = Modifier.size(68.dp))
        }
    }
}

@Composable
fun NearbyStoreCard(store: Store) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KidsGSpacing.lg)
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.Medium)
            .padding(KidsGSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(KidsGShapes.Small)
                    .background(KidsGColors.OrangeLight),
                contentAlignment = Alignment.Center
            ) {
                KidsGIcons.StoreFront(modifier = Modifier.size(24.dp), color = KidsGColors.OrangePrimary)
            }

            Spacer(modifier = Modifier.width(KidsGSpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = store.name,
                        style = KidsGTypography.TitleSmall.copy(fontSize = 13.sp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(KidsGShapes.ExtraSmall)
                            .background(KidsGColors.AccentMintLight)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "★ ${store.rating}",
                            style = KidsGTypography.Caption.copy(fontSize = 9.sp, color = Color(0xFF065F46))
                        )
                    }
                }
                Text(
                    text = "${store.distanceKm} km away • Preparing in ${store.prepTimeMinutes} mins",
                    style = KidsGTypography.Caption
                )
            }

            Text(
                text = "OPEN NOW",
                style = KidsGTypography.Tag.copy(color = KidsGColors.Success, fontSize = 9.sp)
            )
        }
    }
}
