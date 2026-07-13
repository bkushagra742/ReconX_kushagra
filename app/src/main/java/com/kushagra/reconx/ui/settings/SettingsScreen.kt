package com.kushagra.reconx.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.viewmodel.SettingsViewModel

/**
 * SettingsScreen.kt
 * ====================
 * Appearance (theme), Security (biometric app-lock), Database
 * (backup/restore, storage usage), and links into About Application /
 * About Developer -- matching the reference design's Settings tab.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenAboutApp: () -> Unit,
    onOpenAboutDeveloper: () -> Unit,
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val biometricLock by viewModel.biometricLockEnabled.collectAsState()
    val analystName by viewModel.analystName.collectAsState()
    var backupMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Appearance", fontWeight = FontWeight.SemiBold)
                    listOf("system" to "Follow System", "light" to "Light Mode", "dark" to "Dark Mode").forEach { (value, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setThemeMode(value) },
                        ) {
                            RadioButton(selected = themeMode == value, onClick = { viewModel.setThemeMode(value) })
                            Text(label)
                        }
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Security", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Biometric app lock")
                            Text("Require fingerprint/face unlock in addition to login", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = biometricLock, onCheckedChange = { viewModel.setBiometricLock(it) })
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Report Defaults", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = analystName, onValueChange = { viewModel.setAnalystName(it) },
                        label = { Text("Analyst name (shown on reports)") }, modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Database", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        androidx.compose.material3.Button(
                            onClick = { backupMessage = "Backed up to: ${viewModel.backupDatabase().absolutePath}" },
                            modifier = Modifier.weight(1f),
                        ) { Text("Backup Now") }
                    }
                    backupMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    val usedMb = viewModel.getStorageUsedBytes() / (1024.0 * 1024.0)
                    Text("Local storage used: %.2f MB".format(usedMb), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item { Text("About", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp)) }

        item {
            SettingsRow(icon = Icons.Default.Info, title = "About Application", subtitle = "App info & updates", onClick = onOpenAboutApp)
        }
        item {
            SettingsRow(icon = Icons.Default.Person, title = "About Developer", subtitle = "Developer information", onClick = onOpenAboutDeveloper)
        }
    }
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Medium)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}
