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
import androidx.compose.ui.unit.dp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.feature.onboarding.CharacterPose
import com.kidsg.feature.onboarding.CharacterType
import com.kidsg.feature.onboarding.PoseType
import kotlin.math.sin

/**
 * Reusable Animated Character Composable for KidsG Onboarding.
 * Renders realistic Boy and Girl student illustrations matching the storyboard specifications.
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
            val stepPhase = stepCycle * (kotlin.math.PI / 2.0)
            val verticalBob = (sin(stepPhase * 2.0) * 4.0).toFloat()
            val legSwing = (sin(stepPhase) * 22.0).toFloat()
            val armSwing = (-sin(stepPhase) * 25.0).toFloat()
            Tuple5(verticalBob, legSwing, -legSwing, armSwing, -armSwing)
        }
        PoseType.EXCITED -> {
            val cheerBob = (sin(idleFloat * 2.0) * 6.0).toFloat()
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

            // 1. Soft Shadow underneath character
            val shadowWidth = w * (if (pose.type == PoseType.EXCITED) 0.40f else 0.48f)
            drawOval(
                color = KidsGColors.ShadowColor,
                topLeft = Offset((w - shadowWidth) / 2f, h * 0.88f),
                size = Size(shadowWidth, h * 0.08f)
            )

            val isBoy = characterType == CharacterType.BOY

            // Colors matching storyboard character illustrations
            val skinColor = Color(0xFFFFDFBA)
            val skinShadow = Color(0xFFF2C29B)
            val hairColor = if (isBoy) Color(0xFF332014) else Color(0xFF2C1810)
            val jacketColor = KidsGColors.OrangePrimary
            val jacketDark = KidsGColors.OrangeDark
            val innerShirt = Color(0xFF1D4ED8)
            val pinaforePink = Color(0xFFF43F5E)
            val pinaforeLight = Color(0xFFFF9ACD)
            val jeansColor = Color(0xFF1E3A8A)
            val sneakerNavy = Color(0xFF1E293B)
            val sneakerWhite = Color(0xFFF8FAFC)

            // 2. School Backpack (peeking behind shoulders)
            val bagColor = if (isBoy) Color(0xFF2563EB) else Color(0xFFF59E0B)
            drawRoundRect(
                color = bagColor,
                topLeft = Offset(w * 0.28f, cy - h * 0.04f),
                size = Size(w * 0.18f, h * 0.30f),
                cornerRadius = CornerRadius(10.dp.toPx())
            )

            // 3. Legs & Footwear
            // Left Leg
            val leftLegX = w * 0.42f + (leftLegAngle * 0.4f)
            drawLine(
                color = if (isBoy) jeansColor else skinColor,
                start = Offset(w * 0.44f, cy + h * 0.20f),
                end = Offset(leftLegX, cy + h * 0.42f),
                strokeWidth = 13.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Left Shoe / Sock
            if (!isBoy) {
                // White sock for Girl
                drawLine(
                    color = sneakerWhite,
                    start = Offset(leftLegX, cy + h * 0.35f),
                    end = Offset(leftLegX, cy + h * 0.42f),
                    strokeWidth = 13.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            drawRoundRect(
                color = if (isBoy) sneakerNavy else Color(0xFF111111),
                topLeft = Offset(leftLegX - w * 0.05f, cy + h * 0.40f),
                size = Size(w * 0.11f, h * 0.06f),
                cornerRadius = CornerRadius(5.dp.toPx())
            )
            if (isBoy) {
                // Sneaker white sole
                drawRoundRect(
                    color = sneakerWhite,
                    topLeft = Offset(leftLegX - w * 0.05f, cy + h * 0.44f),
                    size = Size(w * 0.11f, h * 0.02f),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }

            // Right Leg
            val rightLegX = w * 0.54f + (rightLegAngle * 0.4f)
            drawLine(
                color = if (isBoy) jeansColor else skinColor,
                start = Offset(w * 0.52f, cy + h * 0.20f),
                end = Offset(rightLegX, cy + h * 0.42f),
                strokeWidth = 13.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Right Shoe / Sock
            if (!isBoy) {
                drawLine(
                    color = sneakerWhite,
                    start = Offset(rightLegX, cy + h * 0.35f),
                    end = Offset(rightLegX, cy + h * 0.42f),
                    strokeWidth = 13.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            drawRoundRect(
                color = if (isBoy) sneakerNavy else Color(0xFF111111),
                topLeft = Offset(rightLegX - w * 0.05f, cy + h * 0.40f),
                size = Size(w * 0.11f, h * 0.06f),
                cornerRadius = CornerRadius(5.dp.toPx())
            )
            if (isBoy) {
                drawRoundRect(
                    color = sneakerWhite,
                    topLeft = Offset(rightLegX - w * 0.05f, cy + h * 0.44f),
                    size = Size(w * 0.11f, h * 0.02f),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }

            // 4. Torso & Outfit (Boy KidsG Orange Hoodie vs Girl Pink Pinafore)
            if (isBoy) {
                // Royal Blue inner shirt collar
                drawRoundRect(
                    color = innerShirt,
                    topLeft = Offset(w * 0.42f, cy - h * 0.04f),
                    size = Size(w * 0.14f, h * 0.08f),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
                // Bright Orange Hoodie Jacket
                drawRoundRect(
                    color = jacketColor,
                    topLeft = Offset(w * 0.35f, cy - h * 0.02f),
                    size = Size(w * 0.28f, h * 0.24f),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
                // Jacket Zipper Line
                drawLine(
                    color = jacketDark,
                    start = Offset(w * 0.49f, cy - h * 0.01f),
                    end = Offset(w * 0.49f, cy + h * 0.21f),
                    strokeWidth = 2.5.dp.toPx()
                )
                // Hoodie Front Pocket
                drawRoundRect(
                    color = jacketDark.copy(alpha = 0.3f),
                    topLeft = Offset(w * 0.41f, cy + h * 0.12f),
                    size = Size(w * 0.16f, h * 0.08f),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            } else {
                // White collared blouse for Girl
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(w * 0.36f, cy - h * 0.04f),
                    size = Size(w * 0.26f, h * 0.12f),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
                // Pink Pinafore Dress Torso & Skirt
                val pinaforePath = Path().apply {
                    moveTo(w * 0.38f, cy - h * 0.02f)
                    lineTo(w * 0.60f, cy - h * 0.02f)
                    lineTo(w * 0.68f, cy + h * 0.22f)
                    lineTo(w * 0.30f, cy + h * 0.22f)
                    close()
                }
                drawPath(pinaforePath, color = pinaforePink)
                // Skirt folds detail
                drawLine(color = pinaforeLight, start = Offset(w * 0.44f, cy + h * 0.08f), end = Offset(w * 0.42f, cy + h * 0.22f), strokeWidth = 2.dp.toPx())
                drawLine(color = pinaforeLight, start = Offset(w * 0.54f, cy + h * 0.08f), end = Offset(w * 0.56f, cy + h * 0.22f), strokeWidth = 2.dp.toPx())
            }

            // 5. Arms & Hands
            val sleeveColor = if (isBoy) jacketColor else pinaforePink

            if (pose.type == PoseType.PUSH_TROLLEY) {
                // Reaching forward to hold trolley handle
                drawLine(
                    color = sleeveColor,
                    start = Offset(w * 0.48f, cy + h * 0.04f),
                    end = Offset(w * 0.74f, cy + h * 0.08f),
                    strokeWidth = 11.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color = skinColor, radius = w * 0.038f, center = Offset(w * 0.75f, cy + h * 0.08f))
            } else if (pose.type == PoseType.EXCITED) {
                // Both arms raised high celebrating!
                drawLine(
                    color = sleeveColor,
                    start = Offset(w * 0.38f, cy + h * 0.04f),
                    end = Offset(w * 0.25f, cy - h * 0.18f),
                    strokeWidth = 11.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color = skinColor, radius = w * 0.038f, center = Offset(w * 0.24f, cy - h * 0.19f))

                drawLine(
                    color = sleeveColor,
                    start = Offset(w * 0.60f, cy + h * 0.04f),
                    end = Offset(w * 0.73f, cy - h * 0.18f),
                    strokeWidth = 11.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color = skinColor, radius = w * 0.038f, center = Offset(w * 0.74f, cy - h * 0.19f))
            } else {
                // Natural arm swing during walk/idle
                val leftArmX = w * 0.38f + (leftArmAngle * 0.3f)
                drawLine(
                    color = sleeveColor,
                    start = Offset(w * 0.38f, cy + h * 0.04f),
                    end = Offset(leftArmX, cy + h * 0.18f),
                    strokeWidth = 10.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color = skinColor, radius = w * 0.035f, center = Offset(leftArmX, cy + h * 0.18f))

                val rightArmX = w * 0.60f + (rightArmAngle * 0.3f)
                drawLine(
                    color = sleeveColor,
                    start = Offset(w * 0.60f, cy + h * 0.04f),
                    end = Offset(rightArmX, cy + h * 0.18f),
                    strokeWidth = 10.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color = skinColor, radius = w * 0.035f, center = Offset(rightArmX, cy + h * 0.18f))
            }

            // 6. Head, Hair & Expressions
            val headCenter = Offset(w * 0.49f, cy - h * 0.16f)

            // Neck
            drawRoundRect(
                color = skinShadow,
                topLeft = Offset(headCenter.x - w * 0.04f, headCenter.y + h * 0.12f),
                size = Size(w * 0.08f, h * 0.06f),
                cornerRadius = CornerRadius(2.dp.toPx())
            )

            // Face Head Base
            drawCircle(color = skinColor, radius = w * 0.18f, center = headCenter)

            // Detailed Hair Styling matching storyboard illustration
            if (isBoy) {
                // Boy: Messy short spiky layered dark brown hair
                val hairPath = Path().apply {
                    moveTo(headCenter.x - w * 0.19f, headCenter.y + h * 0.02f)
                    cubicTo(headCenter.x - w * 0.22f, headCenter.y - h * 0.26f, headCenter.x + w * 0.22f, headCenter.y - h * 0.26f, headCenter.x + w * 0.19f, headCenter.y + h * 0.02f)
                    cubicTo(headCenter.x + w * 0.12f, headCenter.y - h * 0.10f, headCenter.x - w * 0.12f, headCenter.y - h * 0.10f, headCenter.x - w * 0.19f, headCenter.y + h * 0.02f)
                    close()
                }
                drawPath(hairPath, color = hairColor)
                // Front fringe tufts
                val fringePath = Path().apply {
                    moveTo(headCenter.x - w * 0.14f, headCenter.y - h * 0.12f)
                    lineTo(headCenter.x - w * 0.06f, headCenter.y - h * 0.04f)
                    lineTo(headCenter.x + w * 0.02f, headCenter.y - h * 0.12f)
                    lineTo(headCenter.x + w * 0.10f, headCenter.y - h * 0.04f)
                    lineTo(headCenter.x + w * 0.15f, headCenter.y - h * 0.12f)
                    close()
                }
                drawPath(fringePath, color = hairColor)
            } else {
                // Girl: Long cascading dark brown wavy hair
                val hairPath = Path().apply {
                    moveTo(headCenter.x - w * 0.24f, headCenter.y + h * 0.24f)
                    cubicTo(headCenter.x - w * 0.26f, headCenter.y - h * 0.28f, headCenter.x + w * 0.26f, headCenter.y - h * 0.28f, headCenter.x + w * 0.24f, headCenter.y + h * 0.24f)
                    cubicTo(headCenter.x + w * 0.16f, headCenter.y - h * 0.08f, headCenter.x - w * 0.16f, headCenter.y - h * 0.08f, headCenter.x - w * 0.24f, headCenter.y + h * 0.24f)
                    close()
                }
                drawPath(hairPath, color = hairColor)
                // Side wave strands
                drawCircle(color = hairColor, radius = w * 0.08f, center = Offset(headCenter.x - w * 0.18f, headCenter.y + h * 0.10f))
                drawCircle(color = hairColor, radius = w * 0.08f, center = Offset(headCenter.x + w * 0.18f, headCenter.y + h * 0.10f))
            }

            // Sparkling Eyes
            drawCircle(color = KidsGColors.BlackText, radius = w * 0.030f, center = Offset(headCenter.x - w * 0.06f, headCenter.y - h * 0.01f))
            drawCircle(color = Color.White, radius = w * 0.012f, center = Offset(headCenter.x - w * 0.05f, headCenter.y - h * 0.02f))

            drawCircle(color = KidsGColors.BlackText, radius = w * 0.030f, center = Offset(headCenter.x + w * 0.06f, headCenter.y - h * 0.01f))
            drawCircle(color = Color.White, radius = w * 0.012f, center = Offset(headCenter.x + w * 0.07f, headCenter.y - h * 0.02f))

            // Rosy Cheeks
            drawCircle(color = KidsGColors.AccentPink.copy(alpha = 0.5f), radius = w * 0.038f, center = Offset(headCenter.x - w * 0.10f, headCenter.y + h * 0.05f))
            drawCircle(color = KidsGColors.AccentPink.copy(alpha = 0.5f), radius = w * 0.038f, center = Offset(headCenter.x + w * 0.10f, headCenter.y + h * 0.05f))

            // Expression / Smile
            if (pose.type == PoseType.EXCITED) {
                // Joyful open laugh!
                val openMouth = Path().apply {
                    moveTo(headCenter.x - w * 0.06f, headCenter.y + h * 0.05f)
                    cubicTo(headCenter.x, headCenter.y + h * 0.13f, headCenter.x + w * 0.06f, headCenter.y + h * 0.13f, headCenter.x + w * 0.06f, headCenter.y + h * 0.05f)
                    close()
                }
                drawPath(openMouth, color = KidsGColors.BlackText)
            } else {
                // Warm friendly smile arc
                val smileArc = Path().apply {
                    moveTo(headCenter.x - w * 0.05f, headCenter.y + h * 0.06f)
                    cubicTo(headCenter.x, headCenter.y + h * 0.11f, headCenter.x, headCenter.y + h * 0.11f, headCenter.x + w * 0.05f, headCenter.y + h * 0.06f)
                }
                drawPath(smileArc, color = KidsGColors.BlackText, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            }

            // Star particles bursting during excitement
            if (pose.type == PoseType.EXCITED) {
                drawCircle(color = KidsGColors.AccentYellow, radius = 5.dp.toPx(), center = Offset(headCenter.x - w * 0.24f, headCenter.y - h * 0.22f))
                drawCircle(color = KidsGColors.AccentPink, radius = 6.dp.toPx(), center = Offset(headCenter.x + w * 0.26f, headCenter.y - h * 0.20f))
                drawCircle(color = KidsGColors.AccentMint, radius = 5.dp.toPx(), center = Offset(headCenter.x + w * 0.22f, headCenter.y + h * 0.12f))
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
