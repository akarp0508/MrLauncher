package com.akarp0508.mrlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GestureActionTest {

    @Test
    fun everyActionRoundTripsThroughItsId() {
        val actions = GestureAction.simpleActions + GestureAction.OpenApp("com.example/.Main#0")
        actions.forEach { assertEquals(it, GestureAction.fromId(it.id)) }
    }

    @Test
    fun unknownIdsDecodeToNull() {
        assertNull(GestureAction.fromId(null))
        assertNull(GestureAction.fromId("teleport"))
    }

    @Test
    fun everyGestureHasADefault() {
        val settings = LauncherSettings(gestures = emptyMap())
        Gesture.entries.forEach { assertEquals(DefaultGestures[it], settings.gestureAction(it)) }
    }
}
