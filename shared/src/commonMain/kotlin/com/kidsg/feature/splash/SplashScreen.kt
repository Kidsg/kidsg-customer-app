package com.kidsg.feature.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGIcons
import com.kidsg.core.designsystem.KidsGIllustration
import com.kidsg.core.designsystem.KidsGMotion
import com.kidsg.core.designsystem.KidsGTypography
import kotlinx.coroutines.delay

/**
 * Hero Experience 1: Branded Animated KidsG Launch Sequence (Section 8)
 * 1. Clean white desk background.
 * 2. Stationery sparkles appear.
 * 3. A pencil enters and draws the signature KidsG orange curve.
 * 4. "KidsG" brand typography resolves.
 * 5. Mascot makes a playful cheerful bounce.
 * 6. Smoothly hands over to the KidsG Desk.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val pencilProgress = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val mascotScale = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Pencil draws signature curve
        pencilProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )

        // Step 2: Logo and tagline appear
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        )
        taglineAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 350, easing = LinearEasing)
        )

        // Step 3: Mascot pops in playfully
        mascotScale.animateTo(
            targetValue = 1f,
            animationSpec = KidsGMotion.BouncySpring
        )

        // Hold briefly for maximum 1.8s experience
        delay(400)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KidsGColors.White),
        contentAlignment = Alignment.Center
    ) {
        // Floating stationery decor
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Sparkle 1 (Yellow)
            drawCircle(
                color = KidsGColors.AccentYellow.copy(alpha = 0.5f),
                radius = 4.dp.toPx(),
                center = Offset(w * 0.22f, h * 0.32f)
            )
            // Sparkle 2 (Mint)
            drawCircle(
                color = KidsGColors.AccentMint.copy(alpha = 0.6f),
                radius = 5.dp.toPx(),
                center = Offset(w * 0.78f, h * 0.28f)
            )
            // Sparkle 3 (Sky Blue)
            drawCircle(
                color = KidsGColors.AccentSkyBlue.copy(alpha = 0.5f),
                radius = 4.dp.toPx(),
                center = Offset(w * 0.82f, h * 0.68f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Drawn signature curve Canvas
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val p = pencilProgress.value

                    if (p > 0f) {
                        val curve = Path().apply {
                            moveTo(w * 0.1f, h * 0.75f)
                            cubicTo(
                                w * 0.35f, h * 0.15f,
                                w * 0.65f, h * 0.15f,
                                w * (0.1f + p * 0.8f), h * (0.75f - (1f - p) * 0.3f)
                            )
                        }
                        drawPath(
                            path = curve,
                            color = KidsGColors.OrangePrimary,
                            style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            // KidsG Brand Text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(logoAlpha.value)
            ) {
                Text(
                    text = "Kids",
                    style = KidsGTypography.DisplayLarge.copy(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = KidsGColors.BlackText
                    )
                )
                Text(
                    text = "G",
                    style = KidsGTypography.DisplayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = KidsGColors.OrangePrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tagline: "Small Supplies. Big Futures."
            Text(
                text = "Small Supplies. Big Futures.",
                style = KidsGTypography.TitleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = KidsGColors.TextSecondary,
                    fontSize = 14.sp
                ),
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Playful Mascot with signature pencil
            Box(
                modifier = Modifier.scale(mascotScale.value),
                contentAlignment = Alignment.Center
            ) {
                KidsGIllustration.Mascot(modifier = Modifier.size(96.dp))
            }
        }
    }
}
