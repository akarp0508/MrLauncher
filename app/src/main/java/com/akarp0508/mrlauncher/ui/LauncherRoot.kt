package com.akarp0508.mrlauncher.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akarp0508.mrlauncher.data.AppInfo
import com.akarp0508.mrlauncher.data.LauncherSettings
import com.akarp0508.mrlauncher.system.SystemActions
import com.akarp0508.mrlauncher.ui.components.AppActionsDialog
import com.akarp0508.mrlauncher.ui.components.RenameDialog
import com.akarp0508.mrlauncher.ui.drawer.AppDrawer
import com.akarp0508.mrlauncher.ui.home.HomeScreen
import com.akarp0508.mrlauncher.ui.settings.SettingsScreen
import com.akarp0508.mrlauncher.ui.theme.LauncherSurface
import com.akarp0508.mrlauncher.ui.theme.LauncherTheme
import com.akarp0508.mrlauncher.ui.theme.MrLauncherTheme

@Composable
fun LauncherRoot(viewModel: LauncherViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Draw nothing (just wallpaper) for the few ms until settings load, instead of flashing defaults.
    if (!state.isLoaded) return

    MrLauncherTheme(state.settings) {
        val opacity = state.settings.backgroundOpacity(state.screen)
        SystemBarsEffect(
            darkIcons = LauncherTheme.colorsFor(opacity).isDarkText,
            showStatusBar = state.settings.showStatusBar,
        )
        LauncherContent(state, viewModel)
    }
}

@Composable
private fun LauncherContent(state: LauncherUiState, viewModel: LauncherViewModel) {
    val context = LocalContext.current
    var actionsFor by remember { mutableStateOf<AppInfo?>(null) }
    var renaming by remember { mutableStateOf<AppInfo?>(null) }

    // A launcher should never finish on back; from Home this is a no-op.
    BackHandler { viewModel.goHome() }

    AnimatedContent(
        targetState = state.screen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen",
    ) { screen ->
        LauncherSurface(opacity = state.settings.backgroundOpacity(screen)) {
            when (screen) {
                Screen.Home -> HomeScreen(
                    apps = state.homeApps,
                    weather = state.weather,
                    settings = state.settings,
                    onAppClick = viewModel::launch,
                    onAppLongClick = { actionsFor = it },
                    onGesture = viewModel::onGesture,
                    onWeatherClick = { viewModel.refreshWeather(force = true) },
                )
                Screen.Drawer -> AppDrawer(
                    apps = state.drawerApps,
                    query = state.query,
                    onQueryChange = viewModel::onQueryChange,
                    onAppClick = viewModel::launch,
                    onAppLongClick = { actionsFor = it },
                    onOpenSettings = viewModel::openSettings,
                    alignment = state.settings.drawerAlignment,
                    autoShowKeyboard = state.settings.autoShowKeyboard,
                )
                Screen.Settings -> SettingsScreen(
                    state = state,
                    onUpdate = viewModel::updateSettings,
                    onImportFont = viewModel::importFont,
                    searchWeatherLocations = viewModel::searchWeatherLocations,
                    onSetDefaultLauncher = { SystemActions.openDefaultLauncherSettings(context) },
                    onOpenAccessibilitySettings = { SystemActions.openAccessibilitySettings(context) },
                )
            }
        }
    }

    actionsFor?.let { app ->
        AppActionsDialog(
            app = app,
            actions = AppAction.availableFor(app, state.settings.homeApps),
            onAction = { action ->
                actionsFor = null
                if (action == AppAction.Rename) renaming = app else viewModel.onAppAction(app, action)
            },
            onDismiss = { actionsFor = null },
        )
    }

    renaming?.let { app ->
        RenameDialog(
            app = app,
            onRename = { label ->
                viewModel.rename(app, label)
                renaming = null
            },
            onDismiss = { renaming = null },
        )
    }
}

private fun LauncherSettings.backgroundOpacity(screen: Screen): Float = when (screen) {
    Screen.Home -> homeBackgroundAlpha
    Screen.Drawer, Screen.Settings -> drawerBackgroundAlpha
}

@Composable
private fun SystemBarsEffect(darkIcons: Boolean, showStatusBar: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).run {
            isAppearanceLightStatusBars = darkIcons
            isAppearanceLightNavigationBars = darkIcons
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            if (showStatusBar) {
                show(WindowInsetsCompat.Type.statusBars())
            } else {
                hide(WindowInsetsCompat.Type.statusBars())
            }
        }
    }
}
