package com.kidsg.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * KidsG Personality Empty States (Section 27)
 */
@Composable
fun KidsGEmptyState(
    title: String,
    subtitle: String,
    actionButtonText: String? = null,
    onActionClick: () -> Unit = {},
    illustration: @Composable () -> Unit = { KidsGIcons.SchoolBag(modifier = Modifier.size(72.dp), color = KidsGColors.OrangePrimary) },
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(KidsGSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Character / Stationery illustration container
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(KidsGShapes.FullPill)
                .background(KidsGColors.OrangeLight),
            contentAlignment = Alignment.Center
        ) {
            illustration()
        }

        Spacer(modifier = Modifier.height(KidsGSpacing.lg))

        Text(
            text = title,
            style = KidsGTypography.TitleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(KidsGSpacing.xs))

        Text(
            text = subtitle,
            style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextSecondary),
            textAlign = TextAlign.Center
        )

        if (actionButtonText != null) {
            Spacer(modifier = Modifier.height(KidsGSpacing.xl))
            KidsGPrimaryButton(
                text = actionButtonText,
                onClick = onActionClick,
                modifier = Modifier.width(220.dp)
            )
        }
    }
}

/**
 * KidsG Friendly Error State (Section 28)
 */
@Composable
fun KidsGErrorState(
    message: String = "Something went wrong. Let's try that again.",
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(KidsGSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(KidsGShapes.FullPill)
                .background(KidsGColors.AccentPinkLight),
            contentAlignment = Alignment.Center
        ) {
            KidsGIcons.Pencil(modifier = Modifier.size(48.dp), color = KidsGColors.Error)
        }

        Spacer(modifier = Modifier.height(KidsGSpacing.lg))

        Text(
            text = "Oops!",
            style = KidsGTypography.TitleLarge
        )

        Spacer(modifier = Modifier.height(KidsGSpacing.xs))

        Text(
            text = message,
            style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextSecondary),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(KidsGSpacing.lg))

        KidsGSecondaryButton(
            text = "Try Again",
            onClick = onRetry,
            modifier = Modifier.width(180.dp)
        )
    }
}
