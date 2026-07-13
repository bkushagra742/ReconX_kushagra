package com.kushagra.reconx.utils

import com.kushagra.reconx.models.SecurityHeaderFinding

/**
 * RiskScorer.kt
 * ==============
 * Turns a set of security-header findings (and, optionally, CVE severities)
 * into a simple 0-100 risk score and a human severity label, for use in
 * the Reports module. This is a heuristic scoring aid for the analyst, not
 * an automated "vulnerable/not vulnerable" verdict.
 */
object RiskScorer {

    fun scoreHeaderFindings(findings: List<SecurityHeaderFinding>): Int {
        if (findings.isEmpty()) return 0
        val missingCount = findings.count { !it.present }
        val ratio = missingCount.toDouble() / findings.size
        return (ratio * 100).toInt().coerceIn(0, 100)
    }

    fun severityLabel(score: Int): String = when {
        score >= 70 -> "High"
        score >= 40 -> "Medium"
        score >= 15 -> "Low"
        else -> "Informational"
    }

    fun cvssSeverityLabel(score: Double): String = when {
        score >= 9.0 -> "Critical"
        score >= 7.0 -> "High"
        score >= 4.0 -> "Medium"
        score > 0.0 -> "Low"
        else -> "None"
    }
}
