package com.kushagra.reconx.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.kushagra.reconx.reports.MarkdownReportBuilder
import com.kushagra.reconx.reports.ReportData
import com.kushagra.reconx.utils.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * FileExportManager.kt
 * ======================
 * Writes reports (Markdown / TXT / JSON / PDF) to the app's private,
 * app-specific external storage directory (no storage permission needed
 * on API 29+) and exposes a share Intent via FileProvider so the user can
 * send the finished report to email, Drive, etc.
 */
class FileExportManager(private val context: Context) {

    private fun exportsDir(): File =
        File(context.getExternalFilesDir(null), "reports").apply { mkdirs() }

    private fun safeName(name: String): String =
        name.map { if (it.isLetterOrDigit() || it == '_' || it == '-') it else '_' }.joinToString("")

    fun exportMarkdown(data: ReportData): File {
        val file = File(exportsDir(), "${safeName(data.projectName)}_${DateUtils.formatForFilename()}.md")
        file.writeText(MarkdownReportBuilder.build(data))
        return file
    }

    fun exportTxt(data: ReportData): File {
        val file = File(exportsDir(), "${safeName(data.projectName)}_${DateUtils.formatForFilename()}.txt")
        file.writeText(MarkdownReportBuilder.buildPlainText(data))
        return file
    }

    fun exportJson(data: ReportData): File {
        val file = File(exportsDir(), "${safeName(data.projectName)}_${DateUtils.formatForFilename()}.json")
        val root = JSONObject().apply {
            put("project_name", data.projectName)
            put("analyst", data.analystName)
            put("generated_on", data.generatedOn)
            put("domain", data.domain)
            put("entity", data.entity)
            put("keyword", data.keyword)
            put("description", data.description)
            put("findings_summary", data.findingsSummary)
            val queriesArr = JSONArray()
            data.queries.forEach {
                queriesArr.put(JSONObject().apply {
                    put("engine", it.engine); put("category", it.category)
                    put("title", it.title); put("query", it.query)
                })
            }
            put("queries", queriesArr)
            val notesArr = JSONArray()
            data.notes.forEach {
                notesArr.put(JSONObject().apply {
                    put("title", it.title); put("content", it.content); put("created_at", it.createdAt)
                })
            }
            put("notes", notesArr)
        }
        file.writeText(root.toString(2))
        return file
    }

    fun exportPdf(data: ReportData): File {
        val file = File(exportsDir(), "${safeName(data.projectName)}_${DateUtils.formatForFilename()}.pdf")
        PdfReportGenerator.generate(data, file)
        return file
    }

    /** Builds a share Intent (chooser) for the given exported file. */
    fun shareIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "com.kushagra.reconx.fileprovider", file)
        val mime = when (file.extension) {
            "pdf" -> "application/pdf"
            "json" -> "application/json"
            "md", "txt" -> "text/plain"
            else -> "*/*"
        }
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, "Share report")
    }
}
