package dev.flondy.checkyourlove.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dev.flondy.checkyourlove.core.LoveCalculator
import dev.flondy.checkyourlove.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Prüft die Übernahme der Datenbank der alten App (Version 1, eigener SQLiteOpenHelper). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class MigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before fun clean() { context.deleteDatabase(AppDatabase.NAME) }
    @After fun cleanup() { context.deleteDatabase(AppDatabase.NAME) }

    /** Exakt das Schema der alten DbHelper-Klasse. */
    private class LegacyHelper(context: Context) : SQLiteOpenHelper(context, AppDatabase.NAME, null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE NameValuePairs (Names VARCHAR(255), Value INTEGER);")
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }

    @Test fun `legacy rows are preserved after migration`() = runTest {
        LegacyHelper(context).writableDatabase.use { db ->
            listOf("Anna" to 12, "Ben" to 49, "Zoë" to 0).forEach { (n, v) ->
                db.insert("NameValuePairs", null, ContentValues().apply { put("Names", n); put("Value", v) })
            }
            db.insert("NameValuePairs", null, ContentValues().apply { putNull("Names"); putNull("Value") })
        }

        val room = AppDatabase.create(context)
        val dao = room.resultDao()
        assertThat(dao.legacyCount()).isEqualTo(4)

        val sqlite = room.openHelper.readableDatabase
        sqlite.query("SELECT name, value FROM legacy_name_values ORDER BY id").use { c ->
            val rows = buildList { while (c.moveToNext()) add((if (c.isNull(0)) null else c.getString(0)) to (if (c.isNull(1)) null else c.getInt(1))) }
            assertThat(rows).containsExactly("Anna" to 12, "Ben" to 49, "Zoë" to 0, null to null).inOrder()
        }
        sqlite.query("SELECT name FROM sqlite_master WHERE name = 'NameValuePairs'").use { assertThat(it.count).isEqualTo(0) }
        assertThat(sqlite.version).isEqualTo(AppDatabase.VERSION)

        // neue Tabelle ist nach der Migration voll nutzbar
        val repo = HistoryRepository(dao) { 1L }
        repo.save("Anna", "Ben", LoveCalculator.calculate("Anna", "Ben"))
        assertThat(repo.observeAll().first()).hasSize(1)
        room.close()
    }

    @Test fun `fresh install creates empty schema`() = runTest {
        val room = AppDatabase.create(context)
        assertThat(room.resultDao().legacyCount()).isEqualTo(0)
        assertThat(room.resultDao().observeAll().first()).isEmpty()
        room.close()
    }

    @Test fun `legacy database without table is migrated safely`() = runTest {
        object : SQLiteOpenHelper(context, AppDatabase.NAME, null, 1) {
            override fun onCreate(db: SQLiteDatabase) = Unit
            override fun onUpgrade(db: SQLiteDatabase, o: Int, n: Int) = Unit
        }.writableDatabase.close()
        val room = AppDatabase.create(context)
        assertThat(room.resultDao().legacyCount()).isEqualTo(0)
        room.close()
    }
}
