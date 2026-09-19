package com.kidsg.feature.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
actual fun KidsGVideoPlayer(
    videoResName: String,
    modifier: Modifier,
    playWhenReady: Boolean,
    isLooping: Boolean,
    scaleMode: VideoScaleMode,
    onCompleted: () -> Unit
) {
    LaunchedEffect(videoResName) {
        if (!isLooping) {
            delay(2500)
            onCompleted()
        }
    }
    Box(modifier = modifier)
}
