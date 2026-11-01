package com.akarp0508.mrlauncher.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akarp0508.mrlauncher.R
import com.akarp0508.mrlauncher.data.Gesture
import com.akarp0508.mrlauncher.data.LauncherSettings
import com.akarp0508.mrlauncher.ui.LauncherUiState
import com.akarp0508.mrlauncher.ui.home.format
import com.akarp0508.mrlauncher.ui.theme.AccentColorPresets
import com.akarp0508.mrlauncher.ui.theme.BackgroundColorPresets
import com.akarp0508.mrlauncher.ui.theme.TextColorPresets
import kotlin.math.roundToInt

enum class SettingsPage(@StringRes val title: Int, @StringRes val summary: Int) {
    Home(R.string.settings_page_home, R.string.settings_page_home_summary),
    Clock(R.string.settings_page_clock, R.string.settings_page_clock_summary),
    Weather(R.string.settings_page_weather, R.string.settings_page_weather_summary),
    Drawer(R.string.settings_page_drawer, R.string.settings_page_drawer_summary),
    Appearance(R.string.settings_page_appearance, R.string.settings_page_appearance_summary),
    Gestures(R.string.settings_page_gestures, R.string.settings_page_gestures_summary),
    System(R.string.settings_page_system, R.string.settings_page_system_summary),
}

/** What every settings page needs: current state, a way to change it, and the shared dialogs. */
internal class SettingsPageScope(
    val state: LauncherUiState,
    val onUpdate: ((LauncherSettings) -> LauncherSettings) -> Unit,
    val openDialog: (SettingsDialog) -> Unit,
    val onEditHome: () -> Unit,
    val onSetDefaultLauncher: () -> Unit,
    val onOpenAccessibilitySettings: () -> Unit,
    /** For date format samples; settings isn't open long enough for this to go stale. */
    val now: Long,
) {
    val settings: LauncherSettings get() = state.settings
}

internal fun LazyListScope.settingsPage(page: SettingsPage, scope: SettingsPageScope) = when (page) {
    SettingsPage.Home -> homePage(scope)
    SettingsPage.Clock -> clockPage(scope)
    SettingsPage.Weather -> weatherPage(scope)
    SettingsPage.Drawer -> drawerPage(scope)
    SettingsPage.Appearance -> appearancePage(scope)
    SettingsPage.Gestures -> gesturesPage(scope)
    SettingsPage.System -> systemPage(scope)
}

private fun LazyListScope.homePage(scope: SettingsPageScope) = with(scope) {
    item { SettingRow(R.string.settings_edit_home, value = null, onClick = onEditHome) }
    item {
        SettingRow(R.string.settings_alignment, stringResource(settings.homeAlignment.label)) {
            onUpdate { it.copy(homeAlignment = it.homeAlignment.next()) }
        }
    }
    item {
        SettingRow(R.string.settings_home_position, stringResource(settings.homeVerticalPosition.label)) {
            onUpdate { it.copy(homeVerticalPosition = it.homeVerticalPosition.next()) }
        }
    }
    item {
        SettingRow(R.string.settings_item_spacing, stringResource(settings.itemSpacing.label)) {
            onUpdate { it.copy(itemSpacing = it.itemSpacing.next()) }
        }
    }
    item {
        SettingRow(R.string.settings_app_icons, stringResource(settings.homeIcons.label)) {
            onUpdate { it.copy(homeIcons = it.homeIcons.next()) }
        }
    }
    item {
        PercentRow(R.string.settings_text_size, settings.homeTextScale) { delta ->
            onUpdate { it.copy(homeTextScale = stepped(it.homeTextScale, delta, MIN_TEXT_SCALE, MAX_TEXT_SCALE)) }
        }
    }
    item {
        MarginRow(R.string.settings_side_margin, settings.homeSideMargin, MAX_SIDE_MARGIN) { delta ->
            onUpdate { it.copy(homeSideMargin = it.homeSideMargin.stepMargin(delta, MAX_SIDE_MARGIN)) }
        }
    }
    item {
        MarginRow(R.string.settings_vertical_margin, settings.homeVerticalMargin, MAX_VERTICAL_MARGIN) { delta ->
            onUpdate { it.copy(homeVerticalMargin = it.homeVerticalMargin.stepMargin(delta, MAX_VERTICAL_MARGIN)) }
        }
    }
    item {
        ToggleRow(R.string.settings_status_bar, settings.showStatusBar) {
            onUpdate { it.copy(showStatusBar = !it.showStatusBar) }
        }
    }
}

private fun LazyListScope.clockPage(scope: SettingsPageScope) = with(scope) {
    item { ToggleRow(R.string.settings_clock, settings.showClock) { onUpdate { it.copy(showClock = !it.showClock) } } }
    item { ToggleRow(R.string.settings_date, settings.showDate) { onUpdate { it.copy(showDate = !it.showDate) } } }
    item {
        SettingRow(R.string.settings_date_format, settings.dateStyle.format(now)) {
            openDialog(SettingsDialog.DateFormat)
        }
    }
    item {
        SettingRow(R.string.settings_alignment, stringResource(settings.clockAlignment.label)) {
            onUpdate { it.copy(clockAlignment = it.clockAlignment.next()) }
        }
    }
    item {
        PercentRow(R.string.settings_size_clock, settings.clockTextScale) { delta ->
            onUpdate { it.copy(clockTextScale = stepped(it.clockTextScale, delta, MIN_TEXT_SCALE, MAX_TEXT_SCALE)) }
        }
    }
    item {
        PercentRow(R.string.settings_size_date, settings.dateTextScale) { delta ->
            onUpdate { it.copy(dateTextScale = stepped(it.dateTextScale, delta, MIN_TEXT_SCALE, MAX_TEXT_SCALE)) }
        }
    }
}

private fun LazyListScope.weatherPage(scope: SettingsPageScope) = with(scope) {
    item {
        ToggleRow(R.string.settings_weather, settings.showWeather) {
            if (!settings.showWeather && settings.weatherLocation == null) {
                // Nothing to show yet; picking a place turns weather on.
                openDialog(SettingsDialog.WeatherLocationPicker)
            } else {
                onUpdate { it.copy(showWeather = !it.showWeather) }
            }
        }
    }
    item {
        SettingRow(
            R.string.settings_weather_location,
            settings.weatherLocation?.name ?: stringResource(R.string.settings_weather_location_none),
        ) { openDialog(SettingsDialog.WeatherLocationPicker) }
    }
    item {
        SettingRow(R.string.settings_weather_icon, stringResource(settings.weatherIcon.label)) {
            onUpdate { it.copy(weatherIcon = it.weatherIcon.next()) }
        }
    }
    item {
        SettingRow(R.string.settings_weather_units, stringResource(settings.temperatureUnit.label)) {
            onUpdate { it.copy(temperatureUnit = it.temperatureUnit.next()) }
        }
    }
}

private fun LazyListScope.drawerPage(scope: SettingsPageScope) = with(scope) {
    item {
        SettingRow(R.string.settings_alignment, stringResource(settings.drawerAlignment.label)) {
            onUpdate { it.copy(drawerAlignment = it.drawerAlignment.next()) }
        }
    }
    item {
        SettingRow(R.string.settings_app_icons, stringResource(settings.drawerIcons.label)) {
            onUpdate { it.copy(drawerIcons = it.drawerIcons.next()) }
        }
    }
    item {
        PercentRow(R.string.settings_text_size, settings.drawerTextScale) { delta ->
            onUpdate { it.copy(drawerTextScale = stepped(it.drawerTextScale, delta, MIN_TEXT_SCALE, MAX_TEXT_SCALE)) }
        }
    }
    item {
        MarginRow(R.string.settings_side_margin, settings.drawerSideMargin, MAX_SIDE_MARGIN) { delta ->
            onUpdate { it.copy(drawerSideMargin = it.drawerSideMargin.stepMargin(delta, MAX_SIDE_MARGIN)) }
        }
    }
    item {
        ToggleRow(R.string.settings_auto_keyboard, settings.autoShowKeyboard) {
            onUpdate { it.copy(autoShowKeyboard = !it.autoShowKeyboard) }
        }
    }
    item {
        ToggleRow(R.string.settings_auto_launch, settings.autoLaunchSingleMatch) {
            onUpdate { it.copy(autoLaunchSingleMatch = !it.autoLaunchSingleMatch) }
        }
    }
    if (state.hiddenApps.isNotEmpty()) {
        item { SectionHeader(R.string.settings_hidden_apps) }
        items(state.hiddenApps, key = { it.key }) { app ->
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpdate { it.copy(hiddenApps = it.hiddenApps - app.key) } }
                    .padding(vertical = 12.dp),
            )
        }
    }
}

private fun LazyListScope.appearancePage(scope: SettingsPageScope) = with(scope) {
    item {
        SettingRow(R.string.settings_font, stringResource(settings.font.label)) {
            openDialog(SettingsDialog.Font)
        }
    }
    item {
        SettingRow(
            title = R.string.settings_font_weight,
            value = stringResource(settings.textWeight.label),
            valueStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight(settings.textWeight.value)),
            onClick = { openDialog(SettingsDialog.Weight) },
        )
    }
    item {
        ColorRow(
            title = R.string.settings_text_color,
            selected = settings.textColor,
            presets = TextColorPresets,
            allowAuto = true,
            onSelect = { color -> onUpdate { it.copy(textColor = color) } },
            onCustom = { openDialog(SettingsDialog.CustomColor(ColorTarget.Text)) },
        )
    }
    item {
        ColorRow(
            title = R.string.settings_accent_color,
            selected = settings.accentColor,
            presets = AccentColorPresets,
            allowAuto = false,
            onSelect = { color -> onUpdate { it.copy(accentColor = color) } },
            onCustom = { openDialog(SettingsDialog.CustomColor(ColorTarget.Accent)) },
        )
    }
    item {
        ColorRow(
            title = R.string.settings_background_color,
            selected = settings.backgroundColor,
            presets = BackgroundColorPresets,
            allowAuto = false,
            onSelect = { color -> onUpdate { it.copy(backgroundColor = color) } },
            onCustom = { openDialog(SettingsDialog.CustomColor(ColorTarget.Background)) },
        )
    }
    item {
        PercentRow(R.string.settings_home_background, settings.homeBackgroundAlpha) { delta ->
            onUpdate { it.copy(homeBackgroundAlpha = stepped(it.homeBackgroundAlpha, delta, 0f, 1f)) }
        }
    }
    item {
        PercentRow(R.string.settings_drawer_background, settings.drawerBackgroundAlpha) { delta ->
            onUpdate { it.copy(drawerBackgroundAlpha = stepped(it.drawerBackgroundAlpha, delta, 0f, 1f)) }
        }
    }
    item {
        // Capped lower than the rest: settings and dialogs stop fitting the screen beyond this.
        PercentRow(R.string.settings_size_other, settings.textScale) { delta ->
            onUpdate { it.copy(textScale = stepped(it.textScale, delta, MIN_TEXT_SCALE, 1.6f)) }
        }
    }
}

private fun LazyListScope.gesturesPage(scope: SettingsPageScope) = with(scope) {
    items(Gesture.entries) { gesture ->
        SettingRow(gesture.label, settings.gestureAction(gesture).label(state.allApps)) {
            openDialog(SettingsDialog.GestureActionPicker(gesture))
        }
    }
}

private fun LazyListScope.systemPage(scope: SettingsPageScope) = with(scope) {
    item { SettingRow(R.string.settings_default_launcher, value = null, onClick = onSetDefaultLauncher) }
    item { SettingRow(R.string.settings_lock_service, value = null, onClick = onOpenAccessibilitySettings) }
}

private const val MIN_TEXT_SCALE = 0.5f
private const val MAX_TEXT_SCALE = 2f
private const val MARGIN_STEP = 4
private const val MAX_SIDE_MARGIN = 64
private const val MAX_VERTICAL_MARGIN = 96

/** A percentage stepper for text sizes and opacities; [onStep] gets +0.1 or -0.1. */
@Composable
private fun PercentRow(@StringRes title: Int, fraction: Float, onStep: (delta: Float) -> Unit) {
    StepperRow(
        title = title,
        value = stringResource(R.string.percent, (fraction * 100).roundToInt()),
        onDecrease = { onStep(-0.1f) },
        onIncrease = { onStep(0.1f) },
    )
}

/** A margin stepper in [MARGIN_STEP] dp steps; [onStep] gets the signed step. */
@Composable
private fun MarginRow(@StringRes title: Int, value: Int, max: Int, onStep: (delta: Int) -> Unit) {
    StepperRow(
        title = title,
        value = stringResource(R.string.margin_dp, value),
        onDecrease = { if (value > 0) onStep(-MARGIN_STEP) },
        onIncrease = { if (value < max) onStep(MARGIN_STEP) },
    )
}

private fun Int.stepMargin(delta: Int, max: Int) = (this + delta).coerceIn(0, max)

/** Steps by [delta] and rounds to one decimal, so repeated taps don't drift (0.7999…). */
private fun stepped(value: Float, delta: Float, min: Float, max: Float): Float =
    ((value + delta).coerceIn(min, max) * 10).roundToInt() / 10f
