package com.kushagra.reconx.repository

import com.kushagra.reconx.database.dao.ActivityDao
import com.kushagra.reconx.database.dao.QueryDao
import com.kushagra.reconx.database.entity.ActivityEntity
import com.kushagra.reconx.database.entity.QueryEntity
import com.kushagra.reconx.models.GeneratedDork
import com.kushagra.reconx.utils.DateUtils
import kotlinx.coroutines.flow.Flow

class QueryRepository(
    private val queryDao: QueryDao,
    private val activityDao: ActivityDao,
) {
    fun observeByProject(projectId: Long): Flow<List<QueryEntity>> = queryDao.observeByProject(projectId)
    fun observeFavorites(): Flow<List<QueryEntity>> = queryDao.observeFavorites()
    fun observeRecent(limit: Int = 50): Flow<List<QueryEntity>> = queryDao.observeRecent(limit)
    fun observeCount(): Flow<Int> = queryDao.observeCount()

    suspend fun save(dork: GeneratedDork, projectId: Long?, tags: String = ""): Long {
        val id = queryDao.upsert(
            QueryEntity(
                projectId = projectId,
                engine = dork.engine.name,
                category = dork.category,
                title = dork.title,
                queryText = dork.query,
                tags = tags,
                createdAt = DateUtils.now(),
            )
        )
        activityDao.insert(ActivityEntity(action = "Query saved", details = dork.title, timestamp = DateUtils.now()))
        return id
    }

    suspend fun toggleFavorite(query: QueryEntity) {
        queryDao.setFavorite(query.id, !query.isFavorite)
    }

    suspend fun delete(query: QueryEntity) = queryDao.delete(query)

    suspend fun search(text: String): List<QueryEntity> = queryDao.search(text)
}
