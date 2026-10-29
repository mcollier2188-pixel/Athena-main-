package com.example.data

import android.content.Context
import android.net.Uri
import com.example.ui.components.MediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class VaultMediaItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val uri: Uri,
    val type: MediaType,
    val dateAdded: Long = System.currentTimeMillis(),
    val prompt: String? = null,
    val style: String? = null,
    val isAiGenerated: Boolean = false,
    val isVeoVideo: Boolean = false
)

object MediaVaultRepository {
    private val _items = MutableStateFlow<List<VaultMediaItem>>(emptyList())
    val items: StateFlow<List<VaultMediaItem>> = _items.asStateFlow()

    private var isInitialized = false

    fun initializeWithDefaults(context: Context) {
        if (isInitialized) return
        isInitialized = true
        val sampleMedia = listOf(
            VaultMediaItem(
                id = "sample_sunset",
                title = "Cinematic Sunset B-Roll",
                subtitle = "Video • 4K 60fps • 0:45",
                uri = Uri.parse("android.resource://${context.packageName}/raw/sample_sunset"),
                type = MediaType.VIDEO,
                isAiGenerated = false
            ),
            VaultMediaItem(
                id = "sample_synth",
                title = "Studio Synth Sessions",
                subtitle = "Video • 1080p • 1:12",
                uri = Uri.parse("android.resource://${context.packageName}/raw/sample_synth"),
                type = MediaType.VIDEO,
                isAiGenerated = false
            ),
            VaultMediaItem(
                id = "sample_cosmic",
                title = "Cosmic Atmosphere Photo",
                subtitle = "Photo • HDR • Gemini Vision",
                uri = Uri.parse("android.resource://${context.packageName}/drawable/sample_cosmic"),
                type = MediaType.PHOTO,
                isAiGenerated = false
            )
        )
        _items.value = sampleMedia
    }

    fun addItem(item: VaultMediaItem) {
        val current = _items.value.toMutableList()
        // Put newest at the top
        current.add(0, item)
        _items.value = current
    }

    fun removeItem(id: String) {
        _items.value = _items.value.filter { it.id != id }
    }
}
