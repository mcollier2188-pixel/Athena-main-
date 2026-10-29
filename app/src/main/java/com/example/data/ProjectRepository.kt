package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for offline user-defined projects and workspaces.
 */
class ProjectRepository(private val projectDao: ProjectDao) {

    val allActiveProjects: Flow<List<ProjectEntity>> = projectDao.getAllActiveProjects()

    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun getProjectById(id: String): ProjectEntity? = withContext(Dispatchers.IO) {
        projectDao.getProjectById(id)
    }

    suspend fun createProject(
        title: String,
        description: String = "",
        colorTagHex: String = "#00F0FF"
    ): ProjectEntity = withContext(Dispatchers.IO) {
        val project = ProjectEntity(
            title = title,
            description = description,
            colorTagHex = colorTagHex,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis()
        )
        projectDao.insertProject(project)
        project
    }

    suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project.copy(modifiedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.deleteProject(project)
    }

    suspend fun deleteProjectById(id: String) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectById(id)
    }
}
