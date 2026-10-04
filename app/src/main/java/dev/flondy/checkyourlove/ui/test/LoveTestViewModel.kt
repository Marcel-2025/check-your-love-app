package dev.flondy.checkyourlove.ui.test

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.flondy.checkyourlove.core.LoveCalculator
import dev.flondy.checkyourlove.core.NameError
import dev.flondy.checkyourlove.core.ResultRange
import dev.flondy.checkyourlove.data.HistoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TestPhase { INPUT, EVALUATING, RESULT }

data class ResultUi(
    val id: Long?,
    val nameA: String,
    val nameB: String,
    val percent: Int,
    val range: ResultRange,
    val variant: Int,
    val favorite: Boolean,
)

data class LoveTestUiState(
    val nameA: String = "",
    val nameB: String = "",
    val errorA: NameError? = null,
    val errorB: NameError? = null,
    val phase: TestPhase = TestPhase.INPUT,
    val result: ResultUi? = null,
) {
    val canSwap: Boolean get() = phase == TestPhase.INPUT && (nameA.isNotEmpty() || nameB.isNotEmpty())
    val canReset: Boolean get() = phase != TestPhase.EVALUATING && (nameA.isNotEmpty() || nameB.isNotEmpty() || result != null)
}

sealed interface TestEvent {
    data class Completed(val result: ResultUi) : TestEvent
    data object SaveFailed : TestEvent
}

/**
 * Zustand des Liebestests. Rotation: ViewModel bleibt erhalten.
 * Prozess-Neustart: Namen und Ergebnisanzeige werden über [SavedStateHandle] wiederhergestellt;
 * das Ergebnis selbst ist deterministisch und wird neu berechnet.
 */
class LoveTestViewModel(
    private val history: HistoryRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(
        LoveTestUiState(
            nameA = savedState[KEY_A] ?: "",
            nameB = savedState[KEY_B] ?: "",
        )
    )
    val state: StateFlow<LoveTestUiState> = _state.asStateFlow()

    private val _events = Channel<TestEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var evaluationJob: Job? = null

    init {
        if (savedState.get<Boolean>(KEY_SHOWING_RESULT) == true) restoreResult()
    }

    fun onNameAChange(value: String) = updateName(value, isA = true)
    fun onNameBChange(value: String) = updateName(value, isA = false)

    private fun updateName(value: String, isA: Boolean) {
        if (_state.value.phase == TestPhase.EVALUATING) return
        val limited = limitLength(value)
        _state.update {
            if (isA) it.copy(nameA = limited, errorA = null) else it.copy(nameB = limited, errorB = null)
        }
        savedState[if (isA) KEY_A else KEY_B] = limited
    }

    fun swap() {
        val s = _state.value
        if (s.phase == TestPhase.EVALUATING) return
        _state.update { it.copy(nameA = s.nameB, nameB = s.nameA, errorA = s.errorB, errorB = s.errorA) }
        savedState[KEY_A] = s.nameB
        savedState[KEY_B] = s.nameA
    }

    fun reset() {
        evaluationJob?.cancel()
        _state.value = LoveTestUiState()
        savedState[KEY_A] = ""
        savedState[KEY_B] = ""
        savedState[KEY_SHOWING_RESULT] = false
    }

    /** Zurück zur Eingabe; Namen bleiben zum Anpassen erhalten. */
    fun newTest() {
        evaluationJob?.cancel()
        _state.update { it.copy(phase = TestPhase.INPUT, result = null) }
        savedState[KEY_SHOWING_RESULT] = false
    }

    /**
     * Startet die Auswertung. Mehrfachklicks während der Auswertung werden ignoriert.
     * Ungültige Eingaben setzen nur Fehlermeldungen – es bleibt kein Ladezustand hängen.
     */
    fun evaluate(animationMillis: Long) {
        val s = _state.value
        if (s.phase != TestPhase.INPUT) return
        val errorA = LoveCalculator.validate(s.nameA)
        val errorB = LoveCalculator.validate(s.nameB)
        if (errorA != null || errorB != null) {
            _state.update { it.copy(errorA = errorA, errorB = errorB) }
            return
        }
        val calculation = LoveCalculator.calculate(s.nameA, s.nameB)
        _state.update { it.copy(phase = TestPhase.EVALUATING, errorA = null, errorB = null) }
        evaluationJob = viewModelScope.launch {
            val saved = runCatching { history.save(s.nameA, s.nameB, calculation) }.getOrNull()
            delay(animationMillis)
            val result = ResultUi(
                id = saved?.id,
                nameA = LoveCalculator.displayName(s.nameA),
                nameB = LoveCalculator.displayName(s.nameB),
                percent = calculation.percent,
                range = calculation.range,
                variant = calculation.variant,
                favorite = saved?.favorite ?: false,
            )
            _state.update { it.copy(phase = TestPhase.RESULT, result = result) }
            savedState[KEY_SHOWING_RESULT] = true
            savedState[KEY_RESULT_ID] = saved?.id ?: -1L
            if (saved == null) _events.send(TestEvent.SaveFailed)
            _events.send(TestEvent.Completed(result))
        }
    }

    fun toggleFavorite() {
        val result = _state.value.result ?: return
        val id = result.id ?: return
        val newValue = !result.favorite
        _state.update { it.copy(result = result.copy(favorite = newValue)) }
        viewModelScope.launch {
            runCatching { history.setFavorite(id, newValue) }
                .onFailure { _state.update { s -> s.copy(result = s.result?.copy(favorite = !newValue)) } }
        }
    }

    private fun restoreResult() {
        val s = _state.value
        if (LoveCalculator.validate(s.nameA) != null || LoveCalculator.validate(s.nameB) != null) return
        val calculation = LoveCalculator.calculate(s.nameA, s.nameB)
        val id = savedState.get<Long>(KEY_RESULT_ID)?.takeIf { it > 0 }
        _state.update {
            it.copy(
                phase = TestPhase.RESULT,
                result = ResultUi(
                    id, LoveCalculator.displayName(s.nameA), LoveCalculator.displayName(s.nameB),
                    calculation.percent, calculation.range, calculation.variant, favorite = false,
                ),
            )
        }
        if (id != null) viewModelScope.launch {
            val entity = runCatching { history.observe(id).firstOrNull() }.getOrNull()
            _state.update { st -> st.copy(result = st.result?.copy(id = entity?.id, favorite = entity?.favorite ?: false)) }
        }
    }

    private fun limitLength(value: String): String {
        val max = LoveCalculator.MAX_NAME_LENGTH
        return if (value.codePointCount(0, value.length) <= max) value
        else value.substring(0, value.offsetByCodePoints(0, max))
    }

    companion object {
        private const val KEY_A = "name_a"
        private const val KEY_B = "name_b"
        private const val KEY_SHOWING_RESULT = "showing_result"
        private const val KEY_RESULT_ID = "result_id"
    }
}
