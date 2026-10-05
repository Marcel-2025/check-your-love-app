package dev.flondy.checkyourlove.monetization.ads

/**
 * Konservative Häufigkeitsbegrenzung für Interstitials.
 * Angezeigt wird höchstens in einer natürlichen Pause (Tippen auf „Neuer Test“ nach einem
 * Ergebnis) und nur, wenn ALLE Bedingungen erfüllt sind:
 * - mindestens [MIN_TESTS_IN_SESSION] Tests in dieser Sitzung (nie direkt nach dem Start)
 * - mindestens [MIN_TESTS_BETWEEN] Tests seit der letzten Anzeige
 * - mindestens [MIN_INTERVAL_MS] seit der letzten Anzeige
 */
object InterstitialPolicy {
    const val MIN_TESTS_IN_SESSION = 3
    const val MIN_TESTS_BETWEEN = 4
    const val MIN_INTERVAL_MS = 5L * 60 * 1000

    fun shouldShow(
        now: Long,
        lastShownAt: Long,
        testsSinceLast: Int,
        testsThisSession: Int,
    ): Boolean =
        testsThisSession >= MIN_TESTS_IN_SESSION &&
            testsSinceLast >= MIN_TESTS_BETWEEN &&
            now - lastShownAt >= MIN_INTERVAL_MS
}
