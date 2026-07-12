package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.database.entity.ProjectEntity
import com.kushagra.reconx.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectsViewModel(private val repository: ProjectRepository) : ViewModel() {

    val projects: StateFlow<List<ProjectEntity>> = repository.observeProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchChanged(query: String) { _searchQuery.value = query }

    fun createProject(name: String, description: String, domain: String, entity: String, keyword: String) {
        viewModelScope.launch {
            repository.createProject(name, description, domain, entity, keyword)
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch { repository.deleteProject(project) }
    }
}
