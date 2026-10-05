package dev.flondy.checkyourlove.monetization

import dev.flondy.checkyourlove.monetization.billing.Ownership
import dev.flondy.checkyourlove.monetization.billing.PremiumGateway
import dev.flondy.checkyourlove.monetization.billing.PremiumState
import dev.flondy.checkyourlove.monetization.billing.PurchaseEvaluator
import dev.flondy.checkyourlove.monetization.billing.PurchaseEvent
import dev.flondy.checkyourlove.monetization.billing.StoreAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Einmaliger Premium-Kauf über Apple StoreKit 2. */
internal class StoreKitGateway(
    private val bridge: IosStoreBridge,
    private val productId: String,
    private val scope: CoroutineScope,
) : PremiumGateway {

    private val _state = MutableStateFlow(PremiumState())
    override val state: StateFlow<PremiumState> = _state.asStateFlow()
    private val _events = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 8)
    override val events: SharedFlow<PurchaseEvent> = _events.asSharedFlow()

    private fun main(block: () -> Unit) { scope.launch(Dispatchers.Main) { block() } }

    override fun start() {
        if (productId.isBlank()) {
            _state.value = PremiumState(availability = StoreAvailability.NOT_CONFIGURED, ownership = Ownership.NOT_OWNED)
            return
        }
        bridge.observeUpdates { main { sync(PurchaseEvaluator.Source.QUERY) } }
        bridge.fetchProduct(productId) { price ->
            main {
                _state.update {
                    it.copy(
                        availability = if (price != null) StoreAvailability.AVAILABLE else StoreAvailability.UNAVAILABLE,
                        formattedPrice = price,
                    )
                }
            }
        }
        sync(PurchaseEvaluator.Source.QUERY)
    }

    override fun refresh() {
        if (productId.isBlank()) return
        sync(PurchaseEvaluator.Source.QUERY)
    }

    override fun restore() {
        if (productId.isBlank()) { _events.tryEmit(PurchaseEvent.NOTHING_TO_RESTORE); return }
        bridge.restore { ok ->
            main {
                if (!ok) _events.tryEmit(PurchaseEvent.SERVICE_UNAVAILABLE) else sync(PurchaseEvaluator.Source.RESTORE)
            }
        }
    }

    override fun launchPurchase() {
        if (productId.isBlank() || _state.value.purchaseInProgress) return
        _state.update { it.copy(purchaseInProgress = true) }
        bridge.purchase(productId) { result ->
            main {
                _state.update { it.copy(purchaseInProgress = false) }
                when (result) {
                    "success" -> setOwnership(Ownership.OWNED, PurchaseEvaluator.Source.PURCHASE_UPDATE)
                    "pending" -> setOwnership(Ownership.PENDING, PurchaseEvaluator.Source.PURCHASE_UPDATE)
                    "cancelled" -> _events.tryEmit(PurchaseEvent.CANCELED)
                    else -> _events.tryEmit(PurchaseEvent.FAILED) // "failed", "unverified"
                }
            }
        }
    }

    private fun sync(source: PurchaseEvaluator.Source) {
        bridge.isEntitled(productId) { entitled ->
            main {
                val now = when {
                    entitled -> Ownership.OWNED
                    _state.value.ownership == Ownership.PENDING -> Ownership.PENDING
                    else -> Ownership.NOT_OWNED
                }
                setOwnership(now, source)
            }
        }
    }

    private fun setOwnership(now: Ownership, source: PurchaseEvaluator.Source) {
        val previous = _state.value.ownership
        _state.update { it.copy(ownership = now) }
        PurchaseEvaluator.eventFor(source, previous, now)?.let { _events.tryEmit(it) }
    }
}
