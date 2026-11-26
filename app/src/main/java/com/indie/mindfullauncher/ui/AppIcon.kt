package com.indie.mindfullauncher.ui

import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.indie.mindfullauncher.data.IconPackHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private object AppIconCache {
    private val cache = LruCache<String, ImageBitmap>(180)

    fun get(key: String): ImageBitmap? = cache.get(key)

    fun put(key: String, bitmap: ImageBitmap) {
        cache.put(key, bitmap)
    }
}

@Composable
fun AppIcon(
    packageName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconPackPackage: String? = null,
    monochrome: Boolean = false
) {
    val context = LocalContext.current
    val cacheKey = "${iconPackPackage.orEmpty()}:$packageName"
    var bitmap by remember(cacheKey) { mutableStateOf(AppIconCache.get(cacheKey)) }

    LaunchedEffect(cacheKey) {
        if (bitmap == null) {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    val drawable = IconPackHelper.loadAppIconDrawable(context, packageName, iconPackPackage)
                    drawable?.toBitmap(width = 128, height = 128)?.asImageBitmap()
                }.getOrNull()
            }
            if (loaded != null) {
                AppIconCache.put(cacheKey, loaded)
                bitmap = loaded
            }
        }
    }

    val colorFilter = remember(monochrome) {
        if (monochrome) {
            ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
        } else {
            null
        }
    }

    val current = bitmap
    if (current != null) {
        Image(
            bitmap = current,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            colorFilter = colorFilter,
            modifier = modifier.size(size)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}
