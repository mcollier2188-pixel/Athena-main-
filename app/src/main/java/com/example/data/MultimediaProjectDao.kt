package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) for user-created multimedia projects.
 * Exposes reactive [Flow] queries for observing projects, category filtering,
 * and suspend CRUD operations.
 */
@Dao
interface MultimediaProjectDao {

    @Query("SELECT * FROM multimedia_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<MultimediaProjectEntity>>

    @Query("SELECT * FROM multimedia_projects WHERE mediaType = :type ORDER BY createdAt DESC")
    fun getProjectsByType(type: String): Flow<List<MultimediaProjectEntity>>

    @Query("SELECT * FROM multimedia_projects WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteProjects(): Flow<List<MultimediaProjectEntity>>

    @Query("SELECT * FROM multimedia_projects WHERE id = :id LIMIT 1")
    fun getProjectById(id: String): Flow<MultimediaProjectEntity?>

    @Query("SELECT * FROM multimedia_projects WHERE id = :id LIMIT 1")
    suspend fun findProjectById(id: String): MultimediaProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: MultimediaProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<MultimediaProjectEntity>)

    @Update
    suspend fun updateProject(project: MultimediaProjectEntity)

    @Delete
    suspend fun deleteProject(project: MultimediaProjectEntity)

    @Query("DELETE FROM multimedia_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    @Query("SELECT COUNT(*) FROM multimedia_projects")
    suspend fun getProjectCount(): Int
}
