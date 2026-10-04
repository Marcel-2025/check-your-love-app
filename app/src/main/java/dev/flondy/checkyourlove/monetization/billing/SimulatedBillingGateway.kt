package dev.flondy.checkyourlove.monetization.billing

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * NUR FÜR DEBUG-BUILDS ohne konfigurierte Produkt-ID.
 * Simuliert Kaufabläufe (Erfolg, ausstehend, Abbruch, Fehler, Erstattung), damit Oberfläche und
 * Berechtigungslogik getestet werden können. Es fließt kein Geld und es wird nichts an Google gesendet.
 * In Release-Builds wird diese Klasse nie verwendet (BuildConfig.ALLOW_SIMULATED_BILLING = false).
 */
class SimulatedBillingGateway(
    context: Context,
    private val scope: CoroutineScope,
) : PremiumGateway {

    enum class Outcome { SUCCESS, PENDING, CANCEL, ERROR }

    private val prefs = context.applicationContext.getSharedPreferences("billing_simulation", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(PremiumState())
    override val state: StateFlow<PremiumState> = _state.asStateFlow()
    private val _events = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 8)
    override val events: SharedFlow<PurchaseEvent> = _events.asSharedFlow()
    override val isSimulation: Boolean = true

    val nextOutcome = MutableStateFlow(Outcome.SUCCESS)

    override fun start() {
        scope.launch {
            delay(300)
            _state.update {
                it.copy(
                    availability = StoreAvailability.AVAILABLE,
                    formattedPrice = null, // bewusst kein erfundener Preis; die UI zeigt „Testpreis“
                    ownership = storedOwnership(),
                )
            }
        }
    }

    override fun refresh() {
        if (_state.value.availability == StoreAvailability.CHECKING) return
        _state.update { it.copy(ownership = storedOwnership()) }
    }

    override fun restore() {
        val ownership = storedOwnership()
        _state.update { it.copy(ownership = ownership) }
        _events.tryEmit(PurchaseEvaluator.eventFor(PurchaseEvaluator.Source.RESTORE, ownership, ownership)!!)
    }

    override fun launchPurchase(activity: Activity) {
        if (_state.value.purchaseInProgress) return
        _state.update { it.copy(purchaseInProgress = true) }
        scope.launch {
            delay(900)
            when (nextOutcome.value) {
                Outcome.SUCCESS -> setOwnership(Ownership.OWNED, PurchaseEvent.SUCCESS)
                Outcome.PENDING -> setOwnership(Ownership.PENDING, PurchaseEvent.PENDING)
                Outcome.CANCEL -> finish(PurchaseEvent.CANCELED)
                Outcome.ERROR -> finish(PurchaseEvent.FAILED)
            }
        }
    }

    fun completePending() {
        if (_state.value.ownership == Ownership.PENDING) setOwnership(Ownership.OWNED, PurchaseEvent.SUCCESS)
    }

    fun simulateRefund() {
        if (_state.value.ownership == Ownership.OWNED) setOwnership(Ownership.NOT_OWNED, PurchaseEvent.REVOKED)
    }

    private fun finish(event: PurchaseEvent) {
        _state.update { it.copy(purchaseInProgress = false) }
        _events.tryEmit(event)
    }

    private fun setOwnership(ownership: Ownership, event: PurchaseEvent) {
        prefs.edit { putString(KEY, ownership.name) }
        _state.update { it.copy(ownership = ownership, purchaseInProgress = false) }
        _events.tryEmit(event)
    }

    private fun storedOwnership(): Ownership =
        prefs.getString(KEY, null)?.let { runCatching { Ownership.valueOf(it) }.getOrNull() } ?: Ownership.NOT_OWNED

    private companion object { const val KEY = "ownership" }
}
