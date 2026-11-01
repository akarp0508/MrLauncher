package com.akarp0508.mrlauncher.ui.theme

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akarp0508.mrlauncher.data.AUTO_TEXT_COLOR
import com.akarp0508.mrlauncher.data.ItemSpacing
import com.akarp0508.mrlauncher.data.LauncherSettings

@Immutable
data class LauncherColors(
    val text: Color,
    val textDim: Color,
    val accent: Color,
    /** Layer over the wallpaper behind the drawer and settings. */
    val scrim: Color,
    /** True when text is dark, i.e. the wallpaper is light; system bar icons follow this. */
    val isDarkText: Boolean,
)

@Immutable
private data class LauncherStyle(val colors: LauncherColors, val itemSpacing: Dp)

private val LocalLauncherStyle = staticCompositionLocalOf {
    LauncherStyle(
        colors = LauncherColors(LightText, LightText.copy(alpha = 0.6f), Color(0xFFFFB86C), Color.Black, false),
        itemSpacing = 10.dp,
    )
}

object LauncherTheme {
    val colors: LauncherColors
        @Composable @ReadOnlyComposable get() = LocalLauncherStyle.current.colors

    /** Vertical padding around each app label. */
    val itemSpacing: Dp
        @Composable @ReadOnlyComposable get() = LocalLauncherStyle.current.itemSpacing
}

@Composable
fun MrLauncherTheme(settings: LauncherSettings, content: @Composable () -> Unit) {
    val wallpaperIsLight = rememberWallpaperIsLight()
    val text = when {
        settings.textColor != AUTO_TEXT_COLOR -> Color(settings.textColor)
        wallpaperIsLight -> DarkText
        else -> LightText
    }
    val isDarkText = text.luminance() < 0.5f
    val accent = Color(settings.accentColor)
    val colors = LauncherColors(
        text = text,
        textDim = text.copy(alpha = 0.6f),
        accent = accent,
        scrim = (if (isDarkText) Color.White else Color.Black).copy(alpha = settings.scrimAlpha),
        isDarkText = isDarkText,
    )
    val itemSpacing = when (settings.itemSpacing) {
        ItemSpacing.Compact -> 4.dp
        ItemSpacing.Normal -> 10.dp
        ItemSpacing.Roomy -> 16.dp
    }

    val fontFamily = remember(settings.font, settings.customFontPath) {
        fontFamilyFor(settings.font, settings.customFontPath)
    }
    val shadowColor = if (isDarkText) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.45f)
    val typography = remember(fontFamily, settings.textScale, shadowColor) {
        launcherTypography(fontFamily, settings.textScale, shadowColor)
    }

    // Dialogs follow the text: dark text means a light wallpaper, so light dialogs.
    val colorScheme = if (isDarkText) {
        lightColorScheme(
            primary = accent,
            background = Color.Transparent,
            surface = LightDialogSurface,
            surfaceContainerHigh = LightDialogSurface,
            onSurface = DarkText,
        )
    } else {
        darkColorScheme(
            primary = accent,
            onPrimary = Color.Black,
            background = Color.Transparent,
            surface = DarkDialogSurface,
            surfaceContainerHigh = DarkDialogSurface,
            onSurface = LightText,
        )
    }

    MaterialTheme(colorScheme = colorScheme, typography = typography) {
        // There's no Surface behind the launcher (it draws on the wallpaper), so nothing
        // would set the content color and Text would fall back to black.
        CompositionLocalProvider(
            LocalContentColor provides text,
            LocalLauncherStyle provides LauncherStyle(colors, itemSpacing),
            content = content,
        )
    }
}

@Composable
private fun rememberWallpaperIsLight(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return false
    val context = LocalContext.current
    val wallpaperManager = remember(context) { WallpaperManager.getInstance(context) }
    var isLight by remember {
        mutableStateOf(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM).isLight())
    }
    DisposableEffect(wallpaperManager) {
        val listener = WallpaperManager.OnColorsChangedListener { colors, which ->
            if (which and WallpaperManager.FLAG_SYSTEM != 0) isLight = colors.isLight()
        }
        wallpaperManager.addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
        onDispose { wallpaperManager.removeOnColorsChangedListener(listener) }
    }
    return isLight
}

@RequiresApi(Build.VERSION_CODES.O_MR1)
private fun WallpaperColors?.isLight(): Boolean = when {
    this == null -> false
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
        colorHints and WallpaperColors.HINT_SUPPORTS_DARK_TEXT != 0
    else -> primaryColor.luminance() > 0.5f
}
