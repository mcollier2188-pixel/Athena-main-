package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room SQLite Database Entity storing metadata for user-created multimedia projects.
 * Encapsulates titles, timestamps, local asset file paths, media types, formats,
 * file sizes, and preview thumbnail paths.
 */
@Entity(tableName = "multimedia_projects")
data class MultimediaProjectEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val mediaType: String = "VIDEO", // VIDEO, AUDIO, IMAGE, MULTI_ASSET
    val filePath: String, // Absolute path to saved local asset
    val thumbnailPath: String? = null, // Optional local thumbnail / snapshot path
    val fileSizeBytes: Long = 0L,
    val durationSeconds: Float = 0.0f, // For video/audio assets
    val mimeType: String = "video/mp4",
    val resolution: String? = null, // e.g. "1920x1080", "1080x1920"
    val aspectRatio: String = "16:9", // e.g. "16:9", "9:16", "1:1"
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val tags: String = ""
)
