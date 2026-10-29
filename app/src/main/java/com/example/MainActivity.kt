package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.MainApp
import com.example.ui.screens.EntranceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.OrientationManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Native Landscape by default with persisted orientation support
        val orientationManager = OrientationManager.getInstance(this)
        orientationManager.applyToActivity(this)

        enableEdgeToEdge()

        // Hide native navigation bar and enable transient swipe behavior
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())

        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(true) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainApp(
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}
