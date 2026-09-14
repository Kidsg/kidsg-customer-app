package com.kidsg.core.designsystem

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-fidelity KidsG Stationery Icons rendered with vector Canvas.
 * Metaphors: Pencil, School Bag, Notebook, Geometry, Art Palette, Eraser, Star, Compass, Rider.
 */
object KidsGIcons {

    @Composable
    fun Pencil(
        modifier: Modifier = Modifier.size(24.dp),
        color: Color = KidsGColors.OrangePrimary
    ) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            // Pencil body
            val path = Path().apply {
                moveTo(w * 0.75f, h * 0.15f)
                lineTo(w * 0.85f, h * 0.25f)
                lineTo(w * 0.35f, h * 0.75f)
                lineTo(w * 0.20f, h * 0.80f)
                lineTo(w * 0.25f, h * 0.65f)
                close()
            }
            drawPath(path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            // Pencil tip
            drawLine(
                color = color,
                start = Offset(w * 0.20f, h * 0.80f),
                end = Offset(w * 0.24f, h * 0.76f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Pencil stripes
            drawLine(
                color = color,
                start = Offset(w * 0.65f, h * 0.25f),
                end = Offset(w * 0.75f, h * 0.35f),
                strokeWidth = 1.5.dp.toPx()
            )
        }
    }

    @Composable
    fun SchoolBag(
        modifier: Modifier = Modifier.size(24.dp),
        color: Color = KidsGColors.OrangePrimary
    ) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            // Bag Handle
            val handle = Path().apply {
                moveTo(w * 0.38f, h * 0.25f)
                cubicTo(w * 0.38f, h * 0.12f, w * 0.62f, h * 0.12f, w * 0.62f, h * 0.25f)
            }
            drawPath(handle, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

            // Bag Body
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.2f, h * 0.25f),
                size = Size(w * 0.6f, h * 0.65f),
                cornerRadius = CornerRadius(6.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Front Pocket
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.28f, h * 0.52f),
                size = Size(w * 0.44f, h * 0.32f),
                cornerRadius = CornerRadius(4.dp.toPx()),
                style = Stroke(width = 1.8.dp.toPx())
            )

            // Bag Straps Accent
            drawLine(color = color, start = Offset(w * 0.38f, h * 0.25f), end = Offset(w * 0.38f, h * 0.52f), strokeWidth = 1.5.dp.toPx())
            drawLine(color = color, start = Offset(w * 0.62f, h * 0.25f), end = Offset(w * 0.62f, h * 0.52f), strokeWidth = 1.5.dp.toPx())
        }
    }

    @Composable
    fun Notebook(
        modifier: Modifier = Modifier.size(24.dp),
        color: Color = KidsGColors.OrangePrimary
    ) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            // Notebook cover
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.22f, h * 0.15f),
                size = Size(w * 0.62f, h * 0.72f),
                cornerRadius = CornerRadius(4.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Spine rings (spiral)
            for (i in 0..4) {
                val y = h * (0.24f + i * 0.12f)
                drawLine(
                    color = color,
                    start = Offset(w * 0.16f, y),
                    end = Offset(w * 0.26f, y),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Notebook lines
            drawLine(color = color.copy(alpha = 0.6f), start = Offset(w * 0.34f, h * 0.38f), end = Offset(w * 0.70f, h * 0.38f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color = color.copy(alpha = 0.6f), start = Offset(w * 0.34f, h * 0.52f), end = Offset(w * 0.70f, h * 0.52f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color = color.copy(alpha = 0.6f), start = Offset(w * 0.34f, h * 0.66f), end = Offset(w * 0.58f, h * 0.66f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
        }
    }

    @Composable
    fun ArtPalette(
        modifier: Modifier = Modifier.size(24.dp),
        color: Color = KidsGColors.AccentPurple
    ) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            val palette = Path().apply {
                moveTo(w * 0.5f, h * 0.15f)
                cubicTo(w * 0.85f, h * 0.15f, w * 0.9f, h * 0.55f, w * 0.75f, h * 0.8f)
                cubicTo(w * 0.65f, h * 0.9f, w * 0.45f, h * 0.75f, w * 0.35f, h * 0.85f)
                cubicTo(w * 0.15f, h * 0.75f, w * 0.15f, h * 0.15f, w * 0.5f, h * 0.15f)
                close()
            }
            drawPath(palette, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

            // Paint blobs
            drawCircle(color = KidsGColors.AccentYellow, radius = 2.dp.toPx(), center = Offset(w * 0.38f, h * 0.32f))
            drawCircle(color = KidsGColors.AccentPink, radius = 2.dp.toPx(), center = Offset(w * 0.58f, h * 0.30f))
            drawCircle(color = KidsGColors.AccentSkyBlue, radius = 2.dp.toPx(), center = Offset(w * 0.70f, h * 0.48f))
            drawCircle(color = KidsGColors.AccentMint, radius = 2.dp.toPx(), center = Offset(w * 0.40f, h * 0.62f))
        }
    }

    @Composable
    fun Geometry(
        modifier: Modifier = Modifier.size(24.dp),
        color: Color = KidsGColors.AccentSkyBlue
    ) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height

            // Set square triangle
            val triangle = Path().apply {
                moveTo(w * 0.18f, h * 0.82f)
                lineTo(w * 0.82f, h * 0.82f)
                lineTo(w * 0.18f, h * 0.18f)
                close()
            }
            drawPath(triangle, color = color, style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round))

            // Inner cutout triangle
            val innerTriangle = Path().apply {
                moveTo(w * 0.30f, h * 0.72f)
                lineTo(w * 0.65f, h * 0.72f)
                lineTo(w * 0.30f, h * 0.38f)
                close()
            }
            drawPath(innerTriangle, color = color.copy(alpha = 0.5f), style = Stroke(width = 1.5.dp.toPx(), join = StrokeJoin.Round))

            // Ruler markings
            for (i in 1..4) {
                val x = w * (0.22f + i * 0.12f)
                drawLine(color = color, start = Offset(x, h * 0.82f), end = Offset(x, h * 0.76f), strokeWidth = 1.5.dp.toPx())
            }
        }
    }

    @Composable
    fun Star(
        modifier: Modifier = Modifier.size(20.dp),
        color: Color = KidsGColors.AccentYellow
    ) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            val star = Path().apply {
                moveTo(w * 0.5f, h * 0.05f)
                lineTo(w * 0.62f, h * 0.35f)
                lineTo(w * 0.95f, h * 0.38f)
                lineTo(w * 0.70f, h * 0.60f)
                lineTo(w * 0.78f, h * 0.92f)
                lineTo(w * 0.50f, h * 0.75f)
                lineTo(w * 0.22f, h * 0.92f)
                lineTo(w * 0.30f, h * 0.60f)
                lineTo(w * 0.05f, h * 0.38f)
                lineTo(w * 0.38f, h * 0.35f)
                close()
            }
            drawPath(star, color = color)
        }
    }

    @Composable
    fun StoreFront(
        modifier: Modifier = Modifier.size(20.dp),
        color: Color = KidsGColors.TextPrimary
    ) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            // Roof canopy
            val canopy = Path().apply {
                moveTo(w * 0.15f, h * 0.35f)
                lineTo(w * 0.85f, h * 0.35f)
                lineTo(w * 0.75f, h * 0.18f)
                lineTo(w * 0.25f, h * 0.18f)
                close()
            }
            drawPath(canopy, color = color, style = Stroke(width = 1.8.dp.toPx()))
            // Store body
            drawRect(
                color = color,
                topLeft = Offset(w * 0.2f, h * 0.35f),
                size = Size(w * 0.6f, h * 0.5f),
                style = Stroke(width = 1.8.dp.toPx())
            )
            // Door
            drawRect(
                color = color,
                topLeft = Offset(w * 0.42f, h * 0.52f),
                size = Size(w * 0.16f, h * 0.33f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }

    @Composable
    fun DeliveryScooter(
        modifier: Modifier = Modifier.size(24.dp),
        color: Color = KidsGColors.OrangePrimary
    ) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            // Wheels
            drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(w * 0.25f, h * 0.78f), style = Stroke(width = 2.dp.toPx()))
            drawCircle(color = color, radius = 3.dp.toPx(), center = Offset(w * 0.75f, h * 0.78f), style = Stroke(width = 2.dp.toPx()))
            // Body frame
            val frame = Path().apply {
                moveTo(w * 0.25f, h * 0.78f)
                lineTo(w * 0.45f, h * 0.78f)
                lineTo(w * 0.55f, h * 0.50f)
                lineTo(w * 0.70f, h * 0.50f)
                lineTo(w * 0.75f, h * 0.78f)
            }
            drawPath(frame, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
            // Handlebars
            drawLine(color = color, start = Offset(w * 0.68f, h * 0.36f), end = Offset(w * 0.75f, h * 0.50f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            // Delivery Box on Back
            drawRoundRect(
                color = KidsGColors.AccentYellow,
                topLeft = Offset(w * 0.18f, h * 0.42f),
                size = Size(w * 0.24f, h * 0.24f),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(w * 0.18f, h * 0.42f),
                size = Size(w * 0.24f, h * 0.24f),
                cornerRadius = CornerRadius(2.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}
