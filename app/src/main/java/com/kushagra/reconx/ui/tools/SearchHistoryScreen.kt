package com.kushagra.reconx.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.repository.ActivityRepository
import com.kushagra.reconx.ui.components.EmptyState
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.utils.DateUtils

/**
 * SearchHistoryScreen.kt
 * =========================
 * A chronological log of every tool run (dork generation, WHOIS/DNS
 * lookups, hash operations, etc.) -- the mobile equivalent of the desktop
 * app's activity/history table.
 */
@Composable
fun SearchHistoryScreen(activityRepository: ActivityRepository) {
    val history by activityRepository.observeRecentHistory(200).collectAsState(initial = emptyList())

    if (history.isEmpty()) {
        EmptyState("No tool runs recorded yet.", modifier = Modifier.fillMaxWidth().padding(32.dp))
        return
    }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(history) { entry ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(entry.toolName, fontWeight = FontWeight.SemiBold)
                    Text(entry.inputSummary, style = MaterialTheme.typography.bodySmall)
                    Text(
                        DateUtils.relativeTime(entry.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
