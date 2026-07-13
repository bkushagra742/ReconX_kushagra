package com.kushagra.reconx.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.ui.components.StatusPill
import com.kushagra.reconx.viewmodel.WebsiteSecurityViewModel

/**
 * WebsiteSecurityScreen.kt
 * ==========================
 * HTTP header analyzer, security-header checklist, CORS/cookie flags,
 * technology fingerprinting, TLS certificate viewer, robots.txt /
 * security.txt / sitemap detection, redirect chain, response time, and
 * allowed HTTP methods -- all in one analysis run against a URL the user
 * supplies.
 */
@Composable
fun WebsiteSecurityScreen(viewModel: WebsiteSecurityViewModel) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Website Security Analyzer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.url, onValueChange = viewModel::setUrl,
                        label = { Text("URL (e.g. https://example.com)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = viewModel::analyze, modifier = Modifier.fillMaxWidth()) { Text("Analyze") }
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        state.headerResult?.let { result ->
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Response Overview", fontWeight = FontWeight.SemiBold)
                        Text("Status: ${result.statusCode}   Response time: ${result.responseTimeMs} ms", style = MaterialTheme.typography.bodySmall)
                        if (result.redirectChain.size > 1) {
                            Text("Redirect chain:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            result.redirectChain.forEach { Text("→ $it", style = MaterialTheme.typography.bodySmall) }
                        }
                        if (state.allowedMethods.isNotEmpty()) {
                            Text("Allowed HTTP methods: ${state.allowedMethods.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (state.findings.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Security Headers", fontWeight = FontWeight.SemiBold)
                        state.findings.forEach { finding ->
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(finding.header, style = MaterialTheme.typography.bodySmall)
                                StatusPill(if (finding.present) "Present" else "Missing", positive = finding.present)
                            }
                        }
                    }
                }
            }
        }

        state.corsFinding?.let { cors ->
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("CORS Configuration", fontWeight = FontWeight.SemiBold)
                        Text(cors.recommendation, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (state.cookieFindings.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Cookie Security Flags", fontWeight = FontWeight.SemiBold)
                        state.cookieFindings.forEach { c ->
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(c.header, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                StatusPill(if (c.present) "OK" else "Weak", positive = c.present)
                            }
                        }
                    }
                }
            }
        }

        if (state.techFindings.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Technology Fingerprint", fontWeight = FontWeight.SemiBold)
                        state.techFindings.forEach { t ->
                            Text("${t.category}: ${t.name}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        state.certificateInfo?.let { cert ->
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("SSL/TLS Certificate", fontWeight = FontWeight.SemiBold)
                        Text("TLS version: ${cert.tlsVersion}", style = MaterialTheme.typography.bodySmall)
                        Text("Cipher suite: ${cert.cipherSuite}", style = MaterialTheme.typography.bodySmall)
                        Text("Subject: ${cert.subject}", style = MaterialTheme.typography.bodySmall)
                        Text("Issuer: ${cert.issuer}", style = MaterialTheme.typography.bodySmall)
                        Text("Valid: ${cert.validFrom} → ${cert.validTo} (${cert.daysUntilExpiry} days left)", style = MaterialTheme.typography.bodySmall)
                        if (cert.isExpired) StatusPill("Expired", positive = false)
                        if (cert.isSelfSigned) StatusPill("Self-signed", positive = false)
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("robots.txt / security.txt / sitemap.xml", fontWeight = FontWeight.SemiBold)
                    Text("robots.txt: ${if (state.robotsTxt != null) "Found" else "Not found"}", style = MaterialTheme.typography.bodySmall)
                    Text("security.txt: ${if (state.securityTxt != null) "Found" else "Not found"}", style = MaterialTheme.typography.bodySmall)
                    Text("sitemap.xml: ${if (state.sitemapFound == true) "Found" else "Not found"}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
