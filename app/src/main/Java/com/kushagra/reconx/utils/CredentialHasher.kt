package com.kushagra.reconx.utils

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * CredentialHasher.kt
 * ====================
 * The offline login never stores or compares plain-text passwords.
 * Instead, a random per-install salt is generated on first launch (stored
 * in DataStore, see PreferencesManager) and the password is hashed with
 * PBKDF2-style iterated SHA-256 before comparison. This satisfies "do not
 * hardcode credentials in plain text" without requiring any cloud service.
 */
object CredentialHasher {
    private const val ITERATIONS = 12_000

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(password: String, salt: String): String {
        var data = (password + salt).toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        repeat(ITERATIONS) {
            digest.reset()
            data = digest.digest(data)
        }
        return data.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        hash(password, salt) == expectedHash
}
