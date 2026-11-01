package com.akarp0508.mrlauncher.system

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.Toast
import com.akarp0508.mrlauncher.R

object SystemActions {

    fun expandNotifications(context: Context) = statusBar(context, "expandNotificationsPanel")

    fun expandQuickSettings(context: Context) = statusBar(context, "expandSettingsPanel")

    fun openAlarms(context: Context) = start(context, Intent(AlarmClock.ACTION_SHOW_ALARMS))

    fun openCalendar(context: Context) = start(
        context,
        Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALENDAR),
    )

    fun openDefaultLauncherSettings(context: Context) =
        start(context, Intent(Settings.ACTION_HOME_SETTINGS))

    fun openAccessibilitySettings(context: Context) =
        start(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))

    fun lockScreen(context: Context) {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.P ->
                Toast.makeText(context, R.string.lock_unsupported, Toast.LENGTH_LONG).show()
            !LockScreenService.lock() -> {
                Toast.makeText(context, R.string.lock_enable_prompt, Toast.LENGTH_LONG).show()
                openAccessibilitySettings(context)
            }
        }
    }

    /** Not public API, but every minimalist launcher does this and it works on most devices. */
    @SuppressLint("WrongConstant")
    private fun statusBar(context: Context, method: String) {
        runCatching {
            val statusBar = context.getSystemService("statusbar")
            Class.forName("android.app.StatusBarManager").getMethod(method).invoke(statusBar)
        }
    }

    private fun start(context: Context, intent: Intent) {
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
