package com.kushagra.reconx.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.database.entity.NoteEntity
import com.kushagra.reconx.viewmodel.NotesViewModel

/**
 * NoteEditorScreen.kt
 * ======================
 * Create/edit a single Markdown research note. `existingNote` is null for
 * a brand-new note, otherwise pre-fills the form for editing.
 */
@Composable
fun NoteEditorScreen(
    viewModel: NotesViewModel,
    existingNote: NoteEntity?,
    projectId: Long?,
    onSaved: () -> Unit,
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.contentMarkdown ?: "") }
    var category by remember { mutableStateOf(existingNote?.category ?: "General") }
    var tags by remember { mutableStateOf(existingNote?.tags ?: "") }
    var favorite by remember { mutableStateOf(existingNote?.isFavorite ?: false) }

    LaunchedEffect(existingNote) {
        existingNote?.let {
            title = it.title; content = it.contentMarkdown; category = it.category
            tags = it.tags; favorite = it.isFavorite
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = tags, onValueChange = { tags = it }, label = { Text("Tags (comma-separated)") }, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = favorite, onCheckedChange = { favorite = it })
            Text("Mark as favorite")
        }
        OutlinedTextField(
            value = content, onValueChange = { content = it },
            label = { Text("Content (Markdown supported)") },
            modifier = Modifier.fillMaxWidth().height(260.dp),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            androidx.compose.material3.Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.saveNote(existingNote?.id ?: 0, projectId, title, content, category, tags, favorite)
                        onSaved()
                    }
                },
                modifier = Modifier.weight(1f),
            ) { Text("Save Note") }

            if (existingNote != null) {
                OutlinedButton(
                    onClick = { viewModel.deleteNote(existingNote); onSaved() },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("Delete")
                }
            }
        }
    }
}
