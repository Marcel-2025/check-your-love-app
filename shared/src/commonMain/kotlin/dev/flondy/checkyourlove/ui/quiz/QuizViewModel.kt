package dev.flondy.checkyourlove.ui.quiz

import androidx.lifecycle.ViewModel
import dev.flondy.checkyourlove.content.QuizPack
import dev.flondy.checkyourlove.content.QuizQuestion
import dev.flondy.checkyourlove.core.LoveCalculator
import dev.flondy.checkyourlove.core.NameError
import dev.flondy.checkyourlove.core.QuizEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

enum class QuizPhase { SETUP, ANSWER, HANDOVER, GUESS, REVEAL, RESULT }

data class QuizUiState(
    val phase: QuizPhase = QuizPhase.SETUP,
    val nameA: String = "",
    val nameB: String = "",
    val errorA: NameError? = null,
    val errorB: NameError? = null,
    val sameNames: Boolean = false,
    val pack: QuizPack = QuizPack.BASIC,
    val questions: List<QuizQuestion> = emptyList(),
    val index: Int = 0,
    val answer: Int? = null,
    val guess: Int? = null,
    val correct: Int = 0,
) {
    val question: QuizQuestion? get() = questions.getOrNull(index)
    val total: Int get() = questions.size
    private val names get() = listOf(LoveCalculator.displayName(nameA), LoveCalculator.displayName(nameB))
    /** Person, die die aktuelle Frage für sich beantwortet. */
    val answererName: String get() = names[QuizEngine.answerer(index)]
    /** Person, die die Antwort rät. */
    val guesserName: String get() = names[1 - QuizEngine.answerer(index)]
    val isLast: Boolean get() = index >= questions.lastIndex
    val score: Int get() = QuizEngine.score(correct, total)
}

class QuizViewModel(private val random: Random = Random.Default) : ViewModel() {
    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    fun setNameA(v: String) = _state.update { it.copy(nameA = LoveCalculator.takeCodePoints(v, LoveCalculator.MAX_NAME_LENGTH), errorA = null, sameNames = false) }
    fun setNameB(v: String) = _state.update { it.copy(nameB = LoveCalculator.takeCodePoints(v, LoveCalculator.MAX_NAME_LENGTH), errorB = null, sameNames = false) }
    fun selectPack(pack: QuizPack) = _state.update { it.copy(pack = pack) }

    /** Startet eine Runde mit Fragen aus [pool] (bereits in der Gerätesprache). */
    fun start(pool: List<QuizQuestion>): Boolean {
        val s = _state.value
        val ea = LoveCalculator.validate(s.nameA)
        val eb = LoveCalculator.validate(s.nameB)
        val same = ea == null && eb == null && LoveCalculator.normalize(s.nameA) == LoveCalculator.normalize(s.nameB)
        if (ea != null || eb != null || same || pool.isEmpty()) {
            _state.update { it.copy(errorA = ea, errorB = eb, sameNames = same) }
            return false
        }
        val picked = QuizEngine.pick(pool.size, QuizEngine.QUESTIONS_PER_ROUND, random).map { pool[it] }
        _state.update { it.copy(phase = QuizPhase.ANSWER, questions = picked, index = 0, answer = null, guess = null, correct = 0) }
        return true
    }

    fun answer(option: Int) = _state.update {
        if (it.phase != QuizPhase.ANSWER) it else it.copy(answer = option, phase = QuizPhase.HANDOVER)
    }

    fun ready() = _state.update { if (it.phase == QuizPhase.HANDOVER) it.copy(phase = QuizPhase.GUESS) else it }

    fun guess(option: Int) = _state.update {
        if (it.phase != QuizPhase.GUESS) return@update it
        val hit = option == it.answer
        it.copy(guess = option, phase = QuizPhase.REVEAL, correct = it.correct + if (hit) 1 else 0)
    }

    /** Nach der Auflösung: nächste Frage (das Handy ist schon bei der nächsten antwortenden Person) oder Ergebnis. */
    fun next() = _state.update {
        if (it.phase != QuizPhase.REVEAL) it
        else if (it.isLast) it.copy(phase = QuizPhase.RESULT)
        else it.copy(phase = QuizPhase.ANSWER, index = it.index + 1, answer = null, guess = null)
    }

    fun playAgain(pool: List<QuizQuestion>) { _state.update { it.copy(phase = QuizPhase.SETUP) }; start(pool) }

    fun backToSetup() = _state.update { it.copy(phase = QuizPhase.SETUP, questions = emptyList(), index = 0, answer = null, guess = null, correct = 0) }
}
