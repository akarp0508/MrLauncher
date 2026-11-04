package com.akarp0508.koilauncher.data

import android.content.ComponentName
import android.os.UserHandle

data class AppInfo(
    val packageName: String,
    val activityName: String,
    val user: UserHandle,
    /** Stable per-device id of [user]; unlike UserHandle it survives restarts. */
    val userSerial: Long,
    val systemLabel: String,
    val label: String = systemLabel,
    val isWorkProfile: Boolean = false,
) {
    /** Identifier used to persist pins, hides and renames. */
    val key: String get() = "$packageName/$activityName#$userSerial"

    val componentName: ComponentName get() = ComponentName(packageName, activityName)
}
