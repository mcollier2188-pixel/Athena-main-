package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DailyApp
import com.example.model.MiniToolType
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyAppMiniToolModal(
    app: DailyApp,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = app.icon,
                                        contentDescription = app.name,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = app.category.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        // Native app launch button if available
                        if (app.preferredPackage != null) {
                            IconButton(
                                onClick = {
                                    launchExternalApp(context, app.preferredPackage)
                                },
                                tooltip = "Open in Android"
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Open App")
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )

                Divider()

                // Tool Content Area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (app.miniToolType) {
                        MiniToolType.CALCULATOR -> CalculatorToolView()
                        MiniToolType.TODO_LIST -> TodoListToolView()
                        MiniToolType.POMODORO_TIMER -> PomodoroTimerToolView()
                        MiniToolType.UNIT_CONVERTER -> UnitConverterToolView()
                        MiniToolType.DOODLE_SKETCH -> DoodleSketchToolView()
                        MiniToolType.COLOR_PALETTE -> ColorPaletteToolView()
                        MiniToolType.VOICE_MEMO -> VoiceMemoToolView()
                        MiniToolType.QR_GENERATOR -> QrGeneratorToolView()
                        MiniToolType.WORLD_CLOCK -> WorldClockToolView()
                        MiniToolType.WATER_TRACKER -> WaterTrackerToolView()
                        MiniToolType.PASSWORD_GEN -> PasswordGeneratorToolView()
                        MiniToolType.EXPENSE_SPLIT -> ExpenseSplitterToolView()
                        MiniToolType.DAILY_QUOTES -> DailyQuotesToolView()
                        MiniToolType.DEVICE_SPECS -> DeviceSpecsToolView()
                        MiniToolType.SCRATCHPAD -> ScratchpadToolView()
                        MiniToolType.METRONOME -> MetronomeToolView()
                        MiniToolType.SCREEN_LAMP -> ScreenLampToolView()
                        MiniToolType.ASPECT_RATIO -> AspectRatioToolView()
                        MiniToolType.RANDOM_DECIDER -> RandomDeciderToolView()
                        MiniToolType.COUNTDOWN_TIMER -> CountdownTimerToolView()
                        MiniToolType.TIC_TAC_TOE -> TicTacToeToolView()
                        MiniToolType.PUZZLE_2048 -> Puzzle2048ToolView()
                        MiniToolType.MEMORY_FLIP -> MemoryFlipToolView()
                        MiniToolType.SYSTEM_LAUNCH -> SystemLaunchToolView(app.preferredPackage)
                    }
                }
            }
        }
    }
}

private fun launchExternalApp(context: Context, packageName: String) {
    try {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, "App not installed on device: $packageName", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Could not launch $packageName", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun IconButton(onClick: () -> Unit, tooltip: String, content: @Composable () -> Unit) {
    IconButton(onClick = onClick) {
        content()
    }
}

// ---------------- MINI TOOL 1: CALCULATOR & TIP SPLITTER ----------------
@Composable
fun CalculatorToolView() {
    var display by remember { mutableStateOf("0") }
    var tipPercent by remember { mutableFloatStateOf(15f) }
    var peopleCount by remember { mutableIntStateOf(1) }

    val billAmount = display.toDoubleOrNull() ?: 0.0
    val tipAmount = billAmount * (tipPercent / 100.0)
    val totalAmount = billAmount + tipAmount
    val perPerson = if (peopleCount > 0) totalAmount / peopleCount else totalAmount

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Display card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "Amount: $$display", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                if (billAmount > 0) {
                    Text(
                        text = "Tip (${tipPercent.roundToInt()}%): $${String.format(Locale.US, "%.2f", tipAmount)} | Total: $${String.format(Locale.US, "%.2f", totalAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Per Person ($peopleCount): $${String.format(Locale.US, "%.2f", perPerson)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Tip slider & split
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Tip: ${tipPercent.roundToInt()}%", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = tipPercent,
                onValueChange = { tipPercent = it },
                valueRange = 0f..30f,
                steps = 5,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = { if (peopleCount > 1) peopleCount-- }) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                Text("$peopleCount", fontWeight = FontWeight.Bold)
                IconButton(onClick = { peopleCount++ }) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }

        // Number Pad
        val keys = listOf(
            listOf("7", "8", "9", "C"),
            listOf("4", "5", "6", "⌫"),
            listOf("1", "2", "3", "00"),
            listOf("0", ".", "000", "+")
        )

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (row in keys) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (key in row) {
                        Button(
                            onClick = {
                                when (key) {
                                    "C" -> display = "0"
                                    "⌫" -> display = if (display.length > 1) display.dropLast(1) else "0"
                                    "." -> if (!display.contains(".")) display += "."
                                    else -> display = if (display == "0") key else display + key
                                }
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (key == "C" || key == "⌫") {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                            } else {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        ) {
                            Text(key, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- MINI TOOL 2: TO-DO CHECKLIST ----------------
data class TodoItem(val id: Long, val text: String, var isCompleted: Boolean = false)

@Composable
fun TodoListToolView() {
    var items by remember {
        mutableStateOf(
            listOf(
                TodoItem(1, "Review weekly action goals", false),
                TodoItem(2, "Organize desktop workspace & files", true),
                TodoItem(3, "Schedule 30m hydration & walk break", false),
                TodoItem(4, "Clear out inbox & priority messages", false)
            )
        )
    }
    var newText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                label = { Text("Add daily task...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = {
                    if (newText.isNotBlank()) {
                        items = listOf(TodoItem(System.currentTimeMillis(), newText.trim(), false)) + items
                        newText = ""
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val completedCount = items.count { it.isCompleted }
            Text("Completed $completedCount of ${items.size}", style = MaterialTheme.typography.labelMedium)
            TextButton(onClick = { items = items.filter { !it.isCompleted } }) {
                Text("Clear completed")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(items, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.isCompleted,
                            onCheckedChange = { checked ->
                                items = items.map { if (it.id == item.id) it.copy(isCompleted = checked) else it }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.text,
                            modifier = Modifier.weight(1f),
                            style = if (item.isCompleted) MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            ) else MaterialTheme.typography.bodyMedium
                        )
                        IconButton(onClick = { items = items.filter { it.id != item.id } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- MINI TOOL 3: POMODORO TIMER ----------------
@Composable
fun PomodoroTimerToolView() {
    var isWorkSession by remember { mutableStateOf(true) }
    var secondsLeft by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning && secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        if (secondsLeft == 0 && isRunning) {
            isRunning = false
            isWorkSession = !isWorkSession
            secondsLeft = if (isWorkSession) 25 * 60 else 5 * 60
        }
    }

    val totalSeconds = if (isWorkSession) 25 * 60 else 5 * 60
    val progress = (totalSeconds - secondsLeft).toFloat() / totalSeconds.toFloat()
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = isWorkSession,
                onClick = {
                    isWorkSession = true
                    isRunning = false
                    secondsLeft = 25 * 60
                },
                label = { Text("Deep Work (25m)") }
            )
            FilterChip(
                selected = !isWorkSession,
                onClick = {
                    isWorkSession = false
                    isRunning = false
                    secondsLeft = 5 * 60
                },
                label = { Text("Recovery (5m)") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 10.dp,
                color = if (isWorkSession) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isWorkSession) "Stay Locked In" else "Rest & Stretch",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isRunning) "Pause" else "Start Focus")
            }

            OutlinedButton(
                onClick = {
                    isRunning = false
                    secondsLeft = if (isWorkSession) 25 * 60 else 5 * 60
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset")
            }
        }
    }
}

// ---------------- MINI TOOL 4: UNIT CONVERTER ----------------
@Composable
fun UnitConverterToolView() {
    var mode by remember { mutableStateOf("Length") }
    var inputValue by remember { mutableStateOf("1") }

    val modes = listOf("Length", "Temp", "Weight", "Data")
    val inputNum = inputValue.toDoubleOrNull() ?: 0.0

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (m in modes) {
                FilterChip(
                    selected = mode == m,
                    onClick = { mode = m },
                    label = { Text(m) }
                )
            }
        }

        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = { Text("Enter value to convert") },
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (mode) {
                    "Length" -> {
                        Text("• Miles: ${String.format(Locale.US, "%.3f", inputNum * 0.621371)} mi")
                        Text("• Kilometers: ${String.format(Locale.US, "%.3f", inputNum * 1.60934)} km")
                        Text("• Meters: ${String.format(Locale.US, "%.2f", inputNum * 1000)} m")
                        Text("• Feet: ${String.format(Locale.US, "%.2f", inputNum * 3280.84)} ft")
                        Text("• Inches: ${String.format(Locale.US, "%.2f", inputNum * 39370.1)} in")
                    }
                    "Temp" -> {
                        val cToF = (inputNum * 9 / 5) + 32
                        val fToC = (inputNum - 32) * 5 / 9
                        Text("• $inputNum °C = ${String.format(Locale.US, "%.1f", cToF)} °F")
                        Text("• $inputNum °F = ${String.format(Locale.US, "%.1f", fToC)} °C")
                        Text("• $inputNum °C = ${String.format(Locale.US, "%.2f", inputNum + 273.15)} Kelvin")
                    }
                    "Weight" -> {
                        Text("• Kilograms to Pounds: ${String.format(Locale.US, "%.2f", inputNum * 2.20462)} lbs")
                        Text("• Pounds to Kilograms: ${String.format(Locale.US, "%.2f", inputNum * 0.453592)} kg")
                        Text("• Grams: ${String.format(Locale.US, "%.1f", inputNum * 1000)} g")
                        Text("• Ounces: ${String.format(Locale.US, "%.2f", inputNum * 35.274)} oz")
                    }
                    "Data" -> {
                        Text("• Gigabytes to Megabytes: ${inputNum * 1024} MB")
                        Text("• Gigabytes to Terabytes: ${String.format(Locale.US, "%.4f", inputNum / 1024)} TB")
                        Text("• Gigabytes to Bits: ${inputNum * 8} Gb")
                    }
                }
            }
        }
    }
}

// ---------------- MINI TOOL 5: WHITEBOARD & SKETCHPAD ----------------
data class DrawPath(val path: Path, val color: Color, val strokeWidth: Float)

@Composable
fun DoodleSketchToolView() {
    val paths = remember { mutableStateListOf<DrawPath>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentColor by remember { mutableStateOf(Color.Cyan) }
    var strokeWidth by remember { mutableFloatStateOf(6f) }

    val colors = listOf(Color.Cyan, Color.Magenta, Color.Yellow, Color.White, Color.Green, Color.Red)

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (c in colors) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(
                                width = if (currentColor == c) 2.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                            .clickable { currentColor = c }
                    )
                }
            }

            Row {
                IconButton(onClick = { if (paths.isNotEmpty()) paths.removeAt(paths.size - 1) }) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo")
                }
                IconButton(onClick = { paths.clear() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF14141E))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val p = Path().apply { moveTo(offset.x, offset.y) }
                            currentPath = p
                        },
                        onDrag = { change, _ ->
                            currentPath?.lineTo(change.position.x, change.position.y)
                        },
                        onDragEnd = {
                            currentPath?.let {
                                paths.add(DrawPath(it, currentColor, strokeWidth))
                            }
                            currentPath = null
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (p in paths) {
                    drawPath(
                        path = p.path,
                        color = p.color,
                        style = Stroke(width = p.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
                currentPath?.let {
                    drawPath(
                        path = it,
                        color = currentColor,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
    }
}

// ---------------- MINI TOOL 6: COLOR PALETTE STUDIO ----------------
@Composable
fun ColorPaletteToolView() {
    val context = LocalContext.current
    var paletteSeed by remember { mutableIntStateOf(0) }

    val palettes = listOf(
        listOf("#0D1B2A", "#1B263B", "#415A77", "#778DA9", "#E0E1DD"),
        listOf("#2B2D42", "#8D99AE", "#EDF2F4", "#EF233C", "#D90429"),
        listOf("#06D6A0", "#118AB2", "#073B4C", "#FFD166", "#EF476F"),
        listOf("#6B2D5C", "#F39237", "#0081AF", "#2E86AB", "#E6AF2E"),
        listOf("#101014", "#24252D", "#00E5FF", "#7C4DFF", "#FF4081")
    )
    val activePalette = palettes[paletteSeed % palettes.size]

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Harmony Palette #${paletteSeed + 1}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Button(onClick = { paletteSeed++ }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate New")
            }
        }

        Column(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (hex in activePalette) {
                val parsedColor = try {
                    Color(android.graphics.Color.parseColor(hex))
                } catch (e: Exception) {
                    Color.Gray
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable {
                            val clip = ClipData.newPlainText("hex", hex)
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                            Toast.makeText(context, "Copied $hex to clipboard", Toast.LENGTH_SHORT).show()
                        },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(parsedColor)
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$hex (Tap to copy)",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- MINI TOOL 7: VOICE MEMO RECORDER ----------------
@Composable
fun VoiceMemoToolView() {
    var isRecording by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var recordedMemos by remember {
        mutableStateOf(listOf("Thought memo #1 - Today at 09:30 AM (0:45)", "Project ideas - Yesterday (1:20)"))
    }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            secondsElapsed++
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format(Locale.US, "%02d:%02d", secondsElapsed / 60, secondsElapsed % 60),
                    style = MaterialTheme.typography.displayMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                IconButton(
                    onClick = {
                        if (isRecording) {
                            isRecording = false
                            recordedMemos = listOf("Voice memo (${secondsElapsed}s) - Just now") + recordedMemos
                            secondsElapsed = 0
                        } else {
                            isRecording = true
                            secondsElapsed = 0
                        }
                    },
                    modifier = Modifier.size(64.dp).background(
                        if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Record",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(if (isRecording) "Recording live..." else "Tap mic to record audio memo")
            }
        }

        Text("Saved Voice Memos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(recordedMemos) { memo ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(memo, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ---------------- MINI TOOL 8: QR GENERATOR STUDIO ----------------
@Composable
fun QrGeneratorToolView() {
    val context = LocalContext.current
    var qrText by remember { mutableStateOf("https://google.com") }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = qrText,
            onValueChange = { qrText = it },
            label = { Text("Link, Wi-Fi info, or text to encode") },
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSize = 15
                val cellW = size.width / gridSize
                val cellH = size.height / gridSize
                val hash = qrText.hashCode()

                for (r in 0 until gridSize) {
                    for (c in 0 until gridSize) {
                        // Corners
                        val isCorner = (r < 4 && c < 4) || (r < 4 && c > gridSize - 5) || (r > gridSize - 5 && c < 4)
                        val isData = ((hash xor (r * 31 + c * 17)) and (1 shl ((r + c) % 8))) != 0
                        if (isCorner || isData) {
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(c * cellW, r * cellH),
                                size = androidx.compose.ui.geometry.Size(cellW, cellH)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, qrText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share text"))
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Content")
            }
        }
    }
}

// ---------------- MINI TOOL 9: WORLD CLOCK ----------------
@Composable
fun WorldClockToolView() {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val zones = listOf(
        Pair("London (GMT)", TimeZone.getTimeZone("Europe/London")),
        Pair("New York (EST)", TimeZone.getTimeZone("America/New_York")),
        Pair("San Francisco (PST)", TimeZone.getTimeZone("America/Los_Angeles")),
        Pair("Tokyo (JST)", TimeZone.getTimeZone("Asia/Tokyo")),
        Pair("Sydney (AEST)", TimeZone.getTimeZone("Australia/Sydney")),
        Pair("Dubai (GST)", TimeZone.getTimeZone("Asia/Dubai"))
    )

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(zones) { (name, zone) ->
            val sdf = SimpleDateFormat("hh:mm:ss a", Locale.US).apply { timeZone = zone }
            val dateFmt = SimpleDateFormat("EEE, MMM d", Locale.US).apply { timeZone = zone }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(dateFmt.format(Date(currentTime)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        sdf.format(Date(currentTime)),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ---------------- MINI TOOL 10: HYDRATION TRACKER ----------------
@Composable
fun WaterTrackerToolView() {
    var glassesDrank by remember { mutableIntStateOf(3) }
    val goal = 8
    val progress = (glassesDrank.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Daily 8-Glass Goal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape),
            color = Color(0xFF00B4D8)
        )

        Text("$glassesDrank / $goal Glasses (${(progress * 100).roundToInt()}%)", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 12.dp)) {
            for (i in 1..goal) {
                IconButton(
                    onClick = {
                        glassesDrank = if (i <= glassesDrank && i == glassesDrank) i - 1 else i
                    },
                    modifier = Modifier.size(36.dp).background(
                        if (i <= glassesDrank) Color(0xFF00B4D8) else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
                ) {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = "Glass $i",
                        tint = if (i <= glassesDrank) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { if (glassesDrank < 12) glassesDrank++ }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Drink Glass")
            }
            OutlinedButton(onClick = { glassesDrank = 0 }) {
                Text("Reset")
            }
        }
    }
}

// ---------------- MINI TOOL 11: PASSWORD GENERATOR ----------------
@Composable
fun PasswordGeneratorToolView() {
    val context = LocalContext.current
    var length by remember { mutableFloatStateOf(16f) }
    var includeSymbols by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var password by remember { mutableStateOf("") }

    fun generate() {
        val chars = buildString {
            append("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
            if (includeNumbers) append("0123456789")
            if (includeSymbols) append("!@#$%^&*()-_=+[]{}<>?")
        }
        val rand = java.security.SecureRandom()
        val pwd = (1..length.roundToInt()).map { chars[rand.nextInt(chars.length)] }.joinToString("")
        password = pwd
    }

    LaunchedEffect(Unit) { generate() }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Generated Password", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = password,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Length: ${length.roundToInt()}")
            Slider(value = length, onValueChange = { length = it; generate() }, valueRange = 8f..32f, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = includeNumbers, onCheckedChange = { includeNumbers = it; generate() })
            Text("Include Numbers (0-9)")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = includeSymbols, onCheckedChange = { includeSymbols = it; generate() })
            Text("Include Symbols (!@#$)")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { generate() }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Re-generate")
            }
            OutlinedButton(
                onClick = {
                    val clip = ClipData.newPlainText("password", password)
                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                    Toast.makeText(context, "Password copied!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy")
            }
        }
    }
}

// ---------------- MINI TOOL 12: EXPENSE SPLITTER ----------------
@Composable
fun ExpenseSplitterToolView() {
    var billText by remember { mutableStateOf("85.00") }
    var taxText by remember { mutableStateOf("8.5") }
    var friendsCount by remember { mutableIntStateOf(4) }

    val bill = billText.toDoubleOrNull() ?: 0.0
    val tax = taxText.toDoubleOrNull() ?: 0.0
    val totalWithTax = bill + (bill * (tax / 100.0))
    val eachPays = if (friendsCount > 0) totalWithTax / friendsCount else totalWithTax

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = billText,
            onValueChange = { billText = it },
            label = { Text("Subtotal Bill ($)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = taxText,
            onValueChange = { taxText = it },
            label = { Text("Tax Rate (%)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Split Between: $friendsCount people")
            Row {
                IconButton(onClick = { if (friendsCount > 1) friendsCount-- }) { Icon(Icons.Default.Remove, contentDescription = null) }
                IconButton(onClick = { friendsCount++ }) { Icon(Icons.Default.Add, contentDescription = null) }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total with Tax: $${String.format(Locale.US, "%.2f", totalWithTax)}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Each person pays: $${String.format(Locale.US, "%.2f", eachPays)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

// ---------------- MINI TOOL 13: DAILY QUOTES ----------------
@Composable
fun DailyQuotesToolView() {
    val quotes = listOf(
        Pair("Simplicity is the soul of efficiency.", "Austin Freeman"),
        Pair("Focus is saying no to 1,000 other good ideas.", "Steve Jobs"),
        Pair("We don’t rise to the level of our goals; we fall to the level of our systems.", "James Clear"),
        Pair("The secret of getting ahead is getting started.", "Mark Twain"),
        Pair("Live as if you were to die tomorrow. Learn as if you were to live forever.", "Mahatma Gandhi"),
        Pair("Small daily improvements over time lead to stunning results.", "Robin Sharma")
    )
    var quoteIdx by remember { mutableIntStateOf(0) }
    val (quote, author) = quotes[quoteIdx % quotes.size]

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("“$quote”", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))
                Text("— $author", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { quoteIdx++ }) {
            Icon(Icons.Default.Casino, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Next Insight")
        }
    }
}

// ---------------- MINI TOOL 14: DEVICE SPECS ----------------
@Composable
fun DeviceSpecsToolView() {
    val context = LocalContext.current
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    val batLevel = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    val isCharging = bm?.isCharging ?: false

    val runtime = Runtime.getRuntime()
    val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    val maxMemMb = runtime.maxMemory() / (1024 * 1024)

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Battery Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Level: $batLevel% ${if (isCharging) "(Charging)" else "(Discharging)"}")
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Memory (JVM Heap)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Used: $usedMemMb MB / $maxMemMb MB Allocated")
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Operating System", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Android Release: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    Text("Device Model: ${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}")
                }
            }
        }
    }
}

// ---------------- MINI TOOL 15: SCRATCHPAD ----------------
@Composable
fun ScratchpadToolView() {
    val context = LocalContext.current
    var text by remember { mutableStateOf("Meeting notes & quick reminders:\n\n- ") }
    val wordCount = text.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
    val charCount = text.length

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$wordCount words | $charCount chars", style = MaterialTheme.typography.labelMedium)
            TextButton(
                onClick = {
                    val clip = ClipData.newPlainText("scratchpad", text)
                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                    Toast.makeText(context, "Copied scratchpad", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Copy All")
            }
        }

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
    }
}

// ---------------- MINI TOOL 16: METRONOME & TAP BPM ----------------
@Composable
fun MetronomeToolView() {
    var bpm by remember { mutableFloatStateOf(120f) }
    var isTicking by remember { mutableStateOf(false) }
    var beatState by remember { mutableStateOf(false) }
    val tapTimes = remember { mutableStateListOf<Long>() }

    LaunchedEffect(isTicking, bpm) {
        while (isTicking) {
            val intervalMs = (60000.0 / bpm).toLong()
            beatState = !beatState
            delay(intervalMs)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("${bpm.roundToInt()} BPM", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(60.dp)
                .background(if (beatState && isTicking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Slider(value = bpm, onValueChange = { bpm = it }, valueRange = 40f..240f, modifier = Modifier.fillMaxWidth(0.8f))

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { isTicking = !isTicking }) {
                Icon(if (isTicking) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isTicking) "Stop" else "Start Pulse")
            }

            OutlinedButton(
                onClick = {
                    val now = System.currentTimeMillis()
                    tapTimes.add(now)
                    if (tapTimes.size > 4) tapTimes.removeAt(0)
                    if (tapTimes.size >= 2) {
                        val diffs = tapTimes.zipWithNext { a, b -> b - a }
                        val avgDiff = diffs.average()
                        if (avgDiff > 0) {
                            val computed = (60000.0 / avgDiff).toFloat().coerceIn(40f, 240f)
                            bpm = computed
                        }
                    }
                }
            ) {
                Text("Tap BPM")
            }
        }
    }
}

// ---------------- MINI TOOL 17: SCREEN LAMP & GLOW ----------------
@Composable
fun ScreenLampToolView() {
    var warmth by remember { mutableFloatStateOf(0.4f) }
    var brightness by remember { mutableFloatStateOf(0.9f) }

    val lampColor = Color(
        red = (1f * brightness).coerceIn(0f, 1f),
        green = ((0.9f - warmth * 0.2f) * brightness).coerceIn(0f, 1f),
        blue = ((0.8f - warmth * 0.5f) * brightness).coerceIn(0f, 1f)
    )

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(lampColor)
        )

        Column {
            Text("Warmth Tone: ${(warmth * 100).roundToInt()}%")
            Slider(value = warmth, onValueChange = { warmth = it }, valueRange = 0f..1f)

            Text("Glow Intensity: ${(brightness * 100).roundToInt()}%")
            Slider(value = brightness, onValueChange = { brightness = it }, valueRange = 0.2f..1f)
        }
    }
}

// ---------------- MINI TOOL 18: ASPECT RATIO CALC ----------------
@Composable
fun AspectRatioToolView() {
    var widthInput by remember { mutableStateOf("1920") }
    var heightInput by remember { mutableStateOf("1080") }

    val w = widthInput.toDoubleOrNull() ?: 1.0
    val h = heightInput.toDoubleOrNull() ?: 1.0
    val ratio = if (h > 0) w / h else 1.0

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = widthInput, onValueChange = { widthInput = it }, label = { Text("Width (px)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = heightInput, onValueChange = { heightInput = it }, label = { Text("Height (px)") }, modifier = Modifier.weight(1f))
        }

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Aspect Ratio: ${String.format(Locale.US, "%.2f", ratio)}:1", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• 16:9 equivalent: ${w.roundToInt()} × ${(w * 9 / 16).roundToInt()}")
                Text("• 4:3 equivalent: ${w.roundToInt()} × ${(w * 3 / 4).roundToInt()}")
                Text("• 9:16 (Story / Reel): ${w.roundToInt()} × ${(w * 16 / 9).roundToInt()}")
                Text("• 1:1 (Square): ${w.roundToInt()} × ${w.roundToInt()}")
            }
        }
    }
}

// ---------------- MINI TOOL 19: RANDOM DECIDER ----------------
@Composable
fun RandomDeciderToolView() {
    var coinResult by remember { mutableStateOf("Flip Coin") }
    var diceResult by remember { mutableIntStateOf(6) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(coinResult, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { coinResult = if (Math.random() > 0.5) "HEADS" else "TAILS" }) {
                        Text("Flip Coin")
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎲 $diceResult", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { diceResult = (1..6).random() }) {
                        Text("Roll D6")
                    }
                }
            }
        }
    }
}

// ---------------- MINI TOOL 20: COUNTDOWN TIMER ----------------
@Composable
fun CountdownTimerToolView() {
    var targetDays by remember { mutableIntStateOf(14) }
    var eventName by remember { mutableStateOf("Big Milestone Launch") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = eventName,
            onValueChange = { eventName = it },
            label = { Text("Event Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(eventName, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$targetDays Days Remaining",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text("approx. ${targetDays * 24} hours", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { if (targetDays > 1) targetDays-- }) { Text("-1 Day") }
            Button(onClick = { targetDays += 7 }) { Text("+1 Week") }
            Button(onClick = { targetDays += 30 }) { Text("+1 Month") }
        }
    }
}

// ---------------- SYSTEM APP LAUNCH MODAL ----------------
@Composable
fun SystemLaunchToolView(packageName: String?) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Installed System Application", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(packageName ?: "Unknown", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                packageName?.let { launchExternalApp(context, it) }
            }
        ) {
            Icon(Icons.Default.Launch, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Application Now")
        }
    }
}

// ---------------- MINI GAME 1: TIC-TAC-TOE ----------------
@Composable
fun TicTacToeToolView() {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var isXTurn by remember { mutableStateOf(true) }
    var isVsAi by remember { mutableStateOf(true) }
    var xWins by remember { mutableIntStateOf(0) }
    var oWins by remember { mutableIntStateOf(0) }
    var draws by remember { mutableIntStateOf(0) }

    fun checkWinner(b: List<String>): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (line in lines) {
            val (p1, p2, p3) = line
            if (b[p1].isNotEmpty() && b[p1] == b[p2] && b[p2] == b[p3]) {
                return b[p1]
            }
        }
        return if (b.all { it.isNotEmpty() }) "DRAW" else null
    }

    val winner = checkWinner(board)

    fun makeAiMove(currentBoard: List<String>) {
        if (winner != null) return
        val emptyIndices = currentBoard.indices.filter { currentBoard[it].isEmpty() }
        if (emptyIndices.isEmpty()) return

        // 1. Check if AI can win
        for (i in emptyIndices) {
            val test = currentBoard.toMutableList()
            test[i] = "O"
            if (checkWinner(test) == "O") {
                board = test
                isXTurn = true
                return
            }
        }
        // 2. Check if AI needs to block human X
        for (i in emptyIndices) {
            val test = currentBoard.toMutableList()
            test[i] = "X"
            if (checkWinner(test) == "X") {
                val updated = currentBoard.toMutableList()
                updated[i] = "O"
                board = updated
                isXTurn = true
                return
            }
        }
        // 3. Take center if available
        if (currentBoard[4].isEmpty()) {
            val updated = currentBoard.toMutableList()
            updated[4] = "O"
            board = updated
            isXTurn = true
            return
        }
        // 4. Random available move
        val pick = emptyIndices.random()
        val updated = currentBoard.toMutableList()
        updated[pick] = "O"
        board = updated
        isXTurn = true
    }

    LaunchedEffect(winner) {
        if (winner == "X") xWins++
        if (winner == "O") oWins++
        if (winner == "DRAW") draws++
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = isVsAi,
                    onClick = {
                        isVsAi = true
                        board = List(9) { "" }
                        isXTurn = true
                    },
                    label = { Text("vs AI Bot") }
                )
                FilterChip(
                    selected = !isVsAi,
                    onClick = {
                        isVsAi = false
                        board = List(9) { "" }
                        isXTurn = true
                    },
                    label = { Text("2-Player Pass") }
                )
            }

            OutlinedButton(
                onClick = {
                    board = List(9) { "" }
                    isXTurn = true
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
            }
        }

        // Scoreboard
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("Player X: $xWins", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Draws: $draws", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (isVsAi) "Bot O: $oWins" else "Player O: $oWins", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }

        // Status text
        Text(
            text = when (winner) {
                "X" -> "🎉 Player X Wins!"
                "O" -> if (isVsAi) "🤖 AI Bot Wins!" else "🎉 Player O Wins!"
                "DRAW" -> "🤝 It's a Draw!"
                else -> if (isXTurn) "Turn: Player X" else if (isVsAi) "Bot O thinking..." else "Turn: Player O"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (winner != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )

        // 3x3 Board
        Column(
            modifier = Modifier.size(240.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val cellValue = board[index]

                        Button(
                            onClick = {
                                if (cellValue.isEmpty() && winner == null) {
                                    val updated = board.toMutableList()
                                    updated[index] = if (isXTurn) "X" else "O"
                                    board = updated

                                    if (isVsAi && isXTurn && checkWinner(updated) == null) {
                                        isXTurn = false
                                        makeAiMove(updated)
                                    } else {
                                        isXTurn = !isXTurn
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (cellValue.isEmpty()) MaterialTheme.colorScheme.surfaceVariant
                                else if (cellValue == "X") MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = cellValue,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (cellValue == "X") MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- MINI GAME 2: 2048 PUZZLE ----------------
@Composable
fun Puzzle2048ToolView() {
    var score by remember { mutableIntStateOf(0) }
    var bestScore by remember { mutableIntStateOf(2048) }
    var grid by remember {
        mutableStateOf(
            listOf(
                0, 0, 2, 0,
                0, 4, 0, 0,
                0, 2, 8, 0,
                0, 0, 0, 0
            )
        )
    }

    fun spawnRandomTile(board: MutableList<Int>) {
        val empty = board.indices.filter { board[it] == 0 }
        if (empty.isNotEmpty()) {
            board[empty.random()] = if (Math.random() < 0.9) 2 else 4
        }
    }

    fun slideRow(row: List<Int>): Pair<List<Int>, Int> {
        val nonZero = row.filter { it != 0 }.toMutableList()
        var gainedScore = 0
        val merged = mutableListOf<Int>()
        var i = 0
        while (i < nonZero.size) {
            if (i + 1 < nonZero.size && nonZero[i] == nonZero[i + 1]) {
                val sum = nonZero[i] * 2
                merged.add(sum)
                gainedScore += sum
                i += 2
            } else {
                merged.add(nonZero[i])
                i++
            }
        }
        while (merged.size < 4) merged.add(0)
        return Pair(merged, gainedScore)
    }

    fun moveLeft() {
        val newGrid = mutableListOf<Int>()
        var points = 0
        for (r in 0..3) {
            val row = (0..3).map { grid[r * 4 + it] }
            val (slid, pts) = slideRow(row)
            newGrid.addAll(slid)
            points += pts
        }
        if (newGrid != grid) {
            spawnRandomTile(newGrid)
            grid = newGrid
            score += points
            if (score > bestScore) bestScore = score
        }
    }

    fun moveRight() {
        val newGrid = MutableList(16) { 0 }
        var points = 0
        for (r in 0..3) {
            val row = (3 downTo 0).map { grid[r * 4 + it] }
            val (slid, pts) = slideRow(row)
            for (c in 0..3) newGrid[r * 4 + (3 - c)] = slid[c]
            points += pts
        }
        if (newGrid != grid) {
            spawnRandomTile(newGrid)
            grid = newGrid
            score += points
            if (score > bestScore) bestScore = score
        }
    }

    fun moveUp() {
        val newGrid = MutableList(16) { 0 }
        var points = 0
        for (c in 0..3) {
            val col = (0..3).map { grid[it * 4 + c] }
            val (slid, pts) = slideRow(col)
            for (r in 0..3) newGrid[r * 4 + c] = slid[r]
            points += pts
        }
        if (newGrid != grid) {
            spawnRandomTile(newGrid)
            grid = newGrid
            score += points
            if (score > bestScore) bestScore = score
        }
    }

    fun moveDown() {
        val newGrid = MutableList(16) { 0 }
        var points = 0
        for (c in 0..3) {
            val col = (3 downTo 0).map { grid[it * 4 + c] }
            val (slid, pts) = slideRow(col)
            for (r in 0..3) newGrid[(3 - r) * 4 + c] = slid[r]
            points += pts
        }
        if (newGrid != grid) {
            spawnRandomTile(newGrid)
            grid = newGrid
            score += points
            if (score > bestScore) bestScore = score
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("2048 Puzzle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Join numbers to hit 2048!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SCORE", style = MaterialTheme.typography.labelSmall)
                        Text("$score", fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("BEST", style = MaterialTheme.typography.labelSmall)
                        Text("$bestScore", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4x4 Grid
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(Color(0xFFBBADA0), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (r in 0..3) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (c in 0..3) {
                            val value = grid[r * 4 + c]
                            val tileColor = when (value) {
                                0 -> Color(0xFFCDC1B4)
                                2 -> Color(0xFFEEE4DA)
                                4 -> Color(0xFFEDE0C8)
                                8 -> Color(0xFFF2B179)
                                16 -> Color(0xFFF59563)
                                32 -> Color(0xFFF67C5F)
                                64 -> Color(0xFFF65E3B)
                                128 -> Color(0xFFEDCF72)
                                256 -> Color(0xFFEDCC61)
                                512 -> Color(0xFFEDC850)
                                1024 -> Color(0xFFEDC53F)
                                else -> Color(0xFFEDC22E)
                            }
                            val textColor = if (value in 2..4) Color(0xFF776E65) else Color.White

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(tileColor, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (value > 0) {
                                    Text(
                                        text = "$value",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (value > 512) 14.sp else 18.sp,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Direction Controls
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FilledTonalIconButton(onClick = { moveUp() }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilledTonalIconButton(onClick = { moveLeft() }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left")
                }
                FilledTonalIconButton(onClick = {
                    grid = listOf(0, 0, 2, 0, 0, 4, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0)
                    score = 0
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart")
                }
                FilledTonalIconButton(onClick = { moveRight() }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right")
                }
            }
            FilledTonalIconButton(onClick = { moveDown() }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down")
            }
        }
    }
}

// ---------------- MINI GAME 3: MEMORY CARD FLIP ----------------
data class MemoryCard(
    val id: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

@Composable
fun MemoryFlipToolView() {
    val iconPool = listOf(
        Icons.Default.Star,
        Icons.Default.Favorite,
        Icons.Default.ThumbUp,
        Icons.Default.Lightbulb,
        Icons.Default.Pets,
        Icons.Default.Face,
        Icons.Default.Home,
        Icons.Default.SportsEsports
    )

    fun createShuffledCards(): List<MemoryCard> {
        val pairs = (iconPool + iconPool).shuffled()
        return pairs.mapIndexed { idx, ic -> MemoryCard(idx, ic) }
    }

    var cards by remember { mutableStateOf(createShuffledCards()) }
    var moves by remember { mutableIntStateOf(0) }
    var firstSelectedIdx by remember { mutableStateOf<Int?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    val matchedCount = cards.count { it.isMatched } / 2

    fun onCardClick(index: Int) {
        if (isChecking || cards[index].isFlipped || cards[index].isMatched) return

        // Flip card
        cards = cards.mapIndexed { i, c -> if (i == index) c.copy(isFlipped = true) else c }

        if (firstSelectedIdx == null) {
            firstSelectedIdx = index
        } else {
            moves++
            val first = firstSelectedIdx!!
            isChecking = true

            if (cards[first].icon == cards[index].icon) {
                // Matched!
                cards = cards.mapIndexed { i, c ->
                    if (i == first || i == index) c.copy(isMatched = true) else c
                }
                firstSelectedIdx = null
                isChecking = false
            } else {
                // Not matching, flip back after delay
                firstSelectedIdx = null
            }
        }
    }

    LaunchedEffect(firstSelectedIdx, isChecking) {
        if (isChecking && firstSelectedIdx == null) {
            delay(700)
            cards = cards.map { if (!it.isMatched) it.copy(isFlipped = false) else it }
            isChecking = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Memory Card Flip", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (matchedCount == 8) "🏆 All Pairs Matched in $moves moves!" else "Matches: $matchedCount / 8 | Moves: $moves", style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = {
                    cards = createShuffledCards()
                    moves = 0
                    firstSelectedIdx = null
                    isChecking = false
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Restart")
            }
        }

        // 4x4 Cards Grid
        Box(
            modifier = Modifier
                .size(260.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                .padding(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (r in 0..3) {
                    Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (c in 0..3) {
                            val idx = r * 4 + c
                            val card = cards[idx]

                            Button(
                                onClick = { onCardClick(idx) },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (card.isMatched) MaterialTheme.colorScheme.secondaryContainer
                                    else if (card.isFlipped) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                if (card.isFlipped || card.isMatched) {
                                    Icon(
                                        imageVector = card.icon,
                                        contentDescription = null,
                                        tint = if (card.isMatched) MaterialTheme.colorScheme.onSecondaryContainer
                                        else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text("?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

