package com.kushagra.reconx.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.EmptyState
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.viewmodel.ExportEvent
import com.kushagra.reconx.viewmodel.ReportsViewModel

/**
 * ReportsScreen.kt
 * ===================
 * Generate a professional report (Markdown / TXT / JSON / PDF) for a
 * chosen project, including findings, saved queries, notes, analyst name,
 * and date -- then share it via the system share sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel) {
    val projects by viewModel.projects.collectAsState()
    val lastExport by viewModel.lastExport.collectAsState()
    val context = LocalContext.current

    var selectedProjectIndex by remember { mutableStateOf(0) }
    var findingsSummary by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("MARKDOWN") }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(lastExport) {
        val event = lastExport
        if (event is ExportEvent.Success) {
            context.startActivity(
                com.kushagra.reconx.export.FileExportManager(context).shareIntent(event.file),
            )
            viewModel.clearEvent()
        }
    }

    if (projects.isEmpty()) {
        EmptyState("Create a project first, then come back here to export a report.", modifier = Modifier.fillMaxWidth())
        return
    }

    val selectedProject = projects[selectedProjectIndex.coerceIn(0, projects.lastIndex)]

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Generate Report", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = selectedProject.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Project") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().let { it },
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            projects.forEachIndexed { i, p ->
                                DropdownMenuItem(text = { Text(p.name) }, onClick = { selectedProjectIndex = i; expanded = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = findingsSummary, onValueChange = { findingsSummary = it },
                        label = { Text("Findings summary (optional)") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                    )

                    Text("Format", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("MARKDOWN", "JSON", "TXT", "PDF").forEach { f ->
                            FilterChip(selected = format == f, onClick = { format = f }, label = { Text(f) })
                        }
                    }

                    Button(
                        onClick = { viewModel.export(selectedProject, findingsSummary, format) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Export & Share") }
                }
            }
        }

        if (lastExport is ExportEvent.Failure) {
            item {
                Text(
                    (lastExport as ExportEvent.Failure).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
