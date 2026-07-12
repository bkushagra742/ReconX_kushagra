package com.kushagra.reconx.utils

import com.kushagra.reconx.models.DorkTemplate
import com.kushagra.reconx.models.QueryEngine

/**
 * DorkTemplates.kt
 * =================
 * The default OSINT query template library, ported and expanded from the
 * original desktop app's `config.py :: DEFAULT_TEMPLATES`. Every pattern
 * uses only publicly documented search-engine operators (site:, filetype:,
 * intitle:, inurl:, intext:) or the equivalent public query syntax for
 * GitHub code search, Shodan, and Censys. These tools only format search
 * strings for the user to run themselves in the target service.
 */
object DorkTemplates {

    val DEFAULTS: List<DorkTemplate> = listOf(
        // ---------------- Google ----------------
        DorkTemplate(1, QueryEngine.GOOGLE, "File Search", "PDF documents on domain", "site:{domain} filetype:pdf"),
        DorkTemplate(2, QueryEngine.GOOGLE, "File Search", "Office documents on domain",
            "site:{domain} (filetype:doc OR filetype:docx OR filetype:xls OR filetype:xlsx OR filetype:ppt OR filetype:pptx)"),
        DorkTemplate(3, QueryEngine.GOOGLE, "File Search", "Exposed configs/logs",
            "site:{domain} (filetype:env OR filetype:log OR filetype:conf OR filetype:cfg)"),
        DorkTemplate(4, QueryEngine.GOOGLE, "Login & Admin", "Login / admin portals",
            "site:{domain} (inurl:login OR inurl:admin OR inurl:portal OR inurl:signin)"),
        DorkTemplate(5, QueryEngine.GOOGLE, "Login & Admin", "Exposed dev/staging environments",
            "site:{domain} (inurl:dev OR inurl:staging OR inurl:test OR inurl:uat)"),
        DorkTemplate(6, QueryEngine.GOOGLE, "Public Info", "Entity general mentions", "\"{org}\""),
        DorkTemplate(7, QueryEngine.GOOGLE, "Public Info", "Entity + keyword mentions", "\"{org}\" \"{keyword}\""),
        DorkTemplate(8, QueryEngine.GOOGLE, "Public Info", "Entity on social platforms",
            "\"{org}\" (site:linkedin.com OR site:x.com OR site:facebook.com)"),
        DorkTemplate(9, QueryEngine.GOOGLE, "Technology", "API / docs endpoints",
            "site:{domain} (inurl:api OR inurl:docs OR inurl:swagger OR inurl:graphql)"),
        DorkTemplate(10, QueryEngine.GOOGLE, "Technology", "Subdomain enumeration hint",
            "site:*.{domain} -site:www.{domain}"),
        DorkTemplate(11, QueryEngine.GOOGLE, "Research", "Cached / historical mentions",
            "\"{org}\" site:web.archive.org"),
        DorkTemplate(12, QueryEngine.GOOGLE, "Research", "Academic/industry sources",
            "\"{keyword}\" \"{org}\" (site:.edu OR site:.gov OR site:.org)"),

        // ---------------- Bing ----------------
        DorkTemplate(20, QueryEngine.BING, "File Search", "PDF documents on domain", "site:{domain} filetype:pdf"),
        DorkTemplate(21, QueryEngine.BING, "Technology", "IP-linked hostnames", "ip:{domain}"),
        DorkTemplate(22, QueryEngine.BING, "Public Info", "Entity + keyword mentions", "\"{org}\" \"{keyword}\""),
        DorkTemplate(23, QueryEngine.BING, "Login & Admin", "Login / admin portals",
            "site:{domain} (inurl:login OR inurl:admin)"),

        // ---------------- GitHub code search ----------------
        DorkTemplate(30, QueryEngine.GITHUB, "Secrets", "Possible API keys mentioning domain",
            "\"{domain}\" \"api_key\" OR \"apikey\" OR \"secret\""),
        DorkTemplate(31, QueryEngine.GITHUB, "Secrets", "Possible hardcoded credentials",
            "\"{domain}\" password OR passwd OR pwd"),
        DorkTemplate(32, QueryEngine.GITHUB, "Config", "Environment / config files referencing domain",
            "\"{domain}\" filename:.env"),
        DorkTemplate(33, QueryEngine.GITHUB, "Org", "Repositories mentioning organization",
            "\"{org}\" in:readme"),
        DorkTemplate(34, QueryEngine.GITHUB, "Org", "Code mentioning keyword + domain",
            "\"{keyword}\" \"{domain}\""),

        // ---------------- Shodan ----------------
        DorkTemplate(40, QueryEngine.SHODAN, "Host", "Hosts for hostname", "hostname:{domain}"),
        DorkTemplate(41, QueryEngine.SHODAN, "Host", "Organization-tagged hosts", "org:\"{org}\""),
        DorkTemplate(42, QueryEngine.SHODAN, "Service", "Exposed service banner mentioning keyword",
            "hostname:{domain} \"{keyword}\""),
        DorkTemplate(43, QueryEngine.SHODAN, "Service", "SSL cert matching domain", "ssl:\"{domain}\""),
        DorkTemplate(44, QueryEngine.SHODAN, "Service", "Common exposed admin ports",
            "hostname:{domain} port:8080,8443,9090,3389"),

        // ---------------- Censys ----------------
        DorkTemplate(50, QueryEngine.CENSYS, "Host", "Hosts by hostname", "dns.names: {domain}"),
        DorkTemplate(51, QueryEngine.CENSYS, "Host", "Certificate matching domain",
            "services.tls.certificates.leaf_data.subject.common_name: \"{domain}\""),
        DorkTemplate(52, QueryEngine.CENSYS, "Org", "Organization-tagged hosts",
            "autonomous_system.organization: \"{org}\""),
        DorkTemplate(53, QueryEngine.CENSYS, "Service", "Keyword in service banner",
            "services.banner: \"{keyword}\" and dns.names: {domain}"),
    )

    private val PLACEHOLDER_RE = Regex("\\{(\\w+)}")

    /** Only fires a template if every placeholder it references was supplied. */
    fun generate(
        templates: List<DorkTemplate>,
        domain: String,
        org: String,
        keyword: String,
        engineFilter: QueryEngine? = null,
    ): List<com.kushagra.reconx.models.GeneratedDork> {
        val values = mapOf(
            "domain" to domain.trim(),
            "org" to org.trim(),
            "keyword" to keyword.trim(),
        )
        val supplied = values.filterValues { it.isNotEmpty() }.keys

        return templates
            .asSequence()
            .filter { engineFilter == null || it.engine == engineFilter }
            .mapNotNull { tpl ->
                val needed = PLACEHOLDER_RE.findAll(tpl.pattern).map { it.groupValues[1] }.toSet()
                if (needed.isNotEmpty() && !needed.all { it in supplied }) return@mapNotNull null
                var text = tpl.pattern
                values.forEach { (k, v) -> text = text.replace("{$k}", v) }
                com.kushagra.reconx.models.GeneratedDork(tpl.engine, tpl.category, tpl.name, text)
            }
            .toList()
    }
}
