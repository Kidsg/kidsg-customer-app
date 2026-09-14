package com.kidsg.core.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * KidsG Primary Button
 * Signature KidsG Orange with subtle scale press feedback.
 */
@Composable
fun KidsGPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = KidsGColors.OrangePrimary,
    contentColor: Color = KidsGColors.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = KidsGMotion.quickTween(),
        label = "btnScale"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale),
        enabled = enabled,
        shape = KidsGShapes.Medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = KidsGColors.BorderStrong,
            disabledContentColor = KidsGColors.TextMuted
        ),
        contentPadding = PaddingValues(horizontal = KidsGSpacing.lg, vertical = KidsGSpacing.md),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
    ) {
        Text(text = text, style = KidsGTypography.ButtonText)
    }
}

/**
 * KidsG Secondary / Outline Button
 */
@Composable
fun KidsGSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color = KidsGColors.OrangePrimary,
    contentColor: Color = KidsGColors.OrangePrimary
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = KidsGMotion.quickTween(),
        label = "secBtnScale"
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .scale(scale),
        enabled = enabled,
        shape = KidsGShapes.Medium,
        border = BorderStroke(1.5.dp, if (enabled) borderColor else KidsGColors.BorderStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = KidsGColors.White,
            contentColor = contentColor,
            disabledContentColor = KidsGColors.TextMuted
        ),
        interactionSource = interactionSource
    ) {
        Text(text = text, style = KidsGTypography.ButtonText.copy(color = contentColor))
    }
}

/**
 * KidsG Outline Button alias
 */
@Composable
fun KidsGOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color = KidsGColors.OrangePrimary,
    contentColor: Color = KidsGColors.OrangePrimary
) {
    KidsGSecondaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        borderColor = borderColor,
        contentColor = contentColor
    )
}

