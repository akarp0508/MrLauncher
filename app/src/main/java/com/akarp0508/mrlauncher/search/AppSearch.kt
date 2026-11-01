package com.akarp0508.mrlauncher.search

import java.text.Normalizer

/**
 * Fuzzy-ish app search: accent-insensitive ("zolw" finds "Żółw"), ranks prefix matches first,
 * then word-start matches, then substring matches, then initials ("gm" finds "Google Maps").
 */
object AppSearch {

    private val combiningMarks = "\\p{Mn}+".toRegex()
    private val wordSeparators = charArrayOf(' ', '-', '_', '.', ':')

    fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(combiningMarks, "")
            // Ł doesn't decompose under NFD, so it needs special handling.
            .replace('ł', 'l')
            .replace('Ł', 'L')
            .lowercase()

    /** Returns matching [items], best matches first; ties keep their original order. */
    fun <T> filter(items: List<T>, query: String, label: (T) -> String): List<T> {
        val normalizedQuery = normalize(query.trim())
        if (normalizedQuery.isEmpty()) return items
        return items
            .mapNotNull { item -> rank(normalize(label(item)), normalizedQuery)?.let { item to it } }
            .sortedBy { it.second }
            .map { it.first }
    }

    private fun rank(label: String, query: String): Int? {
        val words = label.split(*wordSeparators).filter { it.isNotEmpty() }
        return when {
            label.startsWith(query) -> 0
            words.any { it.startsWith(query) } -> 1
            label.contains(query) -> 2
            words.joinToString("") { it.take(1) }.startsWith(query) -> 3
            else -> null
        }
    }
}
