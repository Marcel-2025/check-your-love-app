package dev.flondy.checkyourlove.monetization.billing

/** Plattformunabhängige Momentaufnahme eines Kaufs (testbar ohne Play-Bibliothek). */
data class PurchaseSnapshot(
    val products: List<String>,
    val state: State,
    val acknowledged: Boolean,
    val token: String,
    val originalJson: String,
    val signature: String,
) {
    enum class State { PURCHASED, PENDING, UNSPECIFIED }
}

data class PurchaseEvaluation(val ownership: Ownership, val tokensToAcknowledge: List<String>)

/**
 * Leitet aus den aktuell von Google Play gemeldeten Käufen die Berechtigung ab.
 * - Nur PURCHASED mit gültiger Signatur schaltet frei.
 * - PENDING schaltet nicht frei.
 * - Erstattete/widerrufene Käufe tauchen in der Abfrage nicht mehr auf → NOT_OWNED.
 */
object PurchaseEvaluator {
    fun evaluate(
        purchases: List<PurchaseSnapshot>,
        productId: String,
        verify: (signedData: String, signature: String) -> Boolean,
    ): PurchaseEvaluation {
        val relevant = purchases.filter { productId in it.products }
        val validPurchased = relevant.filter {
            it.state == PurchaseSnapshot.State.PURCHASED && verify(it.originalJson, it.signature)
        }
        val ownership = when {
            validPurchased.isNotEmpty() -> Ownership.OWNED
            relevant.any { it.state == PurchaseSnapshot.State.PENDING } -> Ownership.PENDING
            else -> Ownership.NOT_OWNED
        }
        return PurchaseEvaluation(
            ownership = ownership,
            tokensToAcknowledge = validPurchased.filterNot { it.acknowledged }.map { it.token },
        )
    }

    enum class Source { QUERY, RESTORE, PURCHASE_UPDATE }

    /** Welche Rückmeldung nach einem Abgleich angezeigt wird. */
    fun eventFor(source: Source, previous: Ownership, now: Ownership): PurchaseEvent? = when (source) {
        Source.RESTORE -> when (now) {
            Ownership.OWNED -> PurchaseEvent.RESTORED
            Ownership.PENDING -> PurchaseEvent.PENDING
            else -> PurchaseEvent.NOTHING_TO_RESTORE
        }
        Source.PURCHASE_UPDATE -> when (now) {
            Ownership.OWNED -> if (previous != Ownership.OWNED) PurchaseEvent.SUCCESS else null
            Ownership.PENDING -> PurchaseEvent.PENDING
            else -> null
        }
        Source.QUERY -> when {
            previous == Ownership.OWNED && now == Ownership.NOT_OWNED -> PurchaseEvent.REVOKED
            previous == Ownership.PENDING && now == Ownership.OWNED -> PurchaseEvent.SUCCESS
            else -> null
        }
    }
}
