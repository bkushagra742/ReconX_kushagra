package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import com.kushagra.reconx.models.PasswordStrengthResult
import com.kushagra.reconx.utils.PasswordStrengthUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PasswordToolsUiState(
    val password: String = "",
    val result: PasswordStrengthResult = PasswordStrengthResult(0, "Empty", 0.0, emptyList()),
    val policyMinLength: Int = 12,
    val policyRequireUpper: Boolean = true,
    val policyRequireDigit: Boolean = true,
    val policyRequireSymbol: Boolean = true,
    val policyViolations: List<String> = emptyList(),
)

/**
 * Password Utilities: offline-only strength/entropy meter and policy
 * checker. No cracking or brute-force logic of any kind is present here.
 */
class PasswordToolsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordToolsUiState())
    val uiState: StateFlow<PasswordToolsUiState> = _uiState.asStateFlow()

    fun setPassword(password: String) {
        val result = PasswordStrengthUtils.analyze(password)
        val s = _uiState.value
        val violations = buildList {
            if (password.length < s.policyMinLength) add("Must be at least ${s.policyMinLength} characters.")
            if (s.policyRequireUpper && password.none { it.isUpperCase() }) add("Must include an uppercase letter.")
            if (s.policyRequireDigit && password.none { it.isDigit() }) add("Must include a digit.")
            if (s.policyRequireSymbol && password.none { !it.isLetterOrDigit() }) add("Must include a symbol.")
        }
        _uiState.value = s.copy(password = password, result = result, policyViolations = violations)
    }

    fun setPolicy(minLength: Int, requireUpper: Boolean, requireDigit: Boolean, requireSymbol: Boolean) {
        _uiState.value = _uiState.value.copy(
            policyMinLength = minLength, policyRequireUpper = requireUpper,
            policyRequireDigit = requireDigit, policyRequireSymbol = requireSymbol,
        )
        setPassword(_uiState.value.password) // re-evaluate against new policy
    }
}
