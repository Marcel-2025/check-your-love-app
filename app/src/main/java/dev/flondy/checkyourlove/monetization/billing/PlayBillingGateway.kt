package dev.flondy.checkyourlove.monetization.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dev.flondy.checkyourlove.monetization.billing.PurchaseEvaluator.Source
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Echte Google-Play-Abrechnung (Play Billing Library 9) für einen einmaligen Kauf. */
class PlayBillingGateway(
    context: Context,
    private val productId: String,
    private val verifier: PurchaseVerifier,
    private val scope: CoroutineScope,
) : PremiumGateway, PurchasesUpdatedListener {

    private val _state = MutableStateFlow(PremiumState())
    override val state: StateFlow<PremiumState> = _state.asStateFlow()
    private val _events = MutableSharedFlow<PurchaseEvent>(extraBufferCapacity = 8)
    override val events: SharedFlow<PurchaseEvent> = _events.asSharedFlow()

    private val client: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    @Volatile private var productDetails: ProductDetails? = null
    @Volatile private var connecting = false
    private val syncMutex = Mutex()

    override fun start() {
        if (client.isReady || connecting) return
        connecting = true
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                connecting = false
                if (result.responseCode == BillingResponseCode.OK) {
                    scope.launch {
                        loadProduct()
                        sync(Source.QUERY)
                    }
                } else {
                    Log.w(TAG, "Billing setup failed: ${result.responseCode} ${result.debugMessage}")
                    _state.update { it.copy(availability = StoreAvailability.UNAVAILABLE) }
                }
            }

            override fun onBillingServiceDisconnected() {
                connecting = false
                // enableAutoServiceReconnection() stellt die Verbindung beim nächsten Aufruf wieder her.
            }
        })
    }

    override fun refresh() {
        if (!client.isReady) { start(); return }
        scope.launch {
            if (productDetails == null) loadProduct()
            sync(Source.QUERY)
        }
    }

    override fun restore() {
        if (!client.isReady) {
            start()
            _events.tryEmit(PurchaseEvent.SERVICE_UNAVAILABLE)
            return
        }
        scope.launch { sync(Source.RESTORE) }
    }

    override fun launchPurchase(activity: Activity) {
        val details = productDetails
        if (details == null || !client.isReady) {
            _events.tryEmit(PurchaseEvent.SERVICE_UNAVAILABLE)
            refresh()
            return
        }
        if (_state.value.purchaseInProgress) return
        val offerToken = details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken
            ?: details.oneTimePurchaseOfferDetails?.offerToken
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .apply { if (offerToken != null) setOfferToken(offerToken) }
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()
        val result = client.launchBillingFlow(activity, flowParams)
        when (result.responseCode) {
            BillingResponseCode.OK -> _state.update { it.copy(purchaseInProgress = true) }
            BillingResponseCode.ITEM_ALREADY_OWNED -> scope.launch { syncAfterAlreadyOwned() }
            BillingResponseCode.USER_CANCELED -> _events.tryEmit(PurchaseEvent.CANCELED)
            else -> _events.tryEmit(mapError(result.responseCode))
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        _state.update { it.copy(purchaseInProgress = false) }
        when (result.responseCode) {
            BillingResponseCode.OK -> {
                val snapshots = purchases.orEmpty().map { it.toSnapshot() }
                if (snapshots.any { productId in it.products }) scope.launch { apply(snapshots, Source.PURCHASE_UPDATE) }
            }
            BillingResponseCode.USER_CANCELED -> _events.tryEmit(PurchaseEvent.CANCELED)
            BillingResponseCode.ITEM_ALREADY_OWNED -> scope.launch { syncAfterAlreadyOwned() }
            else -> _events.tryEmit(mapError(result.responseCode))
        }
    }

    private suspend fun syncAfterAlreadyOwned() {
        sync(Source.QUERY)
        if (_state.value.ownership == Ownership.OWNED) _events.tryEmit(PurchaseEvent.ALREADY_OWNED)
    }

    private suspend fun loadProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(ProductType.INAPP)
                        .build()
                )
            ).build()
        val result = client.queryProductDetails(params)
        val details = result.productDetailsList?.firstOrNull { it.productId == productId }
        productDetails = details
        val price = details?.oneTimePurchaseOfferDetailsList?.firstOrNull()?.formattedPrice
            ?: details?.oneTimePurchaseOfferDetails?.formattedPrice
        _state.update {
            it.copy(
                availability = if (details != null && price != null) StoreAvailability.AVAILABLE else StoreAvailability.UNAVAILABLE,
                formattedPrice = price,
            )
        }
        if (details == null) Log.w(TAG, "Product $productId not found: ${result.billingResult.debugMessage}")
    }

    private suspend fun sync(source: Source) {
        val result = client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()
        )
        if (result.billingResult.responseCode != BillingResponseCode.OK) {
            if (source == Source.RESTORE) _events.tryEmit(PurchaseEvent.SERVICE_UNAVAILABLE)
            return
        }
        apply(result.purchasesList.map { it.toSnapshot() }, source)
    }

    private suspend fun apply(purchases: List<PurchaseSnapshot>, source: Source) = syncMutex.withLock {
        val previous = _state.value.ownership
        val evaluation = PurchaseEvaluator.evaluate(purchases, productId, verifier::verify)
        _state.update { it.copy(ownership = evaluation.ownership) }
        PurchaseEvaluator.eventFor(source, previous, evaluation.ownership)?.let { _events.tryEmit(it) }
        evaluation.tokensToAcknowledge.forEach { acknowledge(it) }
    }

    /** Käufe müssen innerhalb von 3 Tagen bestätigt werden, sonst erstattet Google sie automatisch. */
    private suspend fun acknowledge(token: String) {
        val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build()
        repeat(3) { attempt ->
            val result = client.acknowledgePurchase(params)
            if (result.responseCode == BillingResponseCode.OK) return
            Log.w(TAG, "Acknowledge failed (${result.responseCode}), attempt ${attempt + 1}")
            delay(1_000L shl attempt)
        }
        // Weitere Versuche beim nächsten refresh(), da der Kauf dann weiterhin unbestätigt ist.
    }

    private fun mapError(code: Int): PurchaseEvent = when (code) {
        BillingResponseCode.SERVICE_UNAVAILABLE,
        BillingResponseCode.SERVICE_DISCONNECTED,
        BillingResponseCode.NETWORK_ERROR,
        BillingResponseCode.BILLING_UNAVAILABLE -> PurchaseEvent.SERVICE_UNAVAILABLE
        else -> PurchaseEvent.FAILED
    }

    private fun Purchase.toSnapshot() = PurchaseSnapshot(
        products = products,
        state = when (purchaseState) {
            Purchase.PurchaseState.PURCHASED -> PurchaseSnapshot.State.PURCHASED
            Purchase.PurchaseState.PENDING -> PurchaseSnapshot.State.PENDING
            else -> PurchaseSnapshot.State.UNSPECIFIED
        },
        acknowledged = isAcknowledged,
        token = purchaseToken,
        originalJson = originalJson,
        signature = signature,
    )

    private companion object { const val TAG = "PlayBilling" }
}
