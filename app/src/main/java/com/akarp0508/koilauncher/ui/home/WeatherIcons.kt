package com.akarp0508.koilauncher.ui.home

/**
 * Icon for a WMO weather code. [color] gives emoji; otherwise single-color symbols, where U+FE0E
 * asks for the plain-text form so they take the text color instead of rendering as emoji.
 */
fun weatherIcon(code: Int, isDay: Boolean, color: Boolean): String {
    val (symbol, emoji) = when (code) {
        0 -> if (isDay) "☀" to "☀️" else "☾" to "🌙"
        1 -> if (isDay) "☀" to "🌤️" else "☾" to "🌙"
        2 -> if (isDay) "⛅" to "⛅" else "☁" to "☁️"
        3 -> "☁" to "☁️"
        45, 48 -> "≈" to "🌫️"
        51, 53, 55, 80, 81, 82 -> "☂" to "🌦️"
        56, 57, 61, 63, 65, 66, 67 -> "☂" to "🌧️"
        71, 73, 75, 77, 85, 86 -> "❄" to "🌨️"
        95, 96, 99 -> "⚡" to "⛈️"
        else -> return ""
    }
    return if (color) emoji else symbol + TEXT_PRESENTATION
}

private const val TEXT_PRESENTATION = "︎"
