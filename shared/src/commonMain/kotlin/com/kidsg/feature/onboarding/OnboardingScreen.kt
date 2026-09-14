package com.kidsg.feature.onboarding

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGIllustration
import com.kidsg.core.designsystem.KidsGTypography

/**
 * KidsG "Get Started" First-Install Onboarding Screen
 * Welcomes new parents and students with core value propositions and emotion stickers.
 */
@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onExploreGuest: () -> Unit
) {
    val scrollState = rememberScrollState()

    KidsGIllustration.DeskBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                // KidsG Mascot / Brand Reveal
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    KidsGIllustration.Mascot(modifier = Modifier.size(110.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Brand Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Kids",
                        style = KidsGTypography.DisplayLarge.copy(
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = KidsGColors.BlackText
                        )
                    )
                    Text(
                        text = "G",
                        style = KidsGTypography.DisplayLarge.copy(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = KidsGColors.OrangePrimary
                        )
                    )
                }

                Text(
                    text = "Small Supplies. Big Futures.",
                    style = KidsGTypography.TitleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = KidsGColors.TextSecondary,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Emotion sticker banner: "Study • Create • Grow"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(KidsGColors.AccentYellow.copy(alpha = 0.25f))
                        .border(1.5.dp, KidsGColors.AccentYellow, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "✨ Study • Create • Grow ✨",
                        style = KidsGTypography.BodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8A5B00),
                            fontSize = 13.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 3 Core Value Proposition Cards
                ValuePropCard(
                    iconText = "⚡",
                    title = "15-Minute Stationery Delivery",
                    description = "Prepared by trusted stationery stores in your neighbourhood."
                )

                Spacer(modifier = Modifier.height(12.dp))

                ValuePropCard(
                    iconText = "🎒",
                    title = "Curated for Class 1st to 12th",
                    description = "Notebooks, geometry boxes, graph pads, and exam project kits."
                )

                Spacer(modifier = Modifier.height(12.dp))

                ValuePropCard(
                    iconText = "🏷️",
                    title = "100% Genuine School Brands",
                    description = "Classmate, DOMS, Faber-Castell, Apsara, Hauser & Camlin."
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom CTA Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KidsGColors.OrangePrimary)
                ) {
                    Text(
                        text = "Get Started",
                        style = KidsGTypography.TitleMedium.copy(
                            color = KidsGColors.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onExploreGuest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KidsGColors.TextSecondary)
                ) {
                    Text(
                        text = "Explore Desk First",
                        style = KidsGTypography.BodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = KidsGColors.TextSecondary,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ValuePropCard(
    iconText: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KidsGColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(KidsGColors.Surface),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iconText, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = KidsGTypography.TitleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.BlackText,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = description,
                    style = KidsGTypography.BodySmall.copy(
                        color = KidsGColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
