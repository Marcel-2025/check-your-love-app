package dev.flondy.checkyourlove.monetization.ads

/**
 * Minimale Brücke zum jeweiligen Werbe-SDK (Google Mobile Ads + UMP auf Android bzw. iOS).
 * Die gesamte Logik (Einwilligung abwarten, Frequenzbegrenzung, Belohnungen) liegt im
 * gemeinsamen [AdsManager]; die Plattform führt nur einzelne SDK-Aufrufe aus.
 *
 * Alle Callbacks dürfen auf beliebigen Threads kommen; der AdsManager wechselt selbst
 * auf den Hauptthread.
 */
interface AdsPlatform {
    /** true, wenn laut Einwilligungs-SDK Anzeigen angefragt werden dürfen. */
    fun canRequestAds(): Boolean

    /** true, wenn in den Einstellungen ein Datenschutz-Menüpunkt angeboten werden muss. */
    fun isPrivacyOptionsRequired(): Boolean

    /** Aktualisiert die Einwilligungsinfo und zeigt das Formular, falls erforderlich. */
    fun requestConsent(onDone: () -> Unit)

    fun showPrivacyOptions(onDone: () -> Unit)

    /** Nur für Debug-Builds. */
    fun resetConsent()

    /** Startet das Werbe-SDK (nur nach Einwilligung aufgerufen). */
    fun startSdk(onReady: () -> Unit)

    fun loadInterstitial(unitId: String, onResult: (loaded: Boolean) -> Unit)

    /** Zeigt ein geladenes Interstitial. Rückgabe false, wenn keins bereitsteht. */
    fun showInterstitial(onDismissed: () -> Unit): Boolean

    fun loadRewarded(unitId: String, interstitialFormat: Boolean, onResult: (loaded: Boolean) -> Unit)

    /**
     * Zeigt das Prämienvideo. [onReward] NUR aus dem offiziellen Belohnungs-Callback des SDK,
     * [onClosed] immer beim Schließen. Rückgabe false, wenn keins bereitsteht.
     */
    fun showRewarded(onReward: () -> Unit, onClosed: () -> Unit): Boolean
}

/** Konfiguration der Anzeigenblöcke; leere ID = Format deaktiviert. */
data class AdUnits(
    val banner: String,
    val interstitial: String,
    val rewarded: String,
    /** true = AdMob-Format „Interstitial mit Prämie“, false = „Mit Prämie“ (Rewarded). */
    val rewardedIsInterstitial: Boolean = false,
)
