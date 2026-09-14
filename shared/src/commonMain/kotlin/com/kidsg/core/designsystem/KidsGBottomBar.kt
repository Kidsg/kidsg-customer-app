package com.kidsg.core.designsystem

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class KidsGNavTab(val label: String) {
    HOME("Desk"),
    CATEGORIES("Categories"),
    BAG("Bag"),
    ORDERS("Orders"),
    PROFILE("Profile")
}

/**
 * KidsG Bottom Navigation Bar
 * High-clarity, elevated white bar with active orange indicator and live bag badge.
 */
@Composable
fun KidsGBottomBar(
    currentTab: KidsGNavTab,
    onTabSelected: (KidsGNavTab) -> Unit,
    bagItemCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            KidsGNavTab.entries.forEach { tab ->
                val isSelected = tab == currentTab
                val tint = if (isSelected) KidsGColors.OrangePrimary else KidsGColors.TextMuted

                Box(
                    modifier = Modifier
                        .clip(KidsGShapes.Small)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            when (tab) {
                                KidsGNavTab.HOME -> KidsGIcons.Pencil(
                                    modifier = Modifier.size(22.dp),
                                    color = tint
                                )
                                KidsGNavTab.CATEGORIES -> KidsGIcons.Notebook(
                                    modifier = Modifier.size(22.dp),
                                    color = tint
                                )
                                KidsGNavTab.BAG -> KidsGIcons.SchoolBag(
                                    modifier = Modifier.size(22.dp),
                                    color = tint
                                )
                                KidsGNavTab.ORDERS -> KidsGIcons.DeliveryScooter(
                                    modifier = Modifier.size(22.dp),
                                    color = tint
                                )
                                KidsGNavTab.PROFILE -> KidsGProfileNavIcon(
                                    modifier = Modifier.size(22.dp),
                                    color = tint
                                )
                            }

                            // Live badge on School Bag
                            if (tab == KidsGNavTab.BAG && bagItemCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = 6.dp, y = (-4).dp)
                                        .clip(KidsGShapes.FullPill)
                                        .background(KidsGColors.OrangePrimary)
                                        .padding(horizontal = 5.dp, vertical = 1.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$bagItemCount",
                                        style = KidsGTypography.Caption.copy(
                                            color = KidsGColors.White,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = tab.label,
                            style = KidsGTypography.Caption.copy(
                                color = tint,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KidsGProfileNavIcon(modifier: Modifier = Modifier, color: Color) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Head
        drawCircle(color = color, radius = w * 0.22f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.32f))
        // Shoulders
        val shoulders = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.2f, h * 0.85f)
            cubicTo(w * 0.2f, h * 0.60f, w * 0.8f, h * 0.60f, w * 0.8f, h * 0.85f)
            close()
        }
        drawPath(shoulders, color = color)
    }
}
