package com.kushagra.reconx.network

import com.kushagra.reconx.models.GeoIpResult
import com.kushagra.reconx.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

/**
 * GeoIpClient.kt
 * ===============
 * IP intelligence: resolves a hostname to an IP (if needed), performs
 * reverse DNS, and queries the free, keyless ip-api.com endpoint for
 * geolocation/ASN/org data. Uses org.json (built into Android) instead of
 * adding Gson/Moshi, keeping the dependency list minimal.
 */
object GeoIpClient {

    suspend fun resolveHost(host: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching { InetAddress.getByName(host).hostAddress ?: error("No address found") }
    }

    suspend fun reverseDns(ip: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val addr = InetAddress.getByName(ip)
            val hostName = addr.canonicalHostName
            if (hostName == ip) error("No PTR record found") else hostName
        }
    }

    suspend fun lookup(ip: String, timeoutMs: Int = 8000): Result<GeoIpResult> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL("${Constants.GEOIP_API_BASE}$ip?fields=status,country,regionName,city,isp,org,as,lat,lon,query")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            conn.requestMethod = "GET"
            val body = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = JSONObject(body)
            if (json.optString("status") == "fail") error("Lookup failed for $ip")

            GeoIpResult(
                ip = json.optString("query", ip),
                country = json.optString("country", null),
                region = json.optString("regionName", null),
                city = json.optString("city", null),
                isp = json.optString("isp", null),
                org = json.optString("org", null),
                asn = json.optString("as", null),
                lat = if (json.has("lat")) json.optDouble("lat") else null,
                lon = if (json.has("lon")) json.optDouble("lon") else null,
            )
        }
    }
}
