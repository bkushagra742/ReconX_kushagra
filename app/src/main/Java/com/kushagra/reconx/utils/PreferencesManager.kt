package com.kushagra.reconx.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREFS_NAME)

/**
 * PreferencesManager.kt
 * =======================
 * Wraps Jetpack DataStore for: the hashed login credential (salt + hash,
 * never the plaintext password), appearance preference, biometric app-lock
 * toggle, and misc settings. Replaces SharedPreferences per the
 * "offline-first, no plaintext credentials" requirement.
 */
class PreferencesManager(private val context: Context) {

    private object Keys {
        val AUTH_SALT = stringPreferencesKey("auth_salt")
        val AUTH_HASH = stringPreferencesKey("auth_hash")
        val THEME_MODE = stringPreferencesKey("theme_mode")          // "system" | "light" | "dark"
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val ANALYST_NAME = stringPreferencesKey("analyst_name")
        val DEFAULT_REPORT_FORMAT = stringPreferencesKey("default_report_format")
    }

    /** Seeds the default admin/kushagra credential (hashed) on first run only. */
    suspend fun ensureDefaultCredentialSeeded() {
        val prefs = context.dataStore.data.first()
        if (prefs[Keys.AUTH_HASH] == null) {
            val salt = CredentialHasher.generateSalt()
            val hash = CredentialHasher.hash("kushagra", salt)
            context.dataStore.edit {
                it[Keys.AUTH_SALT] = salt
                it[Keys.AUTH_HASH] = hash
            }
        }
    }

    suspend fun verifyLogin(username: String, password: String): Boolean {
        if (username != "admin") return false
        val prefs = context.dataStore.data.first()
        val salt = prefs[Keys.AUTH_SALT] ?: return false
        val hash = prefs[Keys.AUTH_HASH] ?: return false
        return CredentialHasher.verify(password, salt, hash)
    }

    val themeMode: Flow<String> = context.dataStore.data.map { it[Keys.THEME_MODE] ?: "system" }
    suspend fun setThemeMode(mode: String) { context.dataStore.edit { it[Keys.THEME_MODE] = mode } }

    val biometricLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BIOMETRIC_LOCK] ?: false }
    suspend fun setBiometricLock(enabled: Boolean) { context.dataStore.edit { it[Keys.BIOMETRIC_LOCK] = enabled } }

    val analystName: Flow<String> = context.dataStore.data.map { it[Keys.ANALYST_NAME] ?: "Kushagra Singh Bisht" }
    suspend fun setAnalystName(name: String) { context.dataStore.edit { it[Keys.ANALYST_NAME] = name } }

    val defaultReportFormat: Flow<String> = context.dataStore.data.map { it[Keys.DEFAULT_REPORT_FORMAT] ?: "MARKDOWN" }
    suspend fun setDefaultReportFormat(format: String) {
        context.dataStore.edit { it[Keys.DEFAULT_REPORT_FORMAT] = format }
    }
}
