package com.akarp0508.mrlauncher.ui.theme

import androidx.compose.ui.graphics.Color

val LightText = Color(0xFFF5F5F5)
val DarkText = Color(0xFF141414)
val DarkDialogSurface = Color(0xFF161616)
val LightDialogSurface = Color(0xFFF4F4F4)

/** ARGB ints, same format as LauncherSettings.textColor / accentColor. */
val TextColorPresets = listOf(
    0xFFF5F5F5, // white
    0xFFF3E9D2, // cream
    0xFFCFF5E7, // mint
    0xFFCDE7FF, // sky
    0xFFE2D5FF, // lavender
    0xFFFFD6E0, // rose
    0xFF9E9E9E, // grey
    0xFF141414, // black
).map { it.toInt() }

val AccentColorPresets = listOf(
    0xFFFFB86C, // orange
    0xFFFF6E6E, // red
    0xFF50FA7B, // green
    0xFF8BE9FD, // cyan
    0xFFBD93F9, // purple
    0xFFFF79C6, // pink
    0xFFF1FA8C, // yellow
    0xFFFFFFFF, // white
).map { it.toInt() }
