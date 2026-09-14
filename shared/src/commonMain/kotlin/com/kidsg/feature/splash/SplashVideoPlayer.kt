package com.kidsg.feature.splash

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-agnostic Splash Video Player
 * Plays the user-provided generated splash video with auto-completion callback.
 */
@Composable
expect fun SplashVideoPlayer(
    modifier: Modifier = Modifier,
    onVideoFinished: () -> Unit
)
