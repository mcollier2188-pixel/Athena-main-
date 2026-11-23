package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class EchoColorPalette(
    val bg: Color,
    val bgAlt: Color,
    val card: Color,
    val cardStroke: Color,
    val cyan: Color,
    val cyanSoft: Color,
    val blue: Color,
    val onCyan: Color,
    val text: Color,
    val muted: Color,
    val green: Color,
    val amber: Color,
    val red: Color,
    val purple: Color,
    val darkSurface: Color,
    val overlay: Color,
    val isDark: Boolean
)

/** Exact bit-for-bit preservation of Athena's original dark atmospheric palette. */
val DarkEchoColors = EchoColorPalette(
    bg = Color(0xFF0A0A0F),
    bgAlt = Color(0xFF12121A),
    card = Color(0xFF122430),
    cardStroke = Color(0xFF23404F),
    cyan = Color(0xFF6CC4E0),
    cyanSoft = Color(0x266CC4E0),
    blue = Color(0xFF6EB5FF),
    onCyan = Color(0xFF062431),
    text = Color(0xFFE4EEF2),
    muted = Color(0xFF8AA4AE),
    green = Color(0xFF4ADE80),
    amber = Color(0xFFF59E0B),
    red = Color(0xFFEF4444),
    purple = Color(0xFFA855F7),
    darkSurface = Color(0xFF141A22),
    overlay = Color(0xCC0A0A0F),
    isDark = true
)

/** WCAG 2.1 AA/AAA high-contrast solar-slate Light Mode palette. */
val LightEchoColors = EchoColorPalette(
    bg = Color(0xFFEEF2F6),
    bgAlt = Color(0xFFF8FAFC),
    card = Color(0xFFFFFFFF),
    cardStroke = Color(0xFFCBD5E1),
    cyan = Color(0xFF0369A1),
    cyanSoft = Color(0x260284C7),
    blue = Color(0xFF1D4ED8),
    onCyan = Color(0xFFFFFFFF),
    text = Color(0xFF0F172A),
    muted = Color(0xFF475569),
    green = Color(0xFF15803D),
    amber = Color(0xFFB45309),
    red = Color(0xFFB91C1C),
    purple = Color(0xFF7E22CE),
    darkSurface = Color(0xFFE2E8F0),
    overlay = Color(0xB30F172A),
    isDark = false
)

val LocalEchoColors = staticCompositionLocalOf { DarkEchoColors }

object EchoTheme {
    val colors: EchoColorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalEchoColors.current
}

val EchoBg: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.bg
val EchoBgAlt: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.bgAlt
val EchoCard: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.card
val EchoCardStroke: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.cardStroke
val EchoCyan: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.cyan
val EchoCyanSoft: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.cyanSoft
val EchoBlue: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.blue
val EchoOnCyan: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.onCyan
val EchoText: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.text
val EchoMuted: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.muted
val EchoGreen: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.green
val EchoAmber: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.amber
val EchoRed: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.red
val EchoPurple: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.purple
val EchoDarkSurface: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.darkSurface
val EchoOverlay: Color @Composable @ReadOnlyComposable get() = LocalEchoColors.current.overlay
