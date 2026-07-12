package com.kushagra.reconx.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.kushagra.reconx.viewmodel.IpIntelViewModel

/** IpIntelScreen.kt -- hostname/IP resolution, reverse DNS, ASN/org/geolocation. */
@Composable
fun IpIntelScreen(viewModel: IpIntelViewModel) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("IP Intelligence", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.input, onValueChange = viewModel::setInput,
                        label = { Text("IP address or hostname") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = viewModel::lookup, modifier = Modifier.fillMaxWidth()) { Text("Look Up") }
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        if (state.resolvedIp != null) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Resolution", fontWeight = FontWeight.SemiBold)
                        InfoLine("IP Address", state.resolvedIp ?: "-")
                        InfoLine("Reverse DNS (PTR)", state.reverseDns ?: "Not found")
                    }
                }
            }
        }

        state.geoIp?.let { geo ->
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Geolocation & Organization", fontWeight = FontWeight.SemiBold)
                        InfoLine("Country", geo.country ?: "-")
                        InfoLine("Region", geo.region ?: "-")
                        InfoLine("City", geo.city ?: "-")
                        InfoLine("ISP", geo.isp ?: "-")
                        InfoLine("Organization", geo.org ?: "-")
                        InfoLine("ASN", geo.asn ?: "-")
                        if (geo.lat != null && geo.lon != null) InfoLine("Coordinates", "${geo.lat}, ${geo.lon}")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Text("$label: $value", style = MaterialTheme.typography.bodySmall)
}
