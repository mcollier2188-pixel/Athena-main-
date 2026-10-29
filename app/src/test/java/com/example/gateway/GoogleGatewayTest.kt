package com.example.gateway

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GoogleGatewayTest {

    @Test
    fun testPayloadPreviewFormattingAndZeroTrustSanitization() {
        val query = "What is the geological baseline of the Solent?"
        val preview = GoogleGatewayService.generateOutboundPayloadPreview(
            requester = GatewayRequester.ATHENA,
            requestText = query,
            enableGoogleSearch = true,
            enableGoogleMaps = false
        )

        assertTrue("Preview must contain the query", preview.contains(query))
        assertTrue("Preview must identify requester as ATHENA", preview.contains("ATHENA"))
        assertTrue("Preview must reflect search grounding enabled", preview.contains("\"google_search_grounding\": true"))
        assertTrue("Preview must reflect maps grounding disabled", preview.contains("\"google_maps_grounding\": false"))
        assertTrue("Must guarantee zero archive attachment", preview.contains("\"archive_attached\": false"))
        assertTrue("Must guarantee zero workspace notes attachment", preview.contains("\"workspace_notes_attached\": false"))
        assertTrue("Must guarantee zero agent memory attachment", preview.contains("\"agent_memory_attached\": false"))
        assertTrue("Must guarantee isolation mode ZERO_TRUST_AIRGAP", preview.contains("ZERO_TRUST_AIRGAP"))
    }

    @Test
    fun testMasterSwitchDisableBlocksOutboundTransmission() = runBlocking {
        val response = GoogleGatewayService.executeGatewayRequest(
            context = null,
            requester = GatewayRequester.ATHENA,
            requestText = "Test query",
            enableGoogleSearch = true,
            enableGoogleMaps = false,
            isGatewayMasterEnabled = false
        )

        assertFalse("Request must not succeed when master switch is disabled", response.isSuccess)
        assertNotNull(response.errorMessage)
        assertTrue(
            "Error message must indicate master switch is disabled",
            response.errorMessage?.contains("GATEWAY MASTER SWITCH DISABLED") == true
        )
    }

    @Test
    fun testEmptyQueryValidation() = runBlocking {
        val response = GoogleGatewayService.executeGatewayRequest(
            context = null,
            requester = GatewayRequester.DOLA,
            requestText = "   ",
            enableGoogleSearch = false,
            enableGoogleMaps = false,
            isGatewayMasterEnabled = true
        )

        assertFalse("Empty query must fail validation", response.isSuccess)
        assertTrue(response.errorMessage?.contains("EMPTY REQUEST") == true)
    }

    @Test
    fun testOfflineAirgapSandboxingWhenNetworkUnavailable() = runBlocking {
        val response = GoogleGatewayService.executeGatewayRequest(
            context = null,
            requester = GatewayRequester.OTHER_AI,
            requestText = "Analyze seismic bedrock resonance",
            enableGoogleSearch = true,
            enableGoogleMaps = true,
            isGatewayMasterEnabled = true
        )

        assertTrue(response.isSuccess)
        assertTrue("Response must identify sandbox or offline airgap", response.isOfflineMock || response.sources.isNotEmpty())
        assertTrue("Sources must not be empty", response.sources.isNotEmpty())
    }
}
