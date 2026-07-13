package com.kushagra.reconx.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entities.kt
 * ===========
 * Room entity definitions. Each table maps 1:1 to a feature from the
 * original desktop app (projects, saved queries, notes, activity log,
 * settings) plus the new mobile-only additions (favorites, reports).
 */

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val domain: String = "",
    val entity: String = "",
    val keyword: String = "",
    val status: String = "Active",      // Active / Archived
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "queries")
data class QueryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long?,               // null = not attached to a project (quick search)
    val engine: String,                 // GOOGLE / BING / GITHUB / SHODAN / CENSYS
    val category: String,
    val title: String,
    val queryText: String,
    val tags: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long,
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long?,
    val title: String,
    val contentMarkdown: String,
    val category: String = "General",
    val tags: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val analystName: String,
    val format: String,             // MARKDOWN / JSON / TXT / PDF
    val filePath: String,
    val createdAt: Long,
)

@Entity(tableName = "activity_log")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val details: String = "",
    val timestamp: Long,
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val toolName: String,           // e.g. "WHOIS", "DNS Lookup", "Dork Builder"
    val inputSummary: String,
    val timestamp: Long,
)

@Entity(tableName = "cve_entries")
data class CveEntity(
    @PrimaryKey val cveId: String,
    val product: String,
    val version: String,
    val severity: String,
    val score: Double,
    val summary: String,
)
