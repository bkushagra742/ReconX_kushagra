package com.kushagra.reconx.reports

data class ReportQueryItem(val engine: String, val category: String, val title: String, val query: String)
data class ReportNoteItem(val title: String, val content: String, val createdAt: String)

data class ReportData(
    val projectName: String,
    val analystName: String,
    val generatedOn: String,
    val domain: String,
    val entity: String,
    val keyword: String,
    val description: String,
    val queries: List<ReportQueryItem>,
    val notes: List<ReportNoteItem>,
    val findingsSummary: String = "",
)
