package com.akarp0508.mrlauncher.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "launcher_settings")

class SettingsRepository(private val context: Context) {

    val settings: Flow<LauncherSettings> = context.dataStore.data.map { it.toSettings() }

    suspend fun update(transform: (LauncherSettings) -> LauncherSettings) {
        context.dataStore.edit { prefs -> prefs.write(transform(prefs.toSettings())) }
    }

    private fun Preferences.toSettings(): LauncherSettings {
        val defaults = LauncherSettings()
        val weatherName = this[WEATHER_NAME]
        val weatherLat = this[WEATHER_LAT]
        val weatherLon = this[WEATHER_LON]
        return LauncherSettings(
            homeApps = this[HOME_APPS].orEmpty().split(SEPARATOR).filter { it.isNotEmpty() },
            hiddenApps = this[HIDDEN_APPS].orEmpty(),
            renamedApps = withPrefix(RENAME_PREFIX),
            showClock = this[SHOW_CLOCK] ?: defaults.showClock,
            showDate = this[SHOW_DATE] ?: defaults.showDate,
            homeAlignment = this[HOME_ALIGNMENT].migrateAlignment().toEnum(defaults.homeAlignment),
            homeVerticalPosition = this[HOME_POSITION].toEnum(defaults.homeVerticalPosition),
            itemSpacing = this[ITEM_SPACING].toEnum(defaults.itemSpacing),
            showStatusBar = this[SHOW_STATUS_BAR] ?: defaults.showStatusBar,
            drawerAlignment = this[DRAWER_ALIGNMENT].migrateAlignment().toEnum(defaults.drawerAlignment),
            autoLaunchSingleMatch = this[AUTO_LAUNCH] ?: defaults.autoLaunchSingleMatch,
            autoShowKeyboard = this[AUTO_KEYBOARD] ?: defaults.autoShowKeyboard,
            font = this[FONT].toEnum(defaults.font),
            customFontPath = this[CUSTOM_FONT_PATH],
            textScale = this[TEXT_SCALE] ?: defaults.textScale,
            textColor = this[TEXT_COLOR] ?: defaults.textColor,
            accentColor = this[ACCENT_COLOR] ?: defaults.accentColor,
            backgroundColor = this[BACKGROUND_COLOR] ?: defaults.backgroundColor,
            homeBackgroundAlpha = this[HOME_BACKGROUND_ALPHA] ?: defaults.homeBackgroundAlpha,
            drawerBackgroundAlpha = this[DRAWER_BACKGROUND_ALPHA] ?: defaults.drawerBackgroundAlpha,
            showWeather = this[SHOW_WEATHER] ?: defaults.showWeather,
            weatherLocation = if (weatherName != null && weatherLat != null && weatherLon != null) {
                WeatherLocation(weatherName, weatherLat, weatherLon)
            } else {
                null
            },
            temperatureUnit = this[TEMPERATURE_UNIT].toEnum(defaults.temperatureUnit),
            gestures = Gesture.entries.associateWith { gesture ->
                GestureAction.fromId(this[gestureKey(gesture)]) ?: DefaultGestures.getValue(gesture)
            },
        )
    }

    private fun MutablePreferences.write(settings: LauncherSettings) {
        this[HOME_APPS] = settings.homeApps.joinToString(SEPARATOR)
        this[HIDDEN_APPS] = settings.hiddenApps
        replacePrefixed(RENAME_PREFIX, settings.renamedApps)
        this[SHOW_CLOCK] = settings.showClock
        this[SHOW_DATE] = settings.showDate
        this[HOME_ALIGNMENT] = settings.homeAlignment.name
        this[HOME_POSITION] = settings.homeVerticalPosition.name
        this[ITEM_SPACING] = settings.itemSpacing.name
        this[SHOW_STATUS_BAR] = settings.showStatusBar
        this[DRAWER_ALIGNMENT] = settings.drawerAlignment.name
        this[AUTO_LAUNCH] = settings.autoLaunchSingleMatch
        this[AUTO_KEYBOARD] = settings.autoShowKeyboard
        this[FONT] = settings.font.name
        setOrRemove(CUSTOM_FONT_PATH, settings.customFontPath)
        this[TEXT_SCALE] = settings.textScale
        this[TEXT_COLOR] = settings.textColor
        this[ACCENT_COLOR] = settings.accentColor
        this[BACKGROUND_COLOR] = settings.backgroundColor
        this[HOME_BACKGROUND_ALPHA] = settings.homeBackgroundAlpha
        this[DRAWER_BACKGROUND_ALPHA] = settings.drawerBackgroundAlpha
        this[SHOW_WEATHER] = settings.showWeather
        setOrRemove(WEATHER_NAME, settings.weatherLocation?.name)
        setOrRemove(WEATHER_LAT, settings.weatherLocation?.latitude)
        setOrRemove(WEATHER_LON, settings.weatherLocation?.longitude)
        this[TEMPERATURE_UNIT] = settings.temperatureUnit.name
        Gesture.entries.forEach { this[gestureKey(it)] = settings.gestureAction(it).id }
    }

    private fun Preferences.withPrefix(prefix: String): Map<String, String> =
        asMap().entries
            .filter { it.key.name.startsWith(prefix) }
            .associate { it.key.name.removePrefix(prefix) to it.value as String }

    private fun MutablePreferences.replacePrefixed(prefix: String, values: Map<String, String>) {
        asMap().keys.filter { it.name.startsWith(prefix) }.forEach { this -= it }
        values.forEach { (key, value) -> this[stringPreferencesKey(prefix + key)] = value }
    }

    private fun <T> MutablePreferences.setOrRemove(key: Preferences.Key<T>, value: T?) {
        if (value == null) remove(key) else this[key] = value
    }

    /** Alignment used to be stored as Start/End before it became Left/Right. */
    private fun String?.migrateAlignment(): String? = when (this) {
        "Start" -> LabelAlignment.Left.name
        "End" -> LabelAlignment.Right.name
        else -> this
    }

    private inline fun <reified E : Enum<E>> String?.toEnum(default: E): E =
        enumValues<E>().firstOrNull { it.name == this } ?: default

    private companion object {
        // Package and class names can't contain a newline, so it's a safe list separator.
        const val SEPARATOR = "\n"
        const val RENAME_PREFIX = "rename:"

        fun gestureKey(gesture: Gesture) = stringPreferencesKey("gesture:${gesture.name}")

        val HOME_APPS = stringPreferencesKey("home_apps")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val HOME_ALIGNMENT = stringPreferencesKey("alignment")
        val HOME_POSITION = stringPreferencesKey("home_position")
        val ITEM_SPACING = stringPreferencesKey("item_spacing")
        val SHOW_STATUS_BAR = booleanPreferencesKey("show_status_bar")
        val DRAWER_ALIGNMENT = stringPreferencesKey("drawer_alignment")
        val AUTO_LAUNCH = booleanPreferencesKey("auto_launch_single_match")
        val AUTO_KEYBOARD = booleanPreferencesKey("auto_show_keyboard")
        val FONT = stringPreferencesKey("font")
        val CUSTOM_FONT_PATH = stringPreferencesKey("custom_font_path")
        val TEXT_SCALE = floatPreferencesKey("text_scale")
        val TEXT_COLOR = intPreferencesKey("text_color")
        val ACCENT_COLOR = intPreferencesKey("accent_color")
        val BACKGROUND_COLOR = intPreferencesKey("background_color")
        val HOME_BACKGROUND_ALPHA = floatPreferencesKey("home_background_alpha")
        // Old key name kept so the existing "background dim" value carries over.
        val DRAWER_BACKGROUND_ALPHA = floatPreferencesKey("scrim_alpha")
        val SHOW_WEATHER = booleanPreferencesKey("show_weather")
        val WEATHER_NAME = stringPreferencesKey("weather_name")
        val WEATHER_LAT = doublePreferencesKey("weather_lat")
        val WEATHER_LON = doublePreferencesKey("weather_lon")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    }
}
