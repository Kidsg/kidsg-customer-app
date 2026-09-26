package com.kidsg.feature.onboarding.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidsg.core.designsystem.KidsGColors
import com.kidsg.core.designsystem.KidsGTypography
import com.kidsg.feature.onboarding.CharacterType

/**
 * Interactive Selection Card for Boys / Girls choices with micro-feedback animations.
 * Provides real Compose interactive controls with spring-based scale,
 * orange selection border, subtle orange background, check indicator, and semantics.
 */
@Composable
fun CharacterSelectionCard(
    characterType: CharacterType,
    isSelected: Boolean,
    isDeemphasized: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val label = if (characterType == CharacterType.BOY) "BOYS" else "GIRLS"
    val iconEmoji = if (characterType == CharacterType.BOY) "👦" else "👧"
    val accessibilityLabel = if (characterType == CharacterType.BOY) "Select boys" else "Select girls"

    // Spring scale on touch & selection
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.94f
            isSelected -> 1.05f
            isDeemphasized -> 0.92f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )

    val alphaVal by animateFloatAsState(
        targetValue = if (isDeemphasized) 0.35f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cardAlpha"
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

    Box(
        modifier = modifier
            .semantics {
                role = Role.Button
                contentDescription = accessibilityLabel
            }
            .alpha(alphaVal)
            .scale(scale)
            .shadow(
                elevation = if (isSelected) 4.dp else 0.dp,
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
                enabled = !isDeemphasized,
                onClick = onClick
            )
            .padding(vertical = 14.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = iconEmoji,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = KidsGTypography.TitleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isSelected) KidsGColors.OrangeDark else KidsGColors.BlackText
                    )
                )
            }
        }

        // Selected Checkmark Badge (Top-Right)
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
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
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}
