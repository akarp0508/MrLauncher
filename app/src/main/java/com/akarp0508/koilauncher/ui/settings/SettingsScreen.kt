package com.akarp0508.koilauncher.ui.settings

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akarp0508.koilauncher.R
import com.akarp0508.koilauncher.data.DateStyle
import com.akarp0508.koilauncher.data.FontChoice
import com.akarp0508.koilauncher.data.Gesture
import com.akarp0508.koilauncher.data.GestureAction
import com.akarp0508.koilauncher.data.LauncherSettings
import com.akarp0508.koilauncher.data.TextWeight
import com.akarp0508.koilauncher.data.WeatherLocation
import com.akarp0508.koilauncher.ui.LauncherUiState
import com.akarp0508.koilauncher.ui.home.format
import com.akarp0508.koilauncher.ui.theme.LauncherTheme
import com.akarp0508.koilauncher.ui.theme.fontFamilyFor

internal enum class ColorTarget { Text, Accent, Background }

internal sealed interface SettingsDialog {
    data object Font : SettingsDialog
    data object Weight : SettingsDialog
    data object DateFormat : SettingsDialog
    data class GestureActionPicker(val gesture: Gesture) : SettingsDialog
    data class GestureAppPicker(val gesture: Gesture) : SettingsDialog
    data object WeatherLocationPicker : SettingsDialog
    data class CustomColor(val target: ColorTarget) : SettingsDialog
}

private val FontMimeTypes = arrayOf(
    "font/*", "application/x-font-ttf", "application/x-font-otf", "application/font-sfnt",
    "application/vnd.ms-opentype", "application/octet-stream",
)

/** A menu of [SettingsPage]s; each opens a short page of related settings. */
@Composable
fun SettingsScreen(
    state: LauncherUiState,
    onUpdate: ((LauncherSettings) -> LauncherSettings) -> Unit,
    onImportFont: (Uri) -> Unit,
    /** Called before opening the file picker, so returning from it comes back here. */
    keepScreenOnReturn: () -> Unit,
    searchWeatherLocations: suspend (String) -> List<WeatherLocation>,
    onEditHome: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
) {
    val settings = state.settings
    val now = remember { System.currentTimeMillis() }
    var page by rememberSaveable { mutableStateOf<SettingsPage?>(null) }
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(onImportFont)
    }
    // Registered after the launcher's own back handler, so it wins while a page is open.
    BackHandler(enabled = page != null) { page = null }

    val listState = rememberLazyListState()
    LaunchedEffect(page) { listState.scrollToItem(0) }

    val scope = SettingsPageScope(
        state = state,
        onUpdate = onUpdate,
        openDialog = { dialog = it },
        onEditHome = onEditHome,
        onSetDefaultLauncher = onSetDefaultLauncher,
        onOpenAccessibilitySettings = onOpenAccessibilitySettings,
        now = now,
    )

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 32.dp),
    ) {
        item { PageTitle(page, onBack = { page = null }) }
        val current = page
        if (current == null) {
            items(SettingsPage.entries) { entry ->
                PageLink(entry, summary = pageSummary(entry, settings)) { page = entry }
            }
        } else {
            settingsPage(current, scope)
        }
        item { Spacer(Modifier.height(48.dp)) }
    }

    val fontPreviewStyle = MaterialTheme.typography.titleLarge
    // An imported font is read from disk, so resolve the previews once per file.
    val fontFamilies = remember(settings.customFontPath) {
        FontChoice.entries.associateWith { fontFamilyFor(it, settings.customFontPath) }
    }
    when (val current = dialog) {
        null -> Unit
        SettingsDialog.Font -> OptionDialog(
            title = stringResource(R.string.settings_font),
            // "Custom" is reached through the import entry, and only listed once a file exists.
            options = FontChoice.entries.filter { it != FontChoice.Custom || settings.customFontPath != null },
            selected = settings.font,
            label = { stringResource(it.label) },
            optionStyle = { fontPreviewStyle.copy(fontFamily = fontFamilies.getValue(it)) },
            onSelect = { font ->
                onUpdate { it.copy(font = font) }
                dialog = null
            },
            onDismiss = { dialog = null },
            extra = stringResource(R.string.font_import) to {
                dialog = null
                keepScreenOnReturn()
                fontPicker.launch(FontMimeTypes)
            },
        )
        SettingsDialog.DateFormat -> OptionDialog(
            title = stringResource(R.string.settings_date_format),
            options = DateStyle.entries,
            selected = settings.dateStyle,
            // Each option is today's date in that format.
            label = { it.format(now) },
            onSelect = { style ->
                onUpdate { it.copy(dateStyle = style) }
                dialog = null
            },
            onDismiss = { dialog = null },
        )
        SettingsDialog.Weight -> OptionDialog(
            title = stringResource(R.string.settings_font_weight),
            options = TextWeight.entries,
            selected = settings.textWeight,
            label = { stringResource(it.label) },
            // Preview in the chosen font, so fonts without a given weight show that honestly.
            optionStyle = {
                fontPreviewStyle.copy(fontFamily = fontFamilies.getValue(settings.font), fontWeight = FontWeight(it.value))
            },
            onSelect = { weight ->
                onUpdate { it.copy(textWeight = weight) }
                dialog = null
            },
            onDismiss = { dialog = null },
        )
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

@Composable
private fun PageTitle(page: SettingsPage?, onBack: () -> Unit) {
    val style = MaterialTheme.typography.headlineSmall
    if (page == null) {
        Text(stringResource(R.string.settings_title), style = style, modifier = Modifier.padding(top = 24.dp, bottom = 16.dp))
        return
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onBack)
            .padding(top = 24.dp, bottom = 16.dp),
    ) {
        Text("‹ ", style = style, color = LauncherTheme.colors.accent)
        Text(stringResource(page.title), style = style)
    }
}

@Composable
private fun PageLink(page: SettingsPage, summary: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(page.title), style = MaterialTheme.typography.bodyLarge)
            Text(summary, style = MaterialTheme.typography.labelLarge, color = LauncherTheme.colors.textDim, maxLines = 1)
        }
        Text("›", style = MaterialTheme.typography.bodyLarge, color = LauncherTheme.colors.textDim)
    }
}

/** Weather shows its current state; other pages describe what's inside. */
@Composable
private fun pageSummary(page: SettingsPage, settings: LauncherSettings): String = when {
    page == SettingsPage.Weather && !settings.showWeather -> stringResource(R.string.settings_off)
    page == SettingsPage.Weather -> settings.weatherLocation?.name ?: stringResource(page.summary)
    else -> stringResource(page.summary)
}
