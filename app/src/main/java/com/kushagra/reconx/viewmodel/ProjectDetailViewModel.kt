package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.database.entity.NoteEntity
import com.kushagra.reconx.database.entity.ProjectEntity
import com.kushagra.reconx.database.entity.QueryEntity
import com.kushagra.reconx.database.entity.ReportEntity
import com.kushagra.reconx.repository.NoteRepository
import com.kushagra.reconx.repository.ProjectRepository
import com.kushagra.reconx.repository.QueryRepository
import com.kushagra.reconx.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ProjectDetailViewModel.kt
 * ===========================
 * Drives the "Project Detail" screen: loads one project plus its live
 * streams of queries, notes, and reports (a mobile equivalent of the
 * desktop app's per-project Workspace tab).
 */
class ProjectDetailViewModel(
    private val projectRepository: ProjectRepository,
    private val queryRepository: QueryRepository,
    private val noteRepository: NoteRepository,
    private val reportRepository: ReportRepository,
) : ViewModel() {

    data class UiState(
        val project: ProjectEntity? = null,
        val queries: List<QueryEntity> = emptyList(),
        val notes: List<NoteEntity> = emptyList(),
        val reports: List<ReportEntity> = emptyList(),
    )

    private val _projectId = MutableStateFlow<Long?>(null)
    private val _project = MutableStateFlow<ProjectEntity?>(null)

    private val queriesFlow = _projectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else queryRepository.observeByProject(id)
    }
    private val notesFlow = _projectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else noteRepository.observeByProject(id)
    }
    private val reportsFlow = _projectId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else reportRepository.observeByProject(id)
    }

    val uiState: StateFlow<UiState> = combine(
        _project, queriesFlow, notesFlow, reportsFlow,
    ) { project, queries, notes, reports ->
        UiState(project, queries, notes, reports)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun load(projectId: Long) {
        _projectId.value = projectId
        viewModelScope.launch { _project.value = projectRepository.getProject(projectId) }
    }

    fun toggleFavoriteQuery(query: QueryEntity) {
        viewModelScope.launch { queryRepository.toggleFavorite(query) }
    }

    fun deleteQuery(query: QueryEntity) {
        viewModelScope.launch { queryRepository.delete(query) }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { noteRepository.delete(note) }
    }

    fun updateProjectDetails(name: String, description: String, domain: String, entity: String, keyword: String) {
        val current = _project.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                name = name, description = description, domain = domain,
                entity = entity, keyword = keyword,
            )
            projectRepository.updateProject(updated)
            _project.value = updated
        }
    }
}
