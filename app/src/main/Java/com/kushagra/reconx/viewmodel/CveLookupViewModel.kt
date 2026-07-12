package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.database.entity.CveEntity
import com.kushagra.reconx.repository.CveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CveLookupUiState(
    val product: String = "",
    val results: List<CveEntity> = emptyList(),
    val importedCount: Int = 0,
    val isSearching: Boolean = false,
    val message: String? = null,
)

/**
 * Offline CVE Lookup: the user imports a JSON CVE export once (while
 * online, from any source they trust, e.g. a filtered NVD feed), then
 * every subsequent search is a pure local Room query -- no network calls.
 */
class CveLookupViewModel(private val repository: CveRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CveLookupUiState())
    val uiState: StateFlow<CveLookupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(importedCount = repository.count())
        }
    }

    fun setProduct(v: String) { _uiState.value = _uiState.value.copy(product = v) }

    fun search() {
        val product = _uiState.value.product.trim()
        if (product.isEmpty()) return
        _uiState.value = _uiState.value.copy(isSearching = true)
        viewModelScope.launch {
            val results = repository.findByProduct(product)
            _uiState.value = _uiState.value.copy(results = results, isSearching = false)
        }
    }

    fun importJson(json: String) {
        viewModelScope.launch {
            runCatching { repository.importFromJson(json) }
                .onSuccess { count ->
                    _uiState.value = _uiState.value.copy(
                        importedCount = repository.count(),
                        message = "Imported $count CVE record(s).",
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(message = "Import failed: ${it.message}")
                }
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            repository.clear()
            _uiState.value = _uiState.value.copy(importedCount = 0, results = emptyList(), message = "Offline CVE database cleared.")
        }
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null) }
}
