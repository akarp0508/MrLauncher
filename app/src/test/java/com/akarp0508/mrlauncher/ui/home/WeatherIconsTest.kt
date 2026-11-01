package com.akarp0508.mrlauncher.ui.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherIconsTest {

    @Test
    fun clearSkyShowsSunByDayAndMoonByNight() {
        assertEquals("☀️", weatherIcon(0, isDay = true, color = true))
        assertEquals("🌙", weatherIcon(0, isDay = false, color = true))
    }

    @Test
    fun monochromeSymbolsAskForTextPresentation() {
        assertEquals("☂︎", weatherIcon(63, isDay = true, color = false))
    }

    @Test
    fun everyDocumentedCodeHasAnIconAndUnknownCodesHaveNone() {
        val codes = listOf(0, 1, 2, 3, 45, 48, 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 71, 73, 75, 77, 80, 81, 82, 85, 86, 95, 96, 99)
        codes.forEach { assertTrue("code $it", weatherIcon(it, isDay = true, color = true).isNotEmpty()) }
        assertEquals("", weatherIcon(42, isDay = true, color = false))
    }
}
