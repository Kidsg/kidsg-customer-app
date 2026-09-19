package com.kidsg.feature.onboarding

import androidx.compose.ui.graphics.Color
import com.kidsg.core.designsystem.KidsGColors

enum class StationeryItemType {
    NOTEBOOK,
    PENCIL,
    PEN,
    RULER,
    ERASER,
    CRAYON,
    SCHOOL_BAG,
    LUNCH_BOX,
    WATER_BOTTLE,
    GEOMETRY_BOX,
    SCISSORS,
    GLUE
}

enum class DecorativeItemType {
    STAR,
    HEART,
    PAPER_PLANE,
    SPARKLE,
    SCRIBBLE,
    ARROW,
    DOTS
}

/**
 * Modular Asset Abstraction Layer for Onboarding Animation System.
 * Allows seamless replacement of character images, trolley frames, or stationery icons
 * with PNG sequences, Lottie, Rive, or Vector drawables.
 */
interface OnboardingAssetProvider {
    val boyPrimaryColor: Color
    val girlPrimaryColor: Color
    val trolleyColor: Color
    
    fun getStationeryColor(type: StationeryItemType): Color
    fun getDecorativeColor(type: DecorativeItemType): Color
}

object DefaultOnboardingAssetProvider : OnboardingAssetProvider {
    override val boyPrimaryColor: Color = Color(0xFFFF7A00) // KidsG Orange Jacket
    override val girlPrimaryColor: Color = Color(0xFFFF9ACD) // Soft Pink Uniform
    override val trolleyColor: Color = Color(0xFFFFD166)     // Yellow/Gold Cart

    override fun getStationeryColor(type: StationeryItemType): Color {
        return when (type) {
            StationeryItemType.NOTEBOOK -> Color(0xFFFF9ACD)
            StationeryItemType.PENCIL -> Color(0xFFFFD166)
            StationeryItemType.PEN -> Color(0xFF7DD3FC)
            StationeryItemType.RULER -> Color(0xFFA7F3D0)
            StationeryItemType.ERASER -> Color(0xFFFF9ACD)
            StationeryItemType.CRAYON -> Color(0xFFFF7A00)
            StationeryItemType.SCHOOL_BAG -> Color(0xFF3B82F6)
            StationeryItemType.LUNCH_BOX -> Color(0xFFF59E0B)
            StationeryItemType.WATER_BOTTLE -> Color(0xFF06B6D4)
            StationeryItemType.GEOMETRY_BOX -> Color(0xFF10B981)
            StationeryItemType.SCISSORS -> Color(0xFFEF4444)
            StationeryItemType.GLUE -> Color(0xFF8B5CF6)
        }
    }

    override fun getDecorativeColor(type: DecorativeItemType): Color {
        return when (type) {
            DecorativeItemType.STAR -> KidsGColors.AccentYellow
            DecorativeItemType.HEART -> KidsGColors.AccentPink
            DecorativeItemType.PAPER_PLANE -> KidsGColors.AccentSkyBlue
            DecorativeItemType.SPARKLE -> KidsGColors.AccentMint
            DecorativeItemType.SCRIBBLE -> KidsGColors.OrangePrimary
            DecorativeItemType.ARROW -> KidsGColors.OrangePrimary
            DecorativeItemType.DOTS -> KidsGColors.AccentYellow
        }
    }
}
