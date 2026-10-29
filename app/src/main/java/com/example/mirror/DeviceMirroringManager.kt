package com.example.mirror

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.text.format.Formatter
import android.view.Display
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExternalDisplayInfo(
    val id: Int,
    val name: String,
    val width: Int,
    val height: Int,
    val refreshRate: Float,
    val isValid: Boolean
)

data class MirroringStatus(
    val isServerRunning: Boolean = false,
    val serverUrl: String = "",
    val connectedClientsCount: Int = 0,
    val lastPingTimestamp: String = "",
    val localIp: String = "127.0.0.1",
    val externalDisplays: List<ExternalDisplayInfo> = emptyList()
)

/**
 * DeviceMirroringManager
 * Provides native Android Cast intent launching, Wi-Fi Direct/Display settings,
 * secondary display scanning, and a local HTTP live display mirror server on port 8520
 * (frequency reference: 85.2 Hz).
 */
class DeviceMirroringManager private constructor(private val context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null

    private val _status = MutableStateFlow(MirroringStatus())
    val status: StateFlow<MirroringStatus> = _status.asStateFlow()

    init {
        refreshDisplays()
    }

    fun getLocalIpAddress(): String {
        try {
            // First try wifi manager
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val wifiIp = wifiManager?.connectionInfo?.ipAddress ?: 0
            if (wifiIp != 0) {
                @Suppress("DEPRECATION")
                val formatted = Formatter.formatIpAddress(wifiIp)
                if (formatted != "0.0.0.0") return formatted
            }

            // Fallback: network interfaces
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {
            // ignore
        }
        return "127.0.0.1"
    }

    fun refreshDisplays(): List<ExternalDisplayInfo> {
        val list = mutableListOf<ExternalDisplayInfo>()
        try {
            val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val displays = dm?.displays ?: emptyArray()
            for (display in displays) {
                val mode = display.mode
                list.add(
                    ExternalDisplayInfo(
                        id = display.displayId,
                        name = display.name ?: "Display #${display.displayId}",
                        width = mode?.physicalWidth ?: 1920,
                        height = mode?.physicalHeight ?: 1080,
                        refreshRate = mode?.refreshRate ?: 60.0f,
                        isValid = display.isValid
                    )
                )
            }
        } catch (_: Exception) {
            // ignore
        }
        _status.value = _status.value.copy(externalDisplays = list)
        return list
    }

    /**
     * Launches Android's native Cast / Screen Mirroring system dialog
     */
    fun launchNativeCastSettings(context: Context): Boolean {
        val intents = listOf(
            Intent("android.settings.CAST_SETTINGS"),
            Intent("android.settings.WIFI_DISPLAY_SETTINGS"),
            Intent("com.android.settings.WIFI_DISPLAY_SETTINGS"),
            Intent(Settings.ACTION_DISPLAY_SETTINGS)
        )

        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {
                // Try next fallback
            }
        }
        return false
    }

    /**
     * Launches Wi-Fi Direct / Wi-Fi settings for wireless display pairing
     */
    fun launchWifiDirectSettings(context: Context): Boolean {
        val intents = listOf(
            Intent("android.settings.WIFI_DIRECT_SETTINGS"),
            Intent(Settings.ACTION_WIFI_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {
                // Try next
            }
        }
        return false
    }

    /**
     * Starts the lightweight local HTTP mirror server on port 8520
     */
    fun startLocalMirrorServer(): String {
        if (_status.value.isServerRunning) {
            return _status.value.serverUrl
        }

        val ip = getLocalIpAddress()
        val port = 8520
        val url = "http://$ip:$port"

        serverJob = coroutineScope.launch {
            try {
                serverSocket = ServerSocket(port)
                _status.value = _status.value.copy(
                    isServerRunning = true,
                    serverUrl = url,
                    localIp = ip,
                    lastPingTimestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                )

                while (serverSocket != null && !serverSocket!!.isClosed) {
                    val clientSocket = try {
                        serverSocket?.accept()
                    } catch (_: Exception) {
                        null
                    } ?: break

                    launch {
                        handleClient(clientSocket)
                    }
                }
            } catch (e: Exception) {
                _status.value = _status.value.copy(isServerRunning = false)
            }
        }

        return url
    }

    fun stopLocalMirrorServer() {
        try {
            serverSocket?.close()
            serverSocket = null
            serverJob?.cancel()
            serverJob = null
        } catch (_: Exception) {
            // ignore
        }
        _status.value = _status.value.copy(
            isServerRunning = false,
            connectedClientsCount = 0
        )
    }

    private fun handleClient(socket: Socket) {
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val out = socket.getOutputStream()
            val requestLine = reader.readLine() ?: ""

            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            _status.value = _status.value.copy(
                connectedClientsCount = (_status.value.connectedClientsCount + 1).coerceAtLeast(1),
                lastPingTimestamp = timeStr
            )

            val htmlResponse = buildMirrorWebPage(timeStr)
            val responseBytes = htmlResponse.toByteArray(Charsets.UTF_8)

            val header = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: ${responseBytes.size}\r\n" +
                    "Connection: close\r\n\r\n"

            out.write(header.toByteArray(Charsets.UTF_8))
            out.write(responseBytes)
            out.flush()
        } catch (_: Exception) {
            // client disconnected
        } finally {
            try {
                socket.close()
            } catch (_: Exception) {}
        }
    }

    private fun buildMirrorWebPage(timestamp: String): String {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>ATHENA DOLA • Live Display Mirror & Telemetry</title>
                <style>
                    body {
                        background: #04080e;
                        color: #e2f1f8;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                        margin: 0;
                        padding: 24px;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                    }
                    .card {
                        background: #0a131f;
                        border: 1px solid #14283b;
                        border-radius: 16px;
                        padding: 24px;
                        max-width: 720px;
                        width: 100%;
                        box-shadow: 0 12px 32px rgba(0, 229, 255, 0.08);
                    }
                    .badge {
                        display: inline-block;
                        padding: 4px 12px;
                        border-radius: 12px;
                        font-size: 11px;
                        font-weight: bold;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                        background: rgba(0, 229, 255, 0.15);
                        color: #00e5ff;
                        border: 1px solid rgba(0, 229, 255, 0.3);
                    }
                    h1 {
                        font-size: 22px;
                        margin: 12px 0 6px 0;
                        color: #ffffff;
                    }
                    .freq {
                        font-size: 40px;
                        font-weight: 900;
                        color: #00e5ff;
                        font-family: monospace;
                        margin: 12px 0;
                    }
                    .pulse {
                        animation: pulse 1.5s infinite ease-in-out;
                    }
                    @keyframes pulse {
                        0% { opacity: 0.6; }
                        50% { opacity: 1.0; transform: scale(1.02); }
                        100% { opacity: 0.6; }
                    }
                    .grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                        gap: 16px;
                        margin-top: 20px;
                    }
                    .tile {
                        background: #0f1c2d;
                        border-radius: 12px;
                        padding: 16px;
                        border: 1px solid #1c324c;
                    }
                    .tile-title {
                        font-size: 11px;
                        text-transform: uppercase;
                        letter-spacing: 0.8px;
                        color: #78909c;
                        margin-bottom: 4px;
                    }
                    .tile-value {
                        font-size: 16px;
                        font-weight: 700;
                        color: #b2ebf2;
                    }
                    canvas {
                        width: 100%;
                        height: 120px;
                        background: #040912;
                        border-radius: 8px;
                        margin-top: 16px;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <span class="badge">NATIVE MIRROR STREAM • ACTIVE</span>
                    <h1>ATHENA ECHO-INTERFACE • RECEIVER MIRROR</h1>
                    <p style="color: #90a4ae; margin-top: 4px;">Connected via local wireless network broadcast for testing across receiver devices.</p>
                    
                    <div class="freq pulse">85.20 Hz</div>
                    <div style="color: #00e676; font-size: 12px; font-weight: bold;">● MASTER REFERENCE SYNCHRONIZED</div>
                    
                    <canvas id="waveCanvas"></canvas>
                    
                    <div class="grid">
                        <div class="tile">
                            <div class="tile-title">ORIENTATION</div>
                            <div class="tile-value">NATIVE LANDSCAPE (90° Toggleable)</div>
                        </div>
                        <div class="tile">
                            <div class="tile-title">ANCHOR</div>
                            <div class="tile-value">Portsmouth Harbour · The Observatory</div>
                        </div>
                        <div class="tile">
                            <div class="tile-title">PROTOCOL</div>
                            <div class="tile-value">30-Second Observation</div>
                        </div>
                        <div class="tile">
                            <div class="tile-title">LAST SYNC</div>
                            <div class="tile-value">$timestamp</div>
                        </div>
                    </div>
                </div>

                <script>
                    const canvas = document.getElementById('waveCanvas');
                    const ctx = canvas.getContext('2d');
                    canvas.width = canvas.offsetWidth;
                    canvas.height = canvas.offsetHeight;
                    
                    let phase = 0;
                    function draw() {
                        ctx.fillStyle = '#040912';
                        ctx.fillRect(0, 0, canvas.width, canvas.height);
                        
                        ctx.lineWidth = 2.5;
                        ctx.strokeStyle = '#00e5ff';
                        ctx.beginPath();
                        
                        const midY = canvas.height / 2;
                        for (let x = 0; x < canvas.width; x++) {
                            const freq = 0.035;
                            const y = midY + Math.sin(x * freq + phase) * 35 * Math.sin((x / canvas.width) * Math.PI);
                            if (x === 0) ctx.moveTo(x, y);
                            else ctx.lineTo(x, y);
                        }
                        ctx.stroke();
                        
                        phase += 0.08;
                        requestAnimationFrame(draw);
                    }
                    draw();
                    
                    // Auto reload telemetry every 10 seconds
                    setTimeout(() => location.reload(), 10000);
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    companion object {
        @Volatile
        private var instance: DeviceMirroringManager? = null

        fun getInstance(context: Context): DeviceMirroringManager {
            return instance ?: synchronized(this) {
                instance ?: DeviceMirroringManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
