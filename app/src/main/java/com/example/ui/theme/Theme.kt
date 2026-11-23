package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkEchoColors.cyan,
    onPrimary = DarkEchoColors.onCyan,
    primaryContainer = DarkEchoColors.card,
    onPrimaryContainer = DarkEchoColors.text,
    secondary = DarkEchoColors.blue,
    onSecondary = DarkEchoColors.onCyan,
    secondaryContainer = DarkEchoColors.bgAlt,
    onSecondaryContainer = DarkEchoColors.text,
    tertiary = DarkEchoColors.green,
    background = DarkEchoColors.bg,
    onBackground = DarkEchoColors.text,
    surface = DarkEchoColors.bgAlt,
    onSurface = DarkEchoColors.text,
    surfaceVariant = DarkEchoColors.card,
    onSurfaceVariant = DarkEchoColors.muted,
    outline = DarkEchoColors.cardStroke
)

private val LightColorScheme = lightColorScheme(
    primary = LightEchoColors.cyan,
    onPrimary = LightEchoColors.onCyan,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = LightEchoColors.cyan,
    secondary = LightEchoColors.blue,
    onSecondary = Color.White,
    secondaryContainer = LightEchoColors.bgAlt,
    onSecondaryContainer = LightEchoColors.text,
    tertiary = LightEchoColors.green,
    background = LightEchoColors.bg,
    onBackground = LightEchoColors.text,
    surface = LightEchoColors.card,
    onSurface = LightEchoColors.text,
    surfaceVariant = LightEchoColors.darkSurface,
    onSurfaceVariant = LightEchoColors.muted,
    outline = LightEchoColors.cardStroke
)

private tailrec fun Context.unwrapActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext?.unwrapActivity()
    else -> null
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val echoColors = if (darkTheme) DarkEchoColors else LightEchoColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            view.context.unwrapActivity()?.window?.let { window ->
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalEchoColors provides echoColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun AthenaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = MyApplicationTheme(darkTheme = darkTheme, content = content)
