package com.akarp0508.koilauncher.search

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSearchTest {

    private fun search(query: String, vararg labels: String) =
        AppSearch.filter(labels.toList(), query) { it }

    @Test
    fun emptyQueryReturnsEverything() {
        assertEquals(listOf("b", "a"), search("  ", "b", "a"))
    }

    @Test
    fun prefixMatchesComeBeforeWordAndSubstringMatches() {
        assertEquals(
            listOf("Camera", "Google Camera", "Webcam"),
            search("cam", "Webcam", "Google Camera", "Camera", "Clock"),
        )
    }

    @Test
    fun ignoresCaseAndDiacritics() {
        assertEquals(listOf("Żółw"), search("zo", "Żółw", "Łódź"))
        assertEquals(listOf("Łódź"), search("lodz", "Żółw", "Łódź"))
    }

    @Test
    fun matchesInitials() {
        assertEquals(listOf("Gmail", "Google Maps"), search("gm", "Google Maps", "Gmail", "Messages"))
    }
}
