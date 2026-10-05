package dev.flondy.checkyourlove.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.random.Random

class LoveCalculatorTest {

    @Test fun `same pair in any order gives the same result`() {
        val a = LoveCalculator.calculate("Anna", "Ben")
        val b = LoveCalculator.calculate("Ben", "Anna")
        assertThat(a).isEqualTo(b)
    }

    @Test fun `case and whitespace are ignored`() {
        val reference = LoveCalculator.calculate("Anna Maria", "Ben")
        assertThat(LoveCalculator.calculate("  anna   MARIA ", "BEN\t")).isEqualTo(reference)
        assertThat(LoveCalculator.calculate("ANNA MARIA", "ben")).isEqualTo(reference)
    }

    @Test fun `unicode compatibility forms and invisible characters are normalized`() {
        val reference = LoveCalculator.calculate("Anna", "Ben")
        assertThat(LoveCalculator.calculate("Ａｎｎａ", "Ben")).isEqualTo(reference) // Vollbreite
        assertThat(LoveCalculator.calculate("An​na", "Be­n")).isEqualTo(reference)
        // Zusammengesetztes vs. vorkomponiertes ü
        assertThat(LoveCalculator.calculate("Jürgen", "Eva")).isEqualTo(LoveCalculator.calculate("Jürgen", "Eva"))
    }

    @Test fun `diacritics stay significant`() {
        assertThat(LoveCalculator.pairKey("Jürgen", "Eva")).isNotEqualTo(LoveCalculator.pairKey("Jurgen", "Eva"))
    }

    @Test fun `result is always between 0 and 100 and both bounds are reachable`() {
        val rnd = Random(42)
        val seen = IntArray(101)
        repeat(60_000) {
            val p = LoveCalculator.calculate("n${rnd.nextLong()}", "m${rnd.nextLong()}").percent
            assertThat(p).isAtLeast(0)
            assertThat(p).isAtMost(100)
            seen[p]++
        }
        assertThat(seen[0]).isGreaterThan(0)
        assertThat(seen[100]).isGreaterThan(0)
        // grob gleichverteilt: jeder Wert ~594 mal, Toleranz großzügig
        seen.forEach { assertThat(it).isIn(com.google.common.collect.Range.closed(400, 800)) }
    }

    @Test fun `international names work`() {
        listOf("Александр" to "Мария", "王伟" to "李娜", "محمد" to "فاطمة", "Zoë" to "Siobhán", "İlkay" to "Øyvind")
            .forEach { (a, b) ->
                assertThat(LoveCalculator.validate(a)).isNull()
                val r1 = LoveCalculator.calculate(a, b)
                val r2 = LoveCalculator.calculate(b.uppercase(), a.lowercase())
                assertThat(r1.percent).isEqualTo(r2.percent)
            }
    }

    @Test fun `validation`() {
        assertThat(LoveCalculator.validate("")).isEqualTo(NameError.EMPTY)
        assertThat(LoveCalculator.validate("   ")).isEqualTo(NameError.EMPTY)
        assertThat(LoveCalculator.validate("1234")).isEqualTo(NameError.NO_LETTER)
        assertThat(LoveCalculator.validate("❤️❤️")).isEqualTo(NameError.NO_LETTER)
        assertThat(LoveCalculator.validate("a".repeat(41))).isEqualTo(NameError.TOO_LONG)
        assertThat(LoveCalculator.validate("a".repeat(40))).isNull()
        assertThat(LoveCalculator.validate("O'Brien-Smith")).isNull()
        assertThat(LoveCalculator.validate("R2D2")).isNull()
        assertThat(LoveCalculator.validate("𝐀bc")).isNull() // mathematisches A (Surrogatpaar)
    }

    @Test fun `display name keeps spelling but trims`() {
        assertThat(LoveCalculator.displayName("  Anna   Maria ")).isEqualTo("Anna Maria")
    }

    @Test fun `known value is stable across versions`() {
        // Schützt vor unbeabsichtigten Änderungen der Ergebnislogik (Version 2).
        val r = LoveCalculator.calculate("Anna", "Ben")
        assertThat(r.percent).isEqualTo(79)
        assertThat(LoveCalculator.calculate("ben", "ANNA").percent).isEqualTo(79)
        assertThat(LoveCalculator.ALGORITHM_VERSION).isEqualTo(2)
        println("Anna & Ben = ${r.percent} %")
    }

    @Test fun `own SHA-256 matches the platform implementation`() {
        val rnd = Random(7)
        repeat(300) { n ->
            val input = ByteArray(n) { rnd.nextInt().toByte() }
            val expected = java.security.MessageDigest.getInstance("SHA-256").digest(input)
            assertThat(Sha256.digest(input)).isEqualTo(expected)
        }
    }

    @Test fun `result ranges cover 0 to 100 without gaps`() {
        (0..100).forEach { p -> assertThat(ResultRange.of(p).let { p in it.min..it.max }).isTrue() }
        assertThat(ResultRange.of(79)).isEqualTo(ResultRange.R3)
        assertThat(ResultRange.of(80)).isEqualTo(ResultRange.R4)
        assertThat(ResultRange.of(100)).isEqualTo(ResultRange.R5)
        assertThat(ResultRange.of(-5)).isEqualTo(ResultRange.R0)
    }
}
