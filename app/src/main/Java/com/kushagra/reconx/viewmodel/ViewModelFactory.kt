package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.kushagra.reconx.ReconXApplication

/**
 * ViewModelFactory.kt
 * =====================
 * A single generic factory that supplies every ViewModel in the app with
 * its dependencies from [ReconXApplication]'s manual DI container. Avoids
 * pulling in the Hilt/Dagger dependency-injection library.
 */
class ViewModelFactory(private val app: ReconXApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            LoginViewModel::class.java -> LoginViewModel(app.preferencesManager) as T
            DashboardViewModel::class.java -> DashboardViewModel(
                app.projectRepository, app.queryRepository, app.noteRepository,
                app.reportRepository, app.activityRepository,
            ) as T
            ProjectsViewModel::class.java -> ProjectsViewModel(app.projectRepository) as T
            ProjectDetailViewModel::class.java -> ProjectDetailViewModel(
                app.projectRepository, app.queryRepository, app.noteRepository, app.reportRepository,
            ) as T
            NotesViewModel::class.java -> NotesViewModel(app.noteRepository) as T
            ReportsViewModel::class.java -> ReportsViewModel(
                app.projectRepository, app.queryRepository, app.noteRepository,
                app.reportRepository, app.fileExportManager, app.preferencesManager,
            ) as T
            SettingsViewModel::class.java -> SettingsViewModel(app.preferencesManager, app.database, app) as T
            DorkBuilderViewModel::class.java -> DorkBuilderViewModel(app.queryRepository, app.activityRepository) as T
            DomainIntelViewModel::class.java -> DomainIntelViewModel(app.activityRepository) as T
            WebsiteSecurityViewModel::class.java -> WebsiteSecurityViewModel(app.activityRepository) as T
            IpIntelViewModel::class.java -> IpIntelViewModel(app.activityRepository) as T
            HashToolsViewModel::class.java -> HashToolsViewModel(app.activityRepository) as T
            PasswordToolsViewModel::class.java -> PasswordToolsViewModel() as T
            EncodingToolsViewModel::class.java -> EncodingToolsViewModel() as T
            RegexLabViewModel::class.java -> RegexLabViewModel() as T
            CveLookupViewModel::class.java -> CveLookupViewModel(app.cveRepository) as T
            GlobalSearchViewModel::class.java -> GlobalSearchViewModel(
                app.projectRepository, app.queryRepository, app.noteRepository,
            ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
