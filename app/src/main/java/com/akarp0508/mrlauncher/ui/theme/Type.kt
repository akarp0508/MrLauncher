package com.akarp0508.mrlauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.akarp0508.mrlauncher.data.FontChoice
import com.akarp0508.mrlauncher.data.FontStore
import com.akarp0508.mrlauncher.data.LauncherSettings
import com.akarp0508.mrlauncher.data.TextWeight

/** Fonts that ship with Android itself, so nothing needs to be bundled or downloaded. */
fun fontFamilyFor(choice: FontChoice, customFontPath: String?): FontFamily = when (choice) {
    FontChoice.System -> FontFamily.Default
    FontChoice.Condensed -> deviceFont("sans-serif-condensed")
    FontChoice.Serif -> FontFamily.Serif
    FontChoice.Mono -> FontFamily.Monospace
    FontChoice.SerifMono -> deviceFont("serif-monospace")
    FontChoice.Casual -> deviceFont("casual")
    FontChoice.Cursive -> FontFamily.Cursive
    FontChoice.SmallCaps -> deviceFont("sans-serif-smallcaps")
    FontChoice.Custom -> customFontPath?.let(FontStore::load)?.let(::FontFamily) ?: FontFamily.Default
}

/**
 * A device font family with every weight declared. A single Font(DeviceFontFamilyName) is always
 * loaded at the weight it's declared with, so requesting Light from it would still give Regular.
 */
private fun deviceFont(name: String) = FontFamily(
    TextWeight.entries.map { Font(DeviceFontFamilyName(name), FontWeight(it.value)) },
)

/**
 * The launcher's own text, each part sized independently. Kept apart from Material's
 * [Typography] so resizing e.g. home apps can't also resize dialog titles.
 */
@Immutable
data class LauncherTextStyles(
    val clock: TextStyle,
    /** Date and weather under the clock. */
    val date: TextStyle,
    val homeApp: TextStyle,
    /** Drawer app list and its search field. */
    val drawerApp: TextStyle,
)

fun launcherTextStyles(fontFamily: FontFamily, settings: LauncherSettings, shadowColor: Color?): LauncherTextStyles {
    val text = TextBuilder(fontFamily, shadowColor)
    val weight = FontWeight(settings.textWeight.value)
    return LauncherTextStyles(
        clock = text.style(64 * settings.clockTextScale, weight, spacing = -1f),
        date = text.style(16 * settings.dateTextScale, weight, spacing = 0.5f),
        homeApp = text.style(26 * settings.homeTextScale, weight),
        drawerApp = text.style(20 * settings.drawerTextScale, weight),
    )
}

/** Material slots for menus, settings and dialogs, all sized by [scale]. [shadowColor] null means no shadow. */
fun launcherTypography(fontFamily: FontFamily, scale: Float, shadowColor: Color?): Typography {
    val text = TextBuilder(fontFamily, shadowColor)
    val defaults = Typography()
    return Typography(
        headlineSmall = text.style(26 * scale),
        titleLarge = text.style(20 * scale),
        titleMedium = text.style(16 * scale, spacing = 0.5f),
        bodyLarge = text.style(16 * scale, spacing = 0.3f),
        bodyMedium = text.style(defaults.bodyMedium.fontSize.value * scale),
        labelLarge = text.style(14 * scale, FontWeight.Medium, spacing = 0.5f),
    )
}

private class TextBuilder(private val fontFamily: FontFamily, shadowColor: Color?) {
    // Soft shadow keeps text legible on bare wallpaper.
    private val shadow = shadowColor?.let { Shadow(it, Offset(0f, 2f), blurRadius = 8f) }

    fun style(size: Float, weight: FontWeight = FontWeight.Normal, spacing: Float = 0f) = TextStyle(
        fontFamily = fontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        letterSpacing = spacing.sp,
        shadow = shadow,
    )
}
