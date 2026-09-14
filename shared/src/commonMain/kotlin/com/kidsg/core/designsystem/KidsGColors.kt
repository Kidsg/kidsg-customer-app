package com.kidsg.core.designsystem

import androidx.compose.ui.graphics.Color

/**
 * KidsG Brand Palette
 * Primary: KidsG Orange (#FF7A00), Pure White (#FFFFFF), Jet Black (#111111)
 * Supporting Accents: Accent Yellow, Soft Pink, Sky Blue, Mint Green
 */
object KidsGColors {
    // Signature Actions & Brand
    val OrangePrimary = Color(0xFFFF7A00)
    val OrangeDark = Color(0xFFE56E00)
    val OrangeLight = Color(0xFFFFF2E8)
    val OrangeBorder = Color(0xFFFFCC99)
    val OrangeGradientStart = Color(0xFFFF8B1F)
    val OrangeGradientEnd = Color(0xFFFF6600)

    // Base Surfaces
    val White = Color(0xFFFFFFFF)
    val Background = Color(0xFFF9FAFB)
    val SurfaceDesk = Color(0xFFF4F5F7)
    val SurfaceCard = Color(0xFFFFFFFF)
    val SurfaceCardSubtle = Color(0xFFF8F9FA)
    val BorderSubtle = Color(0xFFEAEAEA)
    val BorderStrong = Color(0xFFD1D5DB)

    // Typography & Contrast
    val BlackText = Color(0xFF111111)
    val TextPrimary = Color(0xFF1A1A1A)
    val TextSecondary = Color(0xFF555555)
    val TextMuted = Color(0xFF8E8E93)
    val TextInverse = Color(0xFFFFFFFF)

    // Stationery Accents (Playful & Delightful)
    val AccentYellow = Color(0xFFFFD166) // Sticky notes & highlighter
    val AccentYellowLight = Color(0xFFFFF9DB)
    val AccentPink = Color(0xFFFF9ACD)   // Eraser pink & art supplies
    val AccentPinkLight = Color(0xFFFFF0F6)
    val AccentSkyBlue = Color(0xFF7DD3FC) // Ruler & geometry blue
    val AccentSkyBlueLight = Color(0xFFF0F9FF)
    val AccentMint = Color(0xFFA7F3D0)   // Sharpener mint & exam pass
    val AccentMintLight = Color(0xFFECFDF5)
    val AccentPurple = Color(0xFFC084FC) // Craft & project purple
    val AccentPurpleLight = Color(0xFFFAF5FF)

    // Functional & Feedback
    val Success = Color(0xFF10B981)
    val SuccessLight = Color(0xFFD1FAE5)
    val Warning = Color(0xFFF59E0B)
    val WarningLight = Color(0xFFFEF3C7)
    val Error = Color(0xFFEF4444)
    val ErrorLight = Color(0xFFFEE2E2)

    // Shadow / Overlay
    val ShadowColor = Color(0x14000000)
    val ShadowElevated = Color(0x24000000)
    val OverlayScrim = Color(0x66000000)
}
