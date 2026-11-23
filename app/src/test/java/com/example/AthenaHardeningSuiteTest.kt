package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ai.ApkBuildResult
import com.example.ai.ApkEngine
import com.example.ai.ApkHtmlExporter
import com.example.ai.AppArchetype
import com.example.ai.AppIconData
import com.example.audio.WavAudioExporter
import com.example.data.WorkspaceDatabase
import com.example.data.WorkspaceItem
import com.example.system.update.AthenaUpdateManager
import com.example.util.ExportFormat
import com.example.util.FrequencyCsvExporter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AthenaHardeningSuiteTest {

    private lateinit var context: Context
    private lateinit var db: WorkspaceDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, WorkspaceDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `wav exporter writes valid RIFF WAVE header and preserves 16-bit PCM samples`() {
        val samples = shortArrayOf(Short.MIN_VALUE, -1024, 0, 16384, Short.MAX_VALUE)
        val outFile = File(context.cacheDir, "test_tone.wav")
        try {
            WavAudioExporter.writeWavFile(outFile, samples, sampleRate = 44100)
            val bytes = outFile.readBytes()
            assertEquals(44 + samples.size * 2, bytes.size)
            assertEquals("RIFF", String(bytes, 0, 4, Charsets.US_ASCII))
            assertEquals("WAVE", String(bytes, 8, 4, Charsets.US_ASCII))
            assertEquals("fmt ", String(bytes, 12, 4, Charsets.US_ASCII))
            assertEquals("data", String(bytes, 36, 4, Charsets.US_ASCII))

            val headerBuf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            assertEquals(36 + samples.size * 2, headerBuf.getInt(4))
            assertEquals(44100, headerBuf.getInt(24))
            assertEquals(samples.size * 2, headerBuf.getInt(40))

            for (i in samples.indices) {
                assertEquals(samples[i], headerBuf.getShort(44 + i * 2))
            }
        } finally {
            outFile.delete()
        }
    }

    @Test
    fun `wav recorder feedSamples and stopAndExport produce valid unboxed wav file`() {
        val chunk = shortArrayOf(1200, -1200, 2400, -2400)
        WavAudioExporter.startRecording()
        WavAudioExporter.feedSamples(chunk, chunk.size)
        val exported = WavAudioExporter.stopAndExport(context, "42.6Hz/Test")
        assertNotNull(exported)
        try {
            assertEquals(44L + chunk.size * 2L, exported!!.length())
            assertFalse(exported.name.contains("/"))
        } finally {
            exported?.delete()
        }
    }

    @Test
    fun `csv exporter sanitizes path traversal filePrefix inside exports cache dir`() {
        val file = FrequencyCsvExporter.createTempExportFile(
            context = context,
            content = "id,frequency_hz\r\n1,42.600\r\n",
            format = ExportFormat.CSV,
            filePrefix = "../../outside_secret"
        )
        try {
            val exportsDir = File(context.cacheDir, "exports").canonicalFile
            assertEquals(exportsDir, file.parentFile?.canonicalFile)
            assertFalse(file.name.contains(".."))
        } finally {
            file.delete()
        }
    }

    @Test
    fun `workspace dao cascades folder trash restore and permanent delete and escapes search wildcards`() = runBlocking {
        val dao = db.workspaceDao()
        val rootFolder = WorkspaceItem(id = "folder_root", name = "Root Folder", kind = "folder")
        val subFolder = WorkspaceItem(id = "folder_sub", name = "Sub Folder", kind = "folder", parentId = "folder_root")
        val nestedNote = WorkspaceItem(
            id = "note_nested",
            name = "100%_lock telemetry",
            kind = "note",
            parentId = "folder_sub",
            text = "Resonance 42.6"
        )
        val decoyNote = WorkspaceItem(
            id = "note_decoy",
            name = "1000Xlock decoy",
            kind = "note",
            parentId = null,
            text = "Unrelated"
        )

        dao.insertItem(rootFolder)
        dao.insertItem(subFolder)
        dao.insertItem(nestedNote)
        dao.insertItem(decoyNote)

        // Wildcard-escaped search: '%' and '_' in query should match literally, not decoyNote
        val searchHits = dao.searchItems("100%_lock").first()
        assertEquals(1, searchHits.size)
        assertEquals("note_nested", searchHits.first().id)

        // Recursive trash cascade
        dao.moveToTrash("folder_root", timestamp = 123456L)
        assertTrue(dao.getItemById("folder_root")!!.isDeleted)
        assertTrue(dao.getItemById("folder_sub")!!.isDeleted)
        assertTrue(dao.getItemById("note_nested")!!.isDeleted)
        assertFalse(dao.getItemById("note_decoy")!!.isDeleted)

        // Recursive restore cascade
        dao.restoreItem("folder_root")
        assertFalse(dao.getItemById("folder_root")!!.isDeleted)
        assertFalse(dao.getItemById("folder_sub")!!.isDeleted)
        assertFalse(dao.getItemById("note_nested")!!.isDeleted)

        // Recursive permanent delete cascade
        dao.deletePermanently("folder_root")
        assertNull(dao.getItemById("folder_root"))
        assertNull(dao.getItemById("folder_sub"))
        assertNull(dao.getItemById("note_nested"))
        assertNotNull(dao.getItemById("note_decoy"))
    }

    @Test
    fun `update manager synchronously saves and consumes recovery checkpoint once`() {
        AthenaUpdateManager.saveStateForUpdateRecovery(context, "BRAIN", "ATHENA v2.1 OTA")
        val firstRead = AthenaUpdateManager.checkAndConsumeUpdateRecovery(context)
        assertNotNull(firstRead)
        assertEquals("BRAIN", firstRead!!.lastScreen)
        assertEquals("ATHENA v2.1 OTA", firstRead.updateTitle)

        val secondRead = AthenaUpdateManager.checkAndConsumeUpdateRecovery(context)
        assertNull(secondRead)
    }

    @Test
    fun `apk html exporter escapes script injection and avoids unterminated js template literals`() {
        val buildResult = ApkBuildResult(
            appName = "<script>alert('xss')</script>",
            packageName = "com.athena.test",
            versionCode = 1,
            versionName = "1.0.0",
            engineUsed = ApkEngine.GEMINI_FLASH,
            archetype = AppArchetype.SCIENTIFIC_ACOUSTICS,
            promptDescription = "Safe \"quote\" & tag test",
            generatedFiles = emptyList(),
            buildLogs = emptyList(),
            packageSizeKb = 128L,
            packageFile = null,
            isSuccess = true
        )
        val icon = AppIconData(
            glyph = "<img src=x onerror=alert(1)>",
            bgStartHex = "#00E5FF; body{display:none}",
            bgEndHex = "#081A2D"
        )

        val exportedFile = ApkHtmlExporter.exportHtmlWebApp(context, buildResult, icon)
        assertNotNull(exportedFile)
        try {
            val html = exportedFile!!.readText(Charsets.UTF_8)
            assertFalse("Raw <script> injection in title must be escaped", html.contains("<title><script>"))
            assertTrue(html.contains("&lt;script&gt;alert"))
            assertFalse("CSS injection in bgStartHex must be sanitized to fallback", html.contains("body{display:none}"))
        } finally {
            exportedFile?.delete()
        }
    }

    @Test
    fun `workspace dao recursive CTE terminates on cyclic rows and viewmodel blocks descendant folder move`() = runBlocking {
        val dao = db.workspaceDao()
        val cyclicA = WorkspaceItem(id = "cycle_a", name = "Cycle A", kind = "folder", parentId = "cycle_b")
        val cyclicB = WorkspaceItem(id = "cycle_b", name = "Cycle B", kind = "folder", parentId = "cycle_a")
        dao.insertItem(cyclicA)
        dao.insertItem(cyclicB)

        val treeIds = kotlinx.coroutines.withTimeout(2_000L) {
            dao.getTreeIds("cycle_a")
        }
        assertEquals(setOf("cycle_a", "cycle_b"), treeIds.toSet())

        kotlinx.coroutines.withTimeout(2_000L) {
            dao.moveToTrash("cycle_a", timestamp = 999L)
            assertTrue(dao.getItemById("cycle_a")!!.isDeleted)
            assertTrue(dao.getItemById("cycle_b")!!.isDeleted)
            dao.restoreItem("cycle_a")
            assertFalse(dao.getItemById("cycle_a")!!.isDeleted)
            dao.deletePermanently("cycle_a")
            assertNull(dao.getItemById("cycle_a"))
            assertNull(dao.getItemById("cycle_b"))
        }

        // Verify WorkspaceViewModel.moveSelectedToFolder blocks moving a folder into its own descendant
        val app = context.applicationContext as android.app.Application
        val vm = com.example.ui.viewmodel.WorkspaceViewModel(app, null, null)
        val appDao = WorkspaceDatabase.getDatabase(context).workspaceDao()

        val parentFolder = WorkspaceItem(id = "vm_parent", name = "Parent", kind = "folder", parentId = null)
        val childFolder = WorkspaceItem(id = "vm_child", name = "Child", kind = "folder", parentId = "vm_parent")
        val grandChildFolder = WorkspaceItem(id = "vm_grandchild", name = "Grandchild", kind = "folder", parentId = "vm_child")
        val siblingNote = WorkspaceItem(id = "vm_sibling_note", name = "Sibling Note", kind = "note", parentId = null)

        fun drainViewModelMove() {
            val deadline = System.currentTimeMillis() + 3_000L
            while (vm.selectedItemIds.value.isNotEmpty() && System.currentTimeMillis() < deadline) {
                org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
                Thread.sleep(10)
            }
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        }

        try {
            appDao.insertItems(listOf(parentFolder, childFolder, grandChildFolder, siblingNote))
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

            // Attempt invalid move: parent -> grandchild (descendant cycle)
            vm.toggleSelectItem("vm_parent")
            vm.moveSelectedToFolder("vm_grandchild")
            drainViewModelMove()
            assertTrue("moveSelectedToFolder should have finished and cleared selection", vm.selectedItemIds.value.isEmpty())
            assertNull(
                "Moving a folder into its own descendant must be rejected",
                appDao.getItemById("vm_parent")!!.parentId
            )

            // Valid move: siblingNote -> grandChildFolder
            vm.toggleSelectItem("vm_sibling_note")
            vm.moveSelectedToFolder("vm_grandchild")
            drainViewModelMove()
            assertTrue("moveSelectedToFolder should have finished and cleared selection", vm.selectedItemIds.value.isEmpty())
            assertEquals("vm_grandchild", appDao.getItemById("vm_sibling_note")!!.parentId)
        } finally {
            appDao.deletePermanently(listOf("vm_parent", "vm_child", "vm_grandchild", "vm_sibling_note"))
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `backup rules xml excludes sensitive prefs without sharedpref allowlist lock-in`() {
        val parser = context.resources.getXml(R.xml.backup_rules)
        val includes = mutableListOf<Pair<String?, String?>>()
        val excludes = mutableListOf<Pair<String?, String?>>()

        try {
            var eventType = parser.eventType
            while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG) {
                    val domain = parser.getAttributeValue(null, "domain")
                    val path = parser.getAttributeValue(null, "path")
                    when (parser.name) {
                        "include" -> includes.add(domain to path)
                        "exclude" -> excludes.add(domain to path)
                    }
                }
                eventType = parser.next()
            }
        } finally {
            parser.close()
        }

        assertTrue(
            "backup_rules.xml must not contain <include> tags that exclude database/file domains on API 24-30",
            includes.isEmpty()
        )
        assertTrue(excludes.contains("sharedpref" to "creative_hub_ai_prefs.xml"))
        assertTrue(excludes.contains("sharedpref" to "connected_accounts_prefs.xml"))
    }

    @Test
    fun `main source tree contains zero query string api key leaks`() {
        val sourceDir = listOf(
            File("app/src/main/java"),
            File("src/main/java")
        ).firstOrNull { it.isDirectory }
        assertNotNull("Expected main Java/Kotlin source directory to exist", sourceDir)

        val queryKeyToken = "?" + "key="
        val sseKeyToken = "alt=sse&" + "key="
        val leakyLocations = mutableListOf<String>()
        sourceDir!!.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
            .forEach { file ->
                file.readLines(Charsets.UTF_8).forEachIndexed { idx, line ->
                    if (line.contains(queryKeyToken) || line.contains(sseKeyToken)) {
                        leakyLocations.add("${file.path}:${idx + 1}: ${line.trim()}")
                    }
                }
            }

        assertTrue(
            "Found query-string API key leaks:\n${leakyLocations.joinToString("\n")}",
            leakyLocations.isEmpty()
        )
    }
}
