package com.kidsg.feature.onboarding.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.kidsg.core.designsystem.KidsGColors

/**
 * Floating Stationery background elements (Pencil, Eraser, Ruler, Notebook, Crayons, Paper Plane)
 * with gentle floating animations and micro-rotation.
 */
@Composable
fun FloatingStationeryBackground(
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floatBg")

    // Oscillating floating offset
    val floatY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val floatRotate by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatRotate"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Floating Pencil (Top Left)
            val pencilPos = Offset(w * 0.14f, h * 0.16f + floatY)
            rotate(degrees = -25f + floatRotate, pivot = pencilPos) {
                // Yellow Body
                drawRoundRect(
                    color = KidsGColors.AccentYellow,
                    topLeft = Offset(pencilPos.x - 20.dp.toPx(), pencilPos.y - 6.dp.toPx()),
                    size = Size(40.dp.toPx(), 12.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                // Pink Eraser
                drawRoundRect(
                    color = KidsGColors.AccentPink,
                    topLeft = Offset(pencilPos.x - 26.dp.toPx(), pencilPos.y - 6.dp.toPx()),
                    size = Size(6.dp.toPx(), 12.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
                // Lead Tip
                val tip = Path().apply {
                    moveTo(pencilPos.x + 20.dp.toPx(), pencilPos.y - 6.dp.toPx())
                    lineTo(pencilPos.x + 30.dp.toPx(), pencilPos.y)
                    lineTo(pencilPos.x + 20.dp.toPx(), pencilPos.y + 6.dp.toPx())
                    close()
                }
                drawPath(tip, color = Color(0xFFFFDFBA))
                drawCircle(color = KidsGColors.BlackText, radius = 2.dp.toPx(), center = Offset(pencilPos.x + 29.dp.toPx(), pencilPos.y))
            }

            // 2. Floating Paper Plane (Top Right)
            val planePos = Offset(w * 0.82f, h * 0.14f - floatY)
            rotate(degrees = 15f - floatRotate, pivot = planePos) {
                val plane = Path().apply {
                    moveTo(planePos.x + 20.dp.toPx(), planePos.y - 12.dp.toPx())
                    lineTo(planePos.x - 20.dp.toPx(), planePos.y + 12.dp.toPx())
                    lineTo(planePos.x - 4.dp.toPx(), planePos.y + 2.dp.toPx())
                    lineTo(planePos.x + 4.dp.toPx(), planePos.y + 14.dp.toPx())
                    close()
                }
                drawPath(plane, color = KidsGColors.AccentSkyBlue)
                drawPath(plane, color = KidsGColors.BlackText, style = Stroke(width = 1.5.dp.toPx()))
            }

            // 3. Floating Eraser (Middle Right)
            val eraserPos = Offset(w * 0.88f, h * 0.42f + floatY)
            rotate(degrees = 12f + floatRotate, pivot = eraserPos) {
                drawRoundRect(
                    color = KidsGColors.AccentPink,
                    topLeft = Offset(eraserPos.x - 16.dp.toPx(), eraserPos.y - 10.dp.toPx()),
                    size = Size(32.dp.toPx(), 20.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                // Paper sleeve
                drawRoundRect(
                    color = KidsGColors.White,
                    topLeft = Offset(eraserPos.x - 8.dp.toPx(), eraserPos.y - 10.dp.toPx()),
                    size = Size(16.dp.toPx(), 20.dp.toPx())
                )
            }

            // 4. Floating Ruler (Middle Left)
            val rulerPos = Offset(w * 0.08f, h * 0.45f - floatY)
            rotate(degrees = -40f - floatRotate, pivot = rulerPos) {
                drawRoundRect(
                    color = KidsGColors.AccentMint,
                    topLeft = Offset(rulerPos.x - 25.dp.toPx(), rulerPos.y - 7.dp.toPx()),
                    size = Size(50.dp.toPx(), 14.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
                // Ruler ticks
                for (i in 0..5) {
                    val tickX = rulerPos.x - 20.dp.toPx() + (i * 8.dp.toPx())
                    drawLine(
                        color = KidsGColors.BlackText,
                        start = Offset(tickX, rulerPos.y - 7.dp.toPx()),
                        end = Offset(tickX, rulerPos.y - 2.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // 5. Floating Stars & Sparkles (Bottom area)
            drawCircle(color = KidsGColors.AccentYellow, radius = 5.dp.toPx(), center = Offset(w * 0.22f, h * 0.72f + floatY))
            drawCircle(color = KidsGColors.AccentPink, radius = 4.dp.toPx(), center = Offset(w * 0.78f, h * 0.68f - floatY))
            drawCircle(color = KidsGColors.AccentSkyBlue, radius = 5.dp.toPx(), center = Offset(w * 0.84f, h * 0.78f + floatY))
        }
    }
}

/**
 * Animated Pencil Transition line component.
 * Draws an orange stroke across screen from left to right, expanding into KidsG logo reveal.
 */
@Composable
fun PencilTransitionOverlay(
    modifier: Modifier = Modifier.fillMaxSize(),
    progress: Float = 1.0f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val lineY = h * 0.50f
        val startX = w * 0.05f
        val currentX = startX + progress * (w * 0.90f)

        // Draw drawn Orange stroke
        if (progress > 0.02f) {
            val strokePath = Path().apply {
                moveTo(startX, lineY)
                quadraticTo(startX + currentX * 0.5f, lineY - 12.dp.toPx(), currentX, lineY)
            }
            drawPath(
                path = strokePath,
                color = KidsGColors.OrangePrimary,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Pencil Tip drawing the line
            val pencilTip = Offset(currentX, lineY)
            val tipPath = Path().apply {
                moveTo(pencilTip.x, pencilTip.y)
                lineTo(pencilTip.x + 22.dp.toPx(), pencilTip.y - 18.dp.toPx())
                lineTo(pencilTip.x + 32.dp.toPx(), pencilTip.y - 10.dp.toPx())
                close()
            }
            drawPath(tipPath, color = KidsGColors.AccentYellow)
            drawCircle(color = KidsGColors.BlackText, radius = 3.dp.toPx(), center = pencilTip)
        }
    }
}
