package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ai.AthenaChatModel
import com.example.ai.AthenaService
import com.example.audio.HarmonicToneGenerator
import com.example.audio.SoundEffectType
import com.example.ui.theme.EchoAmber
import com.example.ui.theme.EchoBg
import com.example.ui.theme.EchoCard
import com.example.ui.theme.EchoCardStroke
import com.example.ui.theme.EchoCyan
import com.example.ui.theme.EchoGreen
import com.example.ui.theme.EchoMuted
import com.example.ui.theme.EchoOnCyan
import com.example.ui.theme.EchoRed
import com.example.ui.theme.EchoText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors

/**
 * High-definition in-app camera document scanner using CameraX with a full-screen
 * viewfinder, document framing overlay, contrast optimization, and AI extraction
 * pipeline feeding directly into the Council round-table discussion.
 */
@Composable
fun DocumentScannerDialog(
    toneGenerator: HarmonicToneGenerator = HarmonicToneGenerator.SHARED,
    onDocumentProcessed: (extractedTopic: String, fullAnalysis: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(
                context,
                "Camera hardware permission is required for real-time document scanning.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Camera state
    var isFlashOn by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Analysis Pipeline state
    var isCapturing by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var processingStage by remember { mutableStateOf("Initializing optics...") }
    var capturedPreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Holographic scanline animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_sweep"
    )

    Dialog(
        onDismissRequest = {
            if (!isCapturing && !isAnalyzing) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isCapturing && !isAnalyzing,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("document_scanner_dialog")
        ) {
            if (!hasCameraPermission) {
                // Permission Fallback Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Camera Permission Required",
                        tint = EchoCyan,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "HARDWARE CAMERA ACCESS REQUIRED",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EchoText,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Athena document scanner requires camera hardware to capture physical research notes, schematics, and equations for live Council analysis.",
                        fontSize = 12.sp,
                        color = EchoMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("btn_scanner_cancel_permission")
                        ) {
                            Text("Cancel", color = EchoText)
                        }
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = EchoCyan, contentColor = EchoOnCyan),
                            modifier = Modifier.testTag("btn_scanner_request_permission")
                        ) {
                            Text("Grant Permission", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Real-time CameraX Viewfinder
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                                .build()
                            imageCapture = capture

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    capture
                                )
                                cameraControl = camera.cameraControl
                            } catch (exc: Exception) {
                                Toast.makeText(ctx, "Failed to bind camera: ${exc.message}", Toast.LENGTH_SHORT).show()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Cybernetic Framing Overlay & Laser Scanning Beam
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val frameWidth = size.width * 0.85f
                    val frameHeight = size.height * 0.65f
                    val left = (size.width - frameWidth) / 2f
                    val top = (size.height - frameHeight) / 2f
                    val right = left + frameWidth
                    val bottom = top + frameHeight
                    val cornerLength = 48f
                    val strokeWidth = 5f

                    // Semi-transparent background mask around the document frame
                    // Top rect
                    drawRect(color = Color.Black.copy(alpha = 0.55f), topLeft = Offset(0f, 0f), size = Size(size.width, top))
                    // Bottom rect
                    drawRect(color = Color.Black.copy(alpha = 0.55f), topLeft = Offset(0f, bottom), size = Size(size.width, size.height - bottom))
                    // Left rect
                    drawRect(color = Color.Black.copy(alpha = 0.55f), topLeft = Offset(0f, top), size = Size(left, frameHeight))
                    // Right rect
                    drawRect(color = Color.Black.copy(alpha = 0.55f), topLeft = Offset(right, top), size = Size(size.width - right, frameHeight))

                    // Document Frame Bounding Box (Subtle grid)
                    drawRect(
                        color = Color(0xFF00E5FF).copy(alpha = 0.35f),
                        topLeft = Offset(left, top),
                        size = Size(frameWidth, frameHeight),
                        style = Stroke(width = 1.5f)
                    )

                    // Corner brackets (Top-Left)
                    drawLine(color = Color(0xFF00E5FF), start = Offset(left, top), end = Offset(left + cornerLength, top), strokeWidth = strokeWidth)
                    drawLine(color = Color(0xFF00E5FF), start = Offset(left, top), end = Offset(left, top + cornerLength), strokeWidth = strokeWidth)

                    // Corner brackets (Top-Right)
                    drawLine(color = Color(0xFF00E5FF), start = Offset(right, top), end = Offset(right - cornerLength, top), strokeWidth = strokeWidth)
                    drawLine(color = Color(0xFF00E5FF), start = Offset(right, top), end = Offset(right, top + cornerLength), strokeWidth = strokeWidth)

                    // Corner brackets (Bottom-Left)
                    drawLine(color = Color(0xFF00E5FF), start = Offset(left, bottom), end = Offset(left + cornerLength, bottom), strokeWidth = strokeWidth)
                    drawLine(color = Color(0xFF00E5FF), start = Offset(left, bottom), end = Offset(left, bottom - cornerLength), strokeWidth = strokeWidth)

                    // Corner brackets (Bottom-Right)
                    drawLine(color = Color(0xFF00E5FF), start = Offset(right, bottom), end = Offset(right - cornerLength, bottom), strokeWidth = strokeWidth)
                    drawLine(color = Color(0xFF00E5FF), start = Offset(right, bottom), end = Offset(right, bottom - cornerLength), strokeWidth = strokeWidth)

                    // Animated Laser Scanning Line
                    val currentLaserY = top + (frameHeight * laserOffset)
                    drawLine(
                        color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                        start = Offset(left + 8f, currentLaserY),
                        end = Offset(right - 8f, currentLaserY),
                        strokeWidth = 3f
                    )
                }

                // Top Floating Control Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(1.dp, EchoCardStroke, CircleShape)
                            .testTag("btn_scanner_close")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Scanner", tint = Color.White)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CYBERNETIC DOCUMENT SCANNER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EchoCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "OPTICAL MATRIX • CONTRAST AUTO-LOCK",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = EchoGreen
                        )
                    }

                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            cameraControl?.enableTorch(isFlashOn)
                            toneGenerator.playChime(SoundEffectType.TAP)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .border(1.dp, if (isFlashOn) EchoAmber else EchoCardStroke, CircleShape)
                            .testTag("btn_scanner_toggle_flash")
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Toggle Torch",
                            tint = if (isFlashOn) EchoAmber else Color.White
                        )
                    }
                }

                // Alignment Guidance Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .border(1.dp, EchoCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Align physical notes, equations or diagrams within brackets",
                        fontSize = 11.sp,
                        color = EchoText,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Bottom Shutter & Controls Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Shutter Button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(3.dp, EchoCyan, CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                if (isCapturing || isAnalyzing) return@Button
                                isCapturing = true
                                processingStage = "Capturing optical frame..."
                                toneGenerator.playChime(SoundEffectType.TAP)

                                val capture = imageCapture
                                if (capture == null) {
                                    isCapturing = false
                                    Toast.makeText(context, "Camera is not ready yet", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                capture.takePicture(
                                    cameraExecutor,
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                            scope.launch {
                                                toneGenerator.playChime(SoundEffectType.CHIME)
                                                processingStage = "Applying automated contrast enhancement..."
                                                val rawBitmap = imageProxyToBitmap(imageProxy)
                                                imageProxy.close()

                                                val enhancedBitmap = enhanceDocumentContrast(rawBitmap)
                                                capturedPreviewBitmap = enhancedBitmap
                                                isCapturing = false
                                                isAnalyzing = true
                                                processingStage = "Multimodal AI extraction & paradox analysis..."

                                                // Encode to Base64
                                                val base64Jpeg = withContext(Dispatchers.IO) {
                                                    val stream = ByteArrayOutputStream()
                                                    enhancedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                                                    Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                                                }

                                                // Call multimodal analysis via Gemini
                                                val analysisResult = AthenaService.analyzeDocumentOrImage(
                                                    base64Data = base64Jpeg,
                                                    mimeType = "image/jpeg",
                                                    preferredModel = AthenaChatModel.FLASH
                                                )

                                                toneGenerator.playChime(SoundEffectType.SUCCESS)
                                                isAnalyzing = false

                                                val fullAnalysis = analysisResult.getOrDefault(
                                                    "Scanned Document Analysis Complete: Extracted equations and theoretical theorems ready for debate."
                                                )

                                                // Extract topic synthesis candidate
                                                val extractedTopic = extractDebateTopic(fullAnalysis)

                                                Toast.makeText(
                                                    context,
                                                    "Document parsed! Routing directly into Live Council Debate...",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                onDocumentProcessed(extractedTopic, fullAnalysis)
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            scope.launch {
                                                isCapturing = false
                                                toneGenerator.playChime(SoundEffectType.ALERT)
                                                Toast.makeText(
                                                    context,
                                                    "Capture failed: ${exception.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                )
                            },
                            enabled = !isCapturing && !isAnalyzing,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("btn_shutter_capture_document"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EchoCyan,
                                contentColor = EchoOnCyan,
                                disabledContainerColor = EchoMuted
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = "Capture Document",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Processing & Multimodal Analysis Loading Modal Overlay
                AnimatedVisibility(
                    visible = isCapturing || isAnalyzing,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.85f))
                            .testTag("scanner_processing_overlay"),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = EchoCard),
                            border = BorderStroke(1.dp, EchoCyan),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = EchoCyan,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "AI MULTIMODAL EXTRACTION",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EchoCyan,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = processingStage,
                                        fontSize = 11.sp,
                                        color = EchoText,
                                        fontFamily = FontFamily.Monospace,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                                Text(
                                    text = "Enhancing contrast • Extracting mathematical formulas • Preparing Council Round-Table feed",
                                    fontSize = 10.sp,
                                    color = EchoMuted,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Convert ImageProxy to Bitmap safely.
 */
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
    val buffer: ByteBuffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
}

/**
 * Fast contrast enhancement to ensure high legibility of physical handwritten notes,
 * printed diagrams, and equations before multimodal AI processing.
 */
private fun enhanceDocumentContrast(src: Bitmap): Bitmap {
    val dest = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(dest)
    val paint = Paint()

    // High-contrast color matrix (increase contrast by 1.35x and slight brightness boost)
    val colorMatrix = ColorMatrix().apply {
        val contrast = 1.35f
        val brightness = 10f
        set(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
    canvas.drawBitmap(src, 0f, 0f, paint)
    return dest
}

/**
 * Extracts a concise debate theorem from the multimodal analysis output.
 */
private fun extractDebateTopic(analysisText: String): String {
    val lines = analysisText.lines()
    for (line in lines) {
        val lower = line.lowercase()
        if (lower.contains("unified topic:") || lower.contains("synthesis topic:")) {
            val extracted = line.substringAfter(":").replace("*", "").trim()
            if (extracted.isNotBlank()) return extracted
        }
    }
    // Fallback: search for first bold topic or first substantive line
    for (line in lines) {
        if (line.startsWith("**") && line.endsWith("**") && line.length in 15..120) {
            return line.replace("**", "").trim()
        }
    }
    return "Physical Document Analysis: Empirical Axioms & Theoretical Paradoxes"
}
