package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.database.entity.ActivityEntity
import com.kushagra.reconx.database.entity.ProjectEntity
import com.kushagra.reconx.database.entity.QueryEntity
import com.kushagra.reconx.repository.ActivityRepository
import com.kushagra.reconx.repository.NoteRepository
import com.kushagra.reconx.repository.ProjectRepository
import com.kushagra.reconx.repository.QueryRepository
import com.kushagra.reconx.repository.ReportRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardStats(
    val projects: Int = 0,
    val notes: Int = 0,
    val queries: Int = 0,
    val reports: Int = 0,
)

data class DashboardUiState(
    val stats: DashboardStats = DashboardStats(),
    val recentProjects: List<ProjectEntity> = emptyList(),
    val favoriteQueries: List<QueryEntity> = emptyList(),
    val recentActivity: List<ActivityEntity> = emptyList(),
)

/**
 * DashboardViewModel.kt
 * =======================
 * Aggregates counts and recent items from every repository into a single
 * observable UI state for the Dashboard screen -- mirroring the desktop
 * app's `Database.get_stats()` + "Recent Activity" panel.
 */
class DashboardViewModel(
    projectRepository: ProjectRepository,
    queryRepository: QueryRepository,
    noteRepository: NoteRepository,
    reportRepository: ReportRepository,
    activityRepository: ActivityRepository,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        projectRepository.observeProjectCount(),
        noteRepository.observeCount(),
        queryRepository.observeCount(),
        reportRepository.observeCount(),
        projectRepository.observeProjects(),
        queryRepository.observeFavorites(),
        activityRepository.observeRecentActivity(10),
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val projectsCount = values[0] as Int
        val notesCount = values[1] as Int
        val queriesCount = values[2] as Int
        val reportsCount = values[3] as Int
        val projects = values[4] as List<ProjectEntity>
        val favQueries = values[5] as List<QueryEntity>
        val recentActivity = values[6] as List<ActivityEntity>

        DashboardUiState(
            stats = DashboardStats(projectsCount, notesCount, queriesCount, reportsCount),
            recentProjects = projects.take(5),
            favoriteQueries = favQueries.take(5),
            recentActivity = recentActivity,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
