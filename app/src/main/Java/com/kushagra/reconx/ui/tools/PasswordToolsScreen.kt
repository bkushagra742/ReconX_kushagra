package com.kushagra.reconx.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.viewmodel.PasswordToolsViewModel

/**
 * PasswordToolsScreen.kt
 * =========================
 * Offline password strength meter, entropy estimate, and policy checker.
 * Contains no cracking/brute-force functionality.
 */
@Composable
fun PasswordToolsScreen(viewModel: PasswordToolsViewModel) {
    val state by viewModel.uiState.collectAsState()

    val strengthColor = when (state.result.label) {
        "Very Weak" -> Color(0xFFEB5757)
        "Weak" -> Color(0xFFF2994A)
        "Fair" -> Color(0xFFF2C94C)
        "Strong" -> Color(0xFF6FCF97)
        "Very Strong" -> Color(0xFF27AE60)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Password Strength Meter", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::setPassword,
                        label = { Text("Password to analyze") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    LinearProgressIndicator(
                        progress = { (state.result.score / 100f).coerceIn(0f, 1f) },
                        color = strengthColor,
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                    )
                    Text(state.result.label, color = strengthColor, fontWeight = FontWeight.SemiBold)
                    Text("Entropy: %.1f bits".format(state.result.entropyBits), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (state.result.suggestions.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Suggestions", fontWeight = FontWeight.SemiBold)
                        state.result.suggestions.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }

        if (state.policyViolations.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Policy Check (min ${state.policyMinLength} chars, upper+digit+symbol)", fontWeight = FontWeight.SemiBold)
                        state.policyViolations.forEach {
                            Text("✗ $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        } else if (state.password.isNotEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("✓ Meets the configured password policy.", color = Color(0xFF27AE60))
                }
            }
        }
    }
}
