package com.kushagra.reconx.scanner

/**
 * TechFingerprinter.kt
 * =====================
 * Lightweight, heuristic technology/CMS/framework/CDN fingerprinting based
 * on response headers -- the same general approach used by tools like
 * Wappalyzer, just header-based rather than shipping a large signature
 * database. Entirely passive: it only reads headers already returned by a
 * normal HTTP response.
 */
object TechFingerprinter {

    data class Finding(val category: String, val name: String, val evidence: String)

    fun fingerprint(headers: Map<String, String>): List<Finding> {
        val normalized = headers.mapKeys { it.key.lowercase() }
        val findings = mutableListOf<Finding>()

        normalized["server"]?.let { server ->
            findings.add(Finding("Web Server", server, "Server header"))
            when {
                server.contains("nginx", true) -> findings.add(Finding("Web Server", "nginx", "Server header"))
                server.contains("apache", true) -> findings.add(Finding("Web Server", "Apache", "Server header"))
                server.contains("cloudflare", true) -> findings.add(Finding("CDN", "Cloudflare", "Server header"))
                server.contains("iis", true) -> findings.add(Finding("Web Server", "Microsoft IIS", "Server header"))
                else -> {}
            }
        }

        normalized["x-powered-by"]?.let { poweredBy ->
            findings.add(Finding("Framework", poweredBy, "X-Powered-By header"))
        }

        if (normalized.containsKey("cf-ray") || normalized.containsKey("cf-cache-status")) {
            findings.add(Finding("CDN", "Cloudflare", "cf-ray / cf-cache-status header"))
        }
        if (normalized.containsKey("x-amz-cf-id")) findings.add(Finding("CDN", "Amazon CloudFront", "x-amz-cf-id header"))
        if (normalized.containsKey("x-akamai-transformed")) findings.add(Finding("CDN", "Akamai", "x-akamai-transformed header"))
        if (normalized.containsKey("x-vercel-id")) findings.add(Finding("Hosting/CDN", "Vercel", "x-vercel-id header"))
        if (normalized.containsKey("x-github-request-id")) findings.add(Finding("Hosting", "GitHub Pages", "x-github-request-id header"))

        normalized["set-cookie"]?.let { cookie ->
            when {
                cookie.contains("wordpress", true) -> findings.add(Finding("CMS", "WordPress", "Set-Cookie header"))
                cookie.contains("wp-", true) -> findings.add(Finding("CMS", "WordPress", "Set-Cookie header"))
                cookie.contains("PHPSESSID", true) -> findings.add(Finding("Language", "PHP", "Set-Cookie header"))
                cookie.contains("JSESSIONID", true) -> findings.add(Finding("Language", "Java (Servlet container)", "Set-Cookie header"))
                cookie.contains("laravel_session", true) -> findings.add(Finding("Framework", "Laravel", "Set-Cookie header"))
                cookie.contains("django", true) -> findings.add(Finding("Framework", "Django", "Set-Cookie header"))
            else -> {}
            }
        }

        normalized["link"]?.let { link ->
            if (link.contains("wp-json", true)) findings.add(Finding("CMS", "WordPress (REST API)", "Link header"))
        }

        return findings.distinct()
    }

    fun fingerprintBody(html: String): List<Finding> {
        val findings = mutableListOf<Finding>()
        val checks = listOf(
            "wp-content" to Finding("CMS", "WordPress", "HTML contains /wp-content/"),
            "Joomla!" to Finding("CMS", "Joomla", "HTML meta generator"),
            "Drupal.settings" to Finding("CMS", "Drupal", "Inline script reference"),
            "shopify" to Finding("CMS/Platform", "Shopify", "HTML reference"),
            "cdn.shopify.com" to Finding("CDN", "Shopify CDN", "Asset URL"),
            "react" to Finding("Framework", "React (possible)", "Bundle reference"),
            "ng-version" to Finding("Framework", "Angular", "ng-version attribute"),
            "__NEXT_DATA__" to Finding("Framework", "Next.js", "__NEXT_DATA__ script tag"),
            "csrf-token" to Finding("Framework", "Rails/Laravel (CSRF meta tag)", "Meta tag"),
        )
        for ((needle, finding) in checks) {
            if (html.contains(needle, ignoreCase = true)) findings.add(finding)
        }
        return findings.distinct()
    }
}
