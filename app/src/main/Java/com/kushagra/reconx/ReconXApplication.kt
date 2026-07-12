package com.kushagra.reconx

import android.app.Application
import com.kushagra.reconx.database.ReconXDatabase
import com.kushagra.reconx.export.FileExportManager
import com.kushagra.reconx.repository.ActivityRepository
import com.kushagra.reconx.repository.CveRepository
import com.kushagra.reconx.repository.NoteRepository
import com.kushagra.reconx.repository.ProjectRepository
import com.kushagra.reconx.repository.QueryRepository
import com.kushagra.reconx.repository.ReportRepository
import com.kushagra.reconx.utils.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * ReconXApplication.kt
 * =====================
 * App-wide manual dependency container. Deliberately avoids Hilt/Dagger to
 * keep the dependency graph (and APK size) minimal for a single-user,
 * offline-first tool of this scope -- every repository/manager below is a
 * cheap, stateless (or DB-backed) singleton constructed once here.
 */
class ReconXApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: ReconXDatabase by lazy { ReconXDatabase.getInstance(this) }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager(this) }
    val fileExportManager: FileExportManager by lazy { FileExportManager(this) }

    val projectRepository: ProjectRepository by lazy {
        ProjectRepository(database.projectDao(), database.activityDao())
    }
    val queryRepository: QueryRepository by lazy {
        QueryRepository(database.queryDao(), database.activityDao())
    }
    val noteRepository: NoteRepository by lazy {
        NoteRepository(database.noteDao(), database.activityDao())
    }
    val reportRepository: ReportRepository by lazy { ReportRepository(database.reportDao()) }
    val activityRepository: ActivityRepository by lazy {
        ActivityRepository(database.activityDao(), database.historyDao())
    }
    val cveRepository: CveRepository by lazy { CveRepository(database.cveDao()) }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            preferencesManager.ensureDefaultCredentialSeeded()
        }
    }
}
