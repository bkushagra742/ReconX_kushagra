package com.kushagra.reconx.models

/**
 * Models.kt
 * =========
 * Plain in-memory data models shared between the network/scanner layer and
 * the UI layer. These are distinct from the Room `*Entity` classes in
 * database/entity/ -- entities are persistence records, these are
 * transient results of a live lookup/tool run.
 */

data class DnsRecordResult(
    val type: String,          // A, AAAA, MX, TXT, NS, SOA, CNAME, SRV
    val name: String,
    val value: String,
    val ttl: Long = 0,
)

data class WhoisResult(
    val domain: String,
    val raw: String,
    val registrar: String? = null,
    val createdOn: String? = null,
    val expiresOn: String? = null,
    val nameServers: List<String> = emptyList(),
    val status: List<String> = emptyList(),
)

data class GeoIpResult(
    val ip: String,
    val country: String? = null,
    val region: String? = null,
    val city: String? = null,
    val isp: String? = null,
    val org: String? = null,
    val asn: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
)

data class HttpHeaderResult(
    val url: String,
    val statusCode: Int,
    val headers: Map<String, String>,
    val responseTimeMs: Long,
    val redirectChain: List<String> = emptyList(),
)

data class SecurityHeaderFinding(
    val header: String,
    val present: Boolean,
    val value: String? = null,
    val recommendation: String = "",
)

data class CertificateInfo(
    val subject: String,
    val issuer: String,
    val validFrom: String,
    val validTo: String,
    val daysUntilExpiry: Long,
    val tlsVersion: String,
    val cipherSuite: String,
    val isExpired: Boolean,
    val isSelfSigned: Boolean,
)

data class HashResult(
    val algorithm: String,
    val input: String,
    val hash: String,
)

data class PasswordStrengthResult(
    val score: Int,           // 0-100
    val label: String,        // Very Weak / Weak / Fair / Strong / Very Strong
    val entropyBits: Double,
    val suggestions: List<String>,
)

enum class QueryEngine { GOOGLE, BING, GITHUB, SHODAN, CENSYS }

data class DorkTemplate(
    val id: Int,
    val engine: QueryEngine,
    val category: String,
    val name: String,
    val pattern: String, // uses {domain} {keyword} {org} placeholders
)

data class GeneratedDork(
    val engine: QueryEngine,
    val category: String,
    val title: String,
    val query: String,
)
