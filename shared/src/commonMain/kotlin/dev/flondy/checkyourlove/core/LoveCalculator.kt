package dev.flondy.checkyourlove.core

import dev.flondy.checkyourlove.platform.isLetterCodePoint
import dev.flondy.checkyourlove.platform.normalizeNfkc

/** Mögliche Eingabefehler eines Namensfeldes. */
enum class NameError { EMPTY, NO_LETTER, TOO_LONG }

/** Ergebnis einer Berechnung – rein funktional, ohne Seiteneffekte. */
data class LoveCalculation(
    val pairKey: String,
    val percent: Int,
    val range: ResultRange,
    val variant: Int,
)

/**
 * Deterministische Ergebnislogik (Version 2).
 *
 * - Namen werden normalisiert: Unicode NFKC, unsichtbare Zeichen entfernt, Leerraum
 *   zusammengefasst, Groß-/Kleinschreibung sprachunabhängig ignoriert.
 *   Diakritika bleiben erhalten („Jürgen“ ≠ „Jurgen“), damit internationale Namen
 *   nicht verfälscht werden.
 * - Das Paar wird sortiert, daher ist die Reihenfolge der Eingabe egal.
 * - Aus SHA-256 des Paarschlüssels wird ein gleichverteilter Wert 0..100 abgeleitet.
 *
 * Premium oder andere Zustände fließen bewusst NICHT in die Berechnung ein.
 */
object LoveCalculator {
    const val ALGORITHM_VERSION = 2
    const val MAX_NAME_LENGTH = 40

    private const val SALT = "CheckYourLove/v2"
    private const val SEPARATOR = '\u001F'
    private val invisible = Regex("[\\u200B-\\u200D\\u2060\\uFEFF\\u00AD]")
    private val whitespace = Regex("\\s+")

    fun normalize(raw: String): String {
        var s = normalizeNfkc(raw)
        s = invisible.replace(s, "")
        s = s.lowercase() // sprachunabhängig (entspricht Locale.ROOT)
        s = normalizeNfkc(s)
        return whitespace.replace(s.trim(), " ")
    }

    /** Anzeigeform: nur getrimmt und Leerraum zusammengefasst, Schreibweise bleibt. */
    fun displayName(raw: String): String = whitespace.replace(invisible.replace(raw, "").trim(), " ")

    fun codePointLength(raw: String): Int {
        var count = 0
        var i = 0
        while (i < raw.length) {
            i += if (raw[i].isHighSurrogate() && i + 1 < raw.length && raw[i + 1].isLowSurrogate()) 2 else 1
            count++
        }
        return count
    }

    /** Kürzt auf höchstens [max] Codepunkte, ohne Surrogatpaare zu zerschneiden. */
    fun takeCodePoints(raw: String, max: Int): String {
        var count = 0
        var i = 0
        while (i < raw.length && count < max) {
            i += if (raw[i].isHighSurrogate() && i + 1 < raw.length && raw[i + 1].isLowSurrogate()) 2 else 1
            count++
        }
        return raw.substring(0, i)
    }

    private fun containsLetter(text: String): Boolean {
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c.isHighSurrogate() && i + 1 < text.length && text[i + 1].isLowSurrogate()) {
                val cp = 0x10000 + ((c.code - 0xD800) shl 10) + (text[i + 1].code - 0xDC00)
                if (isLetterCodePoint(cp)) return true
                i += 2
            } else {
                if (c.isLetter()) return true
                i++
            }
        }
        return false
    }

    fun validate(raw: String): NameError? {
        val display = displayName(raw)
        return when {
            display.isEmpty() -> NameError.EMPTY
            codePointLength(display) > MAX_NAME_LENGTH -> NameError.TOO_LONG
            !containsLetter(display) -> NameError.NO_LETTER
            else -> null
        }
    }

    fun pairKey(a: String, b: String): String {
        val (first, second) = listOf(normalize(a), normalize(b)).sorted()
        return "$first$SEPARATOR$second"
    }

    fun calculate(a: String, b: String): LoveCalculation {
        val key = pairKey(a, b)
        val digest = Sha256.digest("$SALT$SEPARATOR$key".encodeToByteArray())
        val value = digest.toULong(0)
        val percent = (value % 101uL).toInt()
        val variant = (digest.toULong(8) % ResultRange.VARIANTS.toULong()).toInt()
        return LoveCalculation(key, percent, ResultRange.of(percent), variant)
    }

    private fun ByteArray.toULong(offset: Int): ULong {
        var result = 0uL
        for (i in 0 until 8) result = (result shl 8) or (this[offset + i].toULong() and 0xFFuL)
        return result
    }
}

/** Ergebnisbereiche für Titel und Texte. */
enum class ResultRange(val min: Int, val max: Int) {
    R0(0, 19), R1(20, 39), R2(40, 59), R3(60, 79), R4(80, 94), R5(95, 100);

    val isHigh: Boolean get() = min >= 80

    companion object {
        const val VARIANTS = 3
        fun of(percent: Int): ResultRange {
            val p = percent.coerceIn(0, 100)
            return entries.first { p in it.min..it.max }
        }
    }
}
