package com.kushagra.reconx.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.database.ReconXDatabase
import com.kushagra.reconx.utils.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(
    private val preferencesManager: PreferencesManager,
    private val database: ReconXDatabase,
    private val application: Application,
) : ViewModel() {

    val themeMode: StateFlow<String> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val biometricLockEnabled: StateFlow<Boolean> = preferencesManager.biometricLockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val analystName: StateFlow<String> = preferencesManager.analystName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val defaultReportFormat: StateFlow<String> = preferencesManager.defaultReportFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MARKDOWN")

    fun setThemeMode(mode: String) = viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    fun setBiometricLock(enabled: Boolean) = viewModelScope.launch { preferencesManager.setBiometricLock(enabled) }
    fun setAnalystName(name: String) = viewModelScope.launch { preferencesManager.setAnalystName(name) }
    fun setDefaultReportFormat(format: String) = viewModelScope.launch { preferencesManager.setDefaultReportFormat(format) }

    /** Copies the live Room database file to a timestamped backup in app-external storage. */
    fun backupDatabase(): File {
        val dbFile = application.getDatabasePath("reconx.db")
        val backupDir = File(application.getExternalFilesDir(null), "backups").apply { mkdirs() }
        val target = File(backupDir, "reconx_backup_${com.kushagra.reconx.utils.DateUtils.formatForFilename()}.db")
        dbFile.copyTo(target, overwrite = true)
        return target
    }

    fun restoreDatabase(backupFile: File, onDone: () -> Unit) {
        viewModelScope.launch {
            database.close()
            val dbFile = application.getDatabasePath("reconx.db")
            backupFile.copyTo(dbFile, overwrite = true)
            onDone()
        }
    }

    fun getStorageUsedBytes(): Long {
        val dbFile = application.getDatabasePath("reconx.db")
        val exportsDir = File(application.getExternalFilesDir(null), "reports")
        var total = if (dbFile.exists()) dbFile.length() else 0L
        exportsDir.walkTopDown().forEach { if (it.isFile) total += it.length() }
        return total
    }
}
