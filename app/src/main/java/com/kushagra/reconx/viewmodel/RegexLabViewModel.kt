package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RegexMatch(val range: IntRange, val text: String, val groups: List<String>)

data class RegexLabUiState(
    val pattern: String = "",
    val testText: String = "",
    val replacement: String = "",
    val matches: List<RegexMatch> = emptyList(),
    val replacedPreview: String = "",
    val error: String? = null,
    val flagsIgnoreCase: Boolean = false,
    val flagsMultiline: Boolean = false,
)

/** Common, ready-to-use regex patterns for quick recon/validation tasks. */
object CommonRegexLibrary {
    val PATTERNS = mapOf(
        "Email address" to "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        "IPv4 address" to "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b",
        "URL" to "https?://[\\w./?=&%-]+",
        "Domain name" to "\\b[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+\\b",
        "MD5 hash" to "\\b[a-fA-F0-9]{32}\\b",
        "SHA-256 hash" to "\\b[a-fA-F0-9]{64}\\b",
        "MAC address" to "\\b([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}\\b",
        "Credit card (loose)" to "\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b",
        "Phone number (loose)" to "\\+?\\d{1,3}[- ]?\\(?\\d{2,4}\\)?[- ]?\\d{3,4}[- ]?\\d{3,4}",
    )
}

/** Regex Lab: a live regex tester with match highlighting and replace-preview. */
class RegexLabViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegexLabUiState())
    val uiState: StateFlow<RegexLabUiState> = _uiState.asStateFlow()

    fun setPattern(p: String) { _uiState.value = _uiState.value.copy(pattern = p); evaluate() }
    fun setTestText(t: String) { _uiState.value = _uiState.value.copy(testText = t); evaluate() }
    fun setReplacement(r: String) { _uiState.value = _uiState.value.copy(replacement = r); evaluate() }
    fun setIgnoreCase(v: Boolean) { _uiState.value = _uiState.value.copy(flagsIgnoreCase = v); evaluate() }
    fun setMultiline(v: Boolean) { _uiState.value = _uiState.value.copy(flagsMultiline = v); evaluate() }

    fun loadPreset(pattern: String) { setPattern(pattern) }

    private fun evaluate() {
        val s = _uiState.value
        if (s.pattern.isEmpty()) {
            _uiState.value = s.copy(matches = emptyList(), replacedPreview = s.testText, error = null)
            return
        }
        runCatching {
            val options = buildSet {
                if (s.flagsIgnoreCase) add(RegexOption.IGNORE_CASE)
                if (s.flagsMultiline) add(RegexOption.MULTILINE)
            }
            val regex = Regex(s.pattern, options)
            val matches = regex.findAll(s.testText).map {
                RegexMatch(it.range, it.value, it.groupValues.drop(1))
            }.toList()
            val replaced = if (s.replacement.isNotEmpty()) regex.replace(s.testText, s.replacement) else s.testText
            _uiState.value = s.copy(matches = matches, replacedPreview = replaced, error = null)
        }.onFailure {
            _uiState.value = s.copy(matches = emptyList(), error = "Invalid pattern: ${it.message}")
        }
    }
}
