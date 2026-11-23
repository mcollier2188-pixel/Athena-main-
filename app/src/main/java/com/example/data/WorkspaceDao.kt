package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspace_items WHERE isDeleted = 0 ORDER BY isPinned DESC, kind = 'folder' DESC, name ASC")
    fun getAllLive(): Flow<List<WorkspaceItem>>

    @Query("SELECT * FROM workspace_items WHERE isDeleted = 0")
    suspend fun getAll(): List<WorkspaceItem>

    @Query("SELECT * FROM workspace_items WHERE isDeleted = 1 ORDER BY deletedTimestamp DESC")
    fun getTrashItems(): Flow<List<WorkspaceItem>>

    @Query("SELECT * FROM workspace_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): WorkspaceItem?

    @Query("SELECT * FROM workspace_items WHERE isDeleted = 0 AND (parentId = :parentId OR (:parentId IS NULL AND parentId IS NULL)) ORDER BY isPinned DESC, kind = 'folder' DESC, name ASC")
    fun getItemsInFolder(parentId: String?): Flow<List<WorkspaceItem>>

    @Query(
        """
        SELECT * FROM workspace_items
        WHERE isDeleted = 0
          AND (name LIKE '%' || :escapedQuery || '%' ESCAPE '\'
               OR text LIKE '%' || :escapedQuery || '%' ESCAPE '\'
               OR tags LIKE '%' || :escapedQuery || '%' ESCAPE '\')
        ORDER BY isPinned DESC, kind = 'folder' DESC, name ASC
        """
    )
    fun searchItemsEscaped(escapedQuery: String): Flow<List<WorkspaceItem>>

    fun searchItems(query: String): Flow<List<WorkspaceItem>> {
        val escaped = query
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
        return searchItemsEscaped(escaped)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: WorkspaceItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<WorkspaceItem>)

    @Update
    suspend fun updateItem(item: WorkspaceItem)

    @Query(
        """
        WITH RECURSIVE descendants(id) AS (
            SELECT id FROM workspace_items WHERE id = :id
            UNION
            SELECT w.id FROM workspace_items w INNER JOIN descendants d ON w.parentId = d.id
        )
        SELECT id FROM descendants
        """
    )
    suspend fun getTreeIds(id: String): List<String>

    @Query(
        """
        WITH RECURSIVE descendants(id) AS (
            SELECT id FROM workspace_items WHERE id = :id
            UNION
            SELECT w.id FROM workspace_items w INNER JOIN descendants d ON w.parentId = d.id
        )
        UPDATE workspace_items
        SET isDeleted = 1, deletedTimestamp = :timestamp
        WHERE id IN (SELECT id FROM descendants)
        """
    )
    suspend fun moveToTrash(id: String, timestamp: Long = System.currentTimeMillis())

    @Query(
        """
        WITH RECURSIVE descendants(id) AS (
            SELECT id FROM workspace_items WHERE id = :id
            UNION
            SELECT w.id FROM workspace_items w INNER JOIN descendants d ON w.parentId = d.id
        )
        UPDATE workspace_items
        SET isDeleted = 0, deletedTimestamp = NULL
        WHERE id IN (SELECT id FROM descendants)
        """
    )
    suspend fun restoreItem(id: String)

    @Query(
        """
        WITH RECURSIVE descendants(id) AS (
            SELECT id FROM workspace_items WHERE id = :id
            UNION
            SELECT w.id FROM workspace_items w INNER JOIN descendants d ON w.parentId = d.id
        )
        DELETE FROM workspace_items WHERE id IN (SELECT id FROM descendants)
        """
    )
    suspend fun deletePermanently(id: String)

    @Query("DELETE FROM workspace_items WHERE id IN (:ids)")
    suspend fun deletePermanently(ids: List<String>)

    @Query("SELECT COUNT(*) FROM workspace_items")
    suspend fun countAll(): Int
}
