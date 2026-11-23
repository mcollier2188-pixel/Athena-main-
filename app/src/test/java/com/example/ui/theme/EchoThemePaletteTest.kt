package com.example.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.example.ui.components.SettingsDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EchoThemePaletteTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun srgbChannelLuminance(channel: Float): Double {
        val c = channel.toDouble().coerceIn(0.0, 1.0)
        return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }

    private fun relativeLuminance(color: Color): Double {
        val r = srgbChannelLuminance(color.red)
        val g = srgbChannelLuminance(color.green)
        val b = srgbChannelLuminance(color.blue)
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun contrastRatio(foreground: Color, background: Color): Double {
        val l1 = relativeLuminance(foreground)
        val l2 = relativeLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    @Test
    fun darkEchoColors_preservesExactLegacyHexValuesBitForBit() {
        assertTrue(DarkEchoColors.isDark)
        assertEquals(Color(0xFF0A0A0F), DarkEchoColors.bg)
        assertEquals(Color(0xFF12121A), DarkEchoColors.bgAlt)
        assertEquals(Color(0xFF122430), DarkEchoColors.card)
        assertEquals(Color(0xFF23404F), DarkEchoColors.cardStroke)
        assertEquals(Color(0xFF6CC4E0), DarkEchoColors.cyan)
        assertEquals(Color(0x266CC4E0), DarkEchoColors.cyanSoft)
        assertEquals(Color(0xFF6EB5FF), DarkEchoColors.blue)
        assertEquals(Color(0xFF062431), DarkEchoColors.onCyan)
        assertEquals(Color(0xFFE4EEF2), DarkEchoColors.text)
        assertEquals(Color(0xFF8AA4AE), DarkEchoColors.muted)
        assertEquals(Color(0xFF4ADE80), DarkEchoColors.green)
        assertEquals(Color(0xFFF59E0B), DarkEchoColors.amber)
        assertEquals(Color(0xFFEF4444), DarkEchoColors.red)
        assertEquals(Color(0xFFA855F7), DarkEchoColors.purple)
        assertEquals(Color(0xFF141A22), DarkEchoColors.darkSurface)
        assertEquals(Color(0xCC0A0A0F), DarkEchoColors.overlay)
    }

    @Test
    fun lightEchoColors_meetsWcag21ContrastInvariantsAndDistinctSurfaceHierarchy() {
        assertFalse(LightEchoColors.isDark)
        assertNotEquals(DarkEchoColors, LightEchoColors)

        val textContrast = contrastRatio(LightEchoColors.text, LightEchoColors.card)
        val mutedContrast = contrastRatio(LightEchoColors.muted, LightEchoColors.card)
        val cyanOnCardContrast = contrastRatio(LightEchoColors.cyan, LightEchoColors.card)
        val onCyanButtonContrast = contrastRatio(LightEchoColors.onCyan, LightEchoColors.cyan)

        assertTrue("Expected text/card contrast >= 12.0, got $textContrast", textContrast >= 12.0)
        assertTrue("Expected muted/card contrast >= 4.5, got $mutedContrast", mutedContrast >= 4.5)
        assertTrue("Expected cyan/card contrast >= 4.5, got $cyanOnCardContrast", cyanOnCardContrast >= 4.5)
        assertTrue("Expected onCyan/cyan contrast >= 4.5, got $onCyanButtonContrast", onCyanButtonContrast >= 4.5)

        // Four distinct surface elevation tiers
        assertNotEquals(LightEchoColors.bg, LightEchoColors.bgAlt)
        assertNotEquals(LightEchoColors.bg, LightEchoColors.card)
        assertNotEquals(LightEchoColors.card, LightEchoColors.darkSurface)
        assertNotEquals(LightEchoColors.bgAlt, LightEchoColors.darkSurface)
    }

    @Test
    fun myApplicationTheme_liveToggleUpdatesLocalEchoColorsTopLevelGettersAndMaterialScheme() {
        var isDark by mutableStateOf(true)
        lateinit var capturedLocal: EchoColorPalette
        lateinit var capturedEchoTheme: EchoColorPalette
        var capturedBg: Color = Color.Unspecified
        var capturedCard: Color = Color.Unspecified
        var capturedText: Color = Color.Unspecified
        var capturedCyan: Color = Color.Unspecified
        var capturedMaterialBg: Color = Color.Unspecified
        var capturedMaterialSurface: Color = Color.Unspecified

        composeTestRule.setContent {
            AthenaTheme(darkTheme = isDark) {
                capturedLocal = LocalEchoColors.current
                capturedEchoTheme = EchoTheme.colors
                capturedBg = EchoBg
                capturedCard = EchoCard
                capturedText = EchoText
                capturedCyan = EchoCyan
                capturedMaterialBg = MaterialTheme.colorScheme.background
                capturedMaterialSurface = MaterialTheme.colorScheme.surface

                Card(
                    modifier = Modifier.testTag("sample_echo_card"),
                    colors = CardDefaults.cardColors(containerColor = EchoCard)
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = if (EchoTheme.colors.isDark) "Echo Card Dark" else "Echo Card Light",
                            color = EchoText
                        )
                    }
                }
            }
        }

        composeTestRule.waitForIdle()
        assertEquals(DarkEchoColors, capturedLocal)
        assertEquals(DarkEchoColors, capturedEchoTheme)
        assertEquals(DarkEchoColors.bg, capturedBg)
        assertEquals(DarkEchoColors.card, capturedCard)
        assertEquals(DarkEchoColors.text, capturedText)
        assertEquals(DarkEchoColors.cyan, capturedCyan)
        assertEquals(DarkEchoColors.bg, capturedMaterialBg)
        assertEquals(DarkEchoColors.bgAlt, capturedMaterialSurface)
        composeTestRule.onNodeWithText("Echo Card Dark").assertIsDisplayed()

        // Toggle to Light Mode
        composeTestRule.runOnIdle { isDark = false }
        composeTestRule.waitForIdle()

        assertEquals(LightEchoColors, capturedLocal)
        assertEquals(LightEchoColors, capturedEchoTheme)
        assertEquals(LightEchoColors.bg, capturedBg)
        assertEquals(LightEchoColors.card, capturedCard)
        assertEquals(LightEchoColors.text, capturedText)
        assertEquals(LightEchoColors.cyan, capturedCyan)
        assertEquals(LightEchoColors.bg, capturedMaterialBg)
        assertEquals(LightEchoColors.card, capturedMaterialSurface)
        composeTestRule.onNodeWithText("Echo Card Light").assertIsDisplayed()

        // Round-trip back to Dark Mode
        composeTestRule.runOnIdle { isDark = true }
        composeTestRule.waitForIdle()

        assertEquals(DarkEchoColors, capturedLocal)
        assertEquals(DarkEchoColors.card, capturedCard)
        composeTestRule.onNodeWithText("Echo Card Dark").assertIsDisplayed()
    }

    @Test
    fun settingsDialog_themeSwitchClickTogglesDarkAtmosphericToLightSolarTheme() {
        var isDark by mutableStateOf(true)
        lateinit var activePalette: EchoColorPalette

        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = isDark) {
                activePalette = LocalEchoColors.current
                SettingsDialog(
                    isDarkTheme = isDark,
                    onToggleDarkTheme = { isDark = !isDark },
                    onRequestExitApp = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Dark Atmospheric Theme").assertIsDisplayed()
        assertEquals(DarkEchoColors, activePalette)

        composeTestRule.onNodeWithTag("theme_switch").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Light Solar Theme").assertIsDisplayed()
        assertEquals(LightEchoColors, activePalette)
    }
}
