package dev.flondy.checkyourlove.monetization.billing

import dev.flondy.checkyourlove.data.SettingsRepository
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
 * Berechtigungslogik getestet werden können. Es fließt kein Geld und es wird nichts an einen Store gesendet.
 */
class SimulatedBillingGateway(
    private val settings: SettingsRepository,
    private val scope: CoroutineScope,
) : PremiumGateway {

    enum class Outcome { SUCCESS, PENDING, CANCEL, ERROR }

    private val _state = MutableStateFlow(PremiumState())
    override val state: StateFlow<PremiumState> = _state.asStateFlow()
    private val _events = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 8)
    override val events: SharedFlow<PurchaseEvent> = _events.asSharedFlow()
    override val isSimulation: Boolean = true

    val nextOutcome = MutableStateFlow(Outcome.SUCCESS)

    override fun start() {
        scope.launch {
            delay(300)
            val stored = storedOwnership()
            _state.update {
                it.copy(availability = StoreAvailability.AVAILABLE, formattedPrice = null, ownership = stored)
            }
        }
    }

    override fun refresh() {
        if (_state.value.availability == StoreAvailability.CHECKING) return
        scope.launch { _state.update { it.copy(ownership = storedOwnership()) } }
    }

    override fun restore() {
        scope.launch {
            val ownership = storedOwnership()
            _state.update { it.copy(ownership = ownership) }
            PurchaseEvaluator.eventFor(PurchaseEvaluator.Source.RESTORE, ownership, ownership)?.let { _events.tryEmit(it) }
        }
    }

    override fun launchPurchase() {
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
        if (_state.value.ownership == Ownership.PENDING) scope.launch { setOwnership(Ownership.OWNED, PurchaseEvent.SUCCESS) }
    }

    fun simulateRefund() {
        if (_state.value.ownership == Ownership.OWNED) scope.launch { setOwnership(Ownership.NOT_OWNED, PurchaseEvent.REVOKED) }
    }

    private fun finish(event: PurchaseEvent) {
        _state.update { it.copy(purchaseInProgress = false) }
        _events.tryEmit(event)
    }

    private suspend fun setOwnership(ownership: Ownership, event: PurchaseEvent) {
        settings.setSimulatedOwnership(ownership.name)
        _state.update { it.copy(ownership = ownership, purchaseInProgress = false) }
        _events.tryEmit(event)
    }

    private suspend fun storedOwnership(): Ownership =
        settings.simulatedOwnership()?.let { v -> Ownership.entries.firstOrNull { it.name == v } } ?: Ownership.NOT_OWNED
}
