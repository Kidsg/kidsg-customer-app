package com.kidsg.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import com.kidsg.core.designsystem.KidsGPrimaryButton
import com.kidsg.core.designsystem.KidsGSearchBar
import com.kidsg.core.designsystem.KidsGShapes
import com.kidsg.core.designsystem.KidsGSpacing
import com.kidsg.core.designsystem.KidsGTypography

/**
 * Help & Support Screen (Reference Screen 21)
 */
@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    onTrackOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

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
                text = "Help & Support",
                style = KidsGTypography.TitleLarge
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(KidsGSpacing.lg)
        ) {
            Text(
                text = "How can we help?",
                style = KidsGTypography.TitleLarge.copy(fontSize = 20.sp)
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            KidsGSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search for help..."
            )

            Spacer(modifier = Modifier.height(KidsGSpacing.lg))

            // Help Topics Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KidsGShapes.CardRounded)
                    .background(KidsGColors.White)
                    .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.CardRounded)
            ) {
                Column {
                    HelpTopicItem(icon = "📦", title = "Track My Order", onClick = onTrackOrder)
                    HelpTopicItem(icon = "🔄", title = "Returns & Refunds", onClick = {})
                    HelpTopicItem(icon = "💳", title = "Payment Issues", onClick = {})
                    HelpTopicItem(icon = "ℹ️", title = "Product Information", onClick = {})
                    HelpTopicItem(icon = "✉️", title = "Contact Us", onClick = {})
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.xl))

            // Support Agent Contact Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KidsGShapes.CardRounded)
                    .background(KidsGColors.AccentYellowLight)
                    .border(1.dp, KidsGColors.AccentYellow, KidsGShapes.CardRounded)
                    .padding(KidsGSpacing.lg)
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
                        Text(text = "🎧", fontSize = 36.sp)
                        Spacer(modifier = Modifier.width(KidsGSpacing.md))
                        Column {
                            Text(
                                text = "Need more help?",
                                style = KidsGTypography.TitleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "We're here for you 24/7",
                                style = KidsGTypography.Caption.copy(color = KidsGColors.TextSecondary)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(KidsGShapes.FullPill)
                            .background(KidsGColors.OrangePrimary)
                            .clickable { /* Chat with support */ }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Chat Now",
                            style = KidsGTypography.Caption.copy(
                                color = KidsGColors.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HelpTopicItem(
    icon: String,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = KidsGSpacing.lg, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(KidsGSpacing.md))
            Text(
                text = title,
                style = KidsGTypography.BodyLarge.copy(fontSize = 15.sp)
            )
        }

        Text(
            text = "›",
            fontSize = 22.sp,
            color = KidsGColors.TextMuted
        )
    }
}
