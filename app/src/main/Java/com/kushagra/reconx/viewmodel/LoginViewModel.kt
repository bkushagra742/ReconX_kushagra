package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Enter both username and password.")
            return
        }
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch {
            val ok = preferencesManager.verifyLogin(username.trim(), password)
            _uiState.value = if (ok) LoginUiState.Success
            else LoginUiState.Error("Invalid username or password.")
        }
    }

    fun resetError() {
        if (_uiState.value is LoginUiState.Error) _uiState.value = LoginUiState.Idle
    }
}
