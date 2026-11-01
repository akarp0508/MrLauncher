package com.akarp0508.mrlauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads app icons (with the work-profile badge where relevant) and keeps recent ones in memory. */
class AppIconLoader(context: Context) {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)

    private val cache = object : LruCache<String, Bitmap>(MAX_CACHE_BYTES) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    /** Already-loaded icon, if any; lets lists show icons immediately when scrolling back. */
    fun cached(app: AppInfo, sizePx: Int): Bitmap? = cache.get(cacheKey(app, sizePx))

    suspend fun load(app: AppInfo, sizePx: Int): Bitmap? = withContext(Dispatchers.IO) {
        cached(app, sizePx)?.let { return@withContext it }
        val intent = Intent(Intent.ACTION_MAIN).setComponent(app.componentName)
        val info = runCatching { launcherApps.resolveActivity(intent, app.user) }.getOrNull()
            ?: return@withContext null
        runCatching { info.getBadgedIcon(0).toBitmap(sizePx, sizePx) }.getOrNull()
            ?.also { cache.put(cacheKey(app, sizePx), it) }
    }

    /** Call when apps change, so updated icons get picked up. */
    fun clear() = cache.evictAll()

    private fun cacheKey(app: AppInfo, sizePx: Int) = "${app.key}@$sizePx"

    private companion object {
        const val MAX_CACHE_BYTES = 16 * 1024 * 1024
    }
}
