package com.akarp0508.koilauncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.akarp0508.koilauncher.ui.LauncherRoot
import com.akarp0508.koilauncher.ui.LauncherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Light icons until settings load; LauncherRoot then matches them to the text color.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            LauncherRoot(viewModel)
        }
    }

    /** Pressing the home button while already in the launcher. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.goHome()
    }

    override fun onStart() {
        super.onStart()
        viewModel.refreshWeather()
    }

    /** Coming back to the launcher normally lands on the home screen; see [LauncherViewModel.onHidden]. */
    override fun onStop() {
        super.onStop()
        viewModel.onHidden()
    }
}
