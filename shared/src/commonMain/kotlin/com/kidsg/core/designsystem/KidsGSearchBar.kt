package com.kidsg.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * KidsG Desk Search Bar
 * Tactile, inviting, friendly.
 */
@Composable
fun KidsGSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "What do you need today?",
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    isClickableOnly: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(KidsGShapes.Medium)
            .background(KidsGColors.White)
            .border(1.dp, KidsGColors.BorderSubtle, KidsGShapes.Medium)
            .then(if (isClickableOnly) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = KidsGSpacing.md),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Pencil icon
            KidsGIcons.Pencil(
                modifier = Modifier.size(20.dp),
                color = KidsGColors.OrangePrimary
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = KidsGSpacing.sm),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextMuted)
                    )
                }

                if (!isClickableOnly) {
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = KidsGTypography.BodyMedium.copy(color = KidsGColors.TextPrimary),
                        singleLine = true,
                        cursorBrush = SolidColor(KidsGColors.OrangePrimary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch() })
                    )
                }
            }

            // Clear button if text entered
            if (query.isNotEmpty() && !isClickableOnly) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(KidsGShapes.FullPill)
                        .background(KidsGColors.SurfaceDesk)
                        .clickable { onQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        style = KidsGTypography.TitleSmall.copy(color = KidsGColors.TextSecondary)
                    )
                }
            }
        }
    }
}
