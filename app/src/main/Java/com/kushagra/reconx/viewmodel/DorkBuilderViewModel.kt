package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.models.GeneratedDork
import com.kushagra.reconx.models.QueryEngine
import com.kushagra.reconx.repository.ActivityRepository
import com.kushagra.reconx.repository.QueryRepository
import com.kushagra.reconx.utils.DorkTemplates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DorkBuilderUiState(
    val engine: QueryEngine = QueryEngine.GOOGLE,
    val domain: String = "",
    val org: String = "",
    val keyword: String = "",
    val results: List<GeneratedDork> = emptyList(),
)

/** Powers the Google / Bing / GitHub / Shodan / Censys dork builder screens (one ViewModel, engine-parameterized). */
class DorkBuilderViewModel(
    private val queryRepository: QueryRepository,
    private val activityRepository: ActivityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DorkBuilderUiState())
    val uiState: StateFlow<DorkBuilderUiState> = _uiState.asStateFlow()

    fun setEngine(engine: QueryEngine) { _uiState.value = _uiState.value.copy(engine = engine) }
    fun setDomain(v: String) { _uiState.value = _uiState.value.copy(domain = v) }
    fun setOrg(v: String) { _uiState.value = _uiState.value.copy(org = v) }
    fun setKeyword(v: String) { _uiState.value = _uiState.value.copy(keyword = v) }

    fun generate() {
        val s = _uiState.value
        val results = DorkTemplates.generate(DorkTemplates.DEFAULTS, s.domain, s.org, s.keyword, s.engine)
        _uiState.value = s.copy(results = results)
        viewModelScope.launch {
            activityRepository.logToolRun(
                "${s.engine.name} Dork Builder",
                "domain=${s.domain.ifBlank { "-" }} org=${s.org.ifBlank { "-" }} keyword=${s.keyword.ifBlank { "-" }}",
            )
        }
    }

    fun saveToProject(dork: GeneratedDork, projectId: Long?, tags: String = "") {
        viewModelScope.launch { queryRepository.save(dork, projectId, tags) }
    }
}
