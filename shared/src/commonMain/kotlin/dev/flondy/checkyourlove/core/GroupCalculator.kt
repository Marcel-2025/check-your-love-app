package dev.flondy.checkyourlove.core

/** Ein Paar aus dem Gruppen-Test. */
data class GroupPair(val nameA: String, val nameB: String, val calculation: LoveCalculation)

/**
 * Gruppen-Test: alle Paare einer Namensliste, sortiert nach Ergebnis (höchstes zuerst).
 * Nutzt exakt dieselbe Berechnung wie der Einzeltest – ein Paar hat überall denselben Wert.
 */
object GroupCalculator {
    const val MIN_NAMES = 3
    const val MAX_NAMES = 8

    fun pairs(names: List<String>, mode: TestMode): List<GroupPair> {
        val cleaned = names.map { LoveCalculator.displayName(it) }.filter { it.isNotEmpty() }
        val result = mutableListOf<GroupPair>()
        for (i in cleaned.indices) for (j in i + 1 until cleaned.size) {
            result += GroupPair(cleaned[i], cleaned[j], LoveCalculator.calculate(cleaned[i], cleaned[j], mode))
        }
        // Stabile Reihenfolge: Ergebnis absteigend, bei Gleichstand alphabetisch nach Paarschlüssel.
        return result.sortedWith(compareByDescending<GroupPair> { it.calculation.percent }.thenBy { it.calculation.pairKey })
    }

    /** Gleiche Namen (nach Normalisierung) innerhalb der Gruppe – Index der Duplikate. */
    fun duplicateIndices(names: List<String>): Set<Int> {
        val seen = mutableMapOf<String, Int>()
        val dups = mutableSetOf<Int>()
        names.forEachIndexed { i, raw ->
            val key = LoveCalculator.normalize(raw)
            if (key.isEmpty()) return@forEachIndexed
            if (key in seen) dups += i else seen[key] = i
        }
        return dups
    }
}
