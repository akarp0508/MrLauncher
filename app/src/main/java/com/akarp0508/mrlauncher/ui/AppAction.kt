package com.akarp0508.mrlauncher.ui

import androidx.annotation.StringRes
import com.akarp0508.mrlauncher.R
import com.akarp0508.mrlauncher.data.AppInfo

enum class AppAction(@StringRes val label: Int) {
    AddToHome(R.string.action_add_to_home),
    RemoveFromHome(R.string.action_remove_from_home),
    MoveUp(R.string.action_move_up),
    MoveDown(R.string.action_move_down),
    Rename(R.string.action_rename),
    Hide(R.string.action_hide),
    AppDetails(R.string.action_app_details),
    Uninstall(R.string.action_uninstall);

    companion object {
        fun availableFor(app: AppInfo, homeApps: List<String>): List<AppAction> = buildList {
            val homeIndex = homeApps.indexOf(app.key)
            if (homeIndex == -1) {
                add(AddToHome)
            } else {
                add(RemoveFromHome)
                if (homeIndex > 0) add(MoveUp)
                if (homeIndex < homeApps.lastIndex) add(MoveDown)
            }
            add(Rename)
            add(Hide)
            add(AppDetails)
            add(Uninstall)
        }
    }
}
