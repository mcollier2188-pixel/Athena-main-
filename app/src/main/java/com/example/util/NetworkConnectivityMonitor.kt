package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class NetworkStatus(
    val isConnected: Boolean,
    val isWifi: Boolean = false,
    val isCellular: Boolean = false,
    val isValidated: Boolean = false
)

/**
 * Android Network Connectivity Monitor using real-time ConnectivityManager.NetworkCallback.
 */
object NetworkConnectivityMonitor {

    fun getInitialStatus(context: Context): NetworkStatus {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return NetworkStatus(isConnected = false)
        val activeNetwork = cm.activeNetwork ?: return NetworkStatus(isConnected = false)
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return NetworkStatus(isConnected = false)

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

        return NetworkStatus(
            isConnected = hasInternet && isValidated,
            isWifi = isWifi,
            isCellular = isCellular,
            isValidated = isValidated
        )
    }

    /**
     * Cold Flow emitting network status changes via Android's NetworkCallback.
     */
    fun observeNetworkStatus(context: Context): Flow<NetworkStatus> = callbackFlow {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (cm == null) {
            trySend(NetworkStatus(isConnected = false))
            close()
            return@callbackFlow
        }

        // Emit current initial status immediately
        trySend(getInitialStatus(context))

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val caps = cm.getNetworkCapabilities(network)
                val isValidated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
                val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
                val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
                trySend(
                    NetworkStatus(
                        isConnected = true,
                        isWifi = isWifi,
                        isCellular = isCellular,
                        isValidated = isValidated
                    )
                )
            }

            override fun onLost(network: Network) {
                // Re-evaluate remaining active networks if any
                trySend(getInitialStatus(context))
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                val isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                val isCellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

                trySend(
                    NetworkStatus(
                        isConnected = hasInternet && isValidated,
                        isWifi = isWifi,
                        isCellular = isCellular,
                        isValidated = isValidated
                    )
                )
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            cm.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            trySend(getInitialStatus(context))
        }

        awaitClose {
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Ignore if unregistered already
            }
        }
    }.distinctUntilChanged()
}
