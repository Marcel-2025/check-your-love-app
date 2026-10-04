package dev.flondy.checkyourlove.ui

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import dev.flondy.checkyourlove.core.LoveCalculator
import dev.flondy.checkyourlove.core.NameError
import dev.flondy.checkyourlove.data.HistoryRepository
import dev.flondy.checkyourlove.data.db.LoveResultEntity
import dev.flondy.checkyourlove.data.db.ResultDao
import dev.flondy.checkyourlove.ui.test.LoveTestViewModel
import dev.flondy.checkyourlove.ui.test.TestPhase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/** In-Memory-DAO für schnelle JVM-Tests. */
class FakeResultDao : ResultDao {
    val rows = MutableStateFlow<List<LoveResultEntity>>(emptyList())
    private var nextId = 1L
    var failInsert = false
    override fun observeAll(): Flow<List<LoveResultEntity>> = rows.map { it.sortedByDescending { r -> r.createdAt } }
    override fun observeFavorites(): Flow<List<LoveResultEntity>> = rows.map { l -> l.filter { it.favorite } }
    override fun observeById(id: Long): Flow<LoveResultEntity?> = rows.map { l -> l.firstOrNull { it.id == id } }
    override suspend fun findByPairKey(pairKey: String) = rows.value.firstOrNull { it.pairKey == pairKey }
    override suspend fun insert(entity: LoveResultEntity): Long {
        if (failInsert) error("disk full")
        val e = entity.copy(id = nextId++); rows.value = rows.value + e; return e.id
    }
    override suspend fun restore(entity: LoveResultEntity): Long { rows.value = rows.value.filterNot { it.id == entity.id } + entity; return entity.id }
    override suspend fun touch(id: Long, nameA: String, nameB: String, createdAt: Long) {
        rows.value = rows.value.map { if (it.id == id) it.copy(nameA = nameA, nameB = nameB, createdAt = createdAt) else it }
    }
    override suspend fun setFavorite(id: Long, favorite: Boolean) {
        rows.value = rows.value.map { if (it.id == id) it.copy(favorite = favorite) else it }
    }
    override suspend fun delete(id: Long) { rows.value = rows.value.filterNot { it.id == id } }
    override suspend fun deleteAll() { rows.value = emptyList() }
    override suspend fun deleteNonFavorites() { rows.value = rows.value.filter { it.favorite } }
    override suspend fun legacyCount(): Int = 0
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoveTestViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val dao = FakeResultDao()
    private val repo = HistoryRepository(dao) { 42L }

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `invalid input shows errors and never gets stuck in loading`() = runTest(dispatcher) {
        val vm = LoveTestViewModel(repo, SavedStateHandle())
        vm.onNameAChange("   ")
        vm.evaluate(1_000)
        assertThat(vm.state.value.phase).isEqualTo(TestPhase.INPUT)
        assertThat(vm.state.value.errorA).isEqualTo(NameError.EMPTY)
        assertThat(vm.state.value.errorB).isEqualTo(NameError.EMPTY)
        vm.onNameAChange("Anna")
        assertThat(vm.state.value.errorA).isNull() // Fehler verschwindet beim Tippen
        vm.onNameBChange("123")
        vm.evaluate(1_000)
        assertThat(vm.state.value.errorB).isEqualTo(NameError.NO_LETTER)
        assertThat(vm.state.value.phase).isEqualTo(TestPhase.INPUT)
    }

    @Test fun `valid input evaluates once, saves and shows deterministic result`() = runTest(dispatcher) {
        val vm = LoveTestViewModel(repo, SavedStateHandle())
        vm.onNameAChange("Anna")
        vm.onNameBChange("Ben")
        vm.evaluate(1_000)
        vm.evaluate(1_000) // Mehrfachklick
        vm.evaluate(1_000)
        assertThat(vm.state.value.phase).isEqualTo(TestPhase.EVALUATING)
        advanceTimeBy(500)
        assertThat(vm.state.value.phase).isEqualTo(TestPhase.EVALUATING)
        advanceUntilIdle()
        val s = vm.state.value
        assertThat(s.phase).isEqualTo(TestPhase.RESULT)
        assertThat(s.result!!.percent).isEqualTo(LoveCalculator.calculate("Ben", "Anna").percent)
        assertThat(dao.rows.value).hasSize(1)
    }

    @Test fun `swap reset and new test`() = runTest(dispatcher) {
        val vm = LoveTestViewModel(repo, SavedStateHandle())
        vm.onNameAChange("Anna"); vm.onNameBChange("Ben")
        vm.swap()
        assertThat(vm.state.value.nameA).isEqualTo("Ben")
        assertThat(vm.state.value.nameB).isEqualTo("Anna")
        vm.evaluate(10); advanceUntilIdle()
        vm.newTest()
        assertThat(vm.state.value.phase).isEqualTo(TestPhase.INPUT)
        assertThat(vm.state.value.nameA).isEqualTo("Ben")
        vm.reset()
        assertThat(vm.state.value.nameA).isEmpty()
        assertThat(vm.state.value.result).isNull()
    }

    @Test fun `long names are limited`() = runTest(dispatcher) {
        val vm = LoveTestViewModel(repo, SavedStateHandle())
        vm.onNameAChange("x".repeat(200))
        assertThat(vm.state.value.nameA.length).isEqualTo(LoveCalculator.MAX_NAME_LENGTH)
    }

    @Test fun `favorite toggles and persists`() = runTest(dispatcher) {
        val vm = LoveTestViewModel(repo, SavedStateHandle())
        vm.onNameAChange("Anna"); vm.onNameBChange("Ben")
        vm.evaluate(10); advanceUntilIdle()
        vm.toggleFavorite(); advanceUntilIdle()
        assertThat(vm.state.value.result!!.favorite).isTrue()
        assertThat(dao.rows.value.single().favorite).isTrue()
    }

    @Test fun `result still shown when saving fails`() = runTest(dispatcher) {
        dao.failInsert = true
        val vm = LoveTestViewModel(repo, SavedStateHandle())
        vm.onNameAChange("Anna"); vm.onNameBChange("Ben")
        vm.evaluate(10); advanceUntilIdle()
        assertThat(vm.state.value.phase).isEqualTo(TestPhase.RESULT)
        assertThat(vm.state.value.result!!.id).isNull()
    }

    @Test fun `state is restored after process death`() = runTest(dispatcher) {
        val handle = SavedStateHandle()
        val vm = LoveTestViewModel(repo, handle)
        vm.onNameAChange("Anna"); vm.onNameBChange("Ben")
        vm.evaluate(10); advanceUntilIdle()
        vm.toggleFavorite(); advanceUntilIdle()
        // neue Instanz mit demselben SavedStateHandle simuliert Prozessneustart
        val restored = LoveTestViewModel(repo, SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) }))
        advanceUntilIdle()
        assertThat(restored.state.value.phase).isEqualTo(TestPhase.RESULT)
        assertThat(restored.state.value.result!!.percent).isEqualTo(vm.state.value.result!!.percent)
        assertThat(restored.state.value.result!!.favorite).isTrue()
    }
}
