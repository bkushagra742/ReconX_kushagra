package com.kushagra.reconx.network

import com.kushagra.reconx.models.DnsRecordResult
import com.kushagra.reconx.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer
import kotlin.random.Random

/**
 * DnsClient.kt
 * =============
 * A small, dependency-free DNS client that builds a raw DNS query packet
 * (RFC 1035) over UDP and parses the response -- the same fundamental
 * operation `dig`/`nslookup` perform. Supports the record types used by
 * the Domain Intelligence module: A, AAAA, MX, TXT, NS, SOA, CNAME, SRV.
 *
 * Uses a public resolver (Cloudflare's 1.1.1.1) rather than relying on
 * whatever DNS the carrier/Wi-Fi network provides, so results are
 * consistent regardless of the device's network.
 */
object DnsClient {

    private val TYPE_CODES = mapOf(
        "A" to 1, "NS" to 2, "CNAME" to 5, "SOA" to 6,
        "MX" to 15, "TXT" to 16, "AAAA" to 28, "SRV" to 33,
    )
    private val CODE_TYPES = TYPE_CODES.entries.associate { (k, v) -> v to k }

    suspend fun query(name: String, type: String, timeoutMs: Int = 5000): Result<List<DnsRecordResult>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val typeCode = TYPE_CODES[type] ?: error("Unsupported record type: $type")
                val queryId = Random.nextInt(0, 0xFFFF)
                val packet = buildQuery(queryId, name, typeCode)

                DatagramSocket().use { socket ->
                    socket.soTimeout = timeoutMs
                    val address = InetAddress.getByName(Constants.PUBLIC_DNS_RESOLVER)
                    socket.send(DatagramPacket(packet, packet.size, address, Constants.DNS_PORT))

                    val buffer = ByteArray(4096)
                    val response = DatagramPacket(buffer, buffer.size)
                    socket.receive(response)
                    parseResponse(buffer, response.length, name)
                }
            }
        }

    suspend fun queryAllCommonTypes(name: String): Map<String, List<DnsRecordResult>> {
        val results = mutableMapOf<String, List<DnsRecordResult>>()
        for (type in listOf("A", "AAAA", "MX", "TXT", "NS", "SOA", "CNAME")) {
            query(name, type).getOrNull()?.let { if (it.isNotEmpty()) results[type] = it }
        }
        return results
    }

    // ---- DNS wire-format packet construction ----
    private fun buildQuery(id: Int, name: String, typeCode: Int): ByteArray {
        val buffer = ByteBuffer.allocate(512)
        buffer.putShort(id.toShort())
        buffer.putShort(0x0100.toShort())   // flags: standard query, recursion desired
        buffer.putShort(1)                  // QDCOUNT
        buffer.putShort(0); buffer.putShort(0); buffer.putShort(0) // ANCOUNT/NSCOUNT/ARCOUNT

        for (label in name.split(".")) {
            if (label.isEmpty()) continue
            buffer.put(label.length.toByte())
            buffer.put(label.toByteArray(Charsets.US_ASCII))
        }
        buffer.put(0) // root label
        buffer.putShort(typeCode.toShort())
        buffer.putShort(1) // QCLASS = IN

        val result = ByteArray(buffer.position())
        buffer.rewind()
        buffer.get(result)
        return result
    }

    private fun parseResponse(data: ByteArray, length: Int, queryName: String): List<DnsRecordResult> {
        val buf = ByteBuffer.wrap(data, 0, length)
        buf.short // id
        buf.short // flags
        val qdCount = buf.short.toInt() and 0xFFFF
        val anCount = buf.short.toInt() and 0xFFFF
        buf.short; buf.short // ns/ar counts (unused)

        repeat(qdCount) {
            skipName(buf)
            buf.short; buf.short // qtype, qclass
        }

        val records = mutableListOf<DnsRecordResult>()
        repeat(anCount) {
            val name = readName(data, buf)
            val type = buf.short.toInt() and 0xFFFF
            buf.short // class
            val ttl = buf.int.toLong() and 0xFFFFFFFFL
            val rdLength = buf.short.toInt() and 0xFFFF
            val rdStart = buf.position()

            val typeName = CODE_TYPES[type] ?: "TYPE$type"
            val value = when (typeName) {
                "A" -> (0 until 4).map { data[rdStart + it].toInt() and 0xFF }.joinToString(".")
                "AAAA" -> (0 until 16 step 2).joinToString(":") { i ->
                    "%02x%02x".format(data[rdStart + i], data[rdStart + i + 1])
                }
                "MX" -> {
                    val pref = ((data[rdStart].toInt() and 0xFF) shl 8) or (data[rdStart + 1].toInt() and 0xFF)
                    val exchangeBuf = ByteBuffer.wrap(data, rdStart + 2, length - (rdStart + 2))
                    "$pref ${readName(data, exchangeBuf)}"
                }
                "NS", "CNAME" -> {
                    val nameBuf = ByteBuffer.wrap(data, rdStart, length - rdStart)
                    readName(data, nameBuf)
                }
                "TXT" -> {
                    var offset = rdStart
                    val sb = StringBuilder()
                    while (offset < rdStart + rdLength) {
                        val txtLen = data[offset].toInt() and 0xFF
                        sb.append(String(data, offset + 1, txtLen, Charsets.UTF_8))
                        offset += txtLen + 1
                    }
                    sb.toString()
                }
                "SOA" -> {
                    val soaBuf = ByteBuffer.wrap(data, rdStart, length - rdStart)
                    val mname = readName(data, soaBuf)
                    val rname = readName(data, soaBuf)
                    val serial = soaBuf.int
                    "mname=$mname rname=$rname serial=$serial"
                }
                else -> "0x" + (rdStart until rdStart + rdLength).joinToString("") { "%02x".format(data[it]) }
            }
            records.add(DnsRecordResult(typeName, name.ifEmpty { queryName }, value, ttl))
            buf.position(rdStart + rdLength)
        }
        return records
    }

    /** Skips a (possibly compressed) name in the question section. */
    private fun skipName(buf: ByteBuffer) {
        while (true) {
            val len = buf.get().toInt() and 0xFF
            if (len == 0) return
            if (len and 0xC0 == 0xC0) { buf.get(); return } // pointer: 2 bytes total
            buf.position(buf.position() + len)
        }
    }

    /** Reads a (possibly compressed) DNS name, following pointers as needed. */
    private fun readName(data: ByteArray, buf: ByteBuffer): String {
        val labels = mutableListOf<String>()
        var jumped = false
        var safety = 0
        while (safety++ < 128) {
            val len = buf.get().toInt() and 0xFF
            if (len == 0) break
            if (len and 0xC0 == 0xC0) {
                val lo = buf.get().toInt() and 0xFF
                val pointer = ((len and 0x3F) shl 8) or lo
                if (!jumped) jumped = true
                val jumpBuf = ByteBuffer.wrap(data, pointer, data.size - pointer)
                labels.add(readName(data, jumpBuf))
                break
            } else {
                val label = String(data, buf.position(), len, Charsets.US_ASCII)
                buf.position(buf.position() + len)
                labels.add(label)
            }
        }
        return labels.joinToString(".")
    }
}
