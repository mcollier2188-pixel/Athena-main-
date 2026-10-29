package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class DailyAppCategory(val displayName: String, val icon: ImageVector) {
    ALL("All Apps", Icons.Default.Apps),
    UTILITIES("Utilities", Icons.Default.Build),
    TOOLS("Tools", Icons.Default.Construction),
    GAMES("Games", Icons.Default.SportsEsports),
    PRODUCTIVITY("Productivity", Icons.Default.CheckCircle),
    CREATIVE("Creative", Icons.Default.Brush),
    LIFESTYLE("Lifestyle", Icons.Default.Favorite),
    INSTALLED("On Device", Icons.Default.PhoneAndroid)
}

enum class MiniToolType {
    CALCULATOR,
    TODO_LIST,
    POMODORO_TIMER,
    UNIT_CONVERTER,
    DOODLE_SKETCH,
    COLOR_PALETTE,
    VOICE_MEMO,
    QR_GENERATOR,
    WORLD_CLOCK,
    WATER_TRACKER,
    PASSWORD_GEN,
    EXPENSE_SPLIT,
    DAILY_QUOTES,
    DEVICE_SPECS,
    SCRATCHPAD,
    METRONOME,
    SCREEN_LAMP,
    ASPECT_RATIO,
    RANDOM_DECIDER,
    COUNTDOWN_TIMER,
    TIC_TAC_TOE,
    PUZZLE_2048,
    MEMORY_FLIP,
    SYSTEM_LAUNCH
}

data class DailyApp(
    val id: String,
    val name: String,
    val summary: String,
    val category: DailyAppCategory,
    val icon: ImageVector,
    val miniToolType: MiniToolType,
    val preferredPackage: String? = null,
    val alternativePackage: String? = null,
    val badge: String? = null,
    val tags: List<String> = emptyList(),
    val isSystemInstalled: Boolean = false,
    val isPinned: Boolean = false
)
