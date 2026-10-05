package dev.flondy.checkyourlove.platform

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.flondy.checkyourlove.data.SETTINGS_FILE_NAME
import dev.flondy.checkyourlove.data.db.AppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(url?.path) { "Dokumentenordner nicht verfügbar" }
}

internal fun iosDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> =
    Room.databaseBuilder<AppDatabase>(name = documentDirectory() + "/" + AppDatabase.NAME)
        .setDriver(BundledSQLiteDriver())

internal fun iosSettingsPath(): String = documentDirectory() + "/" + SETTINGS_FILE_NAME
