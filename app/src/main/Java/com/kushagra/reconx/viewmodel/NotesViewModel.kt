package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.database.entity.NoteEntity
import com.kushagra.reconx.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    val notes: StateFlow<List<NoteEntity>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFilter = MutableStateFlow("All") // All / Favorites / a category name
    val selectedFilter: StateFlow<String> = _selectedFilter

    fun setFilter(filter: String) { _selectedFilter.value = filter }

    fun saveNote(
        id: Long = 0,
        projectId: Long?,
        title: String,
        content: String,
        category: String,
        tags: String,
        isFavorite: Boolean,
    ) {
        viewModelScope.launch {
            repository.save(id, projectId, title, content, category, tags, isFavorite)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.delete(note) }
    }

    fun toggleFavorite(note: NoteEntity) {
        viewModelScope.launch {
            repository.save(
                note.id, note.projectId, note.title, note.contentMarkdown,
                note.category, note.tags, !note.isFavorite,
            )
        }
    }
}
