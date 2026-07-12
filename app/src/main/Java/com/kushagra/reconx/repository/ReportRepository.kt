package com.kushagra.reconx.repository

import com.kushagra.reconx.database.dao.ReportDao
import com.kushagra.reconx.database.entity.ReportEntity
import com.kushagra.reconx.utils.DateUtils
import kotlinx.coroutines.flow.Flow

class ReportRepository(private val reportDao: ReportDao) {
    fun observeByProject(projectId: Long): Flow<List<ReportEntity>> = reportDao.observeByProject(projectId)
    fun observeAll(): Flow<List<ReportEntity>> = reportDao.observeAll()
    fun observeCount(): Flow<Int> = reportDao.observeCount()

    suspend fun record(projectId: Long, analystName: String, format: String, filePath: String): Long =
        reportDao.upsert(
            ReportEntity(
                projectId = projectId, analystName = analystName, format = format,
                filePath = filePath, createdAt = DateUtils.now(),
            )
        )

    suspend fun delete(report: ReportEntity) = reportDao.delete(report)
}
