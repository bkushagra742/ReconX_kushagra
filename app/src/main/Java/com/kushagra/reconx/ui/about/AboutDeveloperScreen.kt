package com.kushagra.reconx.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.utils.Constants

/**
 * AboutDeveloperScreen.kt
 * ==========================
 * Dedicated developer profile screen: avatar, name, role, bio, and social
 * links, all sourced from utils/Constants.kt (per the "store all URLs in
 * one config file" requirement) rather than being hardcoded here.
 */
@Composable
fun AboutDeveloperScreen() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(36.dp))
                    }
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(Constants.DEVELOPER_NAME, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(Constants.DEVELOPER_ROLE, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(Constants.DEVELOPER_BIO, style = MaterialTheme.typography.bodyMedium)
            }
        }

        item { Text("Connect With Me", fontWeight = FontWeight.SemiBold) }

        item {
            SocialLinkCard(
                label = "GitHub", value = Constants.GITHUB_USERNAME, url = Constants.GITHUB_URL,
                onOpen = { openUrl(context, Constants.GITHUB_URL) },
                onCopy = { clipboard.setText(AnnotatedString(Constants.GITHUB_URL)) },
            )
        }
        item {
            SocialLinkCard(
                label = "Instagram", value = Constants.INSTAGRAM_USERNAME, url = Constants.INSTAGRAM_URL,
                onOpen = { openUrl(context, Constants.INSTAGRAM_URL) },
                onCopy = { clipboard.setText(AnnotatedString(Constants.INSTAGRAM_URL)) },
            )
        }
        item {
            SocialLinkCard(
                label = "LinkedIn", value = Constants.LINKEDIN_USERNAME, url = Constants.LINKEDIN_URL,
                onOpen = { openUrl(context, Constants.LINKEDIN_URL) },
                onCopy = { clipboard.setText(AnnotatedString(Constants.LINKEDIN_URL)) },
            )
        }

        item {
            androidx.compose.material3.OutlinedButton(
                onClick = { shareProfile(context) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Share Developer Profile")
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
private fun SocialLinkCard(label: String, value: String, url: String, onOpen: () -> Unit, onCopy: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(label, fontWeight = FontWeight.SemiBold)
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row {
                IconButton(onClick = onCopy) { Icon(Icons.Default.ContentCopy, contentDescription = "Copy link") }
                IconButton(onClick = onOpen) { Icon(Icons.Default.OpenInNew, contentDescription = "Open") }
            }
        }
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun shareProfile(context: android.content.Context) {
    val text = "Check out ${Constants.DEVELOPER_NAME}'s work:\nGitHub: ${Constants.GITHUB_URL}\nLinkedIn: ${Constants.LINKEDIN_URL}\nInstagram: ${Constants.INSTAGRAM_URL}"
    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
    context.startActivity(Intent.createChooser(intent, "Share developer profile"))
}
