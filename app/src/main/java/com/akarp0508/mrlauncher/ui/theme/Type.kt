package com.akarp0508.mrlauncher.ui.theme

import androidx.compose.material3.Typography
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

/** Fonts that ship with Android itself, so nothing needs to be bundled or downloaded. */
fun fontFamilyFor(choice: FontChoice, customFontPath: String?): FontFamily = when (choice) {
    FontChoice.System -> FontFamily.Default
    FontChoice.Thin -> deviceFont("sans-serif-thin")
    FontChoice.Light -> deviceFont("sans-serif-light")
    FontChoice.Condensed -> deviceFont("sans-serif-condensed")
    FontChoice.Medium -> deviceFont("sans-serif-medium")
    FontChoice.Serif -> FontFamily.Serif
    FontChoice.Mono -> FontFamily.Monospace
    FontChoice.SerifMono -> deviceFont("serif-monospace")
    FontChoice.Casual -> deviceFont("casual")
    FontChoice.Cursive -> FontFamily.Cursive
    FontChoice.SmallCaps -> deviceFont("sans-serif-smallcaps")
    FontChoice.Custom -> customFontPath?.let(FontStore::load)?.let(::FontFamily) ?: FontFamily.Default
}

private fun deviceFont(name: String) = FontFamily(Font(DeviceFontFamilyName(name)))

// Slots used by the launcher:
//   displayLarge  - clock          titleMedium - date, weather
//   headlineSmall - home app list  titleLarge  - drawer app list
//   bodyLarge     - general text   labelLarge  - hints / secondary
/** [shadowColor] null means no shadow. */
fun launcherTypography(fontFamily: FontFamily, scale: Float, shadowColor: Color?): Typography {
    val shadow = shadowColor?.let { Shadow(it, Offset(0f, 2f), blurRadius = 8f) }
    fun style(size: Int, weight: FontWeight = FontWeight.Normal, spacing: Float = 0f) = TextStyle(
        fontFamily = fontFamily,
        fontWeight = weight,
        fontSize = (size * scale).sp,
        letterSpacing = spacing.sp,
        shadow = shadow,
    )
    return Typography(
        displayLarge = style(64, FontWeight.Light, spacing = -1f),
        titleMedium = style(16, spacing = 0.5f),
        headlineSmall = style(26),
        titleLarge = style(20),
        bodyLarge = style(16, spacing = 0.3f),
        labelLarge = style(14, FontWeight.Medium, spacing = 0.5f),
    )
}
