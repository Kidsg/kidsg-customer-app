package com.kidsg.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGTypography
import kotlinx.coroutines.delay

/**
 * KidsG Splash Screen
 * Plays the user-provided generated splash video full-screen with an instant "Skip" overlay.
 * Automatically advances to Onboarding / Get Started when the video finishes or upon skip.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // 5.5-second auto-advance timeout (between 5-6 seconds)
    LaunchedEffect(Unit) {
        delay(5500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Fullscreen Native Video Player
        SplashVideoPlayer(
            modifier = Modifier.fillMaxSize(),
            onVideoFinished = onSplashFinished
        )

        // Overlay: Top bar with brand pill and Skip button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp, vertical = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Kids",
                        style = KidsGTypography.BodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KidsGColors.White,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "G",
                        style = KidsGTypography.BodyMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = KidsGColors.OrangePrimary,
                            fontSize = 15.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Skip Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { onSplashFinished() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Skip ➔",
                    style = KidsGTypography.BodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = KidsGColors.White,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}
