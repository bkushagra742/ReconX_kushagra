package com.kushagra.reconx.utils

import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder

/**
 * EncodingUtils.kt
 * =================
 * Encode/decode helpers for Base64, URL, Hex, Binary, and Unicode escapes.
 * All operations are pure/local -- no network calls.
 */
object EncodingUtils {

    fun base64Encode(input: String): String =
        Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

    fun base64Decode(input: String): Result<String> = runCatching {
        String(Base64.decode(input, Base64.DEFAULT), Charsets.UTF_8)
    }

    fun urlEncode(input: String): String = URLEncoder.encode(input, "UTF-8")

    fun urlDecode(input: String): Result<String> = runCatching {
        URLDecoder.decode(input, "UTF-8")
    }

    fun hexEncode(input: String): String =
        input.toByteArray(Charsets.UTF_8).joinToString(" ") { "%02x".format(it) }

    fun hexDecode(input: String): Result<String> = runCatching {
        val clean = input.replace(" ", "").replace("0x", "")
        val bytes = ByteArray(clean.length / 2) { i ->
            clean.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        String(bytes, Charsets.UTF_8)
    }

    fun binaryEncode(input: String): String =
        input.toByteArray(Charsets.UTF_8).joinToString(" ") {
            String.format("%8s", Integer.toBinaryString(it.toInt() and 0xFF)).replace(' ', '0')
        }

    fun binaryDecode(input: String): Result<String> = runCatching {
        val bytes = input.trim().split(Regex("\\s+")).map { it.toInt(2).toByte() }.toByteArray()
        String(bytes, Charsets.UTF_8)
    }

    fun unicodeEscape(input: String): String =
        input.map { c -> if (c.code > 127) "\\u%04x".format(c.code) else c.toString() }.joinToString("")

    fun unicodeUnescape(input: String): Result<String> = runCatching {
        Regex("\\\\u([0-9a-fA-F]{4})").replace(input) { m ->
            m.groupValues[1].toInt(16).toChar().toString()
        }
    }
}
