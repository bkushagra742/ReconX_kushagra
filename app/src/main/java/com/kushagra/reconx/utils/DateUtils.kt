package com.kushagra.reconx.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val displayFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    private val fileStampFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun now(): Long = System.currentTimeMillis()

    fun formatForDisplay(timestamp: Long): String = displayFormat.format(Date(timestamp))

    fun formatForFilename(timestamp: Long = now()): String = fileStampFormat.format(Date(timestamp))

    fun relativeTime(timestamp: Long): String {
        val diff = now() - timestamp
        val minutes = diff / 60_000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> formatForDisplay(timestamp)
        }
    }
}
