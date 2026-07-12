package com.kushagra.reconx.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.EmptyState
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.viewmodel.GlobalSearchViewModel

/**
 * GlobalSearchScreen.kt
 * ========================
 * A single search box that searches across Projects, Saved Queries, and
 * Notes at once, grouped by type -- satisfies the "search across
 * everything" requirement without needing a separate full-text index.
 */
@Composable
fun GlobalSearchScreen(viewModel: GlobalSearchViewModel) {
    val state by viewModel.uiState.collectAsState()
    val hasAnyResults = state.projects.isNotEmpty() || state.queries.isNotEmpty() || state.notes.isNotEmpty()

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                label = { Text("Search projects, queries, notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.query.isBlank()) {
            item { EmptyState("Start typing to search across your entire workspace.") }
            return@LazyColumn
        }
        if (!hasAnyResults && !state.isSearching) {
            item { EmptyState("No matches found.") }
        }

        if (state.projects.isNotEmpty()) {
            item { Text("Projects", fontWeight = FontWeight.SemiBold) }
            items(state.projects) { p ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column { Text(p.name, fontWeight = FontWeight.Medium); Text(p.domain, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
        if (state.queries.isNotEmpty()) {
            item { Text("Queries", fontWeight = FontWeight.SemiBold) }
            items(state.queries) { q ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column { Text(q.title, fontWeight = FontWeight.Medium); Text(q.queryText, style = MaterialTheme.typography.bodySmall, maxLines = 1) }
                }
            }
        }
        if (state.notes.isNotEmpty()) {
            item { Text("Notes", fontWeight = FontWeight.SemiBold) }
            items(state.notes) { n ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column { Text(n.title, fontWeight = FontWeight.Medium); Text(n.contentMarkdown, style = MaterialTheme.typography.bodySmall, maxLines = 1) }
                }
            }
        }
    }
}
