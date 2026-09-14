package com.kidsg.feature.splash

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun SplashVideoPlayer(
    modifier: Modifier,
    onVideoFinished: () -> Unit
) {
    val context = LocalContext.current
    val videoUri = remember(context) {
        val resId = context.resources.getIdentifier("splash_video", "raw", context.packageName)
        if (resId != 0) {
            Uri.parse("android.resource://${context.packageName}/$resId")
        } else {
            null
        }
    }

    if (videoUri != null) {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(videoUri)
                    setOnCompletionListener {
                        onVideoFinished()
                    }
                    setOnErrorListener { _, _, _ ->
                        onVideoFinished()
                        true
                    }
                    start()
                }
            },
            update = { videoView ->
                if (!videoView.isPlaying) {
                    videoView.start()
                }
            }
        )
    } else {
        DisposableEffect(Unit) {
            onVideoFinished()
            onDispose { }
        }
    }
}
