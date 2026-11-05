package dev.flondy.checkyourlove.ui.group

import androidx.lifecycle.ViewModel
import dev.flondy.checkyourlove.core.GroupCalculator
import dev.flondy.checkyourlove.core.GroupPair
import dev.flondy.checkyourlove.core.LoveCalculator
import dev.flondy.checkyourlove.core.NameError
import dev.flondy.checkyourlove.core.TestMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Fehler eines Namensfeldes im Gruppen-Test. */
sealed interface GroupFieldError {
    data class Invalid(val error: NameError) : GroupFieldError
    data object Duplicate : GroupFieldError
}

data class GroupUiState(
    /** Stabile Schlüssel für Animationen beim Hinzufügen/Entfernen. */
    val keys: List<Int> = listOf(0, 1, 2),
    val names: List<String> = listOf("", "", ""),
    val errors: Map<Int, GroupFieldError> = emptyMap(),
    val mode: TestMode = TestMode.LOVE,
    val tooFew: Boolean = false,
    val results: List<GroupPair>? = null,
) {
    val canAdd: Boolean get() = names.size < GroupCalculator.MAX_NAMES
    val canRemove: Boolean get() = names.size > GroupCalculator.MIN_NAMES
}

/** Gruppen-Test: Ergebnisse werden nicht im Verlauf gespeichert (sonst wäre er schnell voll). */
class GroupViewModel : ViewModel() {
    private val _state = MutableStateFlow(GroupUiState())
    val state: StateFlow<GroupUiState> = _state.asStateFlow()
    private var nextKey = 3

    fun setName(index: Int, value: String) = _state.update {
        if (index !in it.names.indices) return@update it
        val names = it.names.toMutableList().also { list -> list[index] = LoveCalculator.takeCodePoints(value, LoveCalculator.MAX_NAME_LENGTH) }
        it.copy(names = names, errors = it.errors - index, tooFew = false)
    }

    fun add() = _state.update {
        if (!it.canAdd) it else it.copy(names = it.names + "", keys = it.keys + nextKey++)
    }

    fun remove(index: Int) = _state.update {
        if (!it.canRemove || index !in it.names.indices) return@update it
        it.copy(
            names = it.names.filterIndexed { i, _ -> i != index },
            keys = it.keys.filterIndexed { i, _ -> i != index },
            errors = emptyMap(),
        )
    }

    fun setMode(mode: TestMode) = _state.update { if (it.results == null) it.copy(mode = mode) else it }

    /** Prüft alle Felder; leere Felder am Ende werden ignoriert, solange genug Namen bleiben. */
    fun evaluate(): Boolean {
        val s = _state.value
        val filled = s.names.withIndex().filter { it.value.isNotBlank() }
        val errors = mutableMapOf<Int, GroupFieldError>()
        filled.forEach { (i, name) -> LoveCalculator.validate(name)?.let { errors[i] = GroupFieldError.Invalid(it) } }
        GroupCalculator.duplicateIndices(s.names).forEach { if (it !in errors) errors[it] = GroupFieldError.Duplicate }
        val tooFew = filled.size < GroupCalculator.MIN_NAMES
        if (errors.isNotEmpty() || tooFew) {
            _state.update { it.copy(errors = errors, tooFew = tooFew) }
            return false
        }
        _state.update { it.copy(results = GroupCalculator.pairs(filled.map { v -> v.value }, s.mode), errors = emptyMap()) }
        return true
    }

    fun edit() = _state.update { it.copy(results = null) }
}
