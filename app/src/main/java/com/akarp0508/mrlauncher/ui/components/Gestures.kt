package com.akarp0508.mrlauncher.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.akarp0508.mrlauncher.data.Gesture
import kotlin.math.abs

private val SwipeThreshold = 56.dp

/**
 * Detects the home-screen swipes (by dominant direction, once per gesture), double taps and
 * long presses. Children with click handlers still work: a drag past touch slop cancels their
 * click. A long press on a child may fire [onLongPress] as well as the child's own handler.
 */
fun Modifier.homeGestures(onGesture: (Gesture) -> Unit, onLongPress: () -> Unit): Modifier = this
    .pointerInput(onGesture) {
        val threshold = SwipeThreshold.toPx()
        var total = Offset.Zero
        detectDragGestures(
            onDragStart = { total = Offset.Zero },
            onDragEnd = {
                val (dx, dy) = total
                val gesture = when {
                    abs(dy) >= abs(dx) && dy < -threshold -> Gesture.SwipeUp
                    abs(dy) >= abs(dx) && dy > threshold -> Gesture.SwipeDown
                    abs(dx) > abs(dy) && dx < -threshold -> Gesture.SwipeLeft
                    abs(dx) > abs(dy) && dx > threshold -> Gesture.SwipeRight
                    else -> null
                }
                gesture?.let(onGesture)
            },
        ) { change, dragAmount ->
            change.consume()
            total += dragAmount
        }
    }
    .pointerInput(onGesture, onLongPress) {
        detectTapGestures(
            onDoubleTap = { onGesture(Gesture.DoubleTap) },
            onLongPress = { onLongPress() },
        )
    }
