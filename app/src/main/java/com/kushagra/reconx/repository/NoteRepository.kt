package com.kushagra.reconx.repository

import com.kushagra.reconx.database.dao.ActivityDao
import com.kushagra.reconx.database.dao.NoteDao
import com.kushagra.reconx.database.entity.ActivityEntity
import com.kushagra.reconx.database.entity.NoteEntity
import com.kushagra.reconx.utils.DateUtils
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao,
    private val activityDao: ActivityDao,
) {
    fun observeByProject(projectId: Long): Flow<List<NoteEntity>> = noteDao.observeByProject(projectId)
    fun observeAll(): Flow<List<NoteEntity>> = noteDao.observeAll()
    fun observeFavorites(): Flow<List<NoteEntity>> = noteDao.observeFavorites()
    fun observeCount(): Flow<Int> = noteDao.observeCount()

    suspend fun save(
        id: Long = 0,
        projectId: Long?,
        title: String,
        content: String,
        category: String = "General",
        tags: String = "",
        isFavorite: Boolean = false,
    ): Long {
        val now = DateUtils.now()
        val noteId = noteDao.upsert(
            NoteEntity(
                id = id, projectId = projectId, title = title, contentMarkdown = content,
                category = category, tags = tags, isFavorite = isFavorite,
                createdAt = now, updatedAt = now,
            )
        )
        activityDao.insert(ActivityEntity(action = "Note saved", details = title, timestamp = now))
        return noteId
    }

    suspend fun delete(note: NoteEntity) = noteDao.delete(note)

    suspend fun search(query: String): List<NoteEntity> = noteDao.search(query)
}
