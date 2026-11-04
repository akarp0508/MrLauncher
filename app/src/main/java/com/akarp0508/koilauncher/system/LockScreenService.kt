package com.akarp0508.koilauncher.system

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent

/**
 * Exists only so "lock screen" gestures can call [GLOBAL_ACTION_LOCK_SCREEN]; Android offers no
 * other way for a launcher to lock without device-admin. It listens to no events.
 */
class LockScreenService : AccessibilityService() {

    override fun onServiceConnected() {
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    companion object {
        private var instance: LockScreenService? = null

        /** False if the service isn't enabled or the OS is too old. */
        fun lock(): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                instance?.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN) == true
    }
}
