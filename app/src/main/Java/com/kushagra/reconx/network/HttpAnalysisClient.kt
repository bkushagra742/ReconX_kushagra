package com.kushagra.reconx.network

import com.kushagra.reconx.models.HttpHeaderResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * HttpAnalysisClient.kt
 * ======================
 * Fetches HTTP response headers, follows redirects manually (to build a
 * visible redirect chain instead of letting HttpURLConnection hide it),
 * measures response time, probes supported HTTP methods via OPTIONS, and
 * retrieves robots.txt / sitemap.xml / security.txt. All read-only GET/HEAD
 * requests against a target the user already controls or is authorized to
 * assess -- equivalent to what `curl -I` or a browser's network panel does.
 */
object HttpAnalysisClient {

    suspend fun analyze(inputUrl: String, timeoutMs: Int = 10000): Result<HttpHeaderResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                var currentUrl = normalizeUrl(inputUrl)
                val chain = mutableListOf<String>()
                var lastResponse: HttpURLConnection? = null
                val start = System.currentTimeMillis()

                repeat(10) { // max 10 redirects
                    val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = timeoutMs
                        readTimeout = timeoutMs
                        instanceFollowRedirects = false
                        requestMethod = "GET"
                        setRequestProperty("User-Agent", "KushagraReconX/2.0 (+security-research)")
                    }
                    chain.add(currentUrl)
                    val code = conn.responseCode
                    if (code in 300..399) {
                        val location = conn.getHeaderField("Location")
                        conn.disconnect()
                        if (location.isNullOrBlank()) return@repeat
                        currentUrl = if (location.startsWith("http")) location
                        else URL(URL(currentUrl), location).toString()
                    } else {
                        lastResponse = conn
                        return@repeat
                    }
                }

                val finalConn = lastResponse ?: error("Too many redirects or no response")
                val elapsed = System.currentTimeMillis() - start
                val headers = finalConn.headerFields
                    .filterKeys { it != null }
                    .mapKeys { it.key!! }
                    .mapValues { it.value.joinToString("; ") }
                val result = HttpHeaderResult(
                    url = currentUrl,
                    statusCode = finalConn.responseCode,
                    headers = headers,
                    responseTimeMs = elapsed,
                    redirectChain = chain,
                )
                finalConn.disconnect()
                result
            }
        }

    suspend fun fetchTextResource(baseUrl: String, path: String, timeoutMs: Int = 8000): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val url = URL(normalizeUrl(baseUrl).trimEnd('/') + path)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = timeoutMs
                    readTimeout = timeoutMs
                    requestMethod = "GET"
                }
                if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode} for $path")
                conn.inputStream.bufferedReader().readText()
            }
        }

    suspend fun probeAllowedMethods(inputUrl: String, timeoutMs: Int = 8000): Result<List<String>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val conn = (URL(normalizeUrl(inputUrl)).openConnection() as HttpURLConnection).apply {
                    connectTimeout = timeoutMs
                    readTimeout = timeoutMs
                    requestMethod = "OPTIONS"
                }
                val allow = conn.getHeaderField("Allow") ?: conn.getHeaderField("Access-Control-Allow-Methods")
                conn.disconnect()
                allow?.split(",")?.map { it.trim() } ?: emptyList()
            }
        }

    private fun normalizeUrl(input: String): String =
        if (input.startsWith("http://") || input.startsWith("https://")) input else "https://$input"
}
