package com.kidsg.core.designsystem

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.readRawBytes
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.compose.ui.unit.dp

expect fun decodeByteArrayToBitmap(bytes: ByteArray): ImageBitmap?

private val imageCache = mutableMapOf<String, ImageBitmap>()

@Composable
fun KidsGAsyncImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: @Composable (() -> Unit)? = null
) {
    var bitmap by remember(url) { mutableStateOf(imageCache[url]) }
    var isLoading by remember(url) { mutableStateOf(bitmap == null && url.isNotBlank()) }

    LaunchedEffect(url) {
        if (url.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }
        if (imageCache.containsKey(url)) {
            bitmap = imageCache[url]
            isLoading = false
            return@LaunchedEffect
        }

        withContext(Dispatchers.Default) {
            try {
                val client = HttpClient()
                val response = client.get(url)
                val bytes = response.readRawBytes()
                client.close()
                val decoded = decodeByteArrayToBitmap(bytes)
                if (decoded != null) {
                    imageCache[url] = decoded
                    withContext(Dispatchers.Main) {
                        bitmap = decoded
                        isLoading = false
                    }
                } else {
                    withContext(Dispatchers.Main) { isLoading = false }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else if (isLoading) {
            if (placeholder != null) {
                placeholder()
            } else {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize(0.35f),
                        color = KidsGColors.OrangePrimary,
                        strokeWidth = 2.dp
                    )
                }
            }
        } else {
            placeholder?.invoke()
        }
    }
}
