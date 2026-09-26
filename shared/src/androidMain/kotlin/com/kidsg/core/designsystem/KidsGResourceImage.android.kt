package com.kidsg.core.designsystem

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.util.concurrent.ConcurrentHashMap

// Global high-performance bitmap and resource ID caches to guarantee 60-120fps scrolling
private val globalBitmapCache = ConcurrentHashMap<String, ImageBitmap>()
private val globalResIdCache = ConcurrentHashMap<String, Int>()

@Composable
actual fun KidsGResourceImage(
    resName: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    val context = LocalContext.current
    val imageBitmap = remember(resName) {
        // 1. Check in-memory bitmap cache first (instant O(1) lookup during scrolling)
        globalBitmapCache[resName] ?: run {
            // 2. Resolve resource ID with cached lookup
            val resId = globalResIdCache.getOrPut(resName) {
                var id = context.resources.getIdentifier(resName, "drawable", context.packageName)
                if (id == 0) id = context.resources.getIdentifier(resName, "drawable", "com.kidsg.app")
                if (id == 0) id = context.resources.getIdentifier(resName, "drawable", "com.kidsg.shared")
                id
            }

            if (resId != 0) {
                try {
                    val opts = BitmapFactory.Options().apply {
                        inScaled = true
                    }
                    val bitmap = BitmapFactory.decodeResource(context.resources, resId, opts)
                    bitmap?.asImageBitmap()?.also {
                        globalBitmapCache[resName] = it
                    }
                } catch (e: Throwable) {
                    null
                }
            } else {
                null
            }
        }
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(modifier = modifier)
    }
}
