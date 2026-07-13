package com.kushagra.reconx.ui.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.EmptyState
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.ui.components.StatusPill
import com.kushagra.reconx.utils.DateUtils
import com.kushagra.reconx.viewmodel.ProjectsViewModel

/**
 * ProjectsScreen.kt
 * ====================
 * Each project bundles queries, notes, reports, and (through the detail
 * screen) an evidence/timeline view -- the mobile counterpart of the
 * desktop app's per-project Workspace.
 */
@Composable
fun ProjectsScreen(viewModel: ProjectsViewModel, onOpenProject: (Long) -> Unit) {
    val projects by viewModel.projects.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "New project")
            }
        },
    ) { padding ->
        if (projects.isEmpty()) {
            EmptyState("No projects yet -- tap + to start your first investigation.", modifier = Modifier.fillMaxWidth().let { it })
        } else {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(projects) { project ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenProject(project.id) },
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(project.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Created ${DateUtils.formatForDisplay(project.createdAt)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (project.domain.isNotBlank()) {
                                    Text(project.domain, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            StatusPill(project.status, positive = project.status == "Active")
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, description, domain, entity, keyword ->
                viewModel.createProject(name, description, domain, entity, keyword)
                showCreateDialog = false
            },
        )
    }
}

@Composable
private fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }
    var entity by remember { mutableStateOf("") }
    var keyword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Project") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Project name") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = domain, onValueChange = { domain = it }, label = { Text("Domain (optional)") })
                OutlinedTextField(value = entity, onValueChange = { entity = it }, label = { Text("Entity / org (optional)") })
                OutlinedTextField(value = keyword, onValueChange = { keyword = it }, label = { Text("Keyword (optional)") })
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onCreate(name, description, domain, entity, keyword) }) {
                Text("Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
