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
import com.kidsg.domain.model.UserProfile

/**
 * My Profile Screen (Reference Screen 20)
 */
@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    onNavigateToAddresses: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val studentName = userProfile?.studentName?.takeIf { it.isNotBlank() }
        ?: userProfile?.name?.takeIf { it.isNotBlank() }
        ?: "Student"
    val studentGrade = userProfile?.studentGrade?.takeIf { it.isNotBlank() } ?: "Class 1"
    val school = userProfile?.schoolName?.takeIf { it.isNotBlank() } ?: "KidsG Partner School"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KidsGColors.BackgroundPrimary)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KidsGColors.White)
                .padding(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Profile",
                style = KidsGTypography.TitleLarge
            )
        }

        // Profile Avatar & Info Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KidsGColors.White)
                .padding(vertical = KidsGSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(88.dp),
                contentAlignment = Alignment.Center
            ) {
                // Large Avatar Circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFBAE6FD))
                        .border(2.dp, KidsGColors.OrangePrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👦",
                        fontSize = 42.sp
                    )
                }

                // Camera Edit Badge
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(KidsGColors.OrangePrimary)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📷", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(KidsGSpacing.md))

            Text(
                text = studentName,
                style = KidsGTypography.TitleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$studentGrade • $school",
                style = KidsGTypography.BodySmall.copy(color = KidsGColors.TextSecondary)
            )
        }

        Spacer(modifier = Modifier.height(KidsGSpacing.md))

        // Options List (Reference Screen 20)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KidsGColors.White)
        ) {
            ProfileMenuItem(
                icon = "📍",
                title = "My Addresses",
                onClick = onNavigateToAddresses
            )
            ProfileMenuItem(
                icon = "📦",
                title = "My Orders",
                onClick = onNavigateToOrders
            )
            ProfileMenuItem(
                icon = "❤️",
                title = "My Wishlisted Items",
                onClick = { /* Wishlist */ }
            )
            ProfileMenuItem(
                icon = "🔔",
                title = "Notifications",
                onClick = { /* Notifications */ }
            )
            ProfileMenuItem(
                icon = "❓",
                title = "Help & Support",
                onClick = onNavigateToHelp
            )
            ProfileMenuItem(
                icon = "⚙️",
                title = "Settings",
                onClick = { /* Settings */ }
            )
            ProfileMenuItem(
                icon = "🚪",
                title = "Logout",
                titleColor = Color(0xFFEF4444),
                onClick = onLogout
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: String,
    title: String,
    titleColor: Color = KidsGColors.BlackText,
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
                style = KidsGTypography.BodyLarge.copy(
                    fontSize = 15.sp,
                    color = titleColor,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Text(
            text = "›",
            fontSize = 22.sp,
            color = KidsGColors.TextMuted
        )
    }
}
