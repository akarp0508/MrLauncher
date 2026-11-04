package com.akarp0508.koilauncher.system

import android.annotation.SuppressLint
import android.content.Context
import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri
import com.akarp0508.koilauncher.R

object SystemActions {

    fun expandNotifications(context: Context) = statusBar(context, "expandNotificationsPanel")

    fun expandQuickSettings(context: Context) = statusBar(context, "expandSettingsPanel")

    /** The standard intent first; some clock apps don't handle it, so fall back to launching one directly. */
    fun openAlarms(context: Context) {
        if (start(context, Intent(AlarmClock.ACTION_SHOW_ALARMS))) return
        val launch = ClockApps.firstNotNullOfOrNull { context.packageManager.getLaunchIntentForPackage(it) }
        if (launch == null || !start(context, launch)) {
            Toast.makeText(context, R.string.alarms_unavailable, Toast.LENGTH_LONG).show()
        }
    }

    private val ClockApps = listOf(
        "com.google.android.deskclock",
        "com.android.deskclock",
        "com.sec.android.app.clockpackage", // Samsung
        "com.oneplus.deskclock",
        "com.coloros.alarmclock", // Oppo/Realme
        "com.huawei.deskclock",
        "com.motorola.timeweatherwidget",
    )

    fun openCalendar(context: Context) = start(
        context,
        Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALENDAR),
    )

    /**
     * Android has no standard "weather app" intent, so try well-known weather apps, then fall back
     * to a web search for the weather in [place], which most search apps answer with a forecast.
     */
    fun openWeather(context: Context, place: String?) {
        val launch = WeatherApps.firstNotNullOfOrNull { context.packageManager.getLaunchIntentForPackage(it) }
        if (launch != null) {
            start(context, launch)
            return
        }
        val query = listOfNotNull(context.getString(R.string.weather_search_query), place).joinToString(" ")
        val search = Intent(Intent.ACTION_WEB_SEARCH).putExtra(SearchManager.QUERY, query)
        if (!start(context, search)) {
            start(context, Intent(Intent.ACTION_VIEW, "https://www.google.com/search?q=${Uri.encode(query)}".toUri()))
        }
    }

    // Most specific first: a phone's own weather app beats one that just happens to be installed.
    private val WeatherApps = listOf(
        "com.google.android.apps.weather", // Pixel Weather
        "com.sec.android.daemonapp", // Samsung
        "com.miui.weather2", // Xiaomi
        "net.oneplus.weather", // OnePlus
        "com.huawei.android.totemweather", // Huawei
        "com.accuweather.android",
        "com.weather.Weather", // The Weather Channel
        "org.breezyweather",
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

    /** False if nothing could handle [intent]. */
    private fun start(context: Context, intent: Intent): Boolean =
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess
}
