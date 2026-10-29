package com.example.gateway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GoogleDriveSyncManagerTest {

    @Test
    fun testDefaultStateAndStatusMessage() {
        assertNotNull(GoogleDriveSyncManager.isAutoSyncEnabled.value)
        assertNotNull(GoogleDriveSyncManager.isWifiOnly.value)
        assertNotNull(GoogleDriveSyncManager.syncStatusMessage.value)
        assertTrue(GoogleDriveSyncManager.getFormattedLastSyncTime().isNotBlank())
    }

    @Test
    fun testStatusFormatting() {
        val formattedTime = GoogleDriveSyncManager.getFormattedLastSyncTime()
        assertNotNull(formattedTime)
        assertTrue(formattedTime.isNotEmpty())
    }
}
