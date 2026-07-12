package com.kushagra.reconx.utils

import com.kushagra.reconx.models.PasswordStrengthResult
import kotlin.math.log2

/**
 * PasswordStrengthUtils.kt
 * =========================
 * Offline password strength / entropy estimation and policy checking.
 * Deliberately contains NO cracking, dictionary-attack, or brute-force
 * logic of any kind -- this only scores a password the user types in,
 * the same way a signup form's strength meter would.
 */
object PasswordStrengthUtils {

    fun analyze(password: String): PasswordStrengthResult {
        if (password.isEmpty()) {
            return PasswordStrengthResult(0, "Empty", 0.0, listOf("Enter a password to analyze."))
        }

        var poolSize = 0
        if (password.any { it.isLowerCase() }) poolSize += 26
        if (password.any { it.isUpperCase() }) poolSize += 26
        if (password.any { it.isDigit() }) poolSize += 10
        if (password.any { !it.isLetterOrDigit() }) poolSize += 32

        val entropyBits = if (poolSize > 0) password.length * log2(poolSize.toDouble()) else 0.0

        val suggestions = mutableListOf<String>()
        if (password.length < 12) suggestions.add("Use at least 12 characters.")
        if (!password.any { it.isUpperCase() }) suggestions.add("Add uppercase letters.")
        if (!password.any { it.isLowerCase() }) suggestions.add("Add lowercase letters.")
        if (!password.any { it.isDigit() }) suggestions.add("Add numbers.")
        if (!password.any { !it.isLetterOrDigit() }) suggestions.add("Add symbols.")
        if (hasRepeatedPattern(password)) suggestions.add("Avoid repeated characters or patterns.")
        if (isCommonPassword(password)) suggestions.add("Avoid common/dictionary passwords.")

        val score = when {
            isCommonPassword(password) -> 5
            else -> (entropyBits / 1.0).coerceIn(0.0, 100.0).toInt()
        }

        val label = when {
            isCommonPassword(password) -> "Very Weak"
            entropyBits < 28 -> "Weak"
            entropyBits < 36 -> "Fair"
            entropyBits < 60 -> "Strong"
            else -> "Very Strong"
        }

        return PasswordStrengthResult(score.coerceIn(0, 100), label, entropyBits, suggestions)
    }

    private fun hasRepeatedPattern(password: String): Boolean {
        if (password.length < 4) return false
        for (i in 0..password.length - 3) {
            if (password[i] == password[i + 1] && password[i + 1] == password[i + 2]) return true
        }
        return false
    }

    private val COMMON_PASSWORDS = setOf(
        "password", "123456", "12345678", "qwerty", "letmein", "admin",
        "welcome", "iloveyou", "abc123", "111111", "password1", "123456789",
    )

    private fun isCommonPassword(password: String): Boolean =
        password.lowercase() in COMMON_PASSWORDS
}
