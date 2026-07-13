package com.kushagra.reconx.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kushagra.reconx.database.entity.ActivityEntity
import com.kushagra.reconx.database.entity.CveEntity
import com.kushagra.reconx.database.entity.HistoryEntity
import com.kushagra.reconx.database.entity.NoteEntity
import com.kushagra.reconx.database.entity.ProjectEntity
import com.kushagra.reconx.database.entity.QueryEntity
import com.kushagra.reconx.database.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): ProjectEntity?

    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%' OR domain LIKE '%' || :query || '%'")
    suspend fun search(query: String): List<ProjectEntity>

    @Query("SELECT COUNT(*) FROM projects")
    fun observeCount(): Flow<Int>
}

@Dao
interface QueryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(query: QueryEntity): Long

    @Delete
    suspend fun delete(query: QueryEntity)

    @Query("SELECT * FROM queries WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun observeByProject(projectId: Long): Flow<List<QueryEntity>>

    @Query("SELECT * FROM queries WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<QueryEntity>>

    @Query("SELECT * FROM queries ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<QueryEntity>>

    @Query("SELECT * FROM queries WHERE title LIKE '%' || :q || '%' OR queryText LIKE '%' || :q || '%'")
    suspend fun search(q: String): List<QueryEntity>

    @Query("UPDATE queries SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    @Query("SELECT COUNT(*) FROM queries")
    fun observeCount(): Flow<Int>
}

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity): Long

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun observeByProject(projectId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun observeFavorites(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :q || '%' OR contentMarkdown LIKE '%' || :q || '%'")
    suspend fun search(q: String): List<NoteEntity>

    @Query("SELECT COUNT(*) FROM notes")
    fun observeCount(): Flow<Int>
}

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(report: ReportEntity): Long

    @Delete
    suspend fun delete(report: ReportEntity)

    @Query("SELECT * FROM reports WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun observeByProject(projectId: Long): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ReportEntity>>

    @Query("SELECT COUNT(*) FROM reports")
    fun observeCount(): Flow<Int>
}

@Dao
interface ActivityDao {
    @Insert
    suspend fun insert(activity: ActivityEntity)

    @Query("SELECT * FROM activity_log ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 30): Flow<List<ActivityEntity>>
}

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(history: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<HistoryEntity>>

    @Query("DELETE FROM history")
    suspend fun clearAll()
}

@Dao
interface CveDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<CveEntity>)

    @Query("SELECT * FROM cve_entries WHERE product LIKE '%' || :product || '%'")
    suspend fun findByProduct(product: String): List<CveEntity>

    @Query("SELECT COUNT(*) FROM cve_entries")
    suspend fun count(): Int

    @Query("DELETE FROM cve_entries")
    suspend fun clearAll()
}
