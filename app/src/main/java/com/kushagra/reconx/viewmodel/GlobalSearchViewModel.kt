package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.database.entity.NoteEntity
import com.kushagra.reconx.database.entity.ProjectEntity
import com.kushagra.reconx.database.entity.QueryEntity
import com.kushagra.reconx.repository.NoteRepository
import com.kushagra.reconx.repository.ProjectRepository
import com.kushagra.reconx.repository.QueryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GlobalSearchUiState(
    val query: String = "",
    val projects: List<ProjectEntity> = emptyList(),
    val queries: List<QueryEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val isSearching: Boolean = false,
)

/**
 * GlobalSearchViewModel.kt
 * ===========================
 * Powers a single search box that fans out across Projects, Queries, and
 * Notes simultaneously -- the "search everything" requirement. Reports
 * don't have free-text content of their own (they're generated exports),
 * so they're indirectly covered via the project they belong to.
 */
class GlobalSearchViewModel(
    private val projectRepository: ProjectRepository,
    private val queryRepository: QueryRepository,
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GlobalSearchUiState())
    val uiState: StateFlow<GlobalSearchUiState> = _uiState.asStateFlow()

    fun setQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(projects = emptyList(), queries = emptyList(), notes = emptyList())
            return
        }
        _uiState.value = _uiState.value.copy(isSearching = true)
        viewModelScope.launch {
            val projects = projectRepository.search(query)
            val queries = queryRepository.search(query)
            val notes = noteRepository.search(query)
            _uiState.value = _uiState.value.copy(
                projects = projects, queries = queries, notes = notes, isSearching = false,
            )
        }
    }
}
