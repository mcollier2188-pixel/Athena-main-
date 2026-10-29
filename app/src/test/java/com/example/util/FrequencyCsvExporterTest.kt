package com.example.util

import com.example.data.FrequencyHistoryEntry
import com.example.data.ResonanceIntervalCalculation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FrequencyCsvExporterTest {

    @Test
    fun testGenerateCsv_containsHeadersAndFormattedData() {
        val entries = listOf(
            FrequencyHistoryEntry(
                id = 1,
                frequencyHz = 42.600,
                timestamp = 1700000000000L,
                durationSeconds = 120,
                isResonanceLock = true,
                sessionNote = "Peak resonance test"
            ),
            FrequencyHistoryEntry(
                id = 2,
                frequencyHz = 42.000,
                timestamp = 1700000060000L,
                durationSeconds = 45,
                isResonanceLock = false,
                sessionNote = "Note with, comma and \"quotes\""
            )
        )

        val csv = FrequencyCsvExporter.generateCsv(entries)

        assertTrue(csv.startsWith("id,frequency_hz,timestamp_epoch_ms,datetime_utc,duration_seconds,is_resonance_lock,session_note"))
        assertTrue(csv.contains("42.600"))
        assertTrue(csv.contains("true"))
        assertTrue(csv.contains("\"Note with, comma and \"\"quotes\"\"\""))
    }

    @Test
    fun testGeneratePlainText_containsFormattedReportAndSummary() {
        val entries = listOf(
            FrequencyHistoryEntry(
                id = 1,
                frequencyHz = 42.600,
                timestamp = 1700000000000L,
                durationSeconds = 180,
                isResonanceLock = true,
                sessionNote = "Bedrock 42.6 Hz standing lock"
            ),
            FrequencyHistoryEntry(
                id = 2,
                frequencyHz = 85.200,
                timestamp = 1700000060000L,
                durationSeconds = 90,
                isResonanceLock = true,
                sessionNote = "Octave 2x harmonic verification"
            )
        )

        val report = FrequencyCsvExporter.generatePlainText(entries)

        assertTrue(report.contains("ATHENA RESONANCE INTERFACE: HISTORICAL FREQUENCY LOG REPORT"))
        assertTrue(report.contains("42.600 Hz"))
        assertTrue(report.contains("[RESONANCE LOCK - 42.6 Hz]"))
        assertTrue(report.contains("85.200 Hz"))
        assertTrue(report.contains("SUMMARY STATISTICAL FINDINGS"))
        assertTrue(report.contains("Total Sessions:   2 recorded sessions"))
        assertTrue(report.contains("Resonance Locks:  2 (100.0% lock rate)"))
    }

    @Test
    fun testGenerateIntervalsCsvAndText() {
        val intervals = listOf(
            ResonanceIntervalCalculation(
                id = 1,
                name = "Bedrock Fundamental",
                intervalLabel = "Fundamental (1x)",
                multiplier = 1.0,
                baseFrequencyHz = 42.600,
                calculatedFrequencyHz = 42.600,
                wavelengthMeters = 80.28,
                periodSeconds = 0.02347,
                acousticVelocityMps = 3420.0,
                couplingEfficiencyPercent = 98.4,
                note = "Solent chalk baseline"
            )
        )

        val csv = FrequencyCsvExporter.generateIntervalsCsv(intervals)
        assertTrue(csv.startsWith("id,name,interval_label,multiplier,base_frequency_hz,calculated_frequency_hz"))
        assertTrue(csv.contains("Bedrock Fundamental"))
        assertTrue(csv.contains("42.600"))

        val text = FrequencyCsvExporter.generateIntervalsPlainText(intervals)
        assertTrue(text.contains("ATHENA 42.6 HZ RESONANCE NODE: CALCULATED INTERVALS & METRICS"))
        assertTrue(text.contains("Bedrock Fundamental"))
        assertTrue(text.contains("80.28 meters"))
    }

    @Test
    fun testExportHistoricalResonanceData_formatSwitcher() {
        val entries = listOf(
            FrequencyHistoryEntry(
                id = 1,
                frequencyHz = 42.600,
                isResonanceLock = true,
                durationSeconds = 60
            )
        )

        val csvExport = FrequencyCsvExporter.exportHistoricalResonanceData(entries, ExportFormat.CSV)
        assertTrue(csvExport.startsWith("id,frequency_hz"))

        val textExport = FrequencyCsvExporter.exportHistoricalResonanceData(entries, ExportFormat.PLAIN_TEXT)
        assertTrue(textExport.contains("ATHENA RESONANCE INTERFACE"))
    }
}
