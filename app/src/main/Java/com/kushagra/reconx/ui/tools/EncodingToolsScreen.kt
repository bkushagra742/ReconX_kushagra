package com.kushagra.reconx.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import com.kushagra.reconx.viewmodel.EncodingToolsViewModel
import com.kushagra.reconx.viewmodel.EncodingType

/** EncodingToolsScreen.kt -- Base64 / URL / Hex / Binary / Unicode, encode & decode. */
@Composable
fun EncodingToolsScreen(viewModel: EncodingToolsViewModel) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Encoding Tools", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EncodingType.entries.forEach { type ->
                            FilterChip(
                                selected = state.type == type,
                                onClick = { viewModel.setType(type) },
                                label = { Text(type.name) },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.input, onValueChange = viewModel::setInput,
                        label = { Text("Input") }, modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = viewModel::encode, modifier = Modifier.weight(1f)) { Text("Encode") }
                        Button(onClick = viewModel::decode, modifier = Modifier.weight(1f)) { Text("Decode") }
                    }
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                    if (state.output.isNotEmpty()) {
                        Text("Output", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(state.output, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
