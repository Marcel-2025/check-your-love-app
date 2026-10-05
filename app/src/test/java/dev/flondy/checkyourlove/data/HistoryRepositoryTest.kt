package dev.flondy.checkyourlove.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
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

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class HistoryRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: HistoryRepository
    private var time = 1_000L

    @Before fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder<AppDatabase>(context).setDriver(AndroidSQLiteDriver()).build()
        repo = HistoryRepository(db.resultDao()) { time++ }
    }

    @After fun tearDown() = db.close()

    @Test fun `repeated and swapped pair is stored once and moves to top`() = runTest {
        repo.save("Anna", "Ben", LoveCalculator.calculate("Anna", "Ben"))
        repo.save("Clara", "Dan", LoveCalculator.calculate("Clara", "Dan"))
        val again = repo.save(" ben ", "ANNA", LoveCalculator.calculate(" ben ", "ANNA"))
        val all = repo.observeAll().first()
        assertThat(all).hasSize(2)
        assertThat(all.first().id).isEqualTo(again.id)
        assertThat(all.first().nameA).isEqualTo("ben")
    }

    @Test fun `favorites survive re-test and clear can keep them`() = runTest {
        val a = repo.save("Anna", "Ben", LoveCalculator.calculate("Anna", "Ben"))
        repo.save("Clara", "Dan", LoveCalculator.calculate("Clara", "Dan"))
        repo.setFavorite(a.id, true)
        val again = repo.save("Anna", "Ben", LoveCalculator.calculate("Anna", "Ben"))
        assertThat(again.favorite).isTrue()
        assertThat(repo.observeFavorites().first().map { it.id }).containsExactly(a.id)

        repo.clear(keepFavorites = true)
        assertThat(repo.observeAll().first().map { it.id }).containsExactly(a.id)
        repo.clear(keepFavorites = false)
        assertThat(repo.observeAll().first()).isEmpty()
    }

    @Test fun `delete and undo restore the same entry`() = runTest {
        val a = repo.save("Anna", "Ben", LoveCalculator.calculate("Anna", "Ben"))
        repo.setFavorite(a.id, true)
        val entity = repo.observeAll().first().single()
        repo.delete(a.id)
        assertThat(repo.observeAll().first()).isEmpty()
        repo.restore(entity)
        assertThat(repo.observeAll().first().single()).isEqualTo(entity)
    }
}
