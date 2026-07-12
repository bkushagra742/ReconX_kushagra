package com.kushagra.reconx.ui.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.models.QueryEngine
import com.kushagra.reconx.ui.components.EmptyState
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.viewmodel.DorkBuilderViewModel

/**
 * DorkBuilderScreen.kt
 * ======================
 * One shared screen implementation used for all five engines (Google,
 * Bing, GitHub, Shodan, Censys) -- the [engine] parameter both filters the
 * template library and labels the screen, avoiding five near-duplicate
 * files.
 */
@Composable
fun DorkBuilderScreen(engine: QueryEngine, viewModel: DorkBuilderViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(engine) { viewModel.setEngine(engine) }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${engine.name.lowercase().replaceFirstChar { it.uppercase() }} Dork Builder", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.domain, onValueChange = viewModel::setDomain,
                        label = { Text("Domain (e.g. example.com)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.org, onValueChange = viewModel::setOrg,
                        label = { Text("Organization / entity name") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.keyword, onValueChange = viewModel::setKeyword,
                        label = { Text("Keyword") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = viewModel::generate, modifier = Modifier.fillMaxWidth()) {
                        Text("Generate Queries")
                    }
                }
            }
        }

        if (state.results.isEmpty()) {
            item { GlassCard(modifier = Modifier.fillMaxWidth()) { EmptyState("Enter at least one field and generate queries.") } }
        } else {
            items(state.results) { dork ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(dork.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(dork.title, fontWeight = FontWeight.SemiBold)
                        Text(dork.query, style = MaterialTheme.typography.bodySmall, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { copyToClipboard(context, dork.query) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                            IconButton(onClick = { shareText(context, dork.query) }) {
                                Icon(Icons.Default.Share, contentDescription = "Share")
                            }
                            IconButton(onClick = { viewModel.saveToProject(dork, projectId = null) }) {
                                Icon(Icons.Default.Save, contentDescription = "Save to library")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("query", text))
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share query"))
}
