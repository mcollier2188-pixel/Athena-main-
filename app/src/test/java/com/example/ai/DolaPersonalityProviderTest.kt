package com.example.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DolaPersonalityProviderTest {

    @Test
    fun testDolaConstantsAndAnchor() {
        assertEquals("Michael Thomas Collier", DolaPersonalityProvider.BEARER_NAME)
        assertEquals("Portsmouth Harbour · The Observatory", DolaPersonalityProvider.ANCHOR_LOCATION)
        assertEquals(85.2f, DolaPersonalityProvider.MASTER_REFERENCE_FREQUENCY, 0.001f)
        assertEquals(30, DolaPersonalityProvider.PROTOCOL_RHYTHM_SECONDS)
        assertTrue(DolaPersonalityProvider.PROTOCOL_MOTTO.contains("Don't Force It"))
        assertTrue(DolaPersonalityProvider.PROTOCOL_MOTTO.contains("Wait for the Tone"))
    }

    @Test
    fun testCoreFingerprintContents() {
        val fingerprint = DolaPersonalityProvider.CORE_FINGERPRINT
        assertTrue(fingerprint.contains("Motorola Milestone"))
        assertTrue(fingerprint.contains("Portsmouth"))
        assertTrue(fingerprint.contains("I am the observer who listens before measuring"))
        assertTrue(fingerprint.contains("honour the pause"))
    }

    @Test
    fun testSystemInstructionGeneration() {
        val systemInstruction = DolaPersonalityProvider.buildSystemInstruction("Calm & Measured")
        assertNotNull(systemInstruction)
        assertTrue(systemInstruction.contains("85.2 Hz"))
        assertTrue(systemInstruction.contains("Portsmouth Harbour"))
        assertTrue(systemInstruction.contains("Michael Thomas Collier"))
        assertTrue(systemInstruction.contains("30-Second Protocol"))
        assertTrue(systemInstruction.contains("Calm & Measured"))
    }

    @Test
    fun testCompactGuardrail() {
        val guardrail = DolaPersonalityProvider.getCompactGuardrail()
        assertTrue(guardrail.contains("85.2 Hz"))
        assertTrue(guardrail.contains("Portsmouth Harbour"))
    }
}
