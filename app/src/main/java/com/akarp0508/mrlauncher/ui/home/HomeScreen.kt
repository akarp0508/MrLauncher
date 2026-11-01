package com.akarp0508.mrlauncher.ui.home

import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.akarp0508.mrlauncher.R
import com.akarp0508.mrlauncher.data.AppInfo
import com.akarp0508.mrlauncher.data.DateStyle
import com.akarp0508.mrlauncher.data.Gesture
import com.akarp0508.mrlauncher.data.IconMode
import com.akarp0508.mrlauncher.data.LauncherSettings
import com.akarp0508.mrlauncher.data.VerticalPosition
import com.akarp0508.mrlauncher.data.Weather
import com.akarp0508.mrlauncher.ui.components.AppLabel
import com.akarp0508.mrlauncher.ui.components.homeGestures
import com.akarp0508.mrlauncher.ui.components.horizontal
import com.akarp0508.mrlauncher.ui.components.textAlign
import com.akarp0508.mrlauncher.ui.theme.LauncherTheme
import kotlinx.coroutines.delay
import java.util.Date

// Edit mode puts its controls in the side margins and its toolbar in the top margin, so it needs
// at least this much room; with smaller margins it shifts content in a little while editing.
private val MinEditSideMargin = 28.dp
internal val MinEditTopMargin = 48.dp

/**
 * The home screen. With an [editor] it becomes edit mode: same layout, but gestures are off and
 * apps get reorder/remove controls, so what you arrange is what you get.
 */
@Composable
fun HomeScreen(
    apps: List<AppInfo>,
    weather: Weather?,
    settings: LauncherSettings,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onGesture: (Gesture) -> Unit,
    onEditHome: () -> Unit,
    editor: HomeEditor? = null,
) {
    val alignment = settings.homeAlignment.horizontal
    val clockAlignment = settings.clockAlignment.horizontal
    val textAlign = settings.homeAlignment.textAlign
    val position = settings.homeVerticalPosition
    val sideMargin = settings.homeSideMargin.dp.let { if (editor != null) max(it, MinEditSideMargin) else it }
    val verticalMargin = settings.homeVerticalMargin.dp
    val topMargin = if (editor != null) max(verticalMargin, MinEditTopMargin) else verticalMargin
    val sidePadding = Modifier.padding(horizontal = sideMargin)

    Box(Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = alignment,
            modifier = Modifier
                .fillMaxSize()
                .then(if (editor == null) Modifier.homeGestures(onGesture, onLongPress = onEditHome) else Modifier)
                .systemBarsPadding()
                .padding(top = topMargin, bottom = verticalMargin),
        ) {
            Header(
                showTime = settings.showClock,
                showDate = settings.showDate,
                dateStyle = settings.dateStyle,
                weather = weather,
                weatherIconMode = settings.weatherIcon,
                horizontalAlignment = clockAlignment,
                interactive = editor == null,
                onClockClick = { onGesture(Gesture.ClockTap) },
                onDateClick = { onGesture(Gesture.DateTap) },
                onWeatherClick = { onGesture(Gesture.WeatherTap) },
                modifier = sidePadding.align(clockAlignment),
            )

            if (position == VerticalPosition.Top) Spacer(Modifier.height(48.dp)) else Spacer(Modifier.weight(1f))
            if (editor != null) {
                EditableHomeApps(apps, settings.homeAlignment, settings.homeIcons, sideMargin, editor)
            } else {
                if (apps.isEmpty()) {
                    Text(
                        text = stringResource(R.string.home_empty_hint),
                        style = MaterialTheme.typography.labelLarge,
                        color = LauncherTheme.colors.textDim,
                        textAlign = textAlign,
                        modifier = sidePadding,
                    )
                }
                apps.forEach { app ->
                    AppLabel(
                        app = app,
                        style = LauncherTheme.textStyles.homeApp,
                        textAlign = textAlign,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) },
                        icons = settings.homeIcons,
                        modifier = sidePadding,
                    )
                }
            }
            if (position != VerticalPosition.Bottom) Spacer(Modifier.weight(1f))
        }

        if (editor != null) EditToolbar(editor, apps, sideMargin, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun Header(
    showTime: Boolean,
    showDate: Boolean,
    dateStyle: DateStyle,
    weather: Weather?,
    weatherIconMode: IconMode,
    horizontalAlignment: Alignment.Horizontal,
    /** False in edit mode: the header is shown for layout only. */
    interactive: Boolean,
    onClockClick: () -> Unit,
    onDateClick: () -> Unit,
    onWeatherClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(60_000 - value % 60_000) // tick on the minute
        }
    }
    val time = remember(now) { DateFormat.getTimeFormat(context).format(Date(now)) }
    val date = remember(now, dateStyle) { dateStyle.format(now) }
    val dim = LauncherTheme.colors.textDim

    Column(horizontalAlignment = horizontalAlignment, modifier = modifier) {
        if (showTime) {
            Text(
                time,
                style = LauncherTheme.textStyles.clock,
                modifier = Modifier.clickable(enabled = interactive, onClick = onClockClick),
            )
        }
        if (showDate) {
            Text(
                date,
                style = LauncherTheme.textStyles.date,
                color = dim,
                modifier = Modifier.clickable(enabled = interactive, onClick = onDateClick),
            )
        }
        if (weather != null) {
            val description = stringResource(R.string.weather_format, weather.temperature, stringResource(weatherDescription(weather.code)))
            val icon = when (weatherIconMode) {
                IconMode.Off -> ""
                IconMode.Color -> weatherIcon(weather.code, weather.isDay, color = true)
                IconMode.Monochrome -> weatherIcon(weather.code, weather.isDay, color = false)
            }
            Text(
                text = if (icon.isEmpty()) description else "$icon $description",
                style = LauncherTheme.textStyles.date,
                color = dim,
                modifier = Modifier.clickable(enabled = interactive, onClick = onWeatherClick),
            )
        }
    }
}

/** WMO weather interpretation codes, as returned by Open-Meteo. */
@StringRes
private fun weatherDescription(code: Int): Int = when (code) {
    0 -> R.string.weather_clear
    1 -> R.string.weather_mostly_clear
    2 -> R.string.weather_partly_cloudy
    3 -> R.string.weather_overcast
    45, 48 -> R.string.weather_fog
    51, 53, 55 -> R.string.weather_drizzle
    56, 57 -> R.string.weather_freezing_drizzle
    61, 63 -> R.string.weather_rain
    65 -> R.string.weather_heavy_rain
    66, 67 -> R.string.weather_freezing_rain
    71, 73, 77 -> R.string.weather_snow
    75 -> R.string.weather_heavy_snow
    80, 81, 82 -> R.string.weather_showers
    85, 86 -> R.string.weather_snow_showers
    95, 96, 99 -> R.string.weather_thunderstorm
    else -> R.string.weather_unknown
}
