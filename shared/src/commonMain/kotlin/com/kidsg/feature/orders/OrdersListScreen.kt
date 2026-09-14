package com.kidsg.feature.orders

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography

data class DisplayOrderItem(
    val id: String,
    val itemsSummary: String,
    val amount: String,
    val dateText: String,
    val status: String,
    val icon: String
)

/**
 * My Orders List Screen (Reference Screen 18)
 */
@Composable
fun OrdersListScreen(
    onBack: () -> Unit,
    onOrderClick: (orderId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Processing", "Shipped", "Delivered")

    val mockOrders = remember {
        listOf(
            DisplayOrderItem(
                id = "KG12345678",
                itemsSummary = "2 Items",
                amount = "₹290",
                dateText = "Delivered • 12 Aug 2024",
                status = "Delivered",
                icon = "📓"
            ),
            DisplayOrderItem(
                id = "KG12345677",
                itemsSummary = "3 Items",
                amount = "₹550",
                dateText = "Shipped • 10 Aug 2024",
                status = "Shipped",
                icon = "🎒"
            ),
            DisplayOrderItem(
                id = "KG12345676",
                itemsSummary = "1 Item",
                amount = "₹100",
                dateText = "Processing • 8 Aug 2024",
                status = "Processing",
                icon = "✏️"
            ),
            DisplayOrderItem(
                id = "KG12345675",
                itemsSummary = "3 Items",
                amount = "₹420",
                dateText = "Delivered • 5 Aug 2024",
                status = "Delivered",
                icon = "🎨"
            )
        )
    }

    val filteredOrders = remember(selectedFilter) {
        if (selectedFilter == "All") mockOrders else mockOrders.filter { it.status == selectedFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KidsGColors.BackgroundPrimary)
    ) {
        // Top Bar
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
                    .clip(CircleShape)
                    .background(KidsGColors.SurfaceElevated)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "←", style = KidsGTypography.TitleSmall)
            }

            Spacer(modifier = Modifier.width(KidsGSpacing.md))

            Text(
                text = "My Orders",
                style = KidsGTypography.TitleLarge
            )
        }

        // Filter Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(KidsGColors.White)
                .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(KidsGSpacing.sm)
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(KidsGShapes.FullPill)
                        .background(if (isSelected) KidsGColors.OrangePrimary else KidsGColors.SurfaceElevated)
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = filter,
                        style = KidsGTypography.Caption.copy(
                            color = if (isSelected) KidsGColors.White else KidsGColors.TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(KidsGSpacing.sm))

        // Orders List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md),
            verticalArrangement = Arrangement.spacedBy(KidsGSpacing.md)
        ) {
            items(filteredOrders) { order ->
                OrderCard(order = order, onClick = { onOrderClick(order.id) })
            }
        }
    }
}

@Composable
fun OrderCard(
    order: DisplayOrderItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KidsGShapes.CardRounded)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.CardRounded)
            .clickable(onClick = onClick)
            .padding(KidsGSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Item Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KidsGColors.SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = order.icon, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(KidsGSpacing.md))

                Column {
                    Text(
                        text = "#${order.id}",
                        style = KidsGTypography.TitleSmall.copy(fontSize = 14.sp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${order.itemsSummary} • ${order.amount}",
                        style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = order.dateText,
                        style = KidsGTypography.Caption.copy(
                            color = when (order.status) {
                                "Delivered" -> Color(0xFF10B981)
                                "Processing" -> KidsGColors.OrangePrimary
                                else -> Color(0xFF38BDF8)
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Text(
                text = "›",
                fontSize = 24.sp,
                color = KidsGColors.TextMuted
            )
        }
    }
}
