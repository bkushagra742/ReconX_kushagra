package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.models.GeoIpResult
import com.kushagra.reconx.network.GeoIpClient
import com.kushagra.reconx.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IpIntelUiState(
    val input: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val resolvedIp: String? = null,
    val reverseDns: String? = null,
    val geoIp: GeoIpResult? = null,
)

/** IP Intelligence: hostname->IP resolution, reverse DNS (PTR), and ASN/org/geolocation lookup. */
class IpIntelViewModel(private val activityRepository: ActivityRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(IpIntelUiState())
    val uiState: StateFlow<IpIntelUiState> = _uiState.asStateFlow()

    fun setInput(v: String) { _uiState.value = _uiState.value.copy(input = v) }

    fun lookup() {
        val raw = _uiState.value.input.trim()
        if (raw.isEmpty()) return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val isIp = raw.matches(Regex("^\\d{1,3}(\\.\\d{1,3}){3}$"))
            val ipResult = if (isIp) Result.success(raw) else GeoIpClient.resolveHost(raw)

            ipResult.fold(
                onSuccess = { ip ->
                    val reverse = GeoIpClient.reverseDns(ip).getOrNull()
                    val geo = GeoIpClient.lookup(ip).getOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, resolvedIp = ip, reverseDns = reverse, geoIp = geo,
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) },
            )
            activityRepository.logToolRun("IP Intelligence", raw)
        }
    }
}
