package com.kushagra.reconx.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.EmptyState
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.viewmodel.NotesViewModel

/**
 * NotesScreen.kt
 * =================
 * Markdown-capable research notes with categories, tags, favorites, and
 * search/filter -- offline only, backed by Room.
 */
@Composable
fun NotesScreen(viewModel: NotesViewModel, onOpenNote: (Long) -> Unit, onNewNote: () -> Unit) {
    val notes by viewModel.notes.collectAsState()
    val filter by viewModel.selectedFilter.collectAsState()

    val visibleNotes = when (filter) {
        "Favorites" -> notes.filter { it.isFavorite }
        "All" -> notes
        else -> notes.filter { it.category == filter }
    }
    val categories = listOf("All", "Favorites") + notes.map { it.category }.distinct()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onNewNote) { Icon(Icons.Default.Add, contentDescription = "New note") }
        },
    ) {
        Column {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(categories.distinct()) { cat ->
                    FilterChip(selected = filter == cat, onClick = { viewModel.setFilter(cat) }, label = { Text(cat) })
                }
            }

            if (visibleNotes.isEmpty()) {
                EmptyState("No notes here yet -- tap + to write one.")
            } else {
                LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visibleNotes) { note ->
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenNote(note.id) },
                        ) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    Text(note.title, fontWeight = FontWeight.SemiBold)
                                    Text(note.contentMarkdown, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                                    Text(note.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                if (note.isFavorite) {
                                    Icon(Icons.Default.Star, contentDescription = "Favorite", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
