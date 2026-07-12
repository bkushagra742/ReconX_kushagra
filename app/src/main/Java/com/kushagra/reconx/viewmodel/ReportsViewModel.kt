package com.kushagra.reconx.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushagra.reconx.database.entity.ProjectEntity
import com.kushagra.reconx.export.FileExportManager
import com.kushagra.reconx.reports.ReportData
import com.kushagra.reconx.reports.ReportNoteItem
import com.kushagra.reconx.reports.ReportQueryItem
import com.kushagra.reconx.repository.NoteRepository
import com.kushagra.reconx.repository.ProjectRepository
import com.kushagra.reconx.repository.QueryRepository
import com.kushagra.reconx.repository.ReportRepository
import com.kushagra.reconx.utils.DateUtils
import com.kushagra.reconx.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface ExportEvent {
    data class Success(val file: File) : ExportEvent
    data class Failure(val message: String) : ExportEvent
}

class ReportsViewModel(
    private val projectRepository: ProjectRepository,
    private val queryRepository: QueryRepository,
    private val noteRepository: NoteRepository,
    private val reportRepository: ReportRepository,
    private val fileExportManager: FileExportManager,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    val projects: StateFlow<List<ProjectEntity>> = projectRepository.observeProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _lastExport = MutableStateFlow<ExportEvent?>(null)
    val lastExport: StateFlow<ExportEvent?> = _lastExport

    fun export(project: ProjectEntity, findingsSummary: String, format: String) {
        viewModelScope.launch {
            runCatching {
                val queries = queryRepository.observeByProject(project.id)
                val notes = noteRepository.observeByProject(project.id)
                // Single-shot read: take the first emitted list from each Flow.
                val queryList = queries.first()
                val noteList = notes.first()
                val analyst = preferencesManager.analystName.first()

                val data = ReportData(
                    projectName = project.name,
                    analystName = analyst,
                    generatedOn = DateUtils.formatForDisplay(DateUtils.now()),
                    domain = project.domain,
                    entity = project.entity,
                    keyword = project.keyword,
                    description = project.description,
                    findingsSummary = findingsSummary,
                    queries = queryList.map { ReportQueryItem(it.engine, it.category, it.title, it.queryText) },
                    notes = noteList.map { ReportNoteItem(it.title, it.contentMarkdown, DateUtils.formatForDisplay(it.createdAt)) },
                )

                val file = when (format) {
                    "MARKDOWN" -> fileExportManager.exportMarkdown(data)
                    "JSON" -> fileExportManager.exportJson(data)
                    "TXT" -> fileExportManager.exportTxt(data)
                    "PDF" -> fileExportManager.exportPdf(data)
                    else -> error("Unknown format: $format")
                }
                reportRepository.record(project.id, analyst, format, file.absolutePath)
                file
            }.onSuccess { _lastExport.value = ExportEvent.Success(it) }
                .onFailure { _lastExport.value = ExportEvent.Failure(it.message ?: "Export failed") }
        }
    }

    fun clearEvent() { _lastExport.value = null }
}
