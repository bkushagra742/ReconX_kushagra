package com.kushagra.reconx.repository

import com.kushagra.reconx.database.dao.ActivityDao
import com.kushagra.reconx.database.dao.HistoryDao
import com.kushagra.reconx.database.entity.ActivityEntity
import com.kushagra.reconx.database.entity.HistoryEntity
import com.kushagra.reconx.utils.DateUtils
import kotlinx.coroutines.flow.Flow

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val historyDao: HistoryDao,
) {
    fun observeRecentActivity(limit: Int = 30): Flow<List<ActivityEntity>> = activityDao.observeRecent(limit)
    fun observeRecentHistory(limit: Int = 100): Flow<List<HistoryEntity>> = historyDao.observeRecent(limit)

    suspend fun logActivity(action: String, details: String = "") =
        activityDao.insert(ActivityEntity(action = action, details = details, timestamp = DateUtils.now()))

    suspend fun logToolRun(toolName: String, inputSummary: String) =
        historyDao.insert(HistoryEntity(toolName = toolName, inputSummary = inputSummary, timestamp = DateUtils.now()))

    suspend fun clearHistory() = historyDao.clearAll()
}
