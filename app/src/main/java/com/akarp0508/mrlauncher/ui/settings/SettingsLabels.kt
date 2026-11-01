package com.akarp0508.mrlauncher.ui.settings

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.akarp0508.mrlauncher.R
import com.akarp0508.mrlauncher.data.AppInfo
import com.akarp0508.mrlauncher.data.FontChoice
import com.akarp0508.mrlauncher.data.Gesture
import com.akarp0508.mrlauncher.data.GestureAction
import com.akarp0508.mrlauncher.data.IconMode
import com.akarp0508.mrlauncher.data.ItemSpacing
import com.akarp0508.mrlauncher.data.LabelAlignment
import com.akarp0508.mrlauncher.data.TemperatureUnit
import com.akarp0508.mrlauncher.data.TextWeight
import com.akarp0508.mrlauncher.data.VerticalPosition

/** Next value, wrapping around; used by rows that cycle on tap. */
inline fun <reified E : Enum<E>> E.next(): E = enumValues<E>().let { it[(ordinal + 1) % it.size] }

val LabelAlignment.label: Int
    @StringRes get() = when (this) {
        LabelAlignment.Left -> R.string.alignment_left
        LabelAlignment.Center -> R.string.alignment_center
        LabelAlignment.Right -> R.string.alignment_right
    }

val IconMode.label: Int
    @StringRes get() = when (this) {
        IconMode.Off -> R.string.settings_off
        IconMode.Color -> R.string.icons_color
        IconMode.Monochrome -> R.string.icons_monochrome
    }

val VerticalPosition.label: Int
    @StringRes get() = when (this) {
        VerticalPosition.Top -> R.string.position_top
        VerticalPosition.Center -> R.string.position_center
        VerticalPosition.Bottom -> R.string.position_bottom
    }

val ItemSpacing.label: Int
    @StringRes get() = when (this) {
        ItemSpacing.Compact -> R.string.spacing_compact
        ItemSpacing.Normal -> R.string.spacing_normal
        ItemSpacing.Roomy -> R.string.spacing_roomy
    }

val TemperatureUnit.label: Int
    @StringRes get() = when (this) {
        TemperatureUnit.Celsius -> R.string.unit_celsius
        TemperatureUnit.Fahrenheit -> R.string.unit_fahrenheit
    }

val FontChoice.label: Int
    @StringRes get() = when (this) {
        FontChoice.System -> R.string.font_system
        FontChoice.Condensed -> R.string.font_condensed
        FontChoice.Serif -> R.string.font_serif
        FontChoice.Mono -> R.string.font_mono
        FontChoice.SerifMono -> R.string.font_serif_mono
        FontChoice.Casual -> R.string.font_casual
        FontChoice.Cursive -> R.string.font_cursive
        FontChoice.SmallCaps -> R.string.font_small_caps
        FontChoice.Custom -> R.string.font_custom
    }

val TextWeight.label: Int
    @StringRes get() = when (this) {
        TextWeight.Thin -> R.string.weight_thin
        TextWeight.ExtraLight -> R.string.weight_extra_light
        TextWeight.Light -> R.string.weight_light
        TextWeight.Regular -> R.string.weight_regular
        TextWeight.Medium -> R.string.weight_medium
        TextWeight.SemiBold -> R.string.weight_semibold
        TextWeight.Bold -> R.string.weight_bold
    }

val Gesture.label: Int
    @StringRes get() = when (this) {
        Gesture.SwipeUp -> R.string.gesture_swipe_up
        Gesture.SwipeDown -> R.string.gesture_swipe_down
        Gesture.SwipeLeft -> R.string.gesture_swipe_left
        Gesture.SwipeRight -> R.string.gesture_swipe_right
        Gesture.DoubleTap -> R.string.gesture_double_tap
        Gesture.ClockTap -> R.string.gesture_clock_tap
        Gesture.DateTap -> R.string.gesture_date_tap
        Gesture.WeatherTap -> R.string.gesture_weather_tap
    }

@Composable
fun GestureAction.label(apps: List<AppInfo>): String = when (this) {
    GestureAction.None -> stringResource(R.string.gesture_action_none)
    GestureAction.OpenDrawer -> stringResource(R.string.gesture_action_drawer)
    GestureAction.Notifications -> stringResource(R.string.gesture_action_notifications)
    GestureAction.QuickSettings -> stringResource(R.string.gesture_action_quick_settings)
    GestureAction.LockScreen -> stringResource(R.string.gesture_action_lock)
    GestureAction.OpenSettings -> stringResource(R.string.gesture_action_settings)
    GestureAction.EditHome -> stringResource(R.string.gesture_action_edit_home)
    GestureAction.Alarms -> stringResource(R.string.gesture_action_alarms)
    GestureAction.Calendar -> stringResource(R.string.gesture_action_calendar)
    GestureAction.WeatherApp -> stringResource(R.string.gesture_action_weather_app)
    GestureAction.RefreshWeather -> stringResource(R.string.gesture_action_refresh_weather)
    is GestureAction.OpenApp -> apps.firstOrNull { it.key == appKey }?.label
        ?: stringResource(R.string.gesture_action_missing_app)
}
