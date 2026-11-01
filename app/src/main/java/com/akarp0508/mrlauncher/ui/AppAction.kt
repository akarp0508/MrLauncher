package com.akarp0508.mrlauncher.ui

import androidx.annotation.StringRes
import com.akarp0508.mrlauncher.R
import com.akarp0508.mrlauncher.data.AppInfo

enum class AppAction(@StringRes val label: Int) {
    AddToHome(R.string.action_add_to_home),
    RemoveFromHome(R.string.action_remove_from_home),
    Rename(R.string.action_rename),
    Hide(R.string.action_hide),
    AppDetails(R.string.action_app_details),
    Uninstall(R.string.action_uninstall);

    companion object {
        fun availableFor(app: AppInfo, homeApps: List<String>): List<AppAction> = buildList {
            // Reordering lives in the home edit mode.
            add(if (app.key in homeApps) RemoveFromHome else AddToHome)
            add(Rename)
            add(Hide)
            add(AppDetails)
            add(Uninstall)
        }
    }
}
