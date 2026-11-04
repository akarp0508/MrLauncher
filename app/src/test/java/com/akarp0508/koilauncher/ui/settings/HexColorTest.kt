package com.akarp0508.koilauncher.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HexColorTest {

    @Test
    fun parsesWithOrWithoutHashAsOpaque() {
        assertEquals(0xFFFFB86C.toInt(), parseHexColor("#ffb86c"))
        assertEquals(0xFF000000.toInt(), parseHexColor(" 000000 "))
    }

    @Test
    fun rejectsMalformedInput() {
        listOf("", "#fff", "#12345g", "#80FFFFFF").forEach { assertNull(it, parseHexColor(it)) }
    }
}
