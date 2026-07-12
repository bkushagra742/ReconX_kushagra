package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import com.kushagra.reconx.utils.EncodingUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class EncodingType { BASE64, URL, HEX, BINARY, UNICODE }

data class EncodingToolsUiState(
    val type: EncodingType = EncodingType.BASE64,
    val input: String = "",
    val output: String = "",
    val error: String? = null,
)

/** Encoding Tools: Base64 / URL / Hex / Binary / Unicode, both directions, fully offline. */
class EncodingToolsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EncodingToolsUiState())
    val uiState: StateFlow<EncodingToolsUiState> = _uiState.asStateFlow()

    fun setType(type: EncodingType) { _uiState.value = _uiState.value.copy(type = type, output = "", error = null) }

    fun setInput(input: String) { _uiState.value = _uiState.value.copy(input = input) }

    fun encode() {
        val input = _uiState.value.input
        val output = when (_uiState.value.type) {
            EncodingType.BASE64 -> EncodingUtils.base64Encode(input)
            EncodingType.URL -> EncodingUtils.urlEncode(input)
            EncodingType.HEX -> EncodingUtils.hexEncode(input)
            EncodingType.BINARY -> EncodingUtils.binaryEncode(input)
            EncodingType.UNICODE -> EncodingUtils.unicodeEscape(input)
        }
        _uiState.value = _uiState.value.copy(output = output, error = null)
    }

    fun decode() {
        val input = _uiState.value.input
        val result = when (_uiState.value.type) {
            EncodingType.BASE64 -> EncodingUtils.base64Decode(input)
            EncodingType.URL -> EncodingUtils.urlDecode(input)
            EncodingType.HEX -> EncodingUtils.hexDecode(input)
            EncodingType.BINARY -> EncodingUtils.binaryDecode(input)
            EncodingType.UNICODE -> EncodingUtils.unicodeUnescape(input)
        }
        result.fold(
            onSuccess = { _uiState.value = _uiState.value.copy(output = it, error = null) },
            onFailure = { _uiState.value = _uiState.value.copy(output = "", error = "Could not decode: ${it.message}") },
        )
    }
}
