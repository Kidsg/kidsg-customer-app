package com.kidsg.core.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * KidsG Motion System
 * Fast, responsive, tactile stationery physics.
 * Never blocks user interaction; short and purposeful.
 */
object KidsGMotion {
    // Standard Durations
    const val DurationMicro = 150
    const val DurationQuick = 250
    const val DurationMedium = 350
    const val DurationHero = 500
    const val DurationSplash = 1800

    // Easings
    val StandardEasing = FastOutSlowInEasing
    val DecelerateEasing = LinearOutSlowInEasing
    val BounceEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f) // Playful bounce for cart/add buttons

    // Spring Specifications
    val BouncySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val SnappySpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Standard Tween Animations
    fun <T> quickTween() = tween<T>(durationMillis = DurationQuick, easing = StandardEasing)
    fun <T> bouncyTween() = tween<T>(durationMillis = DurationMedium, easing = BounceEasing)
}
