package com.akarp0508.mrlauncher.ui

import android.app.Application
import android.net.Uri
import android.os.SystemClock
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.akarp0508.mrlauncher.R
import com.akarp0508.mrlauncher.data.AppIconLoader
import com.akarp0508.mrlauncher.data.AppInfo
import com.akarp0508.mrlauncher.data.AppRepository
import com.akarp0508.mrlauncher.data.FontChoice
import com.akarp0508.mrlauncher.data.FontStore
import com.akarp0508.mrlauncher.data.Gesture
import com.akarp0508.mrlauncher.data.GestureAction
import com.akarp0508.mrlauncher.data.LauncherSettings
import com.akarp0508.mrlauncher.data.SettingsRepository
import com.akarp0508.mrlauncher.data.Weather
import com.akarp0508.mrlauncher.data.WeatherLocation
import com.akarp0508.mrlauncher.data.WeatherRepository
import com.akarp0508.mrlauncher.search.AppSearch
import com.akarp0508.mrlauncher.system.SystemActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen { Home, EditHome, Drawer, Settings }

data class LauncherUiState(
    /** False until settings are read from disk, so we don't flash default colors. */
    val isLoaded: Boolean = false,
    val screen: Screen = Screen.Home,
    val query: String = "",
    val homeApps: List<AppInfo> = emptyList(),
    /** Visible apps matching [query], best match first. */
    val drawerApps: List<AppInfo> = emptyList(),
    val hiddenApps: List<AppInfo> = emptyList(),
    /** Every app including hidden ones, for pickers. */
    val allApps: List<AppInfo> = emptyList(),
    val weather: Weather? = null,
    val settings: LauncherSettings = LauncherSettings(),
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)
    val iconLoader = AppIconLoader(application)
    private val settingsRepository = SettingsRepository(application)
    private val weatherRepository = WeatherRepository()

    private val screen = MutableStateFlow(Screen.Home)
    private val query = MutableStateFlow("")
    private val weather = MutableStateFlow<Weather?>(null)
    private var weatherJob: Job? = null
    private var lastWeatherFetch = 0L

    private val labelledApps = combine(
        // A change to the app list may come with new icons (e.g. an app update).
        appRepository.installedApps.onEach { iconLoader.clear() },
        settingsRepository.settings,
    ) { installed, settings ->
        val apps = installed
            .map { it.copy(label = settings.renamedApps[it.key] ?: it.systemLabel) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
        apps to settings
    }

    // Eagerly: the launcher activity lives as long as the device is on, and the
    // auto-launch collector below keeps this hot anyway.
    val uiState: StateFlow<LauncherUiState> =
        combine(labelledApps, screen, query, weather) { (apps, settings), screen, query, weather ->
            val byKey = apps.associateBy { it.key }
            val (hidden, visible) = apps.partition { it.key in settings.hiddenApps }
            LauncherUiState(
                isLoaded = true,
                screen = screen,
                query = query,
                homeApps = settings.homeApps.mapNotNull(byKey::get),
                drawerApps = AppSearch.filter(visible, query) { it.label },
                hiddenApps = hidden,
                allApps = apps,
                weather = weather.takeIf { settings.showWeather },
                settings = settings,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, LauncherUiState())

    init {
        viewModelScope.launch {
            uiState.collect { state ->
                val single = state.drawerApps.singleOrNull()
                if (state.screen == Screen.Drawer &&
                    state.settings.autoLaunchSingleMatch &&
                    state.query.isNotBlank() &&
                    single != null
                ) {
                    launch(single)
                }
            }
        }
        viewModelScope.launch {
            settingsRepository.settings
                .map { Triple(it.showWeather, it.weatherLocation, it.temperatureUnit) }
                .distinctUntilChanged()
                .collect { refreshWeather(force = true) }
        }
    }

    fun openDrawer() {
        screen.value = Screen.Drawer
    }

    fun openSettings() {
        screen.value = Screen.Settings
    }

    fun editHome() {
        screen.value = Screen.EditHome
    }

    fun goHome() {
        screen.value = Screen.Home
        query.value = ""
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun launch(app: AppInfo) {
        // Clear right away so a state re-emission can't auto-launch the same app twice.
        // The screen itself resets to Home in MainActivity.onStop, after the app is in front.
        query.value = ""
        appRepository.launch(app)
    }

    fun onGesture(gesture: Gesture) = perform(uiState.value.settings.gestureAction(gesture))

    private fun perform(action: GestureAction) {
        val context = getApplication<Application>()
        when (action) {
            GestureAction.None -> Unit
            GestureAction.OpenDrawer -> openDrawer()
            GestureAction.OpenSettings -> openSettings()
            GestureAction.EditHome -> editHome()
            GestureAction.Notifications -> SystemActions.expandNotifications(context)
            GestureAction.QuickSettings -> SystemActions.expandQuickSettings(context)
            GestureAction.LockScreen -> SystemActions.lockScreen(context)
            GestureAction.Alarms -> SystemActions.openAlarms(context)
            GestureAction.Calendar -> SystemActions.openCalendar(context)
            GestureAction.WeatherApp ->
                SystemActions.openWeather(context, uiState.value.settings.weatherLocation?.name)
            GestureAction.RefreshWeather -> refreshWeather(force = true)
            is GestureAction.OpenApp ->
                uiState.value.allApps.firstOrNull { it.key == action.appKey }?.let(::launch)
        }
    }

    fun onAppAction(app: AppInfo, action: AppAction) {
        when (action) {
            AppAction.AddToHome -> updateSettings { it.copy(homeApps = it.homeApps + app.key) }
            AppAction.RemoveFromHome -> updateSettings { it.copy(homeApps = it.homeApps - app.key) }
            AppAction.Hide -> updateSettings {
                it.copy(hiddenApps = it.hiddenApps + app.key, homeApps = it.homeApps - app.key)
            }
            AppAction.AppDetails -> appRepository.openAppDetails(app)
            AppAction.Uninstall -> appRepository.uninstall(app)
            AppAction.Rename -> Unit // Needs input; handled by the rename dialog via [rename].
        }
    }

    fun setHomeOrder(keys: List<String>) = updateSettings { it.copy(homeApps = keys) }

    fun toggleHomeApp(app: AppInfo) = updateSettings {
        it.copy(homeApps = if (app.key in it.homeApps) it.homeApps - app.key else it.homeApps + app.key)
    }

    /** A blank [label] restores the system label. */
    fun rename(app: AppInfo, label: String) = updateSettings {
        val renames = if (label.isBlank() || label.trim() == app.systemLabel) {
            it.renamedApps - app.key
        } else {
            it.renamedApps + (app.key to label.trim())
        }
        it.copy(renamedApps = renames)
    }

    fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }

    fun importFont(uri: Uri) {
        val context = getApplication<Application>()
        val previous = uiState.value.settings.customFontPath
        viewModelScope.launch {
            val path = withContext(Dispatchers.IO) { FontStore.import(context, uri) }
            if (path == null) {
                Toast.makeText(context, R.string.font_import_failed, Toast.LENGTH_LONG).show()
                return@launch
            }
            settingsRepository.update { it.copy(font = FontChoice.Custom, customFontPath = path) }
            previous?.let { withContext(Dispatchers.IO) { FontStore.delete(it) } }
        }
    }

    suspend fun searchWeatherLocations(query: String): List<WeatherLocation> =
        weatherRepository.searchLocations(query)

    /** Called on every return to the launcher; only hits the network every [WEATHER_TTL_MS]. */
    fun refreshWeather(force: Boolean = false) {
        if (!force && SystemClock.elapsedRealtime() - lastWeatherFetch < WEATHER_TTL_MS) return
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val location = settings.weatherLocation
            if (!settings.showWeather || location == null) {
                weather.value = null
                return@launch
            }
            // On failure keep showing the last reading rather than nothing.
            runCatching { weatherRepository.currentWeather(location, settings.temperatureUnit) }
                .onSuccess {
                    weather.value = it
                    lastWeatherFetch = SystemClock.elapsedRealtime()
                }
        }
    }

    private companion object {
        const val WEATHER_TTL_MS = 30 * 60 * 1000L
    }
}
