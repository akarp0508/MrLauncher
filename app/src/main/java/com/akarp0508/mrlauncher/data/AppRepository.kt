package com.akarp0508.mrlauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * Lists launchable activities across all user profiles (personal + work) via [LauncherApps],
 * and re-emits whenever packages are installed, removed or changed.
 */
class AppRepository(private val context: Context) {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val userManager = context.getSystemService(UserManager::class.java)

    val installedApps: Flow<List<AppInfo>> = callbackFlow {
        val callback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                trySend(Unit)
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
                trySend(Unit)
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                trySend(Unit)
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>, user: UserHandle, replacing: Boolean,
            ) {
                trySend(Unit)
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>, user: UserHandle, replacing: Boolean,
            ) {
                trySend(Unit)
            }
        }
        trySend(Unit)
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        awaitClose { launcherApps.unregisterCallback(callback) }
    }
        .conflate()
        .map { loadApps() }
        .flowOn(Dispatchers.Default)

    private fun loadApps(): List<AppInfo> {
        val myUser = Process.myUserHandle()
        return userManager.userProfiles.flatMap { user ->
            val serial = userManager.getSerialNumberForUser(user)
            // A paused work profile can throw here; just skip it.
            val activities = runCatching { launcherApps.getActivityList(null, user) }
                .getOrDefault(emptyList())
            activities.map { info ->
                AppInfo(
                    packageName = info.componentName.packageName,
                    activityName = info.componentName.className,
                    user = user,
                    userSerial = serial,
                    systemLabel = info.label.toString(),
                    isWorkProfile = user != myUser,
                )
            }
        }.filterNot { it.packageName == context.packageName }
    }

    fun launch(app: AppInfo): Boolean = runCatching {
        launcherApps.startMainActivity(app.componentName, app.user, null, null)
    }.isSuccess

    fun openAppDetails(app: AppInfo) {
        runCatching { launcherApps.startAppDetailsActivity(app.componentName, app.user, null, null) }
    }

    fun uninstall(app: AppInfo) {
        val intent = Intent(Intent.ACTION_DELETE, "package:${app.packageName}".toUri())
            .putExtra(Intent.EXTRA_USER, app.user)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
