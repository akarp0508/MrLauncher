package com.akarp0508.koilauncher.data

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
            dateStyle = this[DATE_STYLE].toEnum(defaults.dateStyle),
            homeAlignment = this[HOME_ALIGNMENT].migrateAlignment().toEnum(defaults.homeAlignment),
            // Until set separately, the clock keeps following the alignment the apps had.
            clockAlignment = (this[CLOCK_ALIGNMENT] ?: this[HOME_ALIGNMENT])
                .migrateAlignment().toEnum(defaults.clockAlignment),
            homeVerticalPosition = this[HOME_POSITION].toEnum(defaults.homeVerticalPosition),
            itemSpacing = this[ITEM_SPACING].toEnum(defaults.itemSpacing),
            homeIcons = this[HOME_ICONS].toEnumOrNull<IconMode>() ?: legacyIconMode(LEGACY_SHOW_HOME_ICONS) ?: defaults.homeIcons,
            drawerIcons = this[DRAWER_ICONS].toEnumOrNull<IconMode>() ?: legacyIconMode(LEGACY_SHOW_DRAWER_ICONS) ?: defaults.drawerIcons,
            showStatusBar = this[SHOW_STATUS_BAR] ?: defaults.showStatusBar,
            homeSideMargin = this[HOME_SIDE_MARGIN] ?: defaults.homeSideMargin,
            homeVerticalMargin = this[HOME_VERTICAL_MARGIN] ?: defaults.homeVerticalMargin,
            drawerSideMargin = this[DRAWER_SIDE_MARGIN] ?: defaults.drawerSideMargin,
            drawerAlignment = this[DRAWER_ALIGNMENT].migrateAlignment().toEnum(defaults.drawerAlignment),
            autoLaunchSingleMatch = this[AUTO_LAUNCH] ?: defaults.autoLaunchSingleMatch,
            autoShowKeyboard = this[AUTO_KEYBOARD] ?: defaults.autoShowKeyboard,
            // Thin/Light/Medium used to be fonts; unknown names fall back to System below.
            font = this[FONT].toEnum(defaults.font),
            textWeight = this[TEXT_WEIGHT].toEnumOrNull<TextWeight>()
                ?: this[FONT].toEnumOrNull<TextWeight>()
                ?: defaults.textWeight,
            customFontPath = this[CUSTOM_FONT_PATH],
            textScale = this[TEXT_SCALE] ?: defaults.textScale,
            clockTextScale = this[CLOCK_TEXT_SCALE] ?: defaults.clockTextScale,
            dateTextScale = this[DATE_TEXT_SCALE] ?: defaults.dateTextScale,
            homeTextScale = this[HOME_TEXT_SCALE] ?: defaults.homeTextScale,
            drawerTextScale = this[DRAWER_TEXT_SCALE] ?: defaults.drawerTextScale,
            textColor = this[TEXT_COLOR] ?: defaults.textColor,
            accentColor = this[ACCENT_COLOR] ?: defaults.accentColor,
            backgroundColor = this[BACKGROUND_COLOR] ?: defaults.backgroundColor,
            homeBackgroundAlpha = this[HOME_BACKGROUND_ALPHA] ?: defaults.homeBackgroundAlpha,
            drawerBackgroundAlpha = this[DRAWER_BACKGROUND_ALPHA] ?: defaults.drawerBackgroundAlpha,
            showWeather = this[SHOW_WEATHER] ?: defaults.showWeather,
            weatherIcon = this[WEATHER_ICON].toEnum(defaults.weatherIcon),
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
        this[DATE_STYLE] = settings.dateStyle.name
        this[HOME_ALIGNMENT] = settings.homeAlignment.name
        this[CLOCK_ALIGNMENT] = settings.clockAlignment.name
        this[HOME_POSITION] = settings.homeVerticalPosition.name
        this[ITEM_SPACING] = settings.itemSpacing.name
        this[HOME_ICONS] = settings.homeIcons.name
        this[DRAWER_ICONS] = settings.drawerIcons.name
        remove(LEGACY_SHOW_HOME_ICONS)
        remove(LEGACY_SHOW_DRAWER_ICONS)
        remove(LEGACY_MONOCHROME_ICONS)
        this[SHOW_STATUS_BAR] = settings.showStatusBar
        this[HOME_SIDE_MARGIN] = settings.homeSideMargin
        this[HOME_VERTICAL_MARGIN] = settings.homeVerticalMargin
        this[DRAWER_SIDE_MARGIN] = settings.drawerSideMargin
        this[DRAWER_ALIGNMENT] = settings.drawerAlignment.name
        this[AUTO_LAUNCH] = settings.autoLaunchSingleMatch
        this[AUTO_KEYBOARD] = settings.autoShowKeyboard
        this[FONT] = settings.font.name
        this[TEXT_WEIGHT] = settings.textWeight.name
        setOrRemove(CUSTOM_FONT_PATH, settings.customFontPath)
        this[TEXT_SCALE] = settings.textScale
        this[CLOCK_TEXT_SCALE] = settings.clockTextScale
        this[DATE_TEXT_SCALE] = settings.dateTextScale
        this[HOME_TEXT_SCALE] = settings.homeTextScale
        this[DRAWER_TEXT_SCALE] = settings.drawerTextScale
        this[TEXT_COLOR] = settings.textColor
        this[ACCENT_COLOR] = settings.accentColor
        this[BACKGROUND_COLOR] = settings.backgroundColor
        this[HOME_BACKGROUND_ALPHA] = settings.homeBackgroundAlpha
        this[DRAWER_BACKGROUND_ALPHA] = settings.drawerBackgroundAlpha
        this[SHOW_WEATHER] = settings.showWeather
        this[WEATHER_ICON] = settings.weatherIcon.name
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

    /** Icons used to be an on/off per place plus one shared monochrome switch. */
    private fun Preferences.legacyIconMode(shown: Preferences.Key<Boolean>): IconMode? = when (this[shown]) {
        null -> null
        false -> IconMode.Off
        true -> if (this[LEGACY_MONOCHROME_ICONS] == true) IconMode.Monochrome else IconMode.Color
    }

    /** Alignment used to be stored as Start/End before it became Left/Right. */
    private fun String?.migrateAlignment(): String? = when (this) {
        "Start" -> LabelAlignment.Left.name
        "End" -> LabelAlignment.Right.name
        else -> this
    }

    private inline fun <reified E : Enum<E>> String?.toEnumOrNull(): E? =
        enumValues<E>().firstOrNull { it.name == this }

    private inline fun <reified E : Enum<E>> String?.toEnum(default: E): E = toEnumOrNull<E>() ?: default

    private companion object {
        // Package and class names can't contain a newline, so it's a safe list separator.
        const val SEPARATOR = "\n"
        const val RENAME_PREFIX = "rename:"

        fun gestureKey(gesture: Gesture) = stringPreferencesKey("gesture:${gesture.name}")

        val HOME_APPS = stringPreferencesKey("home_apps")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val SHOW_CLOCK = booleanPreferencesKey("show_clock")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val DATE_STYLE = stringPreferencesKey("date_style")
        val HOME_ALIGNMENT = stringPreferencesKey("alignment")
        val CLOCK_ALIGNMENT = stringPreferencesKey("clock_alignment")
        val HOME_POSITION = stringPreferencesKey("home_position")
        val ITEM_SPACING = stringPreferencesKey("item_spacing")
        val HOME_ICONS = stringPreferencesKey("home_icons")
        val DRAWER_ICONS = stringPreferencesKey("drawer_icons")
        val LEGACY_SHOW_HOME_ICONS = booleanPreferencesKey("show_home_icons")
        val LEGACY_SHOW_DRAWER_ICONS = booleanPreferencesKey("show_drawer_icons")
        val LEGACY_MONOCHROME_ICONS = booleanPreferencesKey("monochrome_icons")
        val SHOW_STATUS_BAR = booleanPreferencesKey("show_status_bar")
        val HOME_SIDE_MARGIN = intPreferencesKey("home_side_margin")
        val HOME_VERTICAL_MARGIN = intPreferencesKey("home_vertical_margin")
        val DRAWER_SIDE_MARGIN = intPreferencesKey("drawer_side_margin")
        val DRAWER_ALIGNMENT = stringPreferencesKey("drawer_alignment")
        val AUTO_LAUNCH = booleanPreferencesKey("auto_launch_single_match")
        val AUTO_KEYBOARD = booleanPreferencesKey("auto_show_keyboard")
        val FONT = stringPreferencesKey("font")
        val TEXT_WEIGHT = stringPreferencesKey("text_weight")
        val CUSTOM_FONT_PATH = stringPreferencesKey("custom_font_path")
        val TEXT_SCALE = floatPreferencesKey("text_scale")
        val CLOCK_TEXT_SCALE = floatPreferencesKey("clock_text_scale")
        val DATE_TEXT_SCALE = floatPreferencesKey("date_text_scale")
        val HOME_TEXT_SCALE = floatPreferencesKey("home_text_scale")
        val DRAWER_TEXT_SCALE = floatPreferencesKey("drawer_text_scale")
        val TEXT_COLOR = intPreferencesKey("text_color")
        val ACCENT_COLOR = intPreferencesKey("accent_color")
        val BACKGROUND_COLOR = intPreferencesKey("background_color")
        val HOME_BACKGROUND_ALPHA = floatPreferencesKey("home_background_alpha")
        // Old key name kept so the existing "background dim" value carries over.
        val DRAWER_BACKGROUND_ALPHA = floatPreferencesKey("scrim_alpha")
        val SHOW_WEATHER = booleanPreferencesKey("show_weather")
        val WEATHER_ICON = stringPreferencesKey("weather_icon")
        val WEATHER_NAME = stringPreferencesKey("weather_name")
        val WEATHER_LAT = doublePreferencesKey("weather_lat")
        val WEATHER_LON = doublePreferencesKey("weather_lon")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    }
}
