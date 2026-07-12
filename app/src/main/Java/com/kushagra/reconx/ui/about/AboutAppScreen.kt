package com.kushagra.reconx.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.utils.Constants

/**
 * AboutAppScreen.kt
 * ====================
 * Application info: name, version, build number, Android version,
 * database version, license, and open-source acknowledgements.
 */
@Composable
fun AboutAppScreen() {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(Constants.APP_NAME, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(Constants.APP_MOTTO, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow("Version", Constants.APP_VERSION_NAME)
                    InfoRow("Build Number", Constants.APP_VERSION_CODE.toString())
                    InfoRow("Android Version", android.os.Build.VERSION.RELEASE ?: "Unknown")
                    InfoRow("Database Version", Constants.DATABASE_VERSION.toString())
                    InfoRow("License", Constants.APP_LICENSE)
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Open Source Libraries", fontWeight = FontWeight.SemiBold)
                    listOf(
                        "Jetpack Compose & Material 3 -- Apache 2.0",
                        "AndroidX Room -- Apache 2.0",
                        "AndroidX DataStore -- Apache 2.0",
                        "AndroidX Security-Crypto -- Apache 2.0",
                        "AndroidX Biometric -- Apache 2.0",
                        "Kotlin Coroutines -- Apache 2.0",
                        "org.json (bundled with Android) -- public domain / Apache 2.0",
                    ).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        item {
            Text(
                Constants.COPYRIGHT,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
