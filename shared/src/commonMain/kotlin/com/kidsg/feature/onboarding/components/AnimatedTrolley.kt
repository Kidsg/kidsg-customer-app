package com.kidsg.feature.onboarding.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kidsg.core.designsystem.KidsGColors

/**
 * Reusable Animated Trolley Composable with rotating wheels, filled stationery products,
 * and settling item physics.
 */
@Composable
fun StationeryTrolley(
    modifier: Modifier = Modifier.size(160.dp),
    wheelRotationDegrees: Float = 0f,
    isFilled: Boolean = true
) {
    // Micro bounce for stationery items sitting in trolley
    val infiniteTransition = rememberInfiniteTransition(label = "trolleyItems")
    val itemJiggle by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "itemJiggle"
    )

    Box(
        modifier = modifier.semantics {
            contentDescription = "Stationery Trolley with essentials"
        }
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val w = size.width
            val h = size.height
            val cy = h * 0.48f

            // 1. Trolley Ground Shadow
            drawOval(
                color = KidsGColors.ShadowColor,
                topLeft = Offset(w * 0.18f, h * 0.86f),
                size = Size(w * 0.62f, h * 0.08f)
            )

            // 2. Filled Stationery Items inside Cart (drawn behind front cart grid)
            if (isFilled) {
                // Pink Notebook
                drawRoundRect(
                    color = KidsGColors.AccentPink,
                    topLeft = Offset(w * 0.28f, cy - h * 0.22f + itemJiggle),
                    size = Size(w * 0.22f, h * 0.26f),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                // Cute face on notebook
                drawCircle(color = KidsGColors.BlackText, radius = 2.dp.toPx(), center = Offset(w * 0.35f, cy - h * 0.12f + itemJiggle))
                drawCircle(color = KidsGColors.BlackText, radius = 2.dp.toPx(), center = Offset(w * 0.43f, cy - h * 0.12f + itemJiggle))

                // Yellow Pencil sticking out
                val pencilPath = Path().apply {
                    moveTo(w * 0.48f, cy - h * 0.08f + itemJiggle)
                    lineTo(w * 0.54f, cy - h * 0.28f + itemJiggle)
                    lineTo(w * 0.60f, cy - h * 0.26f + itemJiggle)
                    lineTo(w * 0.54f, cy - h * 0.06f + itemJiggle)
                    close()
                }
                drawPath(pencilPath, color = KidsGColors.AccentYellow)

                // Blue School Bag tucked behind
                drawRoundRect(
                    color = Color(0xFF3B82F6),
                    topLeft = Offset(w * 0.56f, cy - h * 0.18f - itemJiggle),
                    size = Size(w * 0.20f, h * 0.22f),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )

                // Green Geometry Box
                drawRoundRect(
                    color = KidsGColors.AccentMint,
                    topLeft = Offset(w * 0.34f, cy - h * 0.06f),
                    size = Size(w * 0.32f, h * 0.10f),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                // Eraser & Crayons
                drawRoundRect(
                    color = KidsGColors.AccentSkyBlue,
                    topLeft = Offset(w * 0.26f, cy - h * 0.04f + itemJiggle),
                    size = Size(w * 0.12f, h * 0.08f),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
            }

            // 3. Trolley Metal Basket Grid Structure
            val cartFront = Path().apply {
                moveTo(w * 0.20f, cy - h * 0.05f)
                lineTo(w * 0.78f, cy - h * 0.05f)
                lineTo(w * 0.70f, cy + h * 0.22f)
                lineTo(w * 0.28f, cy + h * 0.22f)
                close()
            }
            drawPath(cartFront, color = KidsGColors.OrangePrimary.copy(alpha = 0.15f))
            drawPath(cartFront, color = KidsGColors.OrangePrimary, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))

            // Cart Inner Wire Grid Lines
            // Horizontal wires
            drawLine(
                color = KidsGColors.OrangePrimary.copy(alpha = 0.6f),
                start = Offset(w * 0.22f, cy + h * 0.04f),
                end = Offset(w * 0.75f, cy + h * 0.04f),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = KidsGColors.OrangePrimary.copy(alpha = 0.6f),
                start = Offset(w * 0.25f, cy + h * 0.13f),
                end = Offset(w * 0.72f, cy + h * 0.13f),
                strokeWidth = 2.dp.toPx()
            )
            // Vertical wires
            drawLine(color = KidsGColors.OrangePrimary.copy(alpha = 0.5f), start = Offset(w * 0.36f, cy - h * 0.04f), end = Offset(w * 0.40f, cy + h * 0.21f), strokeWidth = 1.5.dp.toPx())
            drawLine(color = KidsGColors.OrangePrimary.copy(alpha = 0.5f), start = Offset(w * 0.50f, cy - h * 0.04f), end = Offset(w * 0.52f, cy + h * 0.21f), strokeWidth = 1.5.dp.toPx())
            drawLine(color = KidsGColors.OrangePrimary.copy(alpha = 0.5f), start = Offset(w * 0.64f, cy - h * 0.04f), end = Offset(w * 0.62f, cy + h * 0.21f), strokeWidth = 1.5.dp.toPx())

            // 4. Cart Push Handle (Left side)
            val handlePath = Path().apply {
                moveTo(w * 0.20f, cy - h * 0.05f)
                lineTo(w * 0.12f, cy - h * 0.12f)
                lineTo(w * 0.12f, cy - h * 0.08f)
            }
            drawPath(handlePath, color = KidsGColors.BlackText, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))

            // 5. Wheels & Base Frame
            // Base frame rod
            drawLine(
                color = KidsGColors.BlackText,
                start = Offset(w * 0.28f, cy + h * 0.22f),
                end = Offset(w * 0.70f, cy + h * 0.22f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Front & Back Wheel Assemblies
            val wheel1Center = Offset(w * 0.35f, cy + h * 0.28f)
            val wheel2Center = Offset(w * 0.65f, cy + h * 0.28f)
            val wheelRadius = w * 0.055f

            // Draw Wheel 1
            rotate(degrees = wheelRotationDegrees, pivot = wheel1Center) {
                drawCircle(color = KidsGColors.BlackText, radius = wheelRadius, center = wheel1Center)
                drawCircle(color = KidsGColors.White, radius = wheelRadius * 0.5f, center = wheel1Center)
                // Wheel spokes for visible rotation
                drawLine(
                    color = KidsGColors.BlackText,
                    start = Offset(wheel1Center.x - wheelRadius * 0.4f, wheel1Center.y),
                    end = Offset(wheel1Center.x + wheelRadius * 0.4f, wheel1Center.y),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // Draw Wheel 2
            rotate(degrees = wheelRotationDegrees, pivot = wheel2Center) {
                drawCircle(color = KidsGColors.BlackText, radius = wheelRadius, center = wheel2Center)
                drawCircle(color = KidsGColors.White, radius = wheelRadius * 0.5f, center = wheel2Center)
                // Wheel spokes
                drawLine(
                    color = KidsGColors.BlackText,
                    start = Offset(wheel2Center.x - wheelRadius * 0.4f, wheel2Center.y),
                    end = Offset(wheel2Center.x + wheelRadius * 0.4f, wheel2Center.y),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }
    }
}
