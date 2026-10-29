package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repository pattern implementation for user-created multimedia projects.
 * Abstracts local SQLite Room queries and ensures thread-safe background execution.
 */
class MultimediaProjectRepository(
    private val dao: MultimediaProjectDao
) {
    val allProjects: Flow<List<MultimediaProjectEntity>> = dao.getAllProjects().flowOn(Dispatchers.IO)
    val favoriteProjects: Flow<List<MultimediaProjectEntity>> = dao.getFavoriteProjects().flowOn(Dispatchers.IO)

    fun getProjectsByType(type: String): Flow<List<MultimediaProjectEntity>> {
        return dao.getProjectsByType(type).flowOn(Dispatchers.IO)
    }

    fun getProjectById(id: String): Flow<MultimediaProjectEntity?> {
        return dao.getProjectById(id).flowOn(Dispatchers.IO)
    }

    suspend fun findProjectById(id: String): MultimediaProjectEntity? = withContext(Dispatchers.IO) {
        dao.findProjectById(id)
    }

    suspend fun saveProject(project: MultimediaProjectEntity) = withContext(Dispatchers.IO) {
        dao.insertProject(project)
    }

    suspend fun updateProject(project: MultimediaProjectEntity) = withContext(Dispatchers.IO) {
        dao.updateProject(project)
    }

    suspend fun deleteProject(project: MultimediaProjectEntity) = withContext(Dispatchers.IO) {
        dao.deleteProject(project)
    }

    suspend fun deleteProjectById(id: String) = withContext(Dispatchers.IO) {
        dao.deleteProjectById(id)
    }

    suspend fun getProjectCount(): Int = withContext(Dispatchers.IO) {
        dao.getProjectCount()
    }
}
