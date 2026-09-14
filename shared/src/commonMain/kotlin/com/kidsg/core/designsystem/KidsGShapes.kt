package com.kidsg.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * KidsG Rounded Shapes System
 * Soft, inviting rounded surfaces that evoke smooth stationery erasers, sticky notes, and cards.
 */
object KidsGShapes {
    val ExtraSmall = RoundedCornerShape(6.dp)
    val Small = RoundedCornerShape(10.dp)
    val Medium = RoundedCornerShape(16.dp)
    val Large = RoundedCornerShape(22.dp)
    val ExtraLarge = RoundedCornerShape(30.dp)
    val FullPill = RoundedCornerShape(50)

    // Specific stationery shapes
    val StickyNote = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
    val DeskCard = RoundedCornerShape(18.dp)
    val BottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomEnd = 0.dp, bottomStart = 0.dp)

    // Aliases for modern screen layouts
    val CardRounded = DeskCard
    val MediumRounded = Medium
}
