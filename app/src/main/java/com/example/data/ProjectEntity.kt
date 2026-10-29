package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Offline User-Defined Project / Workspace Entity stored in local SQLite via Room.
 * Includes title, description, creation timestamp, color tag, and display order.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val colorTagHex: String = "#00F0FF",
    val isArchived: Boolean = false,
    val orderIndex: Int = 0
)
