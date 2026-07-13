package com.kushagra.reconx.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pattern
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.GlassCard
import com.kushagra.reconx.ui.components.SectionHeader
import com.kushagra.reconx.ui.navigation.Destinations

data class ToolItem(val title: String, val subtitle: String, val icon: ImageVector, val route: String)
data class ToolCategory(val name: String, val tools: List<ToolItem>)

/**
 * ToolsHubScreen.kt
 * ===================
 * The "All Tools" grid: every OSINT/security-assessment/utility screen,
 * grouped by category, matching the reference design's Tools tab.
 */
val TOOL_CATEGORIES = listOf(
    ToolCategory(
        "Search",
        listOf(
            ToolItem("Global Search", "Search projects, queries & notes", Icons.Default.Search, Destinations.GLOBAL_SEARCH),
        ),
    ),
    ToolCategory(
        "OSINT & Recon",
        listOf(
            ToolItem("Google Dork Builder", "Advanced search operators", Icons.Default.Search, Destinations.dorkBuilder("GOOGLE")),
            ToolItem("Bing Dork Builder", "Bing search operators", Icons.Default.Search, Destinations.dorkBuilder("BING")),
            ToolItem("GitHub Search Builder", "Code & secret search", Icons.Default.Code, Destinations.dorkBuilder("GITHUB")),
            ToolItem("Shodan Query Builder", "Device/service search", Icons.Default.Public, Destinations.dorkBuilder("SHODAN")),
            ToolItem("Censys Query Builder", "Host/certificate search", Icons.Default.Public, Destinations.dorkBuilder("CENSYS")),
            ToolItem("Search History", "Past tool runs", Icons.Default.Search, Destinations.SEARCH_HISTORY),
        ),
    ),
    ToolCategory(
        "Domain & Website",
        listOf(
            ToolItem("Domain Intelligence", "WHOIS + full DNS suite", Icons.Default.Dns, Destinations.DOMAIN_INTEL),
            ToolItem("Website Security", "Headers, TLS, robots.txt", Icons.Default.Shield, Destinations.WEBSITE_SECURITY),
            ToolItem("IP Intelligence", "GeoIP, ASN, reverse DNS", Icons.Default.Language, Destinations.IP_INTEL),
        ),
    ),
    ToolCategory(
        "Security Utilities",
        listOf(
            ToolItem("Hash Tools", "MD5 / SHA1 / SHA256 / SHA512", Icons.Default.Fingerprint, Destinations.HASH_TOOLS),
            ToolItem("Password Tools", "Strength & entropy meter", Icons.Default.Lock, Destinations.PASSWORD_TOOLS),
            ToolItem("Encoding Tools", "Base64 / URL / Hex / Binary", Icons.Default.Tag, Destinations.ENCODING_TOOLS),
            ToolItem("Regex Lab", "Live regex tester", Icons.Default.Pattern, Destinations.REGEX_LAB),
            ToolItem("Offline CVE Lookup", "Local vulnerability search", Icons.Default.Security, Destinations.CVE_LOOKUP),
            ToolItem("Checklists", "OWASP Top 10 & audit lists", Icons.Default.Shield, Destinations.CHECKLISTS),
        ),
    ),
    ToolCategory(
        "Reporting",
        listOf(
            ToolItem("Generate Report", "Markdown / JSON / TXT / PDF export", Icons.Default.Shield, Destinations.REPORTS),
        ),
    ),
)

@Composable
fun ToolsHubScreen(onToolClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TOOL_CATEGORIES.forEach { category ->
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                SectionHeader(category.name, modifier = Modifier.padding(top = 6.dp, bottom = 2.dp))
            }
            items(category.tools) { tool ->
                ToolCard(tool, onClick = { onToolClick(tool.route) })
            }
        }
    }
}

@Composable
private fun ToolCard(tool: ToolItem, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(tool.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
            }
            Text(tool.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(tool.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
