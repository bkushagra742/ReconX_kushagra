package com.kushagra.reconx.export

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.kushagra.reconx.reports.ReportData
import com.kushagra.reconx.utils.Constants
import java.io.File
import java.io.FileOutputStream

/**
 * PdfReportGenerator.kt
 * =======================
 * Renders a report to PDF using android.graphics.pdf.PdfDocument, which is
 * part of the Android SDK -- no external PDF library dependency required,
 * keeping the APK small.
 */
object PdfReportGenerator {
    private const val PAGE_WIDTH = 595   // A4 at 72dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f

    fun generate(data: ReportData, outFile: File) {
        val document = PdfDocument()
        val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
        val subtitlePaint = Paint().apply { textSize = 10f; isFakeBoldText = false; alpha = 180 }
        val headingPaint = Paint().apply { textSize = 13f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 10f }
        val monoPaint = Paint().apply { textSize = 9f }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas: Canvas = page.canvas
        var y = MARGIN

        fun newPageIfNeeded(lineHeight: Float) {
            if (y + lineHeight > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas
                y = MARGIN
            }
        }

        fun writeLine(text: String, paint: Paint, lineHeight: Float = paint.textSize + 6f) {
            newPageIfNeeded(lineHeight)
            canvas.drawText(text, MARGIN, y, paint)
            y += lineHeight
        }

        fun writeWrapped(text: String, paint: Paint, maxWidth: Float = PAGE_WIDTH - 2 * MARGIN) {
            val words = text.split(" ")
            var line = StringBuilder()
            for (word in words) {
                val trial = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(trial) > maxWidth) {
                    writeLine(line.toString(), paint)
                    line = StringBuilder(word)
                } else {
                    line = StringBuilder(trial)
                }
            }
            if (line.isNotEmpty()) writeLine(line.toString(), paint)
        }

        writeLine(Constants.APP_NAME, titlePaint, 24f)
        writeLine(Constants.APP_MOTTO, subtitlePaint, 18f)
        y += 6f
        writeLine("Project: ${data.projectName}", headingPaint, 20f)
        writeLine("Analyst: ${data.analystName}   Generated: ${data.generatedOn}", bodyPaint)
        writeLine("Domain: ${data.domain.ifBlank { "-" }}   Entity: ${data.entity.ifBlank { "-" }}   Keyword: ${data.keyword.ifBlank { "-" }}", bodyPaint)
        y += 8f

        if (data.description.isNotBlank()) {
            writeLine("Overview", headingPaint, 18f)
            writeWrapped(data.description, bodyPaint)
            y += 8f
        }

        if (data.findingsSummary.isNotBlank()) {
            writeLine("Findings Summary", headingPaint, 18f)
            writeWrapped(data.findingsSummary, bodyPaint)
            y += 8f
        }

        if (data.queries.isNotEmpty()) {
            writeLine("Saved Queries", headingPaint, 18f)
            data.queries.forEach { q ->
                writeWrapped("[${q.engine}] ${q.title}", bodyPaint)
                writeWrapped(q.query, monoPaint)
                y += 4f
            }
            y += 6f
        }

        if (data.notes.isNotEmpty()) {
            writeLine("Research Notes", headingPaint, 18f)
            data.notes.forEach { n ->
                writeWrapped("${n.title} (${n.createdAt})", bodyPaint)
                writeWrapped(n.content, bodyPaint)
                y += 4f
            }
        }

        document.finishPage(page)
        FileOutputStream(outFile).use { document.writeTo(it) }
        document.close()
    }
}
