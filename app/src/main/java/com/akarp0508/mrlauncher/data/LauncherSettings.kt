package com.akarp0508.mrlauncher.data

/** Always physical left/right, regardless of the language's text direction. */
enum class LabelAlignment { Left, Center, Right }

enum class VerticalPosition { Top, Center, Bottom }

enum class ItemSpacing { Compact, Normal, Roomy }

enum class FontChoice {
    System, Thin, Light, Condensed, Medium, Serif, Mono, SerifMono, Casual, Cursive, SmallCaps, Custom,
}

enum class TemperatureUnit { Celsius, Fahrenheit }

enum class Gesture { SwipeUp, SwipeDown, SwipeLeft, SwipeRight, DoubleTap, ClockTap, DateTap }

sealed interface GestureAction {
    /** Persisted form; must stay stable across releases. */
    val id: String

    data object None : GestureAction { override val id = "none" }
    data object OpenDrawer : GestureAction { override val id = "drawer" }
    data object Notifications : GestureAction { override val id = "notifications" }
    data object QuickSettings : GestureAction { override val id = "quick_settings" }
    data object LockScreen : GestureAction { override val id = "lock" }
    data object OpenSettings : GestureAction { override val id = "settings" }
    data object EditHome : GestureAction { override val id = "edit_home" }
    data object Alarms : GestureAction { override val id = "alarms" }
    data object Calendar : GestureAction { override val id = "calendar" }
    data class OpenApp(val appKey: String) : GestureAction {
        override val id get() = APP_PREFIX + appKey
    }

    companion object {
        private const val APP_PREFIX = "app:"

        /** Everything except [OpenApp], which needs an app picked. */
        val simpleActions = listOf(
            None, OpenDrawer, Notifications, QuickSettings, LockScreen, OpenSettings, EditHome, Alarms, Calendar,
        )

        fun fromId(id: String?): GestureAction? = when {
            id == null -> null
            id.startsWith(APP_PREFIX) -> OpenApp(id.removePrefix(APP_PREFIX))
            else -> simpleActions.firstOrNull { it.id == id }
        }
    }
}

val DefaultGestures: Map<Gesture, GestureAction> = mapOf(
    Gesture.SwipeUp to GestureAction.OpenDrawer,
    Gesture.SwipeDown to GestureAction.Notifications,
    Gesture.SwipeLeft to GestureAction.None,
    Gesture.SwipeRight to GestureAction.None,
    Gesture.DoubleTap to GestureAction.None,
    Gesture.ClockTap to GestureAction.Alarms,
    Gesture.DateTap to GestureAction.Calendar,
)

data class WeatherLocation(val name: String, val latitude: Double, val longitude: Double)

/** [LauncherSettings.textColor] value meaning "pick light or dark text from the wallpaper". */
const val AUTO_TEXT_COLOR = 0

data class LauncherSettings(
    // Apps
    /** [AppInfo.key]s pinned to the home screen, in display order. */
    val homeApps: List<String> = emptyList(),
    val hiddenApps: Set<String> = emptySet(),
    /** [AppInfo.key] -> user-chosen label. */
    val renamedApps: Map<String, String> = emptyMap(),

    // Home layout
    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val homeAlignment: LabelAlignment = LabelAlignment.Left,
    val homeVerticalPosition: VerticalPosition = VerticalPosition.Center,
    val itemSpacing: ItemSpacing = ItemSpacing.Normal,
    val showStatusBar: Boolean = true,

    // Drawer
    val drawerAlignment: LabelAlignment = LabelAlignment.Left,
    val autoLaunchSingleMatch: Boolean = true,
    val autoShowKeyboard: Boolean = true,

    // Look
    val font: FontChoice = FontChoice.System,
    val customFontPath: String? = null,
    /** Menus, settings, dialogs and hints. */
    val textScale: Float = 1f,
    val clockTextScale: Float = 1f,
    val dateTextScale: Float = 1f,
    val homeTextScale: Float = 1f,
    val drawerTextScale: Float = 1f,
    /** ARGB, or [AUTO_TEXT_COLOR]. */
    val textColor: Int = AUTO_TEXT_COLOR,
    val accentColor: Int = 0xFFFFB86C.toInt(),
    /** Opaque ARGB; drawn over the wallpaper at the per-screen opacities below. */
    val backgroundColor: Int = 0xFF000000.toInt(),
    /** 0 = bare wallpaper, 1 = solid [backgroundColor]. */
    val homeBackgroundAlpha: Float = 0f,
    val drawerBackgroundAlpha: Float = 0.65f,

    // Weather
    val showWeather: Boolean = false,
    val weatherLocation: WeatherLocation? = null,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.Celsius,

    val gestures: Map<Gesture, GestureAction> = DefaultGestures,
) {
    fun gestureAction(gesture: Gesture): GestureAction =
        gestures[gesture] ?: DefaultGestures.getValue(gesture)
}
