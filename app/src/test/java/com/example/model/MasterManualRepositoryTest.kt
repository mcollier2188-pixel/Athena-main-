package com.example.model

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.core.app.ApplicationProvider
import com.example.audio.HarmonicToneGenerator
import com.example.ui.components.FaqCategory
import com.example.ui.components.FaqDataRepository
import com.example.ui.components.WorkspaceFaqTab
import com.example.ui.screens.ArchiveScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MasterManualRepositoryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun resolveRepoRoot(): File {
        val cwd = File(System.getProperty("user.dir") ?: ".")
        return if (File(cwd, "docs/MASTER_OPERATIONAL_MANUAL.md").isFile) {
            cwd
        } else {
            cwd.parentFile ?: cwd
        }
    }

    @Test
    fun `repo docs manual and bundled android asset manual exist and match verbatim`() {
        val repoRoot = resolveRepoRoot()
        val docsFile = File(repoRoot, "docs/MASTER_OPERATIONAL_MANUAL.md")
        val assetFile = File(repoRoot, "app/src/main/assets/docs/MASTER_OPERATIONAL_MANUAL.md")
        val readmeFile = File(repoRoot, "README.md")

        assertTrue("docs/MASTER_OPERATIONAL_MANUAL.md must exist", docsFile.isFile)
        assertTrue("app/src/main/assets/docs/MASTER_OPERATIONAL_MANUAL.md must exist", assetFile.isFile)
        assertTrue("README.md must exist", readmeFile.isFile)

        val docsText = docsFile.readText(Charsets.UTF_8)
        val assetText = assetFile.readText(Charsets.UTF_8)
        val readmeText = readmeFile.readText(Charsets.UTF_8)

        assertEquals("Repo manual and bundled Android asset must stay byte-identical", docsText, assetText)
        assertTrue("README.md must link to docs/MASTER_OPERATIONAL_MANUAL.md", readmeText.contains("docs/MASTER_OPERATIONAL_MANUAL.md"))
    }

    @Test
    fun `master manual repository parses all 10 chapters and surfaces in FieldDoc and FAQ repositories`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val markdown = MasterManualRepository.getFullManualMarkdown(context)
        val chapters = MasterManualRepository.getChapters(context)

        assertTrue(markdown.contains("2.0-PROD"))
        assertTrue(markdown.contains("2026 Continuum"))
        assertTrue(markdown.contains("Whizz Kid"))
        assertTrue(markdown.contains("Whiskey (Antigravity)"))
        assertTrue(markdown.contains("Rabbit (Android Studio)"))
        assertTrue(markdown.contains("ZY32L2WS6W"))
        assertTrue(markdown.contains("192.168.1.209"))
        assertTrue(markdown.contains("£160.00"))
        assertTrue(markdown.contains("£200.00"))
        assertTrue(markdown.contains("GameSir G7 SE"))
        assertTrue(markdown.contains("Pioneer DDJ-400"))
        assertTrue(markdown.contains("THREE.VideoTexture"))
        assertTrue(markdown.contains("athena_game_engine.js"))
        assertTrue(markdown.contains("OPEN_ATHENA_MASTER_VAULT.bat"))

        assertEquals("All 10 chapters must be parsed", 10, chapters.size)
        assertEquals(1, chapters.first().number)
        assertEquals(10, chapters.last().number)
        assertTrue(chapters.first().title.contains("SYSTEM IDENTITY"))
        assertTrue(chapters.last().title.contains("OPERATOR RUNBOOKS"))

        val fieldDocs = FieldDocRepository.getDocuments(context)
        assertEquals("master_manual_2_0_prod", fieldDocs.first().id)
        assertTrue(fieldDocs.first().content.contains("## CHAPTER 10: OPERATOR RUNBOOKS & TROUBLESHOOTING"))

        val faqItems = FaqDataRepository.getItems(context)
        val manualFaqItems = faqItems.filter { it.category == FaqCategory.MASTER_MANUAL }
        assertEquals(10, manualFaqItems.size)
        assertTrue(FaqDataRepository.getFullCapabilitiesDocumentation(context).contains("MASTER OPERATIONAL & TECHNICAL MANUAL (2.0-PROD)"))
    }

    @Test
    fun `archive screen surfaces 2_0_PROD master manual card and opens reader dialog`() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = true) {
                ArchiveScreen(
                    onNavigateToFrequency = {},
                    onNavigateToNetwork = {},
                    onNavigateToWorkspace = {}
                )
            }
        }
        composeTestRule.mainClock.advanceTimeBy(100)

        composeTestRule.onNodeWithTag("archive_lazy_column")
            .performScrollToIndex(2)
        composeTestRule.mainClock.advanceTimeBy(100)

        composeTestRule.onNodeWithTag("open_master_manual_full_button").performClick()
        composeTestRule.mainClock.advanceTimeBy(150)

        composeTestRule.onNodeWithTag("reader_dialog").assertIsDisplayed()
    }

    @Test
    fun `workspace faq tab filters 2_0_PROD manual category and opens full manual reader`() {
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = false) {
                WorkspaceFaqTab(
                    toneGenerator = HarmonicToneGenerator(),
                    onSaveToWorkspaceNote = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithTag("faq_chip_master_manual").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("faq_card_master_manual_ch_01").assertIsDisplayed()

        composeTestRule.onNodeWithTag("faq_open_master_manual_btn").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("reader_dialog").assertIsDisplayed()
    }
}
