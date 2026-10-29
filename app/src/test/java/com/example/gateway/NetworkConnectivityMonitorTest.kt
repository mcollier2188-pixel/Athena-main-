package com.example.gateway

import com.example.util.NetworkStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkConnectivityMonitorTest {

    @Test
    fun testNetworkStatusProperties() {
        val offline = NetworkStatus(isConnected = false)
        assertFalse(offline.isConnected)
        assertFalse(offline.isWifi)
        assertFalse(offline.isCellular)

        val onlineWifi = NetworkStatus(
            isConnected = true,
            isWifi = true,
            isCellular = false,
            isValidated = true
        )
        assertTrue(onlineWifi.isConnected)
        assertTrue(onlineWifi.isWifi)
        assertFalse(onlineWifi.isCellular)

        val onlineCellular = NetworkStatus(
            isConnected = true,
            isWifi = false,
            isCellular = true,
            isValidated = true
        )
        assertTrue(onlineCellular.isConnected)
        assertFalse(onlineCellular.isWifi)
        assertTrue(onlineCellular.isCellular)
    }
}
