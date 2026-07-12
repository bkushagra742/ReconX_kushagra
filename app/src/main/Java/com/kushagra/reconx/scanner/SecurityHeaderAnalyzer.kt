package com.kushagra.reconx.scanner

import com.kushagra.reconx.models.SecurityHeaderFinding

/**
 * SecurityHeaderAnalyzer.kt
 * ===========================
 * Evaluates a map of HTTP response headers against the standard set of
 * security headers (same checklist used by securityheaders.com / Mozilla
 * Observatory) and returns present/missing findings with a short
 * remediation note for each -- purely defensive, read-only analysis.
 */
object SecurityHeaderAnalyzer {

    private val CHECKS = listOf(
        Triple("Strict-Transport-Security", "Enforces HTTPS for future visits (HSTS).", true),
        Triple("Content-Security-Policy", "Mitigates XSS/data-injection by restricting content sources.", true),
        Triple("X-Frame-Options", "Prevents clickjacking via iframe embedding.", true),
        Triple("X-Content-Type-Options", "Prevents MIME-sniffing (should be 'nosniff').", true),
        Triple("Referrer-Policy", "Controls how much referrer data is leaked cross-origin.", true),
        Triple("Permissions-Policy", "Restricts access to browser features/APIs.", true),
        Triple("X-XSS-Protection", "Legacy XSS filter header (superseded by CSP, still useful defense-in-depth).", false),
        Triple("Cross-Origin-Opener-Policy", "Isolates browsing context from cross-origin windows.", false),
        Triple("Cross-Origin-Resource-Policy", "Restricts which origins can load this resource.", false),
    )

    fun analyze(headers: Map<String, String>): List<SecurityHeaderFinding> {
        val normalized = headers.mapKeys { it.key.lowercase() }
        return CHECKS.map { (header, recommendation, _) ->
            val value = normalized[header.lowercase()]
            SecurityHeaderFinding(
                header = header,
                present = value != null,
                value = value,
                recommendation = if (value == null) "Missing -- $recommendation" else "Present.",
            )
        }
    }

    fun analyzeCookies(setCookieHeader: String?): List<SecurityHeaderFinding> {
        if (setCookieHeader.isNullOrBlank()) return emptyList()
        val cookies = setCookieHeader.split(Regex(",(?=[^;]+=[^;]+)"))
        return cookies.map { cookie ->
            val hasSecure = cookie.contains("Secure", ignoreCase = true)
            val hasHttpOnly = cookie.contains("HttpOnly", ignoreCase = true)
            val hasSameSite = cookie.contains("SameSite", ignoreCase = true)
            val name = cookie.substringBefore("=").trim()
            val missing = buildList {
                if (!hasSecure) add("Secure")
                if (!hasHttpOnly) add("HttpOnly")
                if (!hasSameSite) add("SameSite")
            }
            SecurityHeaderFinding(
                header = "Cookie: $name",
                present = missing.isEmpty(),
                value = cookie.trim(),
                recommendation = if (missing.isEmpty()) "All recommended flags present."
                else "Missing flag(s): ${missing.joinToString(", ")}",
            )
        }
    }

    fun analyzeCors(headers: Map<String, String>): SecurityHeaderFinding {
        val normalized = headers.mapKeys { it.key.lowercase() }
        val allowOrigin = normalized["access-control-allow-origin"]
        return when {
            allowOrigin == null -> SecurityHeaderFinding("CORS", false, null, "No CORS headers detected (cross-origin requests blocked by default).")
            allowOrigin == "*" -> SecurityHeaderFinding("CORS", true, allowOrigin, "Wildcard CORS -- any origin may read responses. Verify this is intentional.")
            else -> SecurityHeaderFinding("CORS", true, allowOrigin, "Restricted to a specific origin.")
        }
    }
}
