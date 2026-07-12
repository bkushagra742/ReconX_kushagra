package com.kushagra.reconx.repository

import com.kushagra.reconx.database.dao.ActivityDao
import com.kushagra.reconx.database.dao.ProjectDao
import com.kushagra.reconx.database.entity.ActivityEntity
import com.kushagra.reconx.database.entity.ProjectEntity
import com.kushagra.reconx.utils.DateUtils
import kotlinx.coroutines.flow.Flow

/**
 * ProjectRepository.kt
 * =====================
 * Repository-pattern wrapper around ProjectDao. The ViewModel layer never
 * touches Room directly -- everything goes through a repository so the
 * data source could be swapped/extended (e.g. sync) without UI changes.
 */
class ProjectRepository(
    private val projectDao: ProjectDao,
    private val activityDao: ActivityDao,
) {
    fun observeProjects(): Flow<List<ProjectEntity>> = projectDao.observeAll()
    fun observeProjectCount(): Flow<Int> = projectDao.observeCount()

    suspend fun getProject(id: Long): ProjectEntity? = projectDao.getById(id)

    suspend fun createProject(
        name: String,
        description: String = "",
        domain: String = "",
        entity: String = "",
        keyword: String = "",
    ): Long {
        val now = DateUtils.now()
        val id = projectDao.upsert(
            ProjectEntity(
                name = name, description = description, domain = domain,
                entity = entity, keyword = keyword, createdAt = now, updatedAt = now,
            )
        )
        activityDao.insert(ActivityEntity(action = "Project created", details = name, timestamp = now))
        return id
    }

    suspend fun updateProject(project: ProjectEntity) {
        projectDao.update(project.copy(updatedAt = DateUtils.now()))
    }

    suspend fun deleteProject(project: ProjectEntity) {
        projectDao.delete(project)
        activityDao.insert(ActivityEntity(action = "Project deleted", details = project.name, timestamp = DateUtils.now()))
    }

    suspend fun search(query: String): List<ProjectEntity> = projectDao.search(query)
}
