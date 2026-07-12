package com.kushagra.reconx.repository

import com.kushagra.reconx.database.dao.CveDao
import com.kushagra.reconx.database.entity.CveEntity
import org.json.JSONArray

/**
 * CveRepository.kt
 * =================
 * Offline CVE database: the user imports a JSON export (e.g. a filtered
 * NVD feed or their own curated list) once, while online; from then on,
 * lookups are pure local SQLite queries with zero network calls -- exactly
 * the "import once, search forever offline" workflow requested.
 *
 * Expected import format: a JSON array of objects with keys
 * cveId, product, version, severity, score, summary.
 */
class CveRepository(private val cveDao: CveDao) {

    suspend fun importFromJson(json: String): Int {
        val array = JSONArray(json)
        val entries = (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            CveEntity(
                cveId = obj.getString("cveId"),
                product = obj.optString("product", ""),
                version = obj.optString("version", ""),
                severity = obj.optString("severity", "UNKNOWN"),
                score = obj.optDouble("score", 0.0),
                summary = obj.optString("summary", ""),
            )
        }
        cveDao.upsertAll(entries)
        return entries.size
    }

    suspend fun findByProduct(product: String): List<CveEntity> = cveDao.findByProduct(product)
    suspend fun count(): Int = cveDao.count()
    suspend fun clear() = cveDao.clearAll()
}
