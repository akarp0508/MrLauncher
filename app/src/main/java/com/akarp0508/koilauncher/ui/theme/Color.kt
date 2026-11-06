package com.akarp0508.koilauncher.ui.theme

import androidx.compose.ui.graphics.Color

val LightText = Color(0xFFF4EFE6)
val DarkText = Color(0xFF141414)

/** ARGB ints, same format as LauncherSettings.textColor / accentColor. */
val TextColorPresets = listOf(
    0xFFF4EFE6, // koi paper
    0xFFF5F5F5, // white
    0xFFF3E9D2, // cream
    0xFFCFF5E7, // mint
    0xFFCDE7FF, // sky
    0xFFE2D5FF, // lavender
    0xFFFFD6E0, // rose
    0xFF9E9E9E, // grey
    0xFF141414, // black
).map { it.toInt() }

val BackgroundColorPresets = listOf(
    0xFF111318, // koi ink
    0xFF000000, // black
    0xFF121212, // near black
    0xFF1E1E24, // charcoal
    0xFF0F1A2B, // navy
    0xFF0F2A1E, // forest
    0xFF2A1028, // plum
    0xFFF3E9D2, // cream
    0xFFFFFFFF, // white
).map { it.toInt() }

val AccentColorPresets = listOf(
    0xFFC6F432, // koi lime
    0xFFFF5A1F, // koi orange
    0xFFFFB86C, // orange
    0xFFFF6E6E, // red
    0xFF50FA7B, // green
    0xFF8BE9FD, // cyan
    0xFFBD93F9, // purple
    0xFFFF79C6, // pink
    0xFFF1FA8C, // yellow
    0xFFFFFFFF, // white
).map { it.toInt() }
