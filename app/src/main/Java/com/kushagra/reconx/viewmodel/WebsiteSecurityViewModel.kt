package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.models.CertificateInfo
import com.kushagra.reconx.models.HttpHeaderResult
import com.kushagra.reconx.models.SecurityHeaderFinding
import com.kushagra.reconx.network.HttpAnalysisClient
import com.kushagra.reconx.repository.ActivityRepository
import com.kushagra.reconx.scanner.SecurityHeaderAnalyzer
import com.kushagra.reconx.scanner.TechFingerprinter
import com.kushagra.reconx.scanner.TlsInspector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URI

data class WebsiteSecurityUiState(
    val url: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val headerResult: HttpHeaderResult? = null,
    val findings: List<SecurityHeaderFinding> = emptyList(),
    val corsFinding: SecurityHeaderFinding? = null,
    val cookieFindings: List<SecurityHeaderFinding> = emptyList(),
    val techFindings: List<TechFingerprinter.Finding> = emptyList(),
    val certificateInfo: CertificateInfo? = null,
    val robotsTxt: String? = null,
    val securityTxt: String? = null,
    val sitemapFound: Boolean? = null,
    val allowedMethods: List<String> = emptyList(),
)

/**
 * Website Security: fetches headers, follows redirects, analyzes security
 * headers/CORS/cookies, fingerprints technology, inspects the TLS
 * certificate, and checks robots.txt / security.txt / sitemap.xml.
 */
class WebsiteSecurityViewModel(private val activityRepository: ActivityRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WebsiteSecurityUiState())
    val uiState: StateFlow<WebsiteSecurityUiState> = _uiState.asStateFlow()

    fun setUrl(v: String) { _uiState.value = _uiState.value.copy(url = v) }

    fun analyze() {
        val input = _uiState.value.url.trim()
        if (input.isEmpty()) return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val normalized = if (input.startsWith("http")) input else "https://$input"
            val headerResult = HttpAnalysisClient.analyze(normalized)

            headerResult.fold(
                onSuccess = { result ->
                    val findings = SecurityHeaderAnalyzer.analyze(result.headers)
                    val cors = SecurityHeaderAnalyzer.analyzeCors(result.headers)
                    val cookies = SecurityHeaderAnalyzer.analyzeCookies(result.headers["Set-Cookie"])
                    val tech = TechFingerprinter.fingerprint(result.headers)

                    val host = runCatching { URI(result.url).host }.getOrNull() ?: input
                    val cert = TlsInspector.inspect(host).getOrNull()

                    val robots = HttpAnalysisClient.fetchTextResource(normalized, "/robots.txt").getOrNull()
                    val securityTxt = HttpAnalysisClient.fetchTextResource(normalized, "/.well-known/security.txt").getOrNull()
                    val sitemapFound = HttpAnalysisClient.fetchTextResource(normalized, "/sitemap.xml").isSuccess
                    val methods = HttpAnalysisClient.probeAllowedMethods(normalized).getOrNull().orEmpty()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        headerResult = result,
                        findings = findings,
                        corsFinding = cors,
                        cookieFindings = cookies,
                        techFindings = tech,
                        certificateInfo = cert,
                        robotsTxt = robots,
                        securityTxt = securityTxt,
                        sitemapFound = sitemapFound,
                        allowedMethods = methods,
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                },
            )
            activityRepository.logToolRun("Website Security Analysis", input)
        }
    }
}
