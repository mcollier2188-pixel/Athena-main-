package com.example.data

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.model.DailyApp
import com.example.model.DailyAppCategory
import com.example.model.MiniToolType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DailyAppsRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("daily_apps_prefs", Context.MODE_PRIVATE)
    private val appContext = context.applicationContext

    private val _pinnedAppIds = MutableStateFlow<Set<String>>(loadPinnedAppIds())
    val pinnedAppIds: StateFlow<Set<String>> = _pinnedAppIds.asStateFlow()

    private fun loadPinnedAppIds(): Set<String> {
        return prefs.getStringSet("pinned_apps", setOf("app_calc", "app_tictactoe", "app_todo", "app_2048")) ?: emptySet()
    }

    fun togglePin(appId: String) {
        val current = _pinnedAppIds.value.toMutableSet()
        if (current.contains(appId)) {
            current.remove(appId)
        } else {
            current.add(appId)
        }
        prefs.edit().putStringSet("pinned_apps", current).apply()
        _pinnedAppIds.value = current
    }

    fun isPinned(appId: String): Boolean {
        return _pinnedAppIds.value.contains(appId)
    }

    val dailyCatalog: List<DailyApp> = listOf(
        // --- UTILITIES ---
        DailyApp(
            id = "app_calc",
            name = "Quick Calculator & Tip Splitter",
            summary = "Instant standard math, tip percentages, and split bill per person.",
            category = DailyAppCategory.UTILITIES,
            icon = Icons.Default.Calculate,
            miniToolType = MiniToolType.CALCULATOR,
            preferredPackage = "com.google.android.calculator",
            badge = "Utility",
            tags = listOf("utilities", "math", "bill", "tip", "finance", "percent")
        ),
        DailyApp(
            id = "app_unit",
            name = "Currency & Unit Converter",
            summary = "Convert length, weight, temperature, cooking volumes, and data sizes.",
            category = DailyAppCategory.UTILITIES,
            icon = Icons.Default.SwapHoriz,
            miniToolType = MiniToolType.UNIT_CONVERTER,
            tags = listOf("utilities", "convert", "miles", "km", "celsius", "fahrenheit", "kg")
        ),
        DailyApp(
            id = "app_passgen",
            name = "Password & Key Generator",
            summary = "Cryptographically strong customizable passwords with length and symbol controls.",
            category = DailyAppCategory.UTILITIES,
            icon = Icons.Default.VpnKey,
            miniToolType = MiniToolType.PASSWORD_GEN,
            tags = listOf("utilities", "security", "password", "pin", "crypto")
        ),
        DailyApp(
            id = "app_device",
            name = "Device Specs & Hardware Info",
            summary = "Real-time battery level, available memory, Android OS version, and display DPI.",
            category = DailyAppCategory.UTILITIES,
            icon = Icons.Default.PermDeviceInformation,
            miniToolType = MiniToolType.DEVICE_SPECS,
            tags = listOf("utilities", "specs", "battery", "ram", "system", "android")
        ),
        DailyApp(
            id = "app_lamp",
            name = "Screen Glow & Soft Lamp",
            summary = "Gentle full-screen reading light with adjustable brightness and warm tone.",
            category = DailyAppCategory.UTILITIES,
            icon = Icons.Default.Lightbulb,
            miniToolType = MiniToolType.SCREEN_LAMP,
            tags = listOf("utilities", "flashlight", "lamp", "reading", "torch", "light")
        ),

        // --- TOOLS ---
        DailyApp(
            id = "app_qr",
            name = "QR Code & Barcode Studio",
            summary = "Generate sharp custom QR codes for any link, Wi-Fi, or plain text snippet.",
            category = DailyAppCategory.TOOLS,
            icon = Icons.Default.QrCode,
            miniToolType = MiniToolType.QR_GENERATOR,
            badge = "Tool",
            tags = listOf("tools", "qr", "barcode", "share", "link", "wifi")
        ),
        DailyApp(
            id = "app_aspect",
            name = "Aspect Ratio & Resolution Calc",
            summary = "Calculate 16:9, 4:3, 9:16, 1:1, and custom resolutions for photo and video.",
            category = DailyAppCategory.TOOLS,
            icon = Icons.Default.AspectRatio,
            miniToolType = MiniToolType.ASPECT_RATIO,
            tags = listOf("tools", "aspect", "ratio", "video", "photo", "screen")
        ),
        DailyApp(
            id = "app_expensesplit",
            name = "Expense & Splitter Calculator",
            summary = "Split group meals, grocery trips, or trips evenly with sales tax.",
            category = DailyAppCategory.TOOLS,
            icon = Icons.Default.ReceiptLong,
            miniToolType = MiniToolType.EXPENSE_SPLIT,
            tags = listOf("tools", "money", "expense", "split", "budget")
        ),
        DailyApp(
            id = "app_notes",
            name = "Distraction-Free Scratchpad",
            summary = "Instant zero-lag plain text memo with live word count and character stats.",
            category = DailyAppCategory.TOOLS,
            icon = Icons.Default.EditNote,
            miniToolType = MiniToolType.SCRATCHPAD,
            preferredPackage = "com.google.android.keep",
            tags = listOf("tools", "notes", "text", "draft", "writing")
        ),
        DailyApp(
            id = "app_metronome",
            name = "Audio Metronome & Tap BPM",
            summary = "Precision rhythm pulse from 40 to 240 BPM with tap-tempo detector.",
            category = DailyAppCategory.TOOLS,
            icon = Icons.Default.MusicNote,
            miniToolType = MiniToolType.METRONOME,
            tags = listOf("tools", "music", "tempo", "bpm", "beat", "rhythm")
        ),

        // --- GAMES ---
        DailyApp(
            id = "app_tictactoe",
            name = "Tic-Tac-Toe Classic & AI",
            summary = "Play Xs and Os in 2-player pass & play or challenge the built-in AI bot.",
            category = DailyAppCategory.GAMES,
            icon = Icons.Default.SportsEsports,
            miniToolType = MiniToolType.TIC_TAC_TOE,
            badge = "Game",
            tags = listOf("games", "play", "tictactoe", "puzzle", "strategy")
        ),
        DailyApp(
            id = "app_2048",
            name = "2048 Mini Tile Puzzle",
            summary = "Slide and merge matching number tiles to reach 2048 and beat your high score.",
            category = DailyAppCategory.GAMES,
            icon = Icons.Default.GridOn,
            miniToolType = MiniToolType.PUZZLE_2048,
            badge = "Game",
            tags = listOf("games", "puzzle", "2048", "numbers", "brain")
        ),
        DailyApp(
            id = "app_memoryflip",
            name = "Memory Card Flip",
            summary = "Flip cards, find matching pairs, and test your memory speed and moves.",
            category = DailyAppCategory.GAMES,
            icon = Icons.Default.Psychology,
            miniToolType = MiniToolType.MEMORY_FLIP,
            badge = "Game",
            tags = listOf("games", "memory", "cards", "flip", "brain")
        ),

        // --- PRODUCTIVITY ---
        DailyApp(
            id = "app_todo",
            name = "Daily To-Do & Checklist",
            summary = "Prioritized daily task list with quick add, check-off, and clear completed.",
            category = DailyAppCategory.PRODUCTIVITY,
            icon = Icons.Default.Checklist,
            miniToolType = MiniToolType.TODO_LIST,
            preferredPackage = "com.google.android.keep",
            badge = "Productivity",
            tags = listOf("productivity", "tasks", "todo", "planner", "routine")
        ),
        DailyApp(
            id = "app_pomodoro",
            name = "Focus & Pomodoro Timer",
            summary = "25m deep work intervals and 5m mindful recovery breaks.",
            category = DailyAppCategory.PRODUCTIVITY,
            icon = Icons.Default.Timer,
            miniToolType = MiniToolType.POMODORO_TIMER,
            preferredPackage = "com.google.android.deskclock",
            badge = "Focus",
            tags = listOf("productivity", "timer", "work", "clock", "study")
        ),
        DailyApp(
            id = "app_voice",
            name = "Voice Memo & Audio Recorder",
            summary = "Record rapid spoken thoughts, play them back, and track recording time.",
            category = DailyAppCategory.PRODUCTIVITY,
            icon = Icons.Default.Mic,
            miniToolType = MiniToolType.VOICE_MEMO,
            preferredPackage = "com.google.android.apps.recorder",
            tags = listOf("productivity", "audio", "sound", "voice", "notes", "record")
        ),
        DailyApp(
            id = "app_countdown",
            name = "Event & Goal Countdown",
            summary = "Track days, hours, and minutes remaining until your milestone moments.",
            category = DailyAppCategory.PRODUCTIVITY,
            icon = Icons.Default.Event,
            miniToolType = MiniToolType.COUNTDOWN_TIMER,
            preferredPackage = "com.google.android.calendar",
            tags = listOf("productivity", "event", "countdown", "days", "milestone")
        ),

        // --- CREATIVE ---
        DailyApp(
            id = "app_doodle",
            name = "Whiteboard & Sketchpad",
            summary = "Fluid finger sketching canvas with color pen palette and one-tap clear.",
            category = DailyAppCategory.CREATIVE,
            icon = Icons.Default.Draw,
            miniToolType = MiniToolType.DOODLE_SKETCH,
            badge = "Creative",
            tags = listOf("creative", "draw", "sketch", "paint", "canvas", "notes")
        ),
        DailyApp(
            id = "app_palette",
            name = "Color Palette Studio",
            summary = "Generate harmonious hex color palettes with copy-to-clipboard codes.",
            category = DailyAppCategory.CREATIVE,
            icon = Icons.Default.Palette,
            miniToolType = MiniToolType.COLOR_PALETTE,
            tags = listOf("creative", "color", "hex", "design", "theme", "art")
        ),

        // --- LIFESTYLE ---
        DailyApp(
            id = "app_water",
            name = "Daily Hydration Tracker",
            summary = "Track your daily 8-glass water goal with visual hydration progress.",
            category = DailyAppCategory.LIFESTYLE,
            icon = Icons.Default.WaterDrop,
            miniToolType = MiniToolType.WATER_TRACKER,
            badge = "Health",
            tags = listOf("lifestyle", "health", "water", "drink", "wellness", "habit")
        ),
        DailyApp(
            id = "app_worldclock",
            name = "World Clock & Timezones",
            summary = "Instant comparison of global time across London, New York, Tokyo, and Sydney.",
            category = DailyAppCategory.LIFESTYLE,
            icon = Icons.Default.Public,
            miniToolType = MiniToolType.WORLD_CLOCK,
            preferredPackage = "com.google.android.deskclock",
            tags = listOf("lifestyle", "time", "clock", "timezone", "global")
        ),
        DailyApp(
            id = "app_quotes",
            name = "Daily Wisdom & Quotes",
            summary = "Inspirational insights, clarity reminders, and daily philosophical prompts.",
            category = DailyAppCategory.LIFESTYLE,
            icon = Icons.Default.AutoStories,
            miniToolType = MiniToolType.DAILY_QUOTES,
            tags = listOf("lifestyle", "wisdom", "motivation", "mindset", "reading")
        ),
        DailyApp(
            id = "app_random",
            name = "Random Decider & Dice Roller",
            summary = "Can't decide? Coin flip, 6-sided dice roll, and random choice picker.",
            category = DailyAppCategory.LIFESTYLE,
            icon = Icons.Default.Casino,
            miniToolType = MiniToolType.RANDOM_DECIDER,
            tags = listOf("lifestyle", "dice", "coin", "random", "game", "decide")
        )
    )

    fun getInstalledDeviceApps(): List<DailyApp> {
        val pm = appContext.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        return resolveInfos
            .filter { it.activityInfo != null && it.activityInfo.packageName != appContext.packageName }
            .map { resolveInfo ->
                val label = resolveInfo.loadLabel(pm).toString()
                val pkg = resolveInfo.activityInfo.packageName
                val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    resolveInfo.activityInfo.applicationInfo.category == ApplicationInfo.CATEGORY_GAME
                } else {
                    false
                } || pkg.contains("game", ignoreCase = true) || label.contains("game", ignoreCase = true)

                val detectedCategory = if (isGame) DailyAppCategory.GAMES else DailyAppCategory.INSTALLED
                val tagsList = mutableListOf("installed", label.lowercase(), pkg)
                if (isGame) tagsList.add("game")

                DailyApp(
                    id = "installed_$pkg",
                    name = label,
                    summary = if (isGame) "Installed Game ($pkg)" else "Installed application ($pkg)",
                    category = detectedCategory,
                    icon = if (isGame) Icons.Default.SportsEsports else Icons.Default.Android,
                    miniToolType = MiniToolType.SYSTEM_LAUNCH,
                    preferredPackage = pkg,
                    badge = if (isGame) "Game" else null,
                    isSystemInstalled = true,
                    tags = tagsList
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    companion object {
        @Volatile
        private var INSTANCE: DailyAppsRepository? = null

        fun getInstance(context: Context): DailyAppsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DailyAppsRepository(context).also { INSTANCE = it }
            }
        }
    }
}
