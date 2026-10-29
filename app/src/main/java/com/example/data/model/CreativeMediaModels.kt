package com.example.data.model

data class CreativeVideoItem(
    val id: String,
    val title: String,
    val channel: String,
    val duration: String,
    val views: String,
    val category: String,
    val summary: String,
    val youtubeVideoId: String = "dQw4w9WgXcQ",
    val chapters: List<String> = emptyList(),
    val videoUriString: String = ""
)

data class CreativePlaylistItem(
    val id: String,
    val title: String,
    val author: String,
    val videoCount: String,
    val description: String,
    val sampleVideoId: String = "dQw4w9WgXcQ",
    val isCustom: Boolean = false,
    val videos: List<CreativeVideoItem> = emptyList()
)
