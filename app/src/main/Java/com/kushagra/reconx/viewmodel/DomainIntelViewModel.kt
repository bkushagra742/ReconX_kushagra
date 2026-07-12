package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.models.DnsRecordResult
import com.kushagra.reconx.models.WhoisResult
import com.kushagra.reconx.network.DnsClient
import com.kushagra.reconx.network.WhoisClient
import com.kushagra.reconx.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DomainIntelUiState(
    val domain: String = "",
    val isLoadingWhois: Boolean = false,
    val isLoadingDns: Boolean = false,
    val whois: WhoisResult? = null,
    val dnsRecords: Map<String, List<DnsRecordResult>> = emptyMap(),
    val error: String? = null,
    val spf: String? = null,
    val dmarc: String? = null,
)

/** Domain Intelligence: WHOIS + full DNS record suite (A/AAAA/MX/TXT/NS/SOA/CNAME) + SPF/DMARC extraction. */
class DomainIntelViewModel(private val activityRepository: ActivityRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DomainIntelUiState())
    val uiState: StateFlow<DomainIntelUiState> = _uiState.asStateFlow()

    fun setDomain(v: String) { _uiState.value = _uiState.value.copy(domain = v) }

    fun runWhois() {
        val domain = _uiState.value.domain.trim()
        if (domain.isEmpty()) return
        _uiState.value = _uiState.value.copy(isLoadingWhois = true, error = null)
        viewModelScope.launch {
            WhoisClient.lookup(domain).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(whois = it, isLoadingWhois = false) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message, isLoadingWhois = false) },
            )
            activityRepository.logToolRun("WHOIS Lookup", domain)
        }
    }

    fun runDnsLookup() {
        val domain = _uiState.value.domain.trim()
        if (domain.isEmpty()) return
        _uiState.value = _uiState.value.copy(isLoadingDns = true, error = null)
        viewModelScope.launch {
            val records = DnsClient.queryAllCommonTypes(domain)
            val spf = records["TXT"]?.firstOrNull { it.value.startsWith("v=spf1", true) }?.value
            val dmarcRecords = DnsClient.query("_dmarc.$domain", "TXT").getOrNull().orEmpty()
            val dmarc = dmarcRecords.firstOrNull { it.value.startsWith("v=DMARC1", true) }?.value
            _uiState.value = _uiState.value.copy(
                dnsRecords = records, isLoadingDns = false, spf = spf, dmarc = dmarc,
            )
            activityRepository.logToolRun("DNS Lookup", domain)
        }
    }
}
