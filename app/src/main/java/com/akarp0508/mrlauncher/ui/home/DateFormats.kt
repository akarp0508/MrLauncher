package com.akarp0508.mrlauncher.ui.home

import android.text.format.DateFormat
import com.akarp0508.mrlauncher.data.DateStyle
import java.util.Locale

/** Formats [timeMillis] in this style, following the device locale's order and conventions. */
fun DateStyle.format(timeMillis: Long): String =
    DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), skeleton), timeMillis).toString()
