package com.example.ai

import com.example.data.MediaVaultRepository
import com.example.data.VaultMediaItem
import com.example.ui.components.MediaType
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GeminiMediaGenerationServiceTest {

    @Test
    fun testAspectRatiosAndStylesConfigured() {
        assertTrue(GeminiMediaGenerationService.ASPECT_RATIOS.isNotEmpty())
        assertTrue(GeminiMediaGenerationService.IMAGE_STYLES.isNotEmpty())
        assertTrue(GeminiMediaGenerationService.VIDEO_GENRES.isNotEmpty())
        assertTrue(GeminiMediaGenerationService.CAMERA_MOVEMENTS.isNotEmpty())

        val squareRatio = GeminiMediaGenerationService.ASPECT_RATIOS.find { it.first == "1:1" }
        assertNotNull(squareRatio)

        val landscapeRatio = GeminiMediaGenerationService.ASPECT_RATIOS.find { it.first == "16:9" }
        assertNotNull(landscapeRatio)
    }

    @Test
    fun testMediaVaultRepositoryAddAndRemove() {
        val testItem = VaultMediaItem(
            id = "test_item_1",
            title = "Test Generated Art",
            subtitle = "Gemini 2.5 Flash • 1:1",
            uri = Uri.parse("file:///cache/test.png"),
            type = MediaType.PHOTO,
            isAiGenerated = true
        )

        MediaVaultRepository.addItem(testItem)
        val items = MediaVaultRepository.items.value
        assertTrue(items.any { it.id == "test_item_1" })

        MediaVaultRepository.removeItem("test_item_1")
        val itemsAfterRemove = MediaVaultRepository.items.value
        assertTrue(itemsAfterRemove.none { it.id == "test_item_1" })
    }

    @Test
    fun testImagenModelConfiguration() {
        assertNotNull(GeminiMediaGenerationService.IMAGE_STYLES)
        assertTrue(GeminiMediaGenerationService.IMAGE_STYLES.contains("Cinematic 35mm Film"))
    }
}
