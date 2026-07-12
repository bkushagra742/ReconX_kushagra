package com.kushagra.reconx.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
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
import com.kushagra.reconx.viewmodel.CommonRegexLibrary
import com.kushagra.reconx.viewmodel.RegexLabViewModel

/** RegexLabScreen.kt -- live regex tester with match highlighting, replace preview, and a common-pattern library. */
@Composable
fun RegexLabScreen(viewModel: RegexLabViewModel) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Regex Lab", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.pattern, onValueChange = viewModel::setPattern,
                        label = { Text("Pattern") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = state.flagsIgnoreCase, onCheckedChange = viewModel::setIgnoreCase)
                        Text("Ignore case")
                        Checkbox(checked = state.flagsMultiline, onCheckedChange = viewModel::setMultiline)
                        Text("Multiline")
                    }
                    OutlinedTextField(
                        value = state.testText, onValueChange = viewModel::setTestText,
                        label = { Text("Test text") }, modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = state.replacement, onValueChange = viewModel::setReplacement,
                        label = { Text("Replacement (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        item {
            Text("Common Patterns", fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CommonRegexLibrary.PATTERNS.entries.toList()) { (label, pattern) ->
                    AssistChip(onClick = { viewModel.loadPreset(pattern) }, label = { Text(label) })
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Matches (${state.matches.size})", fontWeight = FontWeight.SemiBold)
                    state.matches.forEachIndexed { i, m ->
                        Text("#${i + 1}: \"${m.text}\"", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (state.replacement.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Replace Preview", fontWeight = FontWeight.SemiBold)
                        Text(state.replacedPreview, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
