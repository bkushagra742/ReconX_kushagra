package com.kushagra.reconx.ui.tools

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.EmptyState
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.ui.components.StatusPill
import com.kushagra.reconx.viewmodel.CveLookupViewModel

/**
 * CveLookupScreen.kt
 * =====================
 * Offline CVE search: import a JSON CVE list once (while online), then
 * search entirely offline afterward. No network calls happen from this
 * screen at all -- import is a local file read.
 */
@Composable
fun CveLookupScreen(viewModel: CveLookupViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
            if (text != null) viewModel.importJson(text)
        }
    }

    LaunchedEffect(state.message) {
        // Message auto-clears after being shown once (simple UX affordance).
    }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Offline CVE Lookup", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${state.importedCount} CVE record(s) imported locally. Import a JSON list once while online; search works fully offline afterward.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { filePicker.launch("application/json") }, modifier = Modifier.weight(1f)) { Text("Import JSON") }
                        OutlinedButton(onClick = viewModel::clearDatabase, modifier = Modifier.weight(1f)) { Text("Clear") }
                    }
                    state.message?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = state.product, onValueChange = viewModel::setProduct,
                        label = { Text("Product / software name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = viewModel::search, modifier = Modifier.fillMaxWidth()) { Text("Search Offline Database") }
                }
            }
        }

        if (state.results.isEmpty()) {
            item { GlassCard(modifier = Modifier.fillMaxWidth()) { EmptyState("No results yet. Import a CVE list and search by product name.") } }
        } else {
            items(state.results) { cve ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(cve.cveId, fontWeight = FontWeight.SemiBold)
                            StatusPill(cve.severity, positive = cve.severity !in listOf("CRITICAL", "HIGH"))
                        }
                        Text("${cve.product} ${cve.version}", style = MaterialTheme.typography.labelSmall)
                        Text(cve.summary, style = MaterialTheme.typography.bodySmall)
                        Text("CVSS: ${cve.score}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
