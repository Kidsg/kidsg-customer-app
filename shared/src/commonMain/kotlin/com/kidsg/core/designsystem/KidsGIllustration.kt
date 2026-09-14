package com.kidsg.core.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * KidsG Tactile Stationery Desk Background & Mascot Illustrations
 */
object KidsGIllustration {

    /**
     * Subtle student desk graph/grid pattern background
     */
    @Composable
    fun DeskBackground(
        modifier: Modifier = Modifier,
        gridColor: Color = KidsGColors.BorderSubtle.copy(alpha = 0.45f),
        content: @Composable () -> Unit
    ) {
        Box(modifier = modifier.fillMaxSize().background(KidsGColors.Background)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 28.dp.toPx()
                val width = size.width
                val height = size.height

                // Subtle graph dots
                var x = step
                while (x < width) {
                    var y = step
                    while (y < height) {
                        drawCircle(
                            color = gridColor,
                            radius = 1.dp.toPx(),
                            center = Offset(x, y)
                        )
                        y += step
                    }
                    x += step
                }
            }
            content()
        }
    }

    /**
     * Official KidsG Friendly Mascot
     * Modern, smiling, energetic character holding a signature KidsG pencil.
     */
    @Composable
    fun Mascot(
        modifier: Modifier = Modifier.size(100.dp),
        isCheering: Boolean = false
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "mascotFloat")
        val floatOffset by infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "float"
        )

        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            val cy = (h * 0.48f) + floatOffset

            // Shadow under mascot
            drawOval(
                color = KidsGColors.ShadowColor,
                topLeft = Offset(w * 0.25f, h * 0.88f),
                size = Size(w * 0.5f, h * 0.08f)
            )

            // Orange Backpack peek behind
            drawRoundRect(
                color = KidsGColors.OrangePrimary,
                topLeft = Offset(w * 0.22f, cy - h * 0.05f),
                size = Size(w * 0.22f, h * 0.35f),
                cornerRadius = CornerRadius(10.dp.toPx())
            )
            drawRoundRect(
                color = KidsGColors.OrangeDark,
                topLeft = Offset(w * 0.22f, cy - h * 0.05f),
                size = Size(w * 0.22f, h * 0.35f),
                cornerRadius = CornerRadius(10.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Mascot Body (Soft warm skin / modern rounded face)
            val faceColor = Color(0xFFFFDFBA)
            drawCircle(
                color = faceColor,
                radius = w * 0.26f,
                center = Offset(w * 0.5f, cy)
            )

            // Messy friendly hair
            val hairPath = Path().apply {
                moveTo(w * 0.26f, cy - h * 0.12f)
                cubicTo(w * 0.30f, cy - h * 0.34f, w * 0.50f, cy - h * 0.36f, w * 0.74f, cy - h * 0.14f)
                cubicTo(w * 0.65f, cy - h * 0.26f, w * 0.45f, cy - h * 0.28f, w * 0.32f, cy - h * 0.16f)
                close()
            }
            drawPath(hairPath, color = Color(0xFF3E2723))

            // Eyes (sparkling, friendly)
            drawCircle(color = KidsGColors.BlackText, radius = w * 0.035f, center = Offset(w * 0.42f, cy - h * 0.02f))
            drawCircle(color = KidsGColors.White, radius = w * 0.012f, center = Offset(w * 0.43f, cy - h * 0.03f))

            drawCircle(color = KidsGColors.BlackText, radius = w * 0.035f, center = Offset(w * 0.58f, cy - h * 0.02f))
            drawCircle(color = KidsGColors.White, radius = w * 0.012f, center = Offset(w * 0.59f, cy - h * 0.03f))

            // Rosy cheeks
            drawCircle(color = KidsGColors.AccentPink.copy(alpha = 0.5f), radius = w * 0.04f, center = Offset(w * 0.35f, cy + h * 0.06f))
            drawCircle(color = KidsGColors.AccentPink.copy(alpha = 0.5f), radius = w * 0.04f, center = Offset(w * 0.65f, cy + h * 0.06f))

            // Cheerful Smile
            val smile = Path().apply {
                moveTo(w * 0.44f, cy + h * 0.08f)
                cubicTo(w * 0.48f, cy + h * 0.16f, w * 0.52f, cy + h * 0.16f, w * 0.56f, cy + h * 0.08f)
            }
            drawPath(smile, color = KidsGColors.BlackText, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

            // Signature Giant Pencil held in arm
            val pencilPath = Path().apply {
                moveTo(w * 0.70f, cy + h * 0.30f)
                lineTo(w * 0.82f, cy - h * 0.22f)
                lineTo(w * 0.88f, cy - h * 0.20f)
                lineTo(w * 0.76f, cy + h * 0.32f)
                close()
            }
            drawPath(pencilPath, color = KidsGColors.AccentYellow)
            drawPath(pencilPath, color = KidsGColors.BlackText, style = Stroke(width = 1.5.dp.toPx()))

            // Pencil lead tip
            val leadPath = Path().apply {
                moveTo(w * 0.82f, cy - h * 0.22f)
                lineTo(w * 0.85f, cy - h * 0.32f)
                lineTo(w * 0.88f, cy - h * 0.20f)
                close()
            }
            drawPath(leadPath, color = Color(0xFFFFE082))
            drawCircle(color = KidsGColors.BlackText, radius = w * 0.018f, center = Offset(w * 0.85f, cy - h * 0.32f))

            // Eraser on back of pencil
            drawRoundRect(
                color = KidsGColors.AccentPink,
                topLeft = Offset(w * 0.68f, cy + h * 0.30f),
                size = Size(w * 0.10f, h * 0.07f),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }
    }
}
