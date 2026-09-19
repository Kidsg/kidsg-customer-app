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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.feature.onboarding.CharacterPose
import com.kidsg.feature.onboarding.CharacterType
import com.kidsg.feature.onboarding.PoseType
import kotlin.math.sin

/**
 * Reusable Animated Character Composable for KidsG Onboarding.
 * Supports Boy and Girl characters with smooth pose animations (IDLE, WALK, EXCITED, PUSH_TROLLEY).
 */
@Composable
fun KidsGCharacter(
    characterType: CharacterType,
    pose: CharacterPose,
    modifier: Modifier = Modifier.size(160.dp)
) {
    val description = if (characterType == CharacterType.BOY) "Boy character" else "Girl character"

    // Infinite bobbing/breathing transition for idle state
    val infiniteTransition = rememberInfiniteTransition(label = "charIdle")
    val idleFloat by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing"
    )

    // Calculate vertical bobbing and arm/leg angles based on pose
    val (bobbingY, leftLegAngle, rightLegAngle, leftArmAngle, rightArmAngle) = when (pose.type) {
        PoseType.IDLE -> {
            Tuple5(idleFloat, 0f, 0f, 0f, 0f)
        }
        PoseType.WALK, PoseType.PUSH_TROLLEY -> {
            val stepCycle = (pose.walkFrame % 4)
            val stepPhase = stepCycle * (Math.PI / 2)
            val verticalBob = (sin(stepPhase * 2) * 4f).toFloat()
            val legSwing = (sin(stepPhase) * 22f).toFloat()
            val armSwing = (-sin(stepPhase) * 25f).toFloat()
            Tuple5(verticalBob, legSwing, -legSwing, armSwing, -armSwing)
        }
        PoseType.EXCITED -> {
            val cheerBob = (sin(idleFloat * 2) * 6f).toFloat()
            Tuple5(cheerBob, -10f, 10f, -140f, -140f) // Arms raised up high celebrating!
        }
    }

    Box(
        modifier = modifier.semantics {
            contentDescription = description
        }
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val w = size.width
            val h = size.height
            val cy = (h * 0.42f) + bobbingY

            // 1. Shadow underneath character
            val shadowWidth = w * (if (pose.type == PoseType.EXCITED) 0.40f else 0.48f)
            drawOval(
                color = KidsGColors.ShadowColor,
                topLeft = Offset((w - shadowWidth) / 2f, h * 0.88f),
                size = Size(shadowWidth, h * 0.08f)
            )

            val isBoy = characterType == CharacterType.BOY

            // Colors
            val skinColor = Color(0xFFFFDFBA)
            val hairColor = if (isBoy) Color(0xFF332014) else Color(0xFF2C1810)
            val topColor = if (isBoy) KidsGColors.OrangePrimary else KidsGColors.AccentPink
            val pantsColor = if (isBoy) Color(0xFF1E3A8A) else KidsGColors.AccentPink.copy(alpha = 0.8f)
            val shoeColor = Color(0xFF111111)

            // Flip horizontally if facing left
            val flipFactor = if (pose.isFacingRight) 1f else -1f

            // 2. Backpack (behind body)
            drawRoundRect(
                color = if (isBoy) Color(0xFF3B82F6) else Color(0xFFF59E0B),
                topLeft = Offset(w * 0.28f, cy - h * 0.02f),
                size = Size(w * 0.18f, h * 0.28f),
                cornerRadius = CornerRadius(8.dp.toPx())
            )

            // 3. Legs
            // Left Leg
            val leftLegX = w * 0.42f + (leftLegAngle * 0.4f)
            drawLine(
                color = pantsColor,
                start = Offset(w * 0.44f, cy + h * 0.20f),
                end = Offset(leftLegX, cy + h * 0.42f),
                strokeWidth = 12.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Left Shoe
            drawRoundRect(
                color = shoeColor,
                topLeft = Offset(leftLegX - w * 0.04f, cy + h * 0.41f),
                size = Size(w * 0.10f, h * 0.05f),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // Right Leg
            val rightLegX = w * 0.54f + (rightLegAngle * 0.4f)
            drawLine(
                color = pantsColor,
                start = Offset(w * 0.52f, cy + h * 0.20f),
                end = Offset(rightLegX, cy + h * 0.42f),
                strokeWidth = 12.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Right Shoe
            drawRoundRect(
                color = shoeColor,
                topLeft = Offset(rightLegX - w * 0.04f, cy + h * 0.41f),
                size = Size(w * 0.10f, h * 0.05f),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // 4. Torso / Clothes (Hoodie for Boy, Dress/Top for Girl)
            drawRoundRect(
                color = topColor,
                topLeft = Offset(w * 0.36f, cy - h * 0.02f),
                size = Size(w * 0.26f, h * 0.24f),
                cornerRadius = CornerRadius(10.dp.toPx())
            )

            if (!isBoy) {
                // Girl Skirt flare
                val skirtPath = Path().apply {
                    moveTo(w * 0.34f, cy + h * 0.12f)
                    lineTo(w * 0.64f, cy + h * 0.12f)
                    lineTo(w * 0.68f, cy + h * 0.23f)
                    lineTo(w * 0.30f, cy + h * 0.23f)
                    close()
                }
                drawPath(skirtPath, color = KidsGColors.AccentPink)
            } else {
                // Boy Jacket Zipper line
                drawLine(
                    color = KidsGColors.OrangeDark,
                    start = Offset(w * 0.49f, cy - h * 0.01f),
                    end = Offset(w * 0.49f, cy + h * 0.21f),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 5. Arms
            if (pose.type == PoseType.PUSH_TROLLEY) {
                // Reaching forward to hold trolley
                drawLine(
                    color = topColor,
                    start = Offset(w * 0.48f, cy + h * 0.04f),
                    end = Offset(w * 0.72f, cy + h * 0.08f),
                    strokeWidth = 10.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color = skinColor, radius = w * 0.035f, center = Offset(w * 0.73f, cy + h * 0.08f))
            } else if (pose.type == PoseType.EXCITED) {
                // Arms up high in excitement!
                drawLine(
                    color = topColor,
                    start = Offset(w * 0.38f, cy + h * 0.04f),
                    end = Offset(w * 0.26f, cy - h * 0.16f),
                    strokeWidth = 10.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color = skinColor, radius = w * 0.035f, center = Offset(w * 0.25f, cy - h * 0.17f))

                drawLine(
                    color = topColor,
                    start = Offset(w * 0.60f, cy + h * 0.04f),
                    end = Offset(w * 0.72f, cy - h * 0.16f),
                    strokeWidth = 10.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color = skinColor, radius = w * 0.035f, center = Offset(w * 0.73f, cy - h * 0.17f))
            } else {
                // Normal arm swing
                val leftArmX = w * 0.38f + (leftArmAngle * 0.3f)
                drawLine(
                    color = topColor,
                    start = Offset(w * 0.38f, cy + h * 0.04f),
                    end = Offset(leftArmX, cy + h * 0.18f),
                    strokeWidth = 9.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color = skinColor, radius = w * 0.03f, center = Offset(leftArmX, cy + h * 0.18f))

                val rightArmX = w * 0.60f + (rightArmAngle * 0.3f)
                drawLine(
                    color = topColor,
                    start = Offset(w * 0.60f, cy + h * 0.04f),
                    end = Offset(rightArmX, cy + h * 0.18f),
                    strokeWidth = 9.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color = skinColor, radius = w * 0.03f, center = Offset(rightArmX, cy + h * 0.18f))
            }

            // 6. Head & Face
            val headCenter = Offset(w * 0.49f, cy - h * 0.16f)
            drawCircle(color = skinColor, radius = w * 0.18f, center = headCenter)

            // Hair
            if (isBoy) {
                // Short spiky messy hair
                val hairPath = Path().apply {
                    moveTo(headCenter.x - w * 0.18f, headCenter.y - h * 0.02f)
                    cubicTo(headCenter.x - w * 0.15f, headCenter.y - h * 0.26f, headCenter.x + w * 0.15f, headCenter.y - h * 0.26f, headCenter.x + w * 0.18f, headCenter.y - h * 0.02f)
                    cubicTo(headCenter.x + w * 0.10f, headCenter.y - h * 0.16f, headCenter.x - w * 0.10f, headCenter.y - h * 0.16f, headCenter.x - w * 0.18f, headCenter.y - h * 0.02f)
                    close()
                }
                drawPath(hairPath, color = hairColor)
            } else {
                // Girl long wavy hair
                val hairPath = Path().apply {
                    moveTo(headCenter.x - w * 0.22f, headCenter.y + h * 0.16f)
                    cubicTo(headCenter.x - w * 0.24f, headCenter.y - h * 0.26f, headCenter.x + w * 0.24f, headCenter.y - h * 0.26f, headCenter.x + w * 0.22f, headCenter.y + h * 0.16f)
                    cubicTo(headCenter.x + w * 0.14f, headCenter.y - h * 0.12f, headCenter.x - w * 0.14f, headCenter.y - h * 0.12f, headCenter.x - w * 0.22f, headCenter.y + h * 0.16f)
                    close()
                }
                drawPath(hairPath, color = hairColor)
            }

            // Sparkling Eyes
            drawCircle(color = KidsGColors.BlackText, radius = w * 0.028f, center = Offset(headCenter.x - w * 0.06f, headCenter.y - h * 0.01f))
            drawCircle(color = KidsGColors.White, radius = w * 0.010f, center = Offset(headCenter.x - w * 0.05f, headCenter.y - h * 0.02f))

            drawCircle(color = KidsGColors.BlackText, radius = w * 0.028f, center = Offset(headCenter.x + w * 0.06f, headCenter.y - h * 0.01f))
            drawCircle(color = KidsGColors.White, radius = w * 0.010f, center = Offset(headCenter.x + w * 0.07f, headCenter.y - h * 0.02f))

            // Rosy Cheeks
            drawCircle(color = KidsGColors.AccentPink.copy(alpha = 0.5f), radius = w * 0.035f, center = Offset(headCenter.x - w * 0.10f, headCenter.y + h * 0.05f))
            drawCircle(color = KidsGColors.AccentPink.copy(alpha = 0.5f), radius = w * 0.035f, center = Offset(headCenter.x + w * 0.10f, headCenter.y + h * 0.05f))

            // Smile
            if (pose.type == PoseType.EXCITED) {
                // Big open cheerful mouth!
                val openMouth = Path().apply {
                    moveTo(headCenter.x - w * 0.05f, headCenter.y + h * 0.05f)
                    cubicTo(headCenter.x, headCenter.y + h * 0.12f, headCenter.x + w * 0.05f, headCenter.y + h * 0.12f, headCenter.x + w * 0.05f, headCenter.y + h * 0.05f)
                    close()
                }
                drawPath(openMouth, color = KidsGColors.BlackText)
            } else {
                // Cheerful smile arc
                val smileArc = Path().apply {
                    moveTo(headCenter.x - w * 0.05f, headCenter.y + h * 0.06f)
                    cubicTo(headCenter.x, headCenter.y + h * 0.11f, headCenter.x, headCenter.y + h * 0.11f, headCenter.x + w * 0.05f, headCenter.y + h * 0.06f)
                }
                drawPath(smileArc, color = KidsGColors.BlackText, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            }

            // Star particles if excited
            if (pose.type == PoseType.EXCITED) {
                drawCircle(color = KidsGColors.AccentYellow, radius = 4.dp.toPx(), center = Offset(headCenter.x - w * 0.22f, headCenter.y - h * 0.20f))
                drawCircle(color = KidsGColors.AccentPink, radius = 5.dp.toPx(), center = Offset(headCenter.x + w * 0.24f, headCenter.y - h * 0.18f))
                drawCircle(color = KidsGColors.AccentMint, radius = 4.dp.toPx(), center = Offset(headCenter.x + w * 0.20f, headCenter.y + h * 0.10f))
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)
