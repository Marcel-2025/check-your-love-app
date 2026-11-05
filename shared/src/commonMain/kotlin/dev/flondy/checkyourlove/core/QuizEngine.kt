package dev.flondy.checkyourlove.core

/**
 * Paar-Quiz zu zweit an einem Gerät: Pro Frage beantwortet eine Person die Frage für sich,
 * die andere rät danach diese Antwort. Die Rollen wechseln jede Frage.
 */
object QuizEngine {
    const val QUESTIONS_PER_ROUND = 10

    /** Wer bei Frage [index] antwortet (0 = Person A, 1 = Person B). */
    fun answerer(index: Int): Int = index % 2

    /** Wählt [count] Fragen zufällig, aber ohne Wiederholung, aus [poolSize] Fragen. */
    fun pick(poolSize: Int, count: Int, random: kotlin.random.Random): List<Int> =
        (0 until poolSize).shuffled(random).take(count.coerceAtMost(poolSize))

    /** Anteil richtig geratener Antworten in Prozent (gerundet). */
    fun score(correct: Int, total: Int): Int = if (total <= 0) 0 else ((correct * 100.0) / total + 0.5).toInt()
}
