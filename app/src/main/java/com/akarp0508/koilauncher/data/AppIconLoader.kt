package com.akarp0508.koilauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.util.LruCache
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadedIcon(
    val bitmap: Bitmap,
    /** A single-color shape meant to be tinted (e.g. with the text color), not shown as is. */
    val isGlyph: Boolean,
)

/** Loads app icons (with the work-profile badge where relevant) and keeps recent ones in memory. */
class AppIconLoader(context: Context) {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)

    private val cache = object : LruCache<String, LoadedIcon>(MAX_CACHE_BYTES) {
        override fun sizeOf(key: String, value: LoadedIcon) = value.bitmap.byteCount
    }

    /** Already-loaded icon, if any; lets lists show icons immediately when scrolling back. */
    fun cached(app: AppInfo, sizePx: Int, monochrome: Boolean): LoadedIcon? =
        cache.get(cacheKey(app, sizePx, monochrome))

    /**
     * With [monochrome], prefers the app's themed-icon glyph (Android 13+) and otherwise falls
     * back to a greyscale version of its normal icon.
     */
    suspend fun load(app: AppInfo, sizePx: Int, monochrome: Boolean): LoadedIcon? = withContext(Dispatchers.IO) {
        cached(app, sizePx, monochrome)?.let { return@withContext it }
        val intent = Intent(Intent.ACTION_MAIN).setComponent(app.componentName)
        val info = runCatching { launcherApps.resolveActivity(intent, app.user) }.getOrNull()
            ?: return@withContext null
        runCatching {
            val glyph = if (monochrome && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                themedGlyph(info, sizePx)
            } else {
                null
            }
            glyph?.let { LoadedIcon(it, isGlyph = true) } ?: run {
                val bitmap = info.getBadgedIcon(0).toBitmap(sizePx, sizePx)
                LoadedIcon(if (monochrome) bitmap.greyscale() else bitmap, isGlyph = false)
            }
        }.getOrNull()?.also { cache.put(cacheKey(app, sizePx, monochrome), it) }
    }

    /** Call when apps change, so updated icons get picked up. */
    fun clear() = cache.evictAll()

    /** The monochrome layer apps provide for Android's themed icons, cropped like a normal icon. */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun themedGlyph(info: LauncherActivityInfo, sizePx: Int): Bitmap? {
        val adaptive = info.getIcon(0) as? AdaptiveIconDrawable ?: return null
        val glyph = adaptive.monochrome ?: return null
        // Adaptive icon layers are 108dp with only the middle 72dp visible, so the layer is drawn
        // 1.5x the output size and centred.
        val overflow = sizePx / 4
        glyph.setBounds(-overflow, -overflow, sizePx + overflow, sizePx + overflow)
        return createBitmap(sizePx, sizePx).also { glyph.draw(Canvas(it)) }
    }

    private fun Bitmap.greyscale(): Bitmap {
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) }
        return createBitmap(width, height).also { Canvas(it).drawBitmap(this, 0f, 0f, paint) }
    }

    private fun cacheKey(app: AppInfo, sizePx: Int, monochrome: Boolean) =
        "${app.key}@$sizePx${if (monochrome) "/mono" else ""}"

    private companion object {
        const val MAX_CACHE_BYTES = 16 * 1024 * 1024
    }
}
