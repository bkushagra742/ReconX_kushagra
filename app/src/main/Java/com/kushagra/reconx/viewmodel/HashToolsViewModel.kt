package com.kushagra.reconx.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.models.HashResult
import com.kushagra.reconx.repository.ActivityRepository
import com.kushagra.reconx.utils.HashUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HashToolsUiState(
    val inputText: String = "",
    val textResults: List<HashResult> = emptyList(),
    val fileName: String? = null,
    val fileResults: List<HashResult> = emptyList(),
    val compareA: String = "",
    val compareB: String = "",
    val compareResult: Boolean? = null,
    val isHashingFile: Boolean = false,
)

/** Hash Tools: MD5/SHA-1/SHA-256/SHA-512 for text and files, plus hash comparison for integrity checks. */
class HashToolsViewModel(private val activityRepository: ActivityRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HashToolsUiState())
    val uiState: StateFlow<HashToolsUiState> = _uiState.asStateFlow()

    fun setInputText(text: String) {
        val results = if (text.isEmpty()) emptyList() else HashUtils.hashTextAllAlgorithms(text)
        _uiState.value = _uiState.value.copy(inputText = text, textResults = results)
    }

    fun hashFile(resolver: ContentResolver, uri: Uri, displayName: String) {
        _uiState.value = _uiState.value.copy(isHashingFile = true, fileName = displayName)
        viewModelScope.launch {
            val results = withContext(Dispatchers.IO) {
                listOf("MD5", "SHA-1", "SHA-256", "SHA-512").map { algo ->
                    resolver.openInputStream(uri)?.use { stream ->
                        HashResult(algo, displayName, HashUtils.hashStream(stream, algo))
                    } ?: HashResult(algo, displayName, "error: could not open file")
                }
            }
            _uiState.value = _uiState.value.copy(fileResults = results, isHashingFile = false)
            activityRepository.logToolRun("File Hash", displayName)
        }
    }

    fun setCompareA(v: String) { _uiState.value = _uiState.value.copy(compareA = v, compareResult = null) }
    fun setCompareB(v: String) { _uiState.value = _uiState.value.copy(compareB = v, compareResult = null) }

    fun compare() {
        val s = _uiState.value
        _uiState.value = s.copy(compareResult = HashUtils.compare(s.compareA, s.compareB))
    }
}
