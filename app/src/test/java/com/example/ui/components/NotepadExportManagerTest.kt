package com.example.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotepadExportManagerTest {

    @Test
    fun testCalculateStats_emptyText() {
        val stats = NotepadExportManager.calculateStats("")
        assertEquals(0, stats.wordCount)
        assertEquals(0, stats.charCount)
        assertEquals(0, stats.paragraphCount)
    }

    @Test
    fun testCalculateStats_populatedText() {
        val text = "Athena Workspace delivers unconstrained text authoring.\n\nSecond paragraph with multiple words."
        val stats = NotepadExportManager.calculateStats(text)
        assertEquals(11, stats.wordCount)
        assertEquals(text.length, stats.charCount)
        assertEquals(2, stats.paragraphCount)
        assertTrue(stats.readingTimeMinutes >= 1)
    }

    @Test
    fun testCalculateStats_largeWordCount() {
        val words = (1..1000).joinToString(" ") { "word$it" }
        val stats = NotepadExportManager.calculateStats(words)
        assertEquals(1000, stats.wordCount)
        assertEquals(5, stats.readingTimeMinutes)
    }
}
