package dev.flondy.checkyourlove.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [LoveResultEntity::class, LegacyNameValueEntity::class],
    version = AppDatabase.VERSION,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resultDao(): ResultDao

    companion object {
        /** Gleicher Dateiname wie in der alten App, damit bestehende Daten übernommen werden. */
        const val NAME = "CheckYourLove.db"
        const val VERSION = 2

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NAME)
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}

/**
 * Version 1 (alte App, eigener SQLiteOpenHelper): Tabelle `NameValuePairs(Names, Value)`.
 * Version 2: Room-Schema. Alte Zeilen werden vollständig nach `legacy_name_values` kopiert.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `love_results` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name_a` TEXT NOT NULL, `name_b` TEXT NOT NULL, `pair_key` TEXT NOT NULL, " +
                "`percent` INTEGER NOT NULL, `variant` INTEGER NOT NULL, `algorithm_version` INTEGER NOT NULL, " +
                "`created_at` INTEGER NOT NULL, `favorite` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_love_results_pair_key` ON `love_results` (`pair_key`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_love_results_created_at` ON `love_results` (`created_at`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `legacy_name_values` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT, `value` INTEGER)"
        )
        val hasLegacyTable = db.query(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'NameValuePairs'"
        ).use { it.moveToFirst() }
        if (hasLegacyTable) {
            db.execSQL(
                "INSERT INTO `legacy_name_values` (`name`, `value`) SELECT `Names`, `Value` FROM `NameValuePairs`"
            )
            db.execSQL("DROP TABLE `NameValuePairs`")
        }
    }
}
