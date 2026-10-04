package dev.flondy.checkyourlove.monetization.billing

import android.app.Activity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/** Release-Build ohne Produkt-ID: Premium wird nicht angeboten, Werbung läuft normal. */
class DisabledBillingGateway : PremiumGateway {
    private val _state = MutableStateFlow(
        PremiumState(availability = StoreAvailability.NOT_CONFIGURED, ownership = Ownership.NOT_OWNED)
    )
    override val state: StateFlow<PremiumState> = _state.asStateFlow()
    private val _events = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 4)
    override val events: SharedFlow<PurchaseEvent> = _events.asSharedFlow()
    override fun start() = Unit
    override fun refresh() = Unit
    override fun launchPurchase(activity: Activity) = Unit
    override fun restore() { _events.tryEmit(PurchaseEvent.NOTHING_TO_RESTORE) }
}
