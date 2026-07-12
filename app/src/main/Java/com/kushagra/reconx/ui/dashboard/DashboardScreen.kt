package com.kushagra.reconx.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.EmptyState
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.ui.components.SectionHeader
import com.kushagra.reconx.ui.components.StatTile
import com.kushagra.reconx.utils.Constants
import com.kushagra.reconx.utils.DateUtils
import com.kushagra.reconx.viewmodel.DashboardViewModel

/**
 * DashboardScreen.kt
 * ====================
 * The landing screen: greeting, quick stats (Projects/Notes/Queries/
 * Reports), recent projects, favorite queries, and a recent-activity feed
 * -- the mobile equivalent of the desktop app's Dashboard tab plus the
 * reference mockups' "Good Evening, admin" home screen.
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenProject: (Long) -> Unit,
    onOpenTools: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column {
                Text("Good day,", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("admin", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(Constants.APP_MOTTO, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Quick Stats")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatTile("Projects", state.stats.projects.toString(),
                        { Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary) }, Modifier.weight(1f))
                    StatTile("Notes", state.stats.notes.toString(),
                        { Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary) }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    StatTile("Queries", state.stats.queries.toString(),
                        { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) }, Modifier.weight(1f))
                    StatTile("Reports", state.stats.reports.toString(),
                        { Icon(Icons.Default.Summarize, null, tint = MaterialTheme.colorScheme.primary) }, Modifier.weight(1f))
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Recent Projects", actionText = "View all", onActionClick = onOpenTools)
                if (state.recentProjects.isEmpty()) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) { EmptyState("No projects yet -- create one from the Projects tab.") }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.recentProjects.forEach { project ->
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenProject(project.id) }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column {
                                        Text(project.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Updated ${DateUtils.relativeTime(project.updatedAt)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Text(project.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Favorite Queries")
                if (state.favoriteQueries.isEmpty()) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) { EmptyState("Star a query in the Query Library to pin it here.") }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.favoriteQueries.forEach { q ->
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                                    Column {
                                        Text(q.title, fontWeight = FontWeight.Medium)
                                        Text(q.queryText, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader("Recent Activity")
                if (state.recentActivity.isEmpty()) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) { EmptyState("Your activity log will appear here.") }
                } else {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.recentActivity.forEach { activity ->
                                Column {
                                    Text(activity.action, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${activity.details}  ·  ${DateUtils.relativeTime(activity.timestamp)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
