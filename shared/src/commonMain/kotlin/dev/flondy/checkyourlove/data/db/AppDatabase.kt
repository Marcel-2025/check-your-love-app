package dev.flondy.checkyourlove.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [LoveResultEntity::class, LegacyNameValueEntity::class],
    version = AppDatabase.VERSION,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resultDao(): ResultDao

    companion object {
        /** Gleicher Dateiname wie in der alten App, damit bestehende Daten übernommen werden. */
        const val NAME = "CheckYourLove.db"
        const val VERSION = 2

        /** Fertigstellung des plattformspezifischen Builders (Treiber/Pfad kommen von der Plattform). */
        fun build(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase =
            builder
                .addMigrations(MIGRATION_1_2)
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
    }
}

// Room generiert die Implementierungen pro Plattform.
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/**
 * Version 1 (alte Android-App, eigener SQLiteOpenHelper): Tabelle `NameValuePairs(Names, Value)`.
 * Version 2: Room-Schema. Alte Zeilen werden vollständig nach `legacy_name_values` kopiert.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `love_results` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name_a` TEXT NOT NULL, `name_b` TEXT NOT NULL, `pair_key` TEXT NOT NULL, " +
                "`percent` INTEGER NOT NULL, `variant` INTEGER NOT NULL, `algorithm_version` INTEGER NOT NULL, " +
                "`created_at` INTEGER NOT NULL, `favorite` INTEGER NOT NULL)"
        )
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_love_results_pair_key` ON `love_results` (`pair_key`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_love_results_created_at` ON `love_results` (`created_at`)")
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `legacy_name_values` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT, `value` INTEGER)"
        )
        val hasLegacyTable = connection.prepare(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'NameValuePairs'"
        ).use { it.step() }
        if (hasLegacyTable) {
            connection.execSQL(
                "INSERT INTO `legacy_name_values` (`name`, `value`) SELECT `Names`, `Value` FROM `NameValuePairs`"
            )
            connection.execSQL("DROP TABLE `NameValuePairs`")
        }
    }
}
