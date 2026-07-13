package com.kushagra.reconx.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kushagra.reconx.database.dao.ActivityDao
import com.kushagra.reconx.database.dao.CveDao
import com.kushagra.reconx.database.dao.HistoryDao
import com.kushagra.reconx.database.dao.NoteDao
import com.kushagra.reconx.database.dao.ProjectDao
import com.kushagra.reconx.database.dao.QueryDao
import com.kushagra.reconx.database.dao.ReportDao
import com.kushagra.reconx.database.entity.ActivityEntity
import com.kushagra.reconx.database.entity.CveEntity
import com.kushagra.reconx.database.entity.HistoryEntity
import com.kushagra.reconx.database.entity.NoteEntity
import com.kushagra.reconx.database.entity.ProjectEntity
import com.kushagra.reconx.database.entity.QueryEntity
import com.kushagra.reconx.database.entity.ReportEntity

/**
 * ReconXDatabase.kt
 * =================
 * The single Room database for the whole app -- fully offline, stored in
 * the app's private storage (not accessible to other apps without root).
 *
 * Note on "encryption": Room/SQLite itself does not encrypt the .db file.
 * True at-rest encryption would require SQLCipher, which is a fairly heavy
 * native dependency and conflicts with the "minimal dependencies / small
 * APK" requirement. As a middle ground that needs no extra native library,
 * this app (a) keeps the database in app-private storage that's sandboxed
 * by Android from other apps, and (b) uses androidx.security-crypto's
 * EncryptedFile for exported reports containing sensitive findings (see
 * export/SecureFileExporter.kt). If full SQLCipher-grade DB encryption is
 * required later, swap Room.databaseBuilder for SupportFactory from
 * net.zetetic:android-database-sqlcipher.
 */
@Database(
    entities = [
        ProjectEntity::class,
        QueryEntity::class,
        NoteEntity::class,
        ReportEntity::class,
        ActivityEntity::class,
        HistoryEntity::class,
        CveEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class ReconXDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun queryDao(): QueryDao
    abstract fun noteDao(): NoteDao
    abstract fun reportDao(): ReportDao
    abstract fun activityDao(): ActivityDao
    abstract fun historyDao(): HistoryDao
    abstract fun cveDao(): CveDao

    companion object {
        @Volatile private var INSTANCE: ReconXDatabase? = null

        fun getInstance(context: Context): ReconXDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ReconXDatabase::class.java,
                    "reconx.db",
                ).build().also { INSTANCE = it }
            }
    }
}
