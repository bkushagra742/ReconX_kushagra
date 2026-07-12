package com.kushagra.reconx.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.viewmodel.DomainIntelViewModel

/**
 * DomainIntelScreen.kt
 * ======================
 * WHOIS Lookup + full DNS record suite (A/AAAA/MX/TXT/NS/SOA/CNAME) with
 * SPF/DMARC extraction from TXT records.
 */
@Composable
fun DomainIntelScreen(viewModel: DomainIntelViewModel) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Domain Intelligence", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.domain, onValueChange = viewModel::setDomain,
                        label = { Text("Domain (e.g. example.com)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = viewModel::runWhois, modifier = Modifier.weight(1f)) { Text("WHOIS Lookup") }
                        Button(onClick = viewModel::runDnsLookup, modifier = Modifier.weight(1f)) { Text("DNS Lookup") }
                    }
                    if (state.isLoadingWhois || state.isLoadingDns) {
                        CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        state.whois?.let { whois ->
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("WHOIS Result", fontWeight = FontWeight.SemiBold)
                        InfoRow("Registrar", whois.registrar ?: "-")
                        InfoRow("Created On", whois.createdOn ?: "-")
                        InfoRow("Expires On", whois.expiresOn ?: "-")
                        InfoRow("Name Servers", whois.nameServers.joinToString(", ").ifBlank { "-" })
                        InfoRow("Status", whois.status.joinToString(", ").ifBlank { "-" })
                    }
                }
            }
        }

        if (state.dnsRecords.isNotEmpty()) {
            item { Text("DNS Records", fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth()) }
            state.dnsRecords.forEach { (type, records) ->
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(type, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            records.forEach { r ->
                                Text(r.value, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Email Authentication", fontWeight = FontWeight.SemiBold)
                        InfoRow("SPF", state.spf ?: "Not found")
                        InfoRow("DMARC", state.dmarc ?: "Not found")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.End, modifier = Modifier.weight(1f, false))
    }
}
