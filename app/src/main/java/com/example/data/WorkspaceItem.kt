package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workspace_items",
    indices = [Index(value = ["isDeleted", "parentId"])]
)
data class WorkspaceItem(
    @PrimaryKey val id: String,
    val parentId: String? = null,
    val kind: String, // "folder", "note", "file"
    val name: String,
    val text: String = "",
    val fileType: String = "",
    val fileUri: String? = null,
    val tags: String = "", // comma-separated
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedTimestamp: Long? = null,
    val modifiedTimestamp: Long = System.currentTimeMillis(),
    val size: Long = 0L
)
