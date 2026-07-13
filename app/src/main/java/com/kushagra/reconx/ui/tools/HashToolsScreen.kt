package com.kushagra.reconx.ui.tools

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.ui.components.StatusPill
import com.kushagra.reconx.viewmodel.HashToolsViewModel

/** HashToolsScreen.kt -- MD5/SHA1/SHA256/SHA512 for text and files, plus hash comparison. */
@Composable
fun HashToolsScreen(viewModel: HashToolsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val name = queryFileName(uri, context) ?: "selected file"
            viewModel.hashFile(context.contentResolver, uri, name)
        }
    }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Text Hash", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.inputText, onValueChange = viewModel::setInputText,
                        label = { Text("Text to hash") }, modifier = Modifier.fillMaxWidth(),
                    )
                    state.textResults.forEach { r ->
                        Column {
                            Text(r.algorithm, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(r.hash, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("File Hash", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { filePicker.launch("*/*") }, modifier = Modifier.fillMaxWidth()) { Text("Choose File") }
                    if (state.isHashingFile) CircularProgressIndicator(modifier = Modifier.fillMaxWidth())
                    state.fileName?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    state.fileResults.forEach { r ->
                        Column {
                            Text(r.algorithm, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(r.hash, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Hash Compare / Integrity Check", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = state.compareA, onValueChange = viewModel::setCompareA, label = { Text("Hash A") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = state.compareB, onValueChange = viewModel::setCompareB, label = { Text("Hash B") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = viewModel::compare, modifier = Modifier.fillMaxWidth()) { Text("Compare") }
                    state.compareResult?.let { StatusPill(if (it) "Match" else "Mismatch", positive = it) }
                }
            }
        }
    }
}

private fun queryFileName(uri: Uri, context: android.content.Context): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null) ?: return null
    cursor.use {
        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst() && nameIndex >= 0) return it.getString(nameIndex)
    }
    return null
}
