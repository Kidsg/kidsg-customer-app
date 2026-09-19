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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import com.kidsg.core.designsystem.KidsGAsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.data.mock.KidsGMockData
import com.kidsg.domain.model.IntentModeType
import com.kidsg.domain.model.Product
import com.kidsg.domain.model.UserProfile
import com.kidsg.domain.repository.CartRepository
import com.kidsg.domain.repository.ProductRepository

/**
 * KidsG Home Screen — Redesigned according to the exact LittleWrite / KidsG aesthetic:
 * - Brand logo with "KidsG" + "STATIONERY FOR BRIGHT MINDS", Live location pill, Wishlist, Cart badge, Profile
 * - Hero headline: "Make Every Little Idea Beautiful." with smiling stationery cup illustration
 * - Search bar with filter slider button
 * - 6 colorful Category icon circles: Writing Tools, Drawing & Art, Notebooks, School Essentials, Bags & Accessories, Creative Kits
 * - "BACK TO SCHOOL" hero banner with "Be Ready for New Stories" and [ Shop Now -> ]
 * - "Curated for Kids" cards carousel: Little Creators, Parent Picks, Back to School Edit
 * - "Popular with Kids" product list: Premium Colour Pencil Set, Gel Pen Set, Spiral Notebook, Art Set
 * - "Build Your School Kit" interactive card with [ Create My Kit -> ]
 */
@Composable
fun KidsGDeskHomeScreen(
    productRepository: ProductRepository,
    cartRepository: CartRepository,
    userProfile: UserProfile? = null,
    currentLocationName: String = "HSR Layout, Bengaluru",
    onLocationClick: () -> Unit = {},
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
    val mockProducts = remember { KidsGMockData.products }

    val studentName = userProfile?.studentName?.takeIf { it.isNotBlank() }
        ?: userProfile?.name?.takeIf { it.isNotBlank() }
        ?: "Student"

    // 6 Visual Categories matching Image 4
    val categories = remember {
        listOf(
            CategoryItem("writing", "Writing\nTools", "✏️", Color(0xFFFFEDD5), Color(0xFFEA580C)),
            CategoryItem("art", "Drawing &\nArt", "🎨", Color(0xFFFEF3C7), Color(0xFFD97706)),
            CategoryItem("notebooks", "Notebooks\n ", "📓", Color(0xFFD1FAE5), Color(0xFF059669)),
            CategoryItem("essentials", "School\nEssentials", "🎒", Color(0xFFE0F2FE), Color(0xFF0284C7)),
            CategoryItem("bags", "Bags &\nAccessories", "👜", Color(0xFFEDE9FE), Color(0xFF7C3AED)),
            CategoryItem("kits", "Creative\nKits", "🎁", Color(0xFFFEE2E2), Color(0xFFDC2626))
        )
    }

    // Curated collections matching Image 4
    val curatedCollections = remember {
        listOf(
            CuratedCard(
                title = "Little Creators",
                description = "Art supplies for big imaginations.",
                bgColor = Color(0xFFFFF1EB),
                tagColor = Color(0xFFFF7A00),
                emoji = "✏️🎨"
            ),
            CuratedCard(
                title = "Parent Picks",
                description = "Trusted by parents. Loved by kids.",
                bgColor = Color(0xFFEDFBF4),
                tagColor = Color(0xFF10B981),
                emoji = "🐻📓"
            ),
            CuratedCard(
                title = "Back to School Edit",
                description = "Everything they need, all in one place.",
                bgColor = Color(0xFFEFF6FF),
                tagColor = Color(0xFF3B82F6),
                emoji = "🦖🎒"
            )
        )
    }

    // Popular items matching Image 4
    val popularList = remember {
        listOf(
            PopularProductItem(
                id = "prod_color_pencil",
                name = "Premium Colour Pencil Set",
                specs = "12 pcs | Non-toxic",
                rating = 4.8,
                reviews = 124,
                price = 299.0,
                emoji = "🖍️",
                bgColor = Color(0xFFFFF4ED),
                imageUrl = "https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=500&auto=format&fit=crop&q=80"
            ),
            PopularProductItem(
                id = "prod_gel_pen",
                name = "Gel Pen Set",
                specs = "5 pcs | Smooth Writing",
                rating = 4.7,
                reviews = 98,
                price = 199.0,
                emoji = "🖊️",
                bgColor = Color(0xFFF0FDF4),
                imageUrl = "https://images.unsplash.com/photo-1585336261026-407a50ee7b2c?w=500&auto=format&fit=crop&q=80"
            ),
            PopularProductItem(
                id = "prod_notebook",
                name = "Spiral Notebook",
                specs = "A5 | 100 Pages",
                rating = 4.6,
                reviews = 87,
                price = 149.0,
                emoji = "🚀",
                bgColor = Color(0xFFEFF6FF),
                imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&auto=format&fit=crop&q=80"
            ),
            PopularProductItem(
                id = "prod_art_set",
                name = "Art Set",
                specs = "24 pcs | Sketch, Paint, Draw",
                rating = 4.9,
                reviews = 156,
                price = 499.0,
                emoji = "🎨",
                bgColor = Color(0xFFFAF5FF),
                imageUrl = "https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=500&auto=format&fit=crop&q=80"
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF5))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. TOP BRAND BAR & LIVE LOCATION
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    // Live Location Pill (Fetched live on app enter)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFFF7ED))
                            .border(1.dp, Color(0xFFFFD1A4), RoundedCornerShape(20.dp))
                            .clickable(onClick = onLocationClick)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📍", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentLocationName,
                            style = KidsGTypography.Caption.copy(
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "▾", color = KidsGColors.OrangePrimary, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Brand Row (KidsG + Wishlist, Cart Badge, Profile)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // KidsG Brand Identity
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(KidsGColors.OrangePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "✏️", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Kids",
                                        style = KidsGTypography.TitleLarge.copy(
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF111111)
                                        )
                                    )
                                    Text(
                                        text = "G",
                                        style = KidsGTypography.TitleLarge.copy(
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = KidsGColors.OrangePrimary
                                        )
                                    )
                                }
                                Text(
                                    text = "STATIONERY FOR BRIGHT MINDS",
                                    style = KidsGTypography.Caption.copy(
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B),
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                        }

                        // Right action buttons (Wishlist, Cart, Profile)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Wishlist
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .shadow(2.dp, CircleShape)
                                    .clickable { onNavigateToDiscovery(null) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "♡", fontSize = 18.sp, color = Color(0xFF334155))
                            }

                            // School Bag / Cart
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .shadow(2.dp, CircleShape)
                                    .clickable { onNavigateToDiscovery(null) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🛍️", fontSize = 16.sp)
                                if (cart.itemCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(KidsGColors.OrangePrimary)
                                            .align(Alignment.TopEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${cart.itemCount}",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Profile
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFEDD5))
                                    .border(1.5.dp, KidsGColors.OrangePrimary, CircleShape)
                                    .clickable { /* Profile */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = studentName.firstOrNull()?.uppercase() ?: "S",
                                    style = KidsGTypography.Caption.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KidsGColors.OrangePrimary,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 2. HERO HEADLINE & SMILING STATIONERY POT
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Make Every\nLittle Idea",
                            style = KidsGTypography.DisplayMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 34.sp,
                                color = Color(0xFF1E293B)
                            )
                        )
                        Text(
                            text = "Beautiful.",
                            style = KidsGTypography.DisplayMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFE57A22)
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Premium stationery for curious minds.",
                            style = KidsGTypography.BodySmall.copy(color = Color(0xFF64748B), fontSize = 13.sp)
                        )
                    }

                    // Smiling stationery cup illustration
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFFF7ED))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "✏️📐🖍️", fontSize = 20.sp)
                            Text(text = "🏺😊", fontSize = 34.sp)
                        }
                    }
                }
            }

            // 3. SEARCH BAR (With Filter Slider Button)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                        .clickable { onNavigateToSearch() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔍", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Search for stationery...",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        }

                        // Filter Slider Icon
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF8FAFC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⚙️", fontSize = 14.sp)
                        }
                    }
                }
            }

            // 4. CATEGORIES (6 Circular Icons)
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(categories) { cat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onNavigateToCategory?.invoke(cat.id) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(cat.bgColor)
                                    .border(1.dp, cat.borderColor.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = cat.emoji, fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = cat.name,
                                style = KidsGTypography.Caption.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp
                                )
                            )
                        }
                    }
                }
            }

            // 5. HERO BANNER: "BACK TO SCHOOL - Be Ready for New Stories"
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFFFEF3E2))
                        .border(1.dp, Color(0xFFFFDAB0), RoundedCornerShape(22.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BACK TO SCHOOL",
                                style = KidsGTypography.Caption.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC26118),
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Be Ready\nfor New Stories",
                                style = KidsGTypography.TitleLarge.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 24.sp,
                                    color = Color(0xFF1E293B)
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Shop the latest stationery collection for a brighter tomorrow.",
                                style = KidsGTypography.BodySmall.copy(
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onNavigateToDiscovery(null) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Shop Now →",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Banner Graphic (Bag + Water bottle + books)
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFDE68A).copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "🎒🍶", fontSize = 34.sp)
                                Text(text = "📚✈️", fontSize = 24.sp)
                            }
                        }
                    }
                }

                // Pagination Dots
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(KidsGColors.OrangePrimary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCBD5E1))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCBD5E1))
                    )
                }
            }

            // 6. "Curated for Kids" SECTION (Horizontal scroll)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Curated for Kids",
                            style = KidsGTypography.TitleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E293B)
                            )
                        )
                        Text(
                            text = "View All →",
                            style = KidsGTypography.Caption.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFC26118),
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.clickable { onNavigateToDiscovery(null) }
                        )
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(curatedCollections) { curated ->
                            Box(
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(curated.bgColor)
                                    .border(1.dp, curated.tagColor.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                                    .clickable { onNavigateToDiscovery(null) }
                                    .padding(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = curated.title,
                                            style = KidsGTypography.TitleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF1E293B)
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = curated.description,
                                            style = KidsGTypography.Caption.copy(
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B),
                                                lineHeight = 13.sp
                                            )
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(curated.tagColor.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "→", fontSize = 12.sp, color = curated.tagColor)
                                        }
                                        Text(text = curated.emoji, fontSize = 28.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. "Popular with Kids" SECTION (Grid of 4 items matching Image 4)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Popular with Kids",
                            style = KidsGTypography.TitleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E293B)
                            )
                        )
                        Text(
                            text = "View All →",
                            style = KidsGTypography.Caption.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFC26118),
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.clickable { onNavigateToDiscovery(null) }
                        )
                    }

                    // 2x2 Product Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            HomeProductCard(
                                item = popularList[0],
                                inCartCount = cart.items.find { it.product.id == popularList[0].id }?.quantity ?: 0,
                                onAdd = {
                                    val prod = mockProducts.find { it.id == popularList[0].id }
                                        ?: mockProducts.firstOrNull()
                                        ?: Product(
                                            id = popularList[0].id,
                                            name = popularList[0].name,
                                            brand = "KidsG",
                                            price = popularList[0].price,
                                            mrp = popularList[0].price + 50,
                                            rating = popularList[0].rating,
                                            reviewCount = popularList[0].reviews,
                                            categoryId = "writing",
                                            description = popularList[0].specs,
                                            imageUrl = ""
                                        )
                                    onAddToCart(prod)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            HomeProductCard(
                                item = popularList[1],
                                inCartCount = cart.items.find { it.product.id == popularList[1].id }?.quantity ?: 0,
                                onAdd = {
                                    val prod = mockProducts.find { it.id == popularList[1].id }
                                        ?: mockProducts.getOrNull(1)
                                        ?: Product(
                                            id = popularList[1].id,
                                            name = popularList[1].name,
                                            brand = "KidsG",
                                            price = popularList[1].price,
                                            mrp = popularList[1].price + 30,
                                            rating = popularList[1].rating,
                                            reviewCount = popularList[1].reviews,
                                            categoryId = "writing",
                                            description = popularList[1].specs,
                                            imageUrl = ""
                                        )
                                    onAddToCart(prod)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            HomeProductCard(
                                item = popularList[2],
                                inCartCount = cart.items.find { it.product.id == popularList[2].id }?.quantity ?: 0,
                                onAdd = {
                                    val prod = mockProducts.find { it.id == popularList[2].id }
                                        ?: mockProducts.getOrNull(2)
                                        ?: Product(
                                            id = popularList[2].id,
                                            name = popularList[2].name,
                                            brand = "KidsG",
                                            price = popularList[2].price,
                                            mrp = popularList[2].price + 40,
                                            rating = popularList[2].rating,
                                            reviewCount = popularList[2].reviews,
                                            categoryId = "notebooks",
                                            description = popularList[2].specs,
                                            imageUrl = ""
                                        )
                                    onAddToCart(prod)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            HomeProductCard(
                                item = popularList[3],
                                inCartCount = cart.items.find { it.product.id == popularList[3].id }?.quantity ?: 0,
                                onAdd = {
                                    val prod = mockProducts.find { it.id == popularList[3].id }
                                        ?: mockProducts.getOrNull(3)
                                        ?: Product(
                                            id = popularList[3].id,
                                            name = popularList[3].name,
                                            brand = "KidsG",
                                            price = popularList[3].price,
                                            mrp = popularList[3].price + 100,
                                            rating = popularList[3].rating,
                                            reviewCount = popularList[3].reviews,
                                            categoryId = "art",
                                            description = popularList[3].specs,
                                            imageUrl = ""
                                        )
                                    onAddToCart(prod)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 8. "Build Your School Kit" BANNER CARD
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
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
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🎒", fontSize = 28.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Build Your School Kit",
                                    style = KidsGTypography.TitleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Select class, school type and get a complete stationery list in one go.",
                                    style = KidsGTypography.Caption.copy(
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 13.sp
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { onNavigateToDiscovery(null) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Create My Kit →",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Data models for categories and curated collections
private data class CategoryItem(
    val id: String,
    val name: String,
    val emoji: String,
    val bgColor: Color,
    val borderColor: Color
)

private data class CuratedCard(
    val title: String,
    val description: String,
    val bgColor: Color,
    val tagColor: Color,
    val emoji: String
)

private data class PopularProductItem(
    val id: String,
    val name: String,
    val specs: String,
    val rating: Double,
    val reviews: Int,
    val price: Double,
    val emoji: String,
    val bgColor: Color,
    val imageUrl: String
)

@Composable
private fun HomeProductCard(
    item: PopularProductItem,
    inCartCount: Int,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Product Image Container with Wishlist Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.bgColor),
                contentAlignment = Alignment.Center
            ) {
                KidsGAsyncImage(
                    url = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentScale = ContentScale.Crop,
                    placeholder = {
                        Text(text = item.emoji, fontSize = 42.sp)
                    }
                )

                Text(
                    text = "♡",
                    fontSize = 16.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Specs
            Text(
                text = item.name,
                style = KidsGTypography.BodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.specs,
                style = KidsGTypography.Caption.copy(
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "★", color = Color(0xFFF59E0B), fontSize = 12.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${item.rating} (${item.reviews})",
                    style = KidsGTypography.Caption.copy(
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${item.price.toInt()}",
                    style = KidsGTypography.TitleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E293B)
                    )
                )

                // Add button (+) or in-cart indicator
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(KidsGColors.OrangePrimary)
                        .clickable { onAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (inCartCount > 0) "$inCartCount" else "+",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (inCartCount > 0) 13.sp else 18.sp
                    )
                }
            }
        }
    }
}
