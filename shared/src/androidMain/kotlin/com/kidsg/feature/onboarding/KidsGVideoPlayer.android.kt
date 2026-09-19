package com.kidsg.feature.onboarding

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun KidsGVideoPlayer(
    videoResName: String,
    modifier: Modifier,
    playWhenReady: Boolean,
    isLooping: Boolean,
    scaleMode: VideoScaleMode,
    onCompleted: () -> Unit
) {
    val context = LocalContext.current
    val currentOnCompleted by rememberUpdatedState(onCompleted)

    val videoUri = remember(videoResName, context) {
        var resId = context.resources.getIdentifier(videoResName, "raw", context.packageName)
        if (resId == 0) {
            resId = context.resources.getIdentifier(videoResName, "raw", "com.kidsg.app")
        }
        if (resId == 0) {
            resId = context.resources.getIdentifier(videoResName, "raw", "com.kidsg.shared")
        }
        if (resId != 0) {
            Uri.parse("android.resource://${context.packageName}/$resId")
        } else {
            null
        }
    }

    if (videoUri == null) {
        Box(modifier = modifier)
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val textureView = TextureView(ctx)
            var mediaPlayer: MediaPlayer? = null

            fun applyScale(viewWidth: Float, viewHeight: Float, videoWidth: Float, videoHeight: Float) {
                if (viewWidth <= 0f || viewHeight <= 0f || videoWidth <= 0f || videoHeight <= 0f) return

                val videoRatio = videoWidth / videoHeight
                val viewRatio = viewWidth / viewHeight

                val scaleX: Float
                val scaleY: Float

                when (scaleMode) {
                    VideoScaleMode.FIT -> {
                        if (videoRatio > viewRatio) {
                            scaleX = 1.0f
                            scaleY = (viewWidth / videoRatio) / viewHeight
                        } else {
                            scaleX = (viewHeight * videoRatio) / viewWidth
                            scaleY = 1.0f
                        }
                    }
                    VideoScaleMode.CROP -> {
                        if (videoRatio > viewRatio) {
                            scaleX = (viewHeight * videoRatio) / viewWidth
                            scaleY = 1.0f
                        } else {
                            scaleX = 1.0f
                            scaleY = (viewWidth / videoRatio) / viewHeight
                        }
                    }
                    VideoScaleMode.FILL_WIDTH -> {
                        scaleX = 1.0f
                        scaleY = (viewWidth / videoRatio) / viewHeight
                    }
                }

                val matrix = Matrix().apply {
                    setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
                }
                textureView.setTransform(matrix)
            }

            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                    try {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(ctx, videoUri)
                            setSurface(Surface(surface))
                            this.isLooping = isLooping
                            setOnCompletionListener {
                                // Keep last frame on the surface texture naturally; do not reset
                                currentOnCompleted()
                            }
                            setOnErrorListener { _, _, _ ->
                                currentOnCompleted()
                                true
                            }
                            setOnVideoSizeChangedListener { _, vWidth, vHeight ->
                                applyScale(
                                    textureView.width.toFloat(),
                                    textureView.height.toFloat(),
                                    vWidth.toFloat(),
                                    vHeight.toFloat()
                                )
                            }
                            prepareAsync()
                            setOnPreparedListener { mp ->
                                applyScale(
                                    textureView.width.toFloat(),
                                    textureView.height.toFloat(),
                                    mp.videoWidth.toFloat(),
                                    mp.videoHeight.toFloat()
                                )
                                if (playWhenReady) {
                                    mp.start()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        currentOnCompleted()
                    }
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                    mediaPlayer?.let { mp ->
                        if (mp.videoWidth > 0 && mp.videoHeight > 0) {
                            applyScale(width.toFloat(), height.toFloat(), mp.videoWidth.toFloat(), mp.videoHeight.toFloat())
                        }
                    }
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    try {
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        mediaPlayer = null
                    } catch (_: Exception) {}
                    return true
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            }

            textureView
        },
        update = { view ->
            // If playWhenReady changed dynamically
        }
    )
}
