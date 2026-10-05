package dev.flondy.checkyourlove.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import dev.flondy.checkyourlove.data.db.AppDatabase
import java.io.File

/** Gleiche Datei wie in der bisherigen Android-Version → bestehende Daten bleiben erhalten. */
fun androidDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val app = context.applicationContext
    return Room.databaseBuilder<AppDatabase>(app, app.getDatabasePath(AppDatabase.NAME).absolutePath)
        .setDriver(AndroidSQLiteDriver())
}

/** Pfad wie bisher bei `preferencesDataStore(name = "settings")`. */
fun androidSettingsPath(context: Context): String =
    File(context.applicationContext.filesDir, "datastore/$SETTINGS_FILE_NAME").absolutePath
