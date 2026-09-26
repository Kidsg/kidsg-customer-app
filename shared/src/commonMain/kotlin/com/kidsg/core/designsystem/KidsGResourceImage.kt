package com.kidsg.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

/**
 * Multiplatform resource image loader that renders bundled local drawables/assets.
 */
@Composable
expect fun KidsGResourceImage(
    resName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
)
