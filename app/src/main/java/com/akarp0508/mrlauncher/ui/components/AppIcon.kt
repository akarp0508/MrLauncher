package com.akarp0508.mrlauncher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.akarp0508.mrlauncher.data.AppIconLoader
import com.akarp0508.mrlauncher.data.AppInfo
import com.akarp0508.mrlauncher.ui.theme.LauncherTheme
import kotlin.math.roundToInt

/** Null in previews and anywhere icons aren't wired up; [AppIcon] then draws an empty slot. */
val LocalAppIconLoader = staticCompositionLocalOf<AppIconLoader?> { null }

/** An app icon of [size]; the space is reserved while it loads so text doesn't jump. */
@Composable
fun AppIcon(app: AppInfo, size: Dp, monochrome: Boolean, modifier: Modifier = Modifier) {
    val loader = LocalAppIconLoader.current
    val sizePx = with(LocalDensity.current) { size.toPx().roundToInt() }
    // Keyed state, so a slot reused for a different app (e.g. after reordering) never shows the
    // previous app's icon. Cache hits are synchronous, so scrolling back doesn't flicker.
    var icon by remember(loader, app.key, sizePx, monochrome) {
        mutableStateOf(loader?.cached(app, sizePx, monochrome))
    }
    LaunchedEffect(loader, app.key, sizePx, monochrome) {
        if (icon == null) icon = loader?.load(app, sizePx, monochrome)
    }
    val current = icon
    if (current == null) {
        Spacer(modifier.size(size))
    } else {
        val bitmap = remember(current) { current.bitmap.asImageBitmap() }
        Image(
            bitmap = bitmap,
            // Decorative: the label next to it already names the app.
            contentDescription = null,
            colorFilter = if (current.isGlyph) ColorFilter.tint(LauncherTheme.colors.text) else null,
            modifier = modifier.size(size),
        )
    }
}
