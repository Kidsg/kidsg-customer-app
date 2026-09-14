package com.kidsg.feature.splash

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Android Splash Video Player with CenterCrop scaling
 * Fills 100% of the screen width and height with ZERO letterbox gaps at top or bottom.
 */
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
                val textureView = TextureView(ctx)
                var mediaPlayer: MediaPlayer? = null

                fun applyCenterCrop(viewWidth: Float, viewHeight: Float, videoWidth: Float, videoHeight: Float) {
                    if (viewWidth <= 0f || viewHeight <= 0f || videoWidth <= 0f || videoHeight <= 0f) return

                    val videoRatio = videoWidth / videoHeight
                    val viewRatio = viewWidth / viewHeight

                    val scaleX: Float
                    val scaleY: Float

                    if (videoRatio > viewRatio) {
                        // Video is wider than screen -> scale width to fill height, crop left/right
                        scaleX = (viewHeight * videoRatio) / viewWidth
                        scaleY = 1.0f
                    } else {
                        // Video is taller than screen -> scale height to fill width, crop top/bottom
                        scaleX = 1.0f
                        scaleY = (viewWidth / videoRatio) / viewHeight
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
                                isLooping = false
                                setOnCompletionListener { onVideoFinished() }
                                setOnErrorListener { _, _, _ ->
                                    onVideoFinished()
                                    true
                                }
                                setOnVideoSizeChangedListener { _, vWidth, vHeight ->
                                    applyCenterCrop(
                                        textureView.width.toFloat(),
                                        textureView.height.toFloat(),
                                        vWidth.toFloat(),
                                        vHeight.toFloat()
                                    )
                                }
                                prepareAsync()
                                setOnPreparedListener { mp ->
                                    applyCenterCrop(
                                        textureView.width.toFloat(),
                                        textureView.height.toFloat(),
                                        mp.videoWidth.toFloat(),
                                        mp.videoHeight.toFloat()
                                    )
                                    mp.start()
                                }
                            }
                        } catch (e: Exception) {
                            onVideoFinished()
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                        mediaPlayer?.let { mp ->
                            applyCenterCrop(width.toFloat(), height.toFloat(), mp.videoWidth.toFloat(), mp.videoHeight.toFloat())
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
            }
        )
    } else {
        DisposableEffect(Unit) {
            onVideoFinished()
            onDispose { }
        }
    }
}
