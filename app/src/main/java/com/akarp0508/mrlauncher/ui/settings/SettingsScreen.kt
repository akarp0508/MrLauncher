package com.akarp0508.mrlauncher.ui.settings

import android.net.Uri
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akarp0508.mrlauncher.R
import com.akarp0508.mrlauncher.data.FontChoice
import com.akarp0508.mrlauncher.data.Gesture
import com.akarp0508.mrlauncher.data.TextWeight
import com.akarp0508.mrlauncher.data.GestureAction
import com.akarp0508.mrlauncher.data.LauncherSettings
import com.akarp0508.mrlauncher.data.WeatherLocation
import com.akarp0508.mrlauncher.ui.LauncherUiState
import com.akarp0508.mrlauncher.ui.theme.AccentColorPresets
import com.akarp0508.mrlauncher.ui.theme.BackgroundColorPresets
import com.akarp0508.mrlauncher.ui.theme.TextColorPresets
import com.akarp0508.mrlauncher.ui.theme.fontFamilyFor
import kotlin.math.roundToInt

private enum class ColorTarget { Text, Accent, Background }

private sealed interface SettingsDialog {
    data object Font : SettingsDialog
    data object Weight : SettingsDialog
    data class GestureActionPicker(val gesture: Gesture) : SettingsDialog
    data class GestureAppPicker(val gesture: Gesture) : SettingsDialog
    data object WeatherLocationPicker : SettingsDialog
    data class CustomColor(val target: ColorTarget) : SettingsDialog
}

private val FontMimeTypes = arrayOf(
    "font/*", "application/x-font-ttf", "application/x-font-otf", "application/font-sfnt",
    "application/vnd.ms-opentype", "application/octet-stream",
)

@Composable
fun SettingsScreen(
    state: LauncherUiState,
    onUpdate: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onImportFont: (Uri) -> Unit,
    searchWeatherLocations: suspend (String) -> List<WeatherLocation>,
    onEditHome: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
) {
    val settings = state.settings
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(onImportFont)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 32.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 24.dp),
            )
        }

        item { SectionHeader(R.string.settings_section_home) }
        item { SettingRow(R.string.settings_edit_home, value = null, onClick = onEditHome) }
        item { ToggleRow(R.string.settings_clock, settings.showClock) { onUpdate { it.copy(showClock = !it.showClock) } } }
        item { ToggleRow(R.string.settings_date, settings.showDate) { onUpdate { it.copy(showDate = !it.showDate) } } }
        item {
            SettingRow(R.string.settings_clock_alignment, stringResource(settings.clockAlignment.label)) {
                onUpdate { it.copy(clockAlignment = it.clockAlignment.next()) }
            }
        }
        item {
            SettingRow(R.string.settings_app_alignment, stringResource(settings.homeAlignment.label)) {
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
            ToggleRow(R.string.settings_status_bar, settings.showStatusBar) {
                onUpdate { it.copy(showStatusBar = !it.showStatusBar) }
            }
        }

        item { SectionHeader(R.string.settings_section_drawer) }
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
            ToggleRow(R.string.settings_auto_keyboard, settings.autoShowKeyboard) {
                onUpdate { it.copy(autoShowKeyboard = !it.autoShowKeyboard) }
            }
        }
        item {
            ToggleRow(R.string.settings_auto_launch, settings.autoLaunchSingleMatch) {
                onUpdate { it.copy(autoLaunchSingleMatch = !it.autoLaunchSingleMatch) }
            }
        }

        item { SectionHeader(R.string.settings_section_look) }
        item {
            SettingRow(
                title = R.string.settings_font,
                value = stringResource(settings.font.label),
                onClick = { dialog = SettingsDialog.Font },
            )
        }
        item {
            SettingRow(
                title = R.string.settings_font_weight,
                value = stringResource(settings.textWeight.label),
                valueStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight(settings.textWeight.value)),
                onClick = { dialog = SettingsDialog.Weight },
            )
        }
        item {
            ColorRow(
                title = R.string.settings_text_color,
                selected = settings.textColor,
                presets = TextColorPresets,
                allowAuto = true,
                onSelect = { color -> onUpdate { it.copy(textColor = color) } },
                onCustom = { dialog = SettingsDialog.CustomColor(ColorTarget.Text) },
            )
        }
        item {
            ColorRow(
                title = R.string.settings_accent_color,
                selected = settings.accentColor,
                presets = AccentColorPresets,
                allowAuto = false,
                onSelect = { color -> onUpdate { it.copy(accentColor = color) } },
                onCustom = { dialog = SettingsDialog.CustomColor(ColorTarget.Accent) },
            )
        }
        item {
            ColorRow(
                title = R.string.settings_background_color,
                selected = settings.backgroundColor,
                presets = BackgroundColorPresets,
                allowAuto = false,
                onSelect = { color -> onUpdate { it.copy(backgroundColor = color) } },
                onCustom = { dialog = SettingsDialog.CustomColor(ColorTarget.Background) },
            )
        }
        item {
            StepperRow(
                title = R.string.settings_home_background,
                value = stringResource(R.string.percent, (settings.homeBackgroundAlpha * 100).roundToInt()),
                onDecrease = { onUpdate { it.copy(homeBackgroundAlpha = stepped(it.homeBackgroundAlpha, -0.1f, 0f, 1f)) } },
                onIncrease = { onUpdate { it.copy(homeBackgroundAlpha = stepped(it.homeBackgroundAlpha, 0.1f, 0f, 1f)) } },
            )
        }
        item {
            StepperRow(
                title = R.string.settings_drawer_background,
                value = stringResource(R.string.percent, (settings.drawerBackgroundAlpha * 100).roundToInt()),
                onDecrease = { onUpdate { it.copy(drawerBackgroundAlpha = stepped(it.drawerBackgroundAlpha, -0.1f, 0f, 1f)) } },
                onIncrease = { onUpdate { it.copy(drawerBackgroundAlpha = stepped(it.drawerBackgroundAlpha, 0.1f, 0f, 1f)) } },
            )
        }

        item { SectionHeader(R.string.settings_section_text_size) }
        item {
            TextSizeRow(R.string.settings_size_clock, settings.clockTextScale) { delta ->
                onUpdate { it.copy(clockTextScale = stepped(it.clockTextScale, delta, MIN_TEXT_SCALE, MAX_TEXT_SCALE)) }
            }
        }
        item {
            TextSizeRow(R.string.settings_size_date, settings.dateTextScale) { delta ->
                onUpdate { it.copy(dateTextScale = stepped(it.dateTextScale, delta, MIN_TEXT_SCALE, MAX_TEXT_SCALE)) }
            }
        }
        item {
            TextSizeRow(R.string.settings_size_home, settings.homeTextScale) { delta ->
                onUpdate { it.copy(homeTextScale = stepped(it.homeTextScale, delta, MIN_TEXT_SCALE, MAX_TEXT_SCALE)) }
            }
        }
        item {
            TextSizeRow(R.string.settings_size_drawer, settings.drawerTextScale) { delta ->
                onUpdate { it.copy(drawerTextScale = stepped(it.drawerTextScale, delta, MIN_TEXT_SCALE, MAX_TEXT_SCALE)) }
            }
        }
        item {
            // Capped lower than the rest: settings and dialogs stop fitting the screen beyond this.
            TextSizeRow(R.string.settings_size_other, settings.textScale) { delta ->
                onUpdate { it.copy(textScale = stepped(it.textScale, delta, MIN_TEXT_SCALE, 1.6f)) }
            }
        }

        item { SectionHeader(R.string.settings_section_weather) }
        item {
            ToggleRow(R.string.settings_weather, settings.showWeather) {
                if (!settings.showWeather && settings.weatherLocation == null) {
                    dialog = SettingsDialog.WeatherLocationPicker
                }
                onUpdate { it.copy(showWeather = !it.showWeather) }
            }
        }
        item {
            SettingRow(
                R.string.settings_weather_location,
                settings.weatherLocation?.name ?: stringResource(R.string.settings_weather_location_none),
            ) { dialog = SettingsDialog.WeatherLocationPicker }
        }
        item {
            SettingRow(R.string.settings_weather_units, stringResource(settings.temperatureUnit.label)) {
                onUpdate { it.copy(temperatureUnit = it.temperatureUnit.next()) }
            }
        }

        item { SectionHeader(R.string.settings_section_gestures) }
        items(Gesture.entries) { gesture ->
            SettingRow(gesture.label, settings.gestureAction(gesture).label(state.allApps)) {
                dialog = SettingsDialog.GestureActionPicker(gesture)
            }
        }

        item { SectionHeader(R.string.settings_section_system) }
        item { SettingRow(R.string.settings_default_launcher, value = null, onClick = onSetDefaultLauncher) }
        item { SettingRow(R.string.settings_lock_service, value = null, onClick = onOpenAccessibilitySettings) }

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
        item { Spacer(Modifier.height(48.dp)) }
    }

    val fontPreviewStyle = MaterialTheme.typography.titleLarge
    when (val current = dialog) {
        null -> Unit
        SettingsDialog.Font -> OptionDialog(
            title = stringResource(R.string.settings_font),
            // "Custom" is reached through the import entry, and only listed once a file exists.
            options = FontChoice.entries.filter { it != FontChoice.Custom || settings.customFontPath != null },
            selected = settings.font,
            label = { stringResource(it.label) },
            optionStyle = { fontPreviewStyle.copy(fontFamily = fontFamilyFor(it, settings.customFontPath)) },
            onSelect = { font ->
                onUpdate { it.copy(font = font) }
                dialog = null
            },
            onDismiss = { dialog = null },
            extra = stringResource(R.string.font_import) to {
                dialog = null
                fontPicker.launch(FontMimeTypes)
            },
        )
        SettingsDialog.Weight -> {
            val family = remember(settings.font, settings.customFontPath) {
                fontFamilyFor(settings.font, settings.customFontPath)
            }
            OptionDialog(
                title = stringResource(R.string.settings_font_weight),
                options = TextWeight.entries,
                selected = settings.textWeight,
                label = { stringResource(it.label) },
                // Preview in the chosen font, so fonts without a given weight show that honestly.
                optionStyle = { fontPreviewStyle.copy(fontFamily = family, fontWeight = FontWeight(it.value)) },
                onSelect = { weight ->
                    onUpdate { it.copy(textWeight = weight) }
                    dialog = null
                },
                onDismiss = { dialog = null },
            )
        }
        is SettingsDialog.GestureActionPicker -> OptionDialog(
            title = stringResource(current.gesture.label),
            options = GestureAction.simpleActions,
            selected = settings.gestureAction(current.gesture),
            label = { it.label(state.allApps) },
            onSelect = { action ->
                onUpdate { it.copy(gestures = it.gestures + (current.gesture to action)) }
                dialog = null
            },
            onDismiss = { dialog = null },
            extra = stringResource(R.string.gesture_action_open_app) to {
                dialog = SettingsDialog.GestureAppPicker(current.gesture)
            },
        )
        is SettingsDialog.GestureAppPicker -> AppPickerDialog(
            apps = state.allApps,
            onPick = { app ->
                onUpdate { it.copy(gestures = it.gestures + (current.gesture to GestureAction.OpenApp(app.key))) }
                dialog = null
            },
            onDismiss = { dialog = null },
        )
        SettingsDialog.WeatherLocationPicker -> WeatherLocationDialog(
            search = searchWeatherLocations,
            onPick = { location ->
                onUpdate { it.copy(weatherLocation = location, showWeather = true) }
                dialog = null
            },
            onDismiss = { dialog = null },
        )
        is SettingsDialog.CustomColor -> HexColorDialog(
            initial = when (current.target) {
                ColorTarget.Text -> settings.textColor
                ColorTarget.Accent -> settings.accentColor
                ColorTarget.Background -> settings.backgroundColor
            },
            onPick = { color ->
                onUpdate {
                    when (current.target) {
                        ColorTarget.Text -> it.copy(textColor = color)
                        ColorTarget.Accent -> it.copy(accentColor = color)
                        ColorTarget.Background -> it.copy(backgroundColor = color)
                    }
                }
                dialog = null
            },
            onDismiss = { dialog = null },
        )
    }
}

private const val MIN_TEXT_SCALE = 0.5f
private const val MAX_TEXT_SCALE = 2f

/** A percentage stepper; [onStep] gets +0.1 or -0.1. */
@Composable
private fun TextSizeRow(@StringRes title: Int, scale: Float, onStep: (delta: Float) -> Unit) {
    StepperRow(
        title = title,
        value = stringResource(R.string.percent, (scale * 100).roundToInt()),
        onDecrease = { onStep(-0.1f) },
        onIncrease = { onStep(0.1f) },
    )
}

/** Steps by [delta] and rounds to one decimal, so repeated taps don't drift (0.7999…). */
private fun stepped(value: Float, delta: Float, min: Float, max: Float): Float =
    ((value + delta).coerceIn(min, max) * 10).roundToInt() / 10f
