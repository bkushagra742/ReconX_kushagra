package com.kushagra.reconx.ui.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.EmptyState
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.viewmodel.ProjectDetailViewModel

/**
 * ProjectDetailScreen.kt
 * =========================
 * A tabbed view of one project's saved Queries, Notes, and Reports --
 * the mobile equivalent of the desktop app's per-project workspace tabs.
 */
@Composable
fun ProjectDetailScreen(projectId: Long, viewModel: ProjectDetailViewModel) {
    val state by viewModel.uiState.collectAsState()
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Queries", "Notes", "Reports")

    LaunchedEffect(projectId) { viewModel.load(projectId) }

    Column(Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { i, title ->
                Tab(selected = tabIndex == i, onClick = { tabIndex = i }, text = { Text(title) })
            }
        }

        when (tabIndex) {
            0 -> OverviewTab(state.project)
            1 -> QueriesTab(state.queries, onToggleFavorite = viewModel::toggleFavoriteQuery, onDelete = viewModel::deleteQuery)
            2 -> NotesTab(state.notes, onDelete = viewModel::deleteNote)
            3 -> ReportsTab(state.reports)
        }
    }
}

@Composable
private fun OverviewTab(project: com.kushagra.reconx.database.entity.ProjectEntity?) {
    if (project == null) { EmptyState("Loading..."); return }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(project.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    if (project.description.isNotBlank()) Text(project.description, style = MaterialTheme.typography.bodyMedium)
                    Text("Domain: ${project.domain.ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall)
                    Text("Entity: ${project.entity.ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall)
                    Text("Keyword: ${project.keyword.ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall)
                    Text("Status: ${project.status}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun QueriesTab(
    queries: List<com.kushagra.reconx.database.entity.QueryEntity>,
    onToggleFavorite: (com.kushagra.reconx.database.entity.QueryEntity) -> Unit,
    onDelete: (com.kushagra.reconx.database.entity.QueryEntity) -> Unit,
) {
    if (queries.isEmpty()) { EmptyState("No saved queries in this project yet."); return }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(queries) { q ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("[${q.engine}] ${q.title}", fontWeight = FontWeight.SemiBold)
                        Text(q.queryText, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    }
                    Column {
                        IconButton(onClick = { onToggleFavorite(q) }) {
                            Icon(if (q.isFavorite) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = "Favorite")
                        }
                        IconButton(onClick = { onDelete(q) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesTab(
    notes: List<com.kushagra.reconx.database.entity.NoteEntity>,
    onDelete: (com.kushagra.reconx.database.entity.NoteEntity) -> Unit,
) {
    if (notes.isEmpty()) { EmptyState("No notes in this project yet."); return }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(notes) { n ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(n.title, fontWeight = FontWeight.SemiBold)
                        Text(n.contentMarkdown, style = MaterialTheme.typography.bodySmall, maxLines = 3)
                    }
                    IconButton(onClick = { onDelete(n) }) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                }
            }
        }
    }
}

@Composable
private fun ReportsTab(reports: List<com.kushagra.reconx.database.entity.ReportEntity>) {
    if (reports.isEmpty()) { EmptyState("No reports exported for this project yet -- use the Reports tab."); return }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(reports) { r ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("${r.format} report", fontWeight = FontWeight.SemiBold)
                    Text(r.filePath, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
        }
    }
}
