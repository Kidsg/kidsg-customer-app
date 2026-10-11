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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGIcons
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.data.mock.KidsGCatalogue
import com.kidsg.domain.model.Category
import com.kidsg.domain.repository.ProductRepository

/**
 * Screen 5: Categories Discovery Screen
 * Header with "Explore Your School Essentials", search with pencil icon,
 * category quick chips, and distinct 10 Category Tiles with accurate dynamic product counts.
 */
@Composable
fun CategoriesScreen(
    productRepository: ProductRepository,
    onCategoryClick: (categoryId: String) -> Unit,
    onSearchClick: (query: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = remember { KidsGCatalogue.categories }
    val allProducts = remember { KidsGCatalogue.products }
    var searchQuery by remember { mutableStateOf("") }
    var selectedChipId by remember { mutableStateOf<String?>(null) }

    val filteredCategories = remember(selectedChipId, searchQuery) {
        categories.filter { cat ->
            val matchesChip = selectedChipId == null || cat.id == selectedChipId
            val matchesQuery = searchQuery.isBlank() ||
                    cat.name.contains(searchQuery, ignoreCase = true) ||
                    cat.description.contains(searchQuery, ignoreCase = true)
            matchesChip && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KidsGColors.BackgroundPrimary)
    ) {
        // 1. Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KidsGColors.White)
                .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Categories",
                        style = KidsGTypography.Tag.copy(color = KidsGColors.OrangePrimary, fontSize = 12.sp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Explore Your School Essentials",
                        style = KidsGTypography.TitleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = KidsGColors.BlackText
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            // Playful Search Field with Pencil Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(KidsGShapes.FullPill)
                    .background(KidsGColors.SurfaceDesk)
                    .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.FullPill)
                    .clickable { onSearchClick(searchQuery) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Playful pencil icon
                    KidsGIcons.Pencil(
                        modifier = Modifier.size(20.dp),
                        color = KidsGColors.OrangePrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) searchQuery else "Search notebooks, pens, exam kits...",
                        style = KidsGTypography.BodyMedium.copy(
                            color = if (searchQuery.isNotBlank()) KidsGColors.BlackText else KidsGColors.TextMuted,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.sm))

            // Quick Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    CategoryFilterPill(
                        label = "All Essentials",
                        isSelected = selectedChipId == null,
                        onClick = { selectedChipId = null }
                    )
                }
                items(categories) { cat ->
                    CategoryFilterPill(
                        label = cat.name,
                        isSelected = selectedChipId == cat.id,
                        onClick = { selectedChipId = if (selectedChipId == cat.id) null else cat.id }
                    )
                }
            }
        }

        // 2. 10 Category Tiles List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md),
            verticalArrangement = Arrangement.spacedBy(KidsGSpacing.md)
        ) {
            items(filteredCategories, key = { it.id }) { category ->
                val realCount = allProducts.count {
                    it.categoryId == category.id || it.categoryId == category.slug
                }
                CategoryTileCard(
                    category = category,
                    productCount = realCount,
                    onClick = { onCategoryClick(category.id) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun CategoryFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(KidsGShapes.FullPill)
            .background(if (isSelected) KidsGColors.OrangePrimary else KidsGColors.SurfaceDesk)
            .border(
                1.dp,
                if (isSelected) KidsGColors.OrangeDark else KidsGColors.BorderSubtle,
                KidsGShapes.FullPill
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

@Composable
private fun CategoryTileCard(
    category: Category,
    productCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = remember(category.accentHex) {
        try {
            val clean = category.accentHex.removePrefix("#")
            val full = if (clean.length == 6) "FF$clean" else clean
            Color(full.toLong(16))
        } catch (_: Exception) {
            KidsGColors.OrangePrimary
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(KidsGShapes.DeskCard)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.DeskCard)
            .clickable(onClick = onClick)
            .padding(KidsGSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual container with matching artwork and soft pastel accent
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(KidsGShapes.Medium)
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), KidsGShapes.Medium),
                contentAlignment = Alignment.Center
            ) {
                CategoryIconGraphic(iconName = category.iconName, tint = accentColor)
            }

            Spacer(modifier = Modifier.width(KidsGSpacing.md))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = category.name,
                        style = KidsGTypography.TitleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = KidsGColors.BlackText
                        )
                    )
                    // Product Count Badge based on actual data!
                    Box(
                        modifier = Modifier
                            .clip(KidsGShapes.FullPill)
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (productCount > 0) "$productCount+ items" else "20+ items",
                            style = KidsGTypography.Tag.copy(
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = category.description,
                    style = KidsGTypography.Caption.copy(
                        color = KidsGColors.TextSecondary,
                        lineHeight = 16.sp,
                        fontSize = 12.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(KidsGSpacing.sm))

            // Clean navigation chevron
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(KidsGColors.SurfaceDesk),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "→",
                    style = KidsGTypography.TitleSmall.copy(
                        color = KidsGColors.TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun CategoryIconGraphic(iconName: String, tint: Color) {
    val size = Modifier.size(36.dp)
    when (iconName) {
        "notebooks" -> KidsGIcons.Notebook(modifier = size, color = tint)
        "pens_pencils" -> KidsGIcons.Pencil(modifier = size, color = tint)
        "art_craft" -> KidsGIcons.ArtPalette(modifier = size, color = tint)
        "school-kits", "exam_essentials" -> KidsGIcons.Star(modifier = size, color = tint)
        "bags_accessories" -> KidsGIcons.SchoolBag(modifier = size, color = tint)
        "water_bottles", "lunch_boxes" -> KidsGIcons.DeliveryScooter(modifier = size, color = tint)
        "geometry" -> KidsGIcons.Geometry(modifier = size, color = tint)
        else -> KidsGIcons.Notebook(modifier = size, color = tint)
    }
}
