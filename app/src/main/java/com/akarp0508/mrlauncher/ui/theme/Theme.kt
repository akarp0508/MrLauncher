package com.akarp0508.mrlauncher.ui.theme

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akarp0508.mrlauncher.data.AUTO_TEXT_COLOR
import com.akarp0508.mrlauncher.data.ItemSpacing
import com.akarp0508.mrlauncher.data.LauncherSettings

/** Colors for one surface (home, drawer, dialog…); they differ by how opaque its background is. */
@Immutable
data class LauncherColors(
    val text: Color,
    val textDim: Color,
    val accent: Color,
    /** Fill drawn over the wallpaper: the background color at this surface's opacity. */
    val background: Color,
    /** True when text is dark; system bar icons follow this. */
    val isDarkText: Boolean,
)

/** The user's color choices, from which each surface derives its [LauncherColors]. */
@Immutable
private data class Palette(
    /** Null means "auto": pick light or dark text from what's behind it. */
    val fixedText: Color?,
    val accent: Color,
    val background: Color,
    val wallpaperIsLight: Boolean,
) {
    fun colorsFor(opacity: Float): LauncherColors {
        val text = fixedText ?: autoText(opacity)
        return LauncherColors(
            text = text,
            textDim = text.copy(alpha = 0.6f),
            accent = accent,
            background = background.copy(alpha = opacity),
            isDarkText = text.luminance() < 0.5f,
        )
    }

    private fun autoText(opacity: Float): Color {
        // Rough brightness of what's behind the text: the background color laid over the wallpaper.
        val wallpaper = if (wallpaperIsLight) 1f else 0f
        val behind = opacity * background.luminance() + (1 - opacity) * wallpaper
        return if (behind > 0.5f) DarkText else LightText
    }
}

@Immutable
private data class LauncherStyle(
    val palette: Palette,
    val colors: LauncherColors,
    val itemSpacing: Dp,
    val fontFamily: FontFamily,
    /** Text sizes are read from here when building styles. */
    val settings: LauncherSettings,
    val textStyles: LauncherTextStyles,
)

private val LocalLauncherStyle = staticCompositionLocalOf<LauncherStyle> {
    error("LauncherTheme used outside MrLauncherTheme")
}

object LauncherTheme {
    /** Colors of the surface currently being drawn. */
    val colors: LauncherColors
        @Composable @ReadOnlyComposable get() = LocalLauncherStyle.current.colors

    /** Vertical padding around each app label. */
    val itemSpacing: Dp
        @Composable @ReadOnlyComposable get() = LocalLauncherStyle.current.itemSpacing

    /** Clock, date, home and drawer text; see [LauncherTextStyles]. */
    val textStyles: LauncherTextStyles
        @Composable @ReadOnlyComposable get() = LocalLauncherStyle.current.textStyles

    @Composable
    @ReadOnlyComposable
    fun colorsFor(opacity: Float): LauncherColors = LocalLauncherStyle.current.palette.colorsFor(opacity)
}

/**
 * Root theme. Content directly inside it (dialogs) gets colors for a fully opaque background;
 * screens should wrap themselves in [LauncherSurface] with their own opacity.
 */
@Composable
fun MrLauncherTheme(settings: LauncherSettings, content: @Composable () -> Unit) {
    val palette = Palette(
        fixedText = settings.textColor.takeIf { it != AUTO_TEXT_COLOR }?.let(::Color),
        accent = Color(settings.accentColor),
        background = Color(settings.backgroundColor),
        wallpaperIsLight = rememberWallpaperIsLight(),
    )
    val itemSpacing = when (settings.itemSpacing) {
        ItemSpacing.Compact -> 4.dp
        ItemSpacing.Normal -> 10.dp
        ItemSpacing.Roomy -> 16.dp
    }
    val fontFamily = remember(settings.font, settings.customFontPath) {
        fontFamilyFor(settings.font, settings.customFontPath)
    }
    val typography = remember(fontFamily, settings.textScale) {
        launcherTypography(fontFamily, settings.textScale, shadowColor = null)
    }

    val dialog = palette.colorsFor(1f)
    // Nudge dialogs slightly toward the text color so they stand out from a same-colored screen.
    val dialogSurface = lerp(palette.background, dialog.text, 0.08f)
    val onAccent = if (palette.accent.luminance() > 0.5f) Color.Black else Color.White
    val colorScheme = if (dialog.isDarkText) {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = onAccent,
            background = Color.Transparent,
            surface = dialogSurface,
            surfaceContainerHigh = dialogSurface,
            onSurface = dialog.text,
            onSurfaceVariant = dialog.text,
        )
    } else {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = onAccent,
            background = Color.Transparent,
            surface = dialogSurface,
            surfaceContainerHigh = dialogSurface,
            onSurface = dialog.text,
            onSurfaceVariant = dialog.text,
        )
    }

    MaterialTheme(colorScheme = colorScheme, typography = typography) {
        CompositionLocalProvider(
            LocalContentColor provides dialog.text,
            LocalLauncherStyle provides LauncherStyle(
                palette = palette,
                colors = dialog,
                itemSpacing = itemSpacing,
                fontFamily = fontFamily,
                settings = settings,
                textStyles = remember(fontFamily, settings) { launcherTextStyles(fontFamily, settings, shadowColor = null) },
            ),
            content = content,
        )
    }
}

/** A full-screen layer over the wallpaper, filled with the background color at [opacity]. */
@Composable
fun LauncherSurface(opacity: Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val style = LocalLauncherStyle.current
    val colors = remember(style.palette, opacity) { style.palette.colorsFor(opacity) }
    // A shadow keeps text legible on bare wallpaper; over a solid background it's just blur.
    val shadowAlpha = 0.45f * (1f - opacity)
    val shadowColor = if (shadowAlpha > 0f) {
        (if (colors.isDarkText) Color.White else Color.Black).copy(alpha = shadowAlpha)
    } else {
        null
    }
    val typography = remember(style.fontFamily, style.settings.textScale, shadowColor) {
        launcherTypography(style.fontFamily, style.settings.textScale, shadowColor)
    }
    val textStyles = remember(style.fontFamily, style.settings, shadowColor) {
        launcherTextStyles(style.fontFamily, style.settings, shadowColor)
    }
    MaterialTheme(colorScheme = MaterialTheme.colorScheme, typography = typography) {
        CompositionLocalProvider(
            LocalContentColor provides colors.text,
            LocalLauncherStyle provides style.copy(colors = colors, textStyles = textStyles),
        ) {
            Box(modifier.fillMaxSize().background(colors.background)) { content() }
        }
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
