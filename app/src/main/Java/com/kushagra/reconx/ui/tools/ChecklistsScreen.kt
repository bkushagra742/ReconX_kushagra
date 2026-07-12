package com.kushagra.reconx.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kushagra.reconx.ui.components.GlassCard

/**
 * ChecklistsScreen.kt
 * ======================
 * Static, offline reference checklists (OWASP Top 10, a general security
 * audit checklist, a bug-bounty recon checklist, and an incident-response
 * checklist), each with tappable checkboxes tracked in-memory per session.
 */
private val OWASP_TOP_10_2021 = listOf(
    "A01: Broken Access Control",
    "A02: Cryptographic Failures",
    "A03: Injection",
    "A04: Insecure Design",
    "A05: Security Misconfiguration",
    "A06: Vulnerable and Outdated Components",
    "A07: Identification and Authentication Failures",
    "A08: Software and Data Integrity Failures",
    "A09: Security Logging and Monitoring Failures",
    "A10: Server-Side Request Forgery (SSRF)",
)

private val SECURITY_AUDIT_CHECKLIST = listOf(
    "Inventory all public-facing assets (domains, subdomains, IPs)",
    "Confirm HTTPS is enforced everywhere (HSTS present)",
    "Review authentication & session management",
    "Check for verbose error messages / stack traces",
    "Review CORS and security headers",
    "Verify TLS configuration and certificate validity",
    "Check for exposed admin/debug endpoints",
    "Review third-party dependencies for known CVEs",
    "Confirm logging & monitoring are in place",
    "Document and prioritize findings by risk",
)

private val BUG_BOUNTY_RECON_CHECKLIST = listOf(
    "Enumerate subdomains (passive + certificate transparency)",
    "Resolve subdomains to live hosts",
    "Fingerprint technology stack per host",
    "Run WHOIS + DNS record enumeration",
    "Check robots.txt / sitemap.xml for hidden paths",
    "Search GitHub/GitLab for leaked secrets referencing the target",
    "Review JS files for endpoints and API keys",
    "Check for exposed cloud storage buckets",
    "Test for common misconfigurations (CORS, headers, methods)",
    "Document scope compliance before any active testing",
)

private val INCIDENT_RESPONSE_CHECKLIST = listOf(
    "Identify and confirm the incident",
    "Contain affected systems",
    "Preserve evidence (logs, memory, disk images)",
    "Eradicate root cause",
    "Recover and restore services",
    "Notify stakeholders per policy/regulation",
    "Document timeline and actions taken",
    "Conduct post-incident review",
    "Update detection rules / playbooks",
)

private data class ChecklistDef(val title: String, val items: List<String>)

private val CHECKLISTS = listOf(
    ChecklistDef("OWASP Top 10 (2021)", OWASP_TOP_10_2021),
    ChecklistDef("Security Audit Checklist", SECURITY_AUDIT_CHECKLIST),
    ChecklistDef("Bug Bounty Recon Checklist", BUG_BOUNTY_RECON_CHECKLIST),
    ChecklistDef("Incident Response Checklist", INCIDENT_RESPONSE_CHECKLIST),
)

@Composable
fun ChecklistsScreen() {
    val checkedState = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        items(CHECKLISTS) { checklist ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(checklist.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    checklist.items.forEach { item ->
                        val key = "${checklist.title}:$item"
                        val checked = checkedState[key] ?: false
                        androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(checked = checked, onCheckedChange = { checkedState[key] = it })
                            Text(item, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
