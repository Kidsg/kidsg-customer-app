package com.kidsg.feature.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
actual fun SplashVideoPlayer(
    modifier: Modifier,
    onVideoFinished: () -> Unit
) {
    // Graceful fallback for iOS targets / simulator
    LaunchedEffect(Unit) {
        delay(2500)
        onVideoFinished()
    }
}
