package com.kushagra.reconx.network

import com.kushagra.reconx.models.WhoisResult
import com.kushagra.reconx.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket
import java.net.SocketTimeoutException

/**
 * WhoisClient.kt
 * ===============
 * A minimal WHOIS client using the plain WHOIS protocol (RFC 3912): open a
 * TCP socket to port 43, send "domain\r\n", read the text response. This is
 * the same mechanism the `whois` command-line tool and every WHOIS website
 * use -- it queries public registry data about a domain the user already
 * has (or wants) to look up; it performs no scanning of the target itself.
 */
object WhoisClient {

    suspend fun lookup(domain: String, timeoutMs: Int = 8000): Result<WhoisResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val server = resolveWhoisServer(domain)
                val raw = query(server, domain, timeoutMs)
                parse(domain, raw)
            }
        }

    /** IANA's WHOIS server can redirect us to the correct registry WHOIS server. */
    private fun query(server: String, domain: String, timeoutMs: Int): String {
        Socket().use { socket ->
            socket.connect(java.net.InetSocketAddress(server, Constants.WHOIS_PORT), timeoutMs)
            socket.soTimeout = timeoutMs
            socket.getOutputStream().write("$domain\r\n".toByteArray())
            socket.getOutputStream().flush()
            return socket.getInputStream().bufferedReader().readText()
        }
    }

    private fun resolveWhoisServer(domain: String): String {
        // Ask IANA which registry WHOIS server is authoritative for this TLD,
        // then re-query that server directly for full detail.
        return try {
            val ianaResponse = query(Constants.WHOIS_SERVER_DEFAULT, domain, 6000)
            val match = Regex("refer:\\s*(\\S+)").find(ianaResponse)
            match?.groupValues?.get(1)?.trim() ?: Constants.WHOIS_SERVER_DEFAULT
        } catch (e: SocketTimeoutException) {
            Constants.WHOIS_SERVER_DEFAULT
        }
    }

    private fun parse(domain: String, raw: String): WhoisResult {
        fun find(vararg labels: String): String? {
            for (label in labels) {
                val m = Regex("(?im)^$label:\\s*(.+)$").find(raw)
                if (m != null) return m.groupValues[1].trim()
            }
            return null
        }

        val nameServers = Regex("(?im)^Name Server:\\s*(.+)$").findAll(raw)
            .map { it.groupValues[1].trim() }.toList()
        val statuses = Regex("(?im)^Domain Status:\\s*(.+)$").findAll(raw)
            .map { it.groupValues[1].trim() }.toList()

        return WhoisResult(
            domain = domain,
            raw = raw,
            registrar = find("Registrar"),
            createdOn = find("Creation Date", "Created On", "created"),
            expiresOn = find("Registry Expiry Date", "Expiration Date", "paid-till"),
            nameServers = nameServers,
            status = statuses,
        )
    }
}
