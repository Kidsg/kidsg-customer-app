package com.kidsg.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.domain.model.Category

/**
 * KidsG Stationery Wall Category Card
 * Distinct visual stationery object with curated accent background and item count badge.
 */
@Composable
fun KidsGCategoryCard(
    category: Category,
    onClick: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = try {
        Color(parseColorHex(category.accentHex))
    } catch (_: Exception) {
        KidsGColors.OrangeLight
    }

    Box(
        modifier = modifier
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.Medium)
            .clickable { onClick(category) }
            .padding(KidsGSpacing.md),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Visual stationery container
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(KidsGShapes.Small)
                    .background(accentColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                CategoryVisual(iconName = category.iconName, tint = accentColor)
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.sm))

            Text(
                text = category.name,
                style = KidsGTypography.TitleSmall.copy(fontSize = 13.sp),
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            if (category.itemCount > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${category.itemCount}+ items",
                    style = KidsGTypography.Caption.copy(fontSize = 10.sp, color = KidsGColors.TextMuted)
                )
            }
        }
    }
}

@Composable
fun CategoryVisual(iconName: String, tint: Color, modifier: Modifier = Modifier.size(34.dp)) {
    when (iconName) {
        "notebooks" -> KidsGIcons.Notebook(modifier = modifier, color = KidsGColors.OrangePrimary)
        "pens_pencils" -> KidsGIcons.Pencil(modifier = modifier, color = KidsGColors.OrangePrimary)
        "art_craft" -> KidsGIcons.ArtPalette(modifier = modifier, color = KidsGColors.AccentPurple)
        "geometry" -> KidsGIcons.Geometry(modifier = modifier, color = KidsGColors.AccentSkyBlue)
        "exam_essentials" -> KidsGIcons.Star(modifier = modifier, color = KidsGColors.AccentYellow)
        "bags_accessories" -> KidsGIcons.SchoolBag(modifier = modifier, color = KidsGColors.OrangePrimary)
        else -> KidsGIcons.SchoolBag(modifier = modifier, color = KidsGColors.OrangePrimary)
    }
}

fun parseColorHex(hex: String): Long {
    val clean = hex.removePrefix("#")
    val full = if (clean.length == 6) "FF$clean" else clean
    return full.toLong(16)
}
