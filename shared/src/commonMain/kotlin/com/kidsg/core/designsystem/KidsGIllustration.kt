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

    /**
     * Storyboard Scene 1: "School Tomorrow?"
     * Kid at desk realizing he needs stationery, desk with book & alarm clock.
     */
    @Composable
    fun Scene1SchoolTomorrow(modifier: Modifier = Modifier) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            // Desk surface
            drawRoundRect(
                color = Color(0xFFF7E6D0),
                topLeft = Offset(w * 0.08f, h * 0.65f),
                size = Size(w * 0.84f, h * 0.28f),
                cornerRadius = CornerRadius(12.dp.toPx())
            )

            // Open Notebook on desk
            drawRoundRect(
                color = KidsGColors.White,
                topLeft = Offset(w * 0.18f, h * 0.62f),
                size = Size(w * 0.38f, h * 0.20f),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            // Notebook lines
            drawLine(
                color = KidsGColors.BorderSubtle,
                start = Offset(w * 0.22f, h * 0.68f),
                end = Offset(w * 0.52f, h * 0.68f),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = KidsGColors.BorderSubtle,
                start = Offset(w * 0.22f, h * 0.74f),
                end = Offset(w * 0.48f, h * 0.74f),
                strokeWidth = 2.dp.toPx()
            )

            // Alarm Clock on right
            drawCircle(
                color = KidsGColors.AccentYellow,
                radius = w * 0.10f,
                center = Offset(w * 0.72f, h * 0.70f)
            )
            drawCircle(
                color = KidsGColors.White,
                radius = w * 0.075f,
                center = Offset(w * 0.72f, h * 0.70f)
            )
            // Clock hands
            drawLine(
                color = KidsGColors.BlackText,
                start = Offset(w * 0.72f, h * 0.70f),
                end = Offset(w * 0.72f, h * 0.65f),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = KidsGColors.OrangePrimary,
                start = Offset(w * 0.72f, h * 0.70f),
                end = Offset(w * 0.76f, h * 0.72f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Kid's head thinking
            drawCircle(
                color = Color(0xFFFFDFBA),
                radius = w * 0.20f,
                center = Offset(w * 0.50f, h * 0.38f)
            )
            // Kid hair
            drawCircle(
                color = Color(0xFF332014),
                radius = w * 0.21f,
                center = Offset(w * 0.50f, h * 0.33f)
            )
            drawCircle(
                color = Color(0xFFFFDFBA),
                radius = w * 0.19f,
                center = Offset(w * 0.50f, h * 0.40f)
            )
            // Curious eyes looking up
            drawCircle(color = KidsGColors.BlackText, radius = w * 0.03f, center = Offset(w * 0.44f, h * 0.37f))
            drawCircle(color = KidsGColors.White, radius = w * 0.012f, center = Offset(w * 0.45f, h * 0.36f))
            drawCircle(color = KidsGColors.BlackText, radius = w * 0.03f, center = Offset(w * 0.56f, h * 0.37f))
            drawCircle(color = KidsGColors.White, radius = w * 0.012f, center = Offset(w * 0.57f, h * 0.36f))

            // Cheeks
            drawCircle(color = KidsGColors.AccentPink.copy(alpha = 0.5f), radius = w * 0.035f, center = Offset(w * 0.38f, h * 0.42f))
            drawCircle(color = KidsGColors.AccentPink.copy(alpha = 0.5f), radius = w * 0.035f, center = Offset(w * 0.62f, h * 0.42f))

            // Thinking question mark / doodle
            val questionPath = Path().apply {
                moveTo(w * 0.78f, h * 0.24f)
                cubicTo(w * 0.84f, h * 0.18f, w * 0.88f, h * 0.26f, w * 0.82f, h * 0.32f)
                lineTo(w * 0.82f, h * 0.37f)
            }
            drawPath(questionPath, color = KidsGColors.OrangePrimary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(color = KidsGColors.OrangePrimary, radius = 2.5.dp.toPx(), center = Offset(w * 0.82f, h * 0.42f))
        }
    }

    /**
     * Storyboard Scene 2: Kid Asks Mom
     * Kid pleading with speech bubble "Mom! I need a new notebook and pens! 🥺"
     */
    @Composable
    fun Scene2KidAsksMom(modifier: Modifier = Modifier) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            // Mom on Right
            // Hair
            drawOval(
                color = Color(0xFF2C1810),
                topLeft = Offset(w * 0.52f, h * 0.15f),
                size = Size(w * 0.42f, h * 0.65f)
            )
            // Mom Face
            drawCircle(
                color = Color(0xFFFFE0BD),
                radius = w * 0.18f,
                center = Offset(w * 0.72f, h * 0.38f)
            )
            // Warm smiling eyes
            val eyeLeft = Path().apply {
                moveTo(w * 0.66f, h * 0.36f)
                cubicTo(w * 0.68f, h * 0.34f, w * 0.70f, h * 0.34f, w * 0.72f, h * 0.36f)
            }
            drawPath(eyeLeft, color = KidsGColors.BlackText, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            val eyeRight = Path().apply {
                moveTo(w * 0.76f, h * 0.36f)
                cubicTo(w * 0.78f, h * 0.34f, w * 0.80f, h * 0.34f, w * 0.82f, h * 0.36f)
            }
            drawPath(eyeRight, color = KidsGColors.BlackText, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            // Gentle smile
            val momSmile = Path().apply {
                moveTo(w * 0.70f, h * 0.42f)
                cubicTo(w * 0.73f, h * 0.46f, w * 0.77f, h * 0.46f, w * 0.80f, h * 0.42f)
            }
            drawPath(momSmile, color = KidsGColors.OrangeDark, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

            // Kid on Left (Pleading)
            drawCircle(
                color = Color(0xFFFFDFBA),
                radius = w * 0.17f,
                center = Offset(w * 0.28f, h * 0.52f)
            )
            // Kid hair
            drawCircle(
                color = Color(0xFF332014),
                radius = w * 0.18f,
                center = Offset(w * 0.28f, h * 0.47f)
            )
            drawCircle(
                color = Color(0xFFFFDFBA),
                radius = w * 0.16f,
                center = Offset(w * 0.28f, h * 0.54f)
            )
            // Pleading big eyes
            drawCircle(color = KidsGColors.BlackText, radius = w * 0.035f, center = Offset(w * 0.23f, h * 0.52f))
            drawCircle(color = KidsGColors.White, radius = w * 0.015f, center = Offset(w * 0.24f, h * 0.51f))
            drawCircle(color = KidsGColors.BlackText, radius = w * 0.035f, center = Offset(w * 0.33f, h * 0.52f))
            drawCircle(color = KidsGColors.White, radius = w * 0.015f, center = Offset(w * 0.34f, h * 0.51f))
            // Hands joined together (pleading)
            drawRoundRect(
                color = Color(0xFFFFDFBA),
                topLeft = Offset(w * 0.24f, h * 0.70f),
                size = Size(w * 0.12f, h * 0.15f),
                cornerRadius = CornerRadius(8.dp.toPx())
            )
        }
    }

    /**
     * Storyboard Scene 3: Parent Orders on KidsG
     * Mom holding phone with orange KidsG interface "Let's order on KidsG!"
     */
    @Composable
    fun Scene3ParentOrders(modifier: Modifier = Modifier) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            // Phone frame held in hand
            drawRoundRect(
                color = KidsGColors.BlackText,
                topLeft = Offset(w * 0.32f, h * 0.22f),
                size = Size(w * 0.36f, h * 0.62f),
                cornerRadius = CornerRadius(16.dp.toPx())
            )
            // Phone screen
            drawRoundRect(
                color = KidsGColors.White,
                topLeft = Offset(w * 0.34f, h * 0.25f),
                size = Size(w * 0.32f, h * 0.56f),
                cornerRadius = CornerRadius(12.dp.toPx())
            )
            // KidsG Orange App Header on phone screen
            drawRoundRect(
                color = KidsGColors.OrangePrimary,
                topLeft = Offset(w * 0.34f, h * 0.25f),
                size = Size(w * 0.32f, h * 0.15f),
                cornerRadius = CornerRadius(12.dp.toPx())
            )
            // Mini KidsG logo 'G'
            drawCircle(
                color = KidsGColors.White,
                radius = w * 0.035f,
                center = Offset(w * 0.50f, h * 0.32f)
            )
            // Mock items in app
            drawRoundRect(
                color = KidsGColors.Surface,
                topLeft = Offset(w * 0.36f, h * 0.43f),
                size = Size(w * 0.28f, h * 0.08f),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            drawRoundRect(
                color = KidsGColors.Surface,
                topLeft = Offset(w * 0.36f, h * 0.53f),
                size = Size(w * 0.28f, h * 0.08f),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
            // Orange "Order Now" button
            drawRoundRect(
                color = KidsGColors.OrangePrimary,
                topLeft = Offset(w * 0.36f, h * 0.65f),
                size = Size(w * 0.28f, h * 0.07f),
                cornerRadius = CornerRadius(6.dp.toPx())
            )

            // Sparkles around phone
            drawCircle(color = KidsGColors.AccentYellow, radius = 4.dp.toPx(), center = Offset(w * 0.24f, h * 0.28f))
            drawCircle(color = KidsGColors.AccentPink, radius = 5.dp.toPx(), center = Offset(w * 0.76f, h * 0.32f))
            drawCircle(color = KidsGColors.AccentMint, radius = 4.dp.toPx(), center = Offset(w * 0.74f, h * 0.68f))
        }
    }

    /**
     * Storyboard Scene 4: Ordering Happiness
     * Shopping cart packed with stationery essentials from nearby stores
     */
    @Composable
    fun Scene4OrderingHappiness(modifier: Modifier = Modifier) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            // Shopping Cart Basket
            val cartPath = Path().apply {
                moveTo(w * 0.22f, h * 0.42f)
                lineTo(w * 0.76f, h * 0.42f)
                lineTo(w * 0.68f, h * 0.68f)
                lineTo(w * 0.30f, h * 0.68f)
                close()
            }
            drawPath(cartPath, color = KidsGColors.OrangePrimary.copy(alpha = 0.12f))
            drawPath(cartPath, color = KidsGColors.OrangePrimary, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))

            // Cart handle
            val handlePath = Path().apply {
                moveTo(w * 0.22f, h * 0.42f)
                lineTo(w * 0.15f, h * 0.38f)
            }
            drawPath(handlePath, color = KidsGColors.BlackText, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))

            // Cart Wheels
            drawCircle(color = KidsGColors.BlackText, radius = w * 0.045f, center = Offset(w * 0.36f, h * 0.74f))
            drawCircle(color = KidsGColors.White, radius = w * 0.018f, center = Offset(w * 0.36f, h * 0.74f))
            drawCircle(color = KidsGColors.BlackText, radius = w * 0.045f, center = Offset(w * 0.62f, h * 0.74f))
            drawCircle(color = KidsGColors.White, radius = w * 0.018f, center = Offset(w * 0.62f, h * 0.74f))

            // Notebook poking out of cart
            drawRoundRect(
                color = KidsGColors.AccentYellow,
                topLeft = Offset(w * 0.28f, h * 0.24f),
                size = Size(w * 0.22f, h * 0.25f),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            // Pens sticking up
            drawLine(
                color = KidsGColors.AccentSkyBlue,
                start = Offset(w * 0.52f, h * 0.44f),
                end = Offset(w * 0.58f, h * 0.20f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = KidsGColors.AccentPink,
                start = Offset(w * 0.60f, h * 0.44f),
                end = Offset(w * 0.66f, h * 0.22f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Speed lines showing fast local delivery
            drawLine(
                color = KidsGColors.OrangePrimary.copy(alpha = 0.6f),
                start = Offset(w * 0.08f, h * 0.55f),
                end = Offset(w * 0.16f, h * 0.55f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = KidsGColors.OrangePrimary.copy(alpha = 0.4f),
                start = Offset(w * 0.04f, h * 0.62f),
                end = Offset(w * 0.14f, h * 0.62f),
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }

    /**
     * Storyboard Scene 5: Yay! Delivery Arrives
     * Friendly Delivery partner holding KidsG parcel box + excited Kid
     */
    @Composable
    fun Scene5DeliveryArrives(modifier: Modifier = Modifier) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            // Delivery Partner on Left
            // Orange Helmet / Cap
            drawRoundRect(
                color = KidsGColors.OrangePrimary,
                topLeft = Offset(w * 0.18f, h * 0.18f),
                size = Size(w * 0.28f, h * 0.18f),
                cornerRadius = CornerRadius(14.dp.toPx())
            )
            // Face
            drawCircle(
                color = Color(0xFFFFDFBA),
                radius = w * 0.14f,
                center = Offset(w * 0.32f, h * 0.34f)
            )
            // Friendly Beard / Smile
            drawCircle(color = KidsGColors.BlackText, radius = w * 0.025f, center = Offset(w * 0.28f, h * 0.32f))
            drawCircle(color = KidsGColors.BlackText, radius = w * 0.025f, center = Offset(w * 0.37f, h * 0.32f))
            val riderSmile = Path().apply {
                moveTo(w * 0.28f, h * 0.37f)
                cubicTo(w * 0.32f, h * 0.42f, w * 0.36f, h * 0.42f, w * 0.40f, h * 0.37f)
            }
            drawPath(riderSmile, color = KidsGColors.BlackText, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))

            // KidsG Delivery Parcel Box in hands
            drawRoundRect(
                color = Color(0xFFD4A373),
                topLeft = Offset(w * 0.22f, h * 0.50f),
                size = Size(w * 0.34f, h * 0.22f),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            // KidsG label on box
            drawRoundRect(
                color = KidsGColors.OrangePrimary,
                topLeft = Offset(w * 0.28f, h * 0.57f),
                size = Size(w * 0.22f, h * 0.08f),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            // Excited Kid on Right waving
            drawCircle(
                color = Color(0xFFFFDFBA),
                radius = w * 0.15f,
                center = Offset(w * 0.74f, h * 0.42f)
            )
            // Messy hair
            drawCircle(
                color = Color(0xFF332014),
                radius = w * 0.16f,
                center = Offset(w * 0.74f, h * 0.37f)
            )
            drawCircle(
                color = Color(0xFFFFDFBA),
                radius = w * 0.14f,
                center = Offset(w * 0.74f, h * 0.44f)
            )
            // Cheerful open mouth smile
            val kidSmile = Path().apply {
                moveTo(w * 0.69f, h * 0.46f)
                cubicTo(w * 0.74f, h * 0.54f, w * 0.78f, h * 0.54f, w * 0.83f, h * 0.46f)
                close()
            }
            drawPath(kidSmile, color = KidsGColors.BlackText)
            // Waving arm
            drawLine(
                color = Color(0xFFFFDFBA),
                start = Offset(w * 0.86f, h * 0.48f),
                end = Offset(w * 0.94f, h * 0.32f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }

    /**
     * Storyboard Scene 6: Brand Reveal & Ready for Tomorrow
     * "KidsG - Small Supplies. Big Futures." with sticky note "Learn Create Grow Repeat"
     */
    @Composable
    fun Scene6BrandReveal(modifier: Modifier = Modifier) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            // Yellow Sticky Note: "Learn Create Grow Repeat"
            drawRoundRect(
                color = KidsGColors.AccentYellow,
                topLeft = Offset(w * 0.24f, h * 0.22f),
                size = Size(w * 0.52f, h * 0.44f),
                cornerRadius = CornerRadius(8.dp.toPx())
            )
            // Fold corner on sticky note
            val foldPath = Path().apply {
                moveTo(w * 0.66f, h * 0.22f)
                lineTo(w * 0.76f, h * 0.32f)
                lineTo(w * 0.66f, h * 0.32f)
                close()
            }
            drawPath(foldPath, color = Color(0xFFF0B800))

            // Sticky note handwritten lines
            drawLine(
                color = KidsGColors.BlackText.copy(alpha = 0.7f),
                start = Offset(w * 0.32f, h * 0.32f),
                end = Offset(w * 0.62f, h * 0.32f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = KidsGColors.BlackText.copy(alpha = 0.7f),
                start = Offset(w * 0.32f, h * 0.40f),
                end = Offset(w * 0.58f, h * 0.40f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = KidsGColors.BlackText.copy(alpha = 0.7f),
                start = Offset(w * 0.32f, h * 0.48f),
                end = Offset(w * 0.64f, h * 0.48f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = KidsGColors.OrangePrimary,
                start = Offset(w * 0.32f, h * 0.56f),
                end = Offset(w * 0.54f, h * 0.56f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Signature KidsG Pencil pointing at note
            val pencilPath = Path().apply {
                moveTo(w * 0.72f, h * 0.58f)
                lineTo(w * 0.88f, h * 0.74f)
                lineTo(w * 0.82f, h * 0.80f)
                lineTo(w * 0.66f, h * 0.64f)
                close()
            }
            drawPath(pencilPath, color = KidsGColors.OrangePrimary)
            // Pencil tip
            val tipPath = Path().apply {
                moveTo(w * 0.72f, h * 0.58f)
                lineTo(w * 0.66f, h * 0.64f)
                lineTo(w * 0.60f, h * 0.54f)
                close()
            }
            drawPath(tipPath, color = Color(0xFFFFE0BD))
            drawCircle(color = KidsGColors.BlackText, radius = 3.dp.toPx(), center = Offset(w * 0.60f, h * 0.54f))

            // Playful star doodles
            drawCircle(color = KidsGColors.AccentSkyBlue, radius = 5.dp.toPx(), center = Offset(w * 0.16f, h * 0.28f))
            drawCircle(color = KidsGColors.AccentMint, radius = 6.dp.toPx(), center = Offset(w * 0.84f, h * 0.24f))
            drawCircle(color = KidsGColors.AccentPink, radius = 5.dp.toPx(), center = Offset(w * 0.18f, h * 0.65f))
        }
    }
}
