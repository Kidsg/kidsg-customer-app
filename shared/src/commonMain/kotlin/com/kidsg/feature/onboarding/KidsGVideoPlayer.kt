package com.kidsg.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class VideoScaleMode {
    FIT,
    CROP,
    FILL_WIDTH
}

/**
 * Reusable, hardware-accelerated KidsG Video Player.
 * Plays trimmed MP4 videos from raw resources, handles lifecycle,
 * avoids memory leaks, and holds the last frame naturally on completion.
 */
@Composable
expect fun KidsGVideoPlayer(
    videoResName: String,
    modifier: Modifier = Modifier,
    playWhenReady: Boolean = true,
    isLooping: Boolean = false,
    scaleMode: VideoScaleMode = VideoScaleMode.FIT,
    onCompleted: () -> Unit = {}
)
