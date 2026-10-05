package dev.flondy.checkyourlove.monetization.billing

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/** Ob und wie ein Premium-Kauf angeboten werden kann. */
enum class StoreAvailability { CHECKING, AVAILABLE, UNAVAILABLE, NOT_CONFIGURED }

/** Besitzstatus laut Google Play (bzw. Simulation). */
enum class Ownership { UNKNOWN, NOT_OWNED, PENDING, OWNED }

/** Einmalige Rückmeldungen für die Oberfläche. */
enum class PurchaseEvent {
    SUCCESS, PENDING, CANCELED, FAILED, ALREADY_OWNED, RESTORED, NOTHING_TO_RESTORE, REVOKED, SERVICE_UNAVAILABLE
}

data class PremiumState(
    val availability: StoreAvailability = StoreAvailability.CHECKING,
    val ownership: Ownership = Ownership.UNKNOWN,
    /** Vom Store gelieferter, lokalisierter Preis. Niemals selbst erfunden. */
    val formattedPrice: String? = null,
    val purchaseInProgress: Boolean = false,
)

/**
 * Abstraktion über den Premium-Kauf. Implementierungen:
 * - PlayBillingGateway (Android, Modul :app): Google-Play-Abrechnung
 * - StoreKitGateway (iOS, iosMain + Swift): Apple StoreKit 2
 * - [SimulatedBillingGateway]: nur Android-Debug-Builds ohne Produkt-ID, klar als Simulation gekennzeichnet
 * - [DisabledBillingGateway]: ohne Konfiguration – Kauf deaktiviert
 */
interface PremiumGateway {
    val state: StateFlow<PremiumState>
    val events: SharedFlow<PurchaseEvent>
    val isSimulation: Boolean get() = false

    fun start()
    /** Gleicht den Besitzstatus mit dem Store ab (Start, Rückkehr in die App). */
    fun refresh()
    fun launchPurchase()
    /** Wie [refresh], meldet das Ergebnis aber explizit (Button „Käufe wiederherstellen“). */
    fun restore()
}
