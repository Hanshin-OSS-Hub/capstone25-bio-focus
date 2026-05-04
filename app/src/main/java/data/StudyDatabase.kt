package data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [StudyPlan::class], version = 4, exportSchema = false)
abstract class StudyDatabase : RoomDatabase() {

    abstract fun studyPlanDao(): StudyPlanDao

    companion object {
        @Volatile
        private var INSTANCE: StudyDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE study_plans ADD COLUMN sessionStartMillis INTEGER")
                db.execSQL("ALTER TABLE study_plans ADD COLUMN sessionEndMillis INTEGER")
                db.execSQL("ALTER TABLE study_plans ADD COLUMN hrvRmssd REAL")
                db.execSQL("ALTER TABLE study_plans ADD COLUMN hrvSyncedAt INTEGER")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE study_plans_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        subject TEXT NOT NULL,
                        title TEXT NOT NULL,
                        date TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        sessionStartMillis INTEGER,
                        sessionEndMillis INTEGER,
                        hrvRmssd REAL,
                        hrvSyncedAt INTEGER
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    INSERT INTO study_plans_new (
                        id,
                        subject,
                        title,
                        date,
                        isCompleted,
                        sessionStartMillis,
                        sessionEndMillis,
                        hrvRmssd,
                        hrvSyncedAt
                    )
                    SELECT
                        id,
                        subject,
                        title,
                        date,
                        isCompleted,
                        sessionStartMillis,
                        sessionEndMillis,
                        hrvRmssd,
                        hrvSyncedAt
                    FROM study_plans
                    """.trimIndent()
                )

                db.execSQL("DROP TABLE study_plans")
                db.execSQL("ALTER TABLE study_plans_new RENAME TO study_plans")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE study_plans ADD COLUMN targetMinutes INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getDatabase(context: Context): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}