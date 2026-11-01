package com.akarp0508.mrlauncher.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect

/**
 * The current time, refreshed every minute, when the time or time zone changes, and whenever the
 * launcher comes back into view. A plain delay loop isn't enough: it pauses while the phone sleeps,
 * so after waking the clock would show the time the screen went off until the next tick.
 */
@Composable
fun rememberCurrentTime(): Long {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                now = System.currentTimeMillis()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED) // also sent when 12/24-hour format changes
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        // Only system broadcasts, which reach non-exported receivers too.
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }
    LifecycleStartEffect(Unit) {
        now = System.currentTimeMillis()
        onStopOrDispose { }
    }
    return now
}
