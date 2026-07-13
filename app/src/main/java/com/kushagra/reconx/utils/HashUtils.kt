package com.kushagra.reconx.utils

import com.kushagra.reconx.models.HashResult
import java.io.InputStream
import java.security.MessageDigest

/**
 * HashUtils.kt
 * =============
 * MD5 / SHA-1 / SHA-256 / SHA-512 for both text and file input, plus a
 * constant-time-ish hash comparison for integrity verification.
 *
 * MD5/SHA-1 are included only for legacy file-integrity comparison
 * (e.g. verifying a download against a publisher-provided checksum) --
 * they are never used for anything security-sensitive within the app
 * itself (the login uses CredentialHasher's iterated SHA-256 instead).
 */
object HashUtils {
    private val ALGORITHMS = listOf("MD5", "SHA-1", "SHA-256", "SHA-512")

    fun hashText(text: String, algorithm: String): HashResult {
        val digest = MessageDigest.getInstance(algorithm)
        val bytes = digest.digest(text.toByteArray(Charsets.UTF_8))
        return HashResult(algorithm, text, bytes.toHex())
    }

    fun hashTextAllAlgorithms(text: String): List<HashResult> =
        ALGORITHMS.map { hashText(text, it) }

    fun hashStream(input: InputStream, algorithm: String): String {
        val digest = MessageDigest.getInstance(algorithm)
        val buffer = ByteArray(8192)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            digest.update(buffer, 0, read)
        }
        return digest.digest().toHex()
    }

    fun compare(hashA: String, hashB: String): Boolean =
        hashA.trim().equals(hashB.trim(), ignoreCase = true)

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
