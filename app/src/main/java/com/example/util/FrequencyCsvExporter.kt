package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.FrequencyHistoryEntry
import com.example.data.ResonanceIntervalCalculation
import com.example.data.WorkspaceDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Supported export formats for resonance findings and history.
 */
enum class ExportFormat(val extension: String, val mimeType: String, val displayName: String) {
    CSV("csv", "text/csv", "CSV Spreadsheet (.csv)"),
    PLAIN_TEXT("txt", "text/plain", "Simple Text Report (.txt)")
}

/**
 * Utility for exporting historical resonance data from the Room database
 * into RFC 4180 compliant CSV format or clean, human-readable simple text format (.txt).
 */
object FrequencyCsvExporter {

    private const val CSV_HEADER = "id,frequency_hz,timestamp_epoch_ms,datetime_utc,duration_seconds,is_resonance_lock,session_note"
    private const val INTERVALS_CSV_HEADER = "id,name,interval_label,multiplier,base_frequency_hz,calculated_frequency_hz,wavelength_meters,period_seconds,acoustic_velocity_mps,coupling_efficiency_percent,timestamp_epoch_ms,datetime_utc,note"

    // =========================================================================
    // 1. Frequency History Export (CSV & Simple Text)
    // =========================================================================

    /**
     * Converts a list of [FrequencyHistoryEntry] objects into an RFC 4180 compliant CSV string.
     */
    fun generateCsv(entries: List<FrequencyHistoryEntry>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val sb = StringBuilder()
        sb.append(CSV_HEADER).append("\r\n")

        entries.forEach { entry ->
            val dateStr = dateFormat.format(Date(entry.timestamp))
            val escapedNote = escapeCsvField(entry.sessionNote ?: "")
            sb.append(entry.id).append(",")
                .append(String.format(Locale.US, "%.3f", entry.frequencyHz)).append(",")
                .append(entry.timestamp).append(",")
                .append(dateStr).append(",")
                .append(entry.durationSeconds).append(",")
                .append(entry.isResonanceLock).append(",")
                .append(escapedNote)
                .append("\r\n")
        }

        return sb.toString()
    }

    /**
     * Converts a list of [FrequencyHistoryEntry] objects into a clean, human-readable plain text report.
     * Perfect for research field notebooks, quick review, printing, or sharing in messaging apps.
     */
    fun generatePlainText(entries: List<FrequencyHistoryEntry>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val exportTimestamp = dateFormat.format(Date())

        val lockCount = entries.count { it.isResonanceLock }
        val lockPercent = if (entries.isNotEmpty()) (lockCount * 100.0 / entries.size) else 0.0
        val totalDurationSeconds = entries.sumOf { it.durationSeconds }
        val avgFreq = if (entries.isNotEmpty()) entries.map { it.frequencyHz }.average() else 0.0
        val minFreq = entries.minOfOrNull { it.frequencyHz } ?: 0.0
        val maxFreq = entries.maxOfOrNull { it.frequencyHz } ?: 0.0

        val sb = StringBuilder()
        sb.append("=======================================================================\n")
        sb.append("ATHENA RESONANCE INTERFACE: HISTORICAL FREQUENCY LOG REPORT\n")
        sb.append("Target Reference: 42.600 Hz Bedrock Planetary Waveguide\n")
        sb.append("Export Generated: $exportTimestamp\n")
        sb.append("Total Sessions:   ${entries.size} recorded sessions\n")
        sb.append("Resonance Locks:  $lockCount (${String.format(Locale.US, "%.1f", lockPercent)}% lock rate)\n")
        sb.append("=======================================================================\n\n")

        if (entries.isEmpty()) {
            sb.append("No historical resonance sessions recorded.\n")
        } else {
            entries.forEachIndexed { index, entry ->
                val dateStr = dateFormat.format(Date(entry.timestamp))
                val lockStatus = if (entry.isResonanceLock) "[RESONANCE LOCK - 42.6 Hz]" else "[FREE SWEEP]"
                val durationFormatted = formatDuration(entry.durationSeconds)

                sb.append("-----------------------------------------------------------------------\n")
                sb.append("ENTRY #${index + 1} (Database ID: ${entry.id})\n")
                sb.append("Frequency:      ${String.format(Locale.US, "%.3f Hz", entry.frequencyHz)} $lockStatus\n")
                sb.append("Recorded At:    $dateStr\n")
                sb.append("Duration:       $durationFormatted (${entry.durationSeconds}s)\n")
                if (!entry.sessionNote.isNullOrBlank()) {
                    sb.append("Session Note:   ${entry.sessionNote.trim()}\n")
                }
            }
            sb.append("-----------------------------------------------------------------------\n\n")

            sb.append("=======================================================================\n")
            sb.append("SUMMARY STATISTICAL FINDINGS:\n")
            sb.append("• Total Recorded Sessions:  ${entries.size}\n")
            sb.append("• 42.6 Hz Resonance Locks:  $lockCount\n")
            sb.append("• Total Monitored Time:     ${formatDuration(totalDurationSeconds)} ($totalDurationSeconds seconds)\n")
            sb.append("• Mean Logged Frequency:    ${String.format(Locale.US, "%.3f Hz", avgFreq)}\n")
            sb.append("• Frequency Dynamic Range:  ${String.format(Locale.US, "%.3f Hz – %.3f Hz", minFreq, maxFreq)}\n")
            sb.append("• Solent Tidal Envelope:    Active 11.74s Standing Cycle\n")
            sb.append("=======================================================================\n")
            sb.append("Generated by ATHENA Echo Synthesizer & Resonance Analyzer\n")
        }

        return sb.toString()
    }

    // =========================================================================
    // 2. Resonance Interval Calculations Export (CSV & Simple Text)
    // =========================================================================

    /**
     * Converts a list of [ResonanceIntervalCalculation] objects into RFC 4180 CSV format.
     */
    fun generateIntervalsCsv(intervals: List<ResonanceIntervalCalculation>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val sb = StringBuilder()
        sb.append(INTERVALS_CSV_HEADER).append("\r\n")

        intervals.forEach { item ->
            val dateStr = dateFormat.format(Date(item.timestampEpochMs))
            val escapedNote = escapeCsvField(item.note)
            val escapedName = escapeCsvField(item.name)
            val escapedLabel = escapeCsvField(item.intervalLabel)

            sb.append(item.id).append(",")
                .append(escapedName).append(",")
                .append(escapedLabel).append(",")
                .append(String.format(Locale.US, "%.4f", item.multiplier)).append(",")
                .append(String.format(Locale.US, "%.3f", item.baseFrequencyHz)).append(",")
                .append(String.format(Locale.US, "%.3f", item.calculatedFrequencyHz)).append(",")
                .append(String.format(Locale.US, "%.2f", item.wavelengthMeters)).append(",")
                .append(String.format(Locale.US, "%.5f", item.periodSeconds)).append(",")
                .append(String.format(Locale.US, "%.1f", item.acousticVelocityMps)).append(",")
                .append(String.format(Locale.US, "%.2f", item.couplingEfficiencyPercent)).append(",")
                .append(item.timestampEpochMs).append(",")
                .append(dateStr).append(",")
                .append(escapedNote)
                .append("\r\n")
        }

        return sb.toString()
    }

    /**
     * Converts a list of [ResonanceIntervalCalculation] objects into a formatted text report.
     */
    fun generateIntervalsPlainText(intervals: List<ResonanceIntervalCalculation>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val exportTimestamp = dateFormat.format(Date())

        val sb = StringBuilder()
        sb.append("=======================================================================\n")
        sb.append("ATHENA 42.6 HZ RESONANCE NODE: CALCULATED INTERVALS & METRICS\n")
        sb.append("Base Planetary Fundamental: 42.600 Hz\n")
        sb.append("Export Timestamp:          $exportTimestamp\n")
        sb.append("Total Calculations:        ${intervals.size}\n")
        sb.append("=======================================================================\n\n")

        if (intervals.isEmpty()) {
            sb.append("No resonance intervals recorded.\n")
        } else {
            intervals.forEachIndexed { index, item ->
                val dateStr = dateFormat.format(Date(item.timestampEpochMs))
                sb.append("-----------------------------------------------------------------------\n")
                sb.append("INTERVAL #${index + 1}: ${item.name} [${item.intervalLabel}]\n")
                sb.append("Harmonic Multiplier:       ${String.format(Locale.US, "%.4fx", item.multiplier)}\n")
                sb.append("Base Frequency (f0):       ${String.format(Locale.US, "%.3f Hz", item.baseFrequencyHz)}\n")
                sb.append("Calculated Frequency (f):  ${String.format(Locale.US, "%.3f Hz", item.calculatedFrequencyHz)}\n")
                sb.append("Acoustic Wavelength (λ):   ${String.format(Locale.US, "%.2f meters", item.wavelengthMeters)}\n")
                sb.append("Wave Period (T):           ${String.format(Locale.US, "%.5f seconds", item.periodSeconds)}\n")
                sb.append("Strata Velocity (v):       ${String.format(Locale.US, "%.1f m/s", item.acousticVelocityMps)}\n")
                sb.append("Coupling Efficiency:       ${String.format(Locale.US, "%.1f%%", item.couplingEfficiencyPercent)}\n")
                sb.append("Computed Timestamp:        $dateStr\n")
                if (item.note.isNotBlank()) {
                    sb.append("Technical Notes:           ${item.note.trim()}\n")
                }
            }
            sb.append("-----------------------------------------------------------------------\n")
        }
        return sb.toString()
    }

    // =========================================================================
    // 3. Direct Room Database Export Functions
    // =========================================================================

    /**
     * Exports historical resonance data from the Room database into either CSV or Simple Text format.
     * This directly queries the [WorkspaceDatabase] Room instance.
     */
    suspend fun exportResonanceHistoryFromDatabase(
        database: WorkspaceDatabase,
        format: ExportFormat = ExportFormat.CSV
    ): String {
        val entries = database.frequencyHistoryDao().getAllHistoryList()
        return when (format) {
            ExportFormat.CSV -> generateCsv(entries)
            ExportFormat.PLAIN_TEXT -> generatePlainText(entries)
        }
    }

    /**
     * Convenience method to export historical resonance data from Room database using application [Context].
     */
    suspend fun exportResonanceHistoryFromDatabase(
        context: Context,
        format: ExportFormat = ExportFormat.CSV
    ): String {
        val db = WorkspaceDatabase.getDatabase(context)
        return exportResonanceHistoryFromDatabase(db, format)
    }

    /**
     * Exports calculated resonance intervals from the Room database into either CSV or Simple Text format.
     */
    suspend fun exportIntervalsFromDatabase(
        database: WorkspaceDatabase,
        format: ExportFormat = ExportFormat.CSV
    ): String {
        val intervals = database.resonanceIntervalDao().getAllIntervalsList()
        return when (format) {
            ExportFormat.CSV -> generateIntervalsCsv(intervals)
            ExportFormat.PLAIN_TEXT -> generateIntervalsPlainText(intervals)
        }
    }

    /**
     * Unified export function accepting entries and chosen [ExportFormat].
     */
    fun exportHistoricalResonanceData(
        entries: List<FrequencyHistoryEntry>,
        format: ExportFormat = ExportFormat.CSV
    ): String {
        return when (format) {
            ExportFormat.CSV -> generateCsv(entries)
            ExportFormat.PLAIN_TEXT -> generatePlainText(entries)
        }
    }

    // =========================================================================
    // 4. File I/O, Sharing & Clipboard Operations
    // =========================================================================

    /**
     * Writes CSV string to the given [OutputStream] (e.g. from Storage Access Framework Uri).
     */
    fun writeCsvToOutputStream(outputStream: OutputStream, entries: List<FrequencyHistoryEntry>) {
        writeToOutputStream(outputStream, generateCsv(entries))
    }

    /**
     * Writes any content string to the given [OutputStream].
     */
    fun writeToOutputStream(outputStream: OutputStream, content: String) {
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(content)
            writer.flush()
        }
    }

    /**
     * Creates a temporary .csv file in cacheDir/exports for sharing via FileProvider.
     */
    fun createTempCsvFile(context: Context, entries: List<FrequencyHistoryEntry>): File {
        return createTempExportFile(
            context = context,
            content = generateCsv(entries),
            format = ExportFormat.CSV,
            filePrefix = "resonance_frequency_history"
        )
    }

    /**
     * Creates a temporary file in cacheDir/exports for sharing via FileProvider.
     */
    fun createTempExportFile(
        context: Context,
        content: String,
        format: ExportFormat,
        filePrefix: String = "resonance_frequency_history"
    ): File {
        val exportDir = File(context.cacheDir, "exports").apply {
            if (!exists()) mkdirs()
        }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${filePrefix}_$timestamp.${format.extension}"
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { outputStream ->
            writeToOutputStream(outputStream, content)
        }
        return file
    }

    /**
     * Creates an Intent to share the exported CSV file with external applications.
     */
    fun createShareIntent(context: Context, csvFile: File): Intent {
        val format = if (csvFile.name.endsWith(".txt", ignoreCase = true)) ExportFormat.PLAIN_TEXT else ExportFormat.CSV
        return createShareIntent(context, csvFile, format)
    }

    /**
     * Creates an Intent to share any exported file with external applications.
     */
    fun createShareIntent(
        context: Context,
        file: File,
        format: ExportFormat,
        subject: String? = null
    ): Intent {
        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val defaultSubject = if (format == ExportFormat.CSV) {
            "Resonance Frequency History Export (${file.name})"
        } else {
            "Resonance Research Findings Report (${file.name})"
        }

        return Intent(Intent.ACTION_SEND).apply {
            type = format.mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, subject ?: defaultSubject)
            putExtra(Intent.EXTRA_TEXT, "Attached is the exported resonance data from the ATHENA Echo Interface.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Copies CSV contents directly to clipboard.
     */
    fun copyCsvToClipboard(context: Context, entries: List<FrequencyHistoryEntry>): String {
        return copyToClipboard(context, generateCsv(entries), "Frequency History CSV")
    }

    /**
     * Copies Plain Text contents directly to clipboard.
     */
    fun copyPlainTextToClipboard(context: Context, entries: List<FrequencyHistoryEntry>): String {
        return copyToClipboard(context, generatePlainText(entries), "Resonance Findings Report")
    }

    /**
     * Copies any string contents to system clipboard.
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Resonance Findings"): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        return text
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private fun escapeCsvField(field: String): String {
        if (field.isEmpty()) return ""
        val containsSpecialChars = field.contains(',') || field.contains('"') || field.contains('\n') || field.contains('\r')
        return if (containsSpecialChars) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }
    }

    private fun formatDuration(seconds: Long): String {
        if (seconds <= 0) return "0s"
        val mins = seconds / 60
        val secs = seconds % 60
        return if (mins > 0) {
            "${mins}m ${secs}s"
        } else {
            "${secs}s"
        }
    }
}
