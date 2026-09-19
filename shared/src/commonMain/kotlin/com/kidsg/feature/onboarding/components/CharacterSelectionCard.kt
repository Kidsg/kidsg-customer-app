package com.kidsg.feature.onboarding.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.feature.onboarding.CharacterPose
import com.kidsg.feature.onboarding.CharacterType
import com.kidsg.feature.onboarding.PoseType

/**
 * Interactive Selection Card for Boys / Girls choices with micro-feedback animations.
 */
@Composable
fun CharacterSelectionCard(
    characterType: CharacterType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Spring scale on touch & selection
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.94f
            isSelected -> 1.05f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    // Animated colors & elevation
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) KidsGColors.OrangePrimary else KidsGColors.BorderSubtle,
        label = "borderColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) KidsGColors.OrangeLight else KidsGColors.White,
        label = "cardBg"
    )

    val title = if (characterType == CharacterType.BOY) "Boys" else "Girls"
    val avatarBg = if (characterType == CharacterType.BOY) Color(0xFFE0F2FE) else Color(0xFFFCE7F3)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isSelected) 8.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = 16.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Character Avatar Circle Container
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
                contentAlignment = Alignment.Center
            ) {
                KidsGCharacter(
                    characterType = characterType,
                    pose = CharacterPose(type = PoseType.IDLE, isFacingRight = characterType == CharacterType.BOY),
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title Label
            Text(
                text = title,
                style = KidsGTypography.TitleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isSelected) KidsGColors.OrangeDark else KidsGColors.BlackText
                )
            )
        }

        // Selected Checkmark Badge (Top-Right)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(KidsGColors.OrangePrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    style = KidsGTypography.BodySmall.copy(
                        color = KidsGColors.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}
