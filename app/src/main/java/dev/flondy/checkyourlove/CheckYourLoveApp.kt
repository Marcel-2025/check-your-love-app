package dev.flondy.checkyourlove

import android.app.Application
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dev.flondy.checkyourlove.data.HistoryRepository
import dev.flondy.checkyourlove.data.SettingsRepository
import dev.flondy.checkyourlove.data.db.AppDatabase
import dev.flondy.checkyourlove.monetization.EntitlementRepository
import dev.flondy.checkyourlove.monetization.ads.AdUnits
import dev.flondy.checkyourlove.monetization.ads.AdsManager
import dev.flondy.checkyourlove.monetization.billing.DisabledBillingGateway
import dev.flondy.checkyourlove.monetization.billing.PlayBillingGateway
import dev.flondy.checkyourlove.monetization.billing.PremiumGateway
import dev.flondy.checkyourlove.monetization.billing.PurchaseVerifier
import dev.flondy.checkyourlove.monetization.billing.SimulatedBillingGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Einfache manuelle Abhängigkeitsverwaltung – für diese App-Größe ausreichend. */
class AppContainer(context: Context) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val database: AppDatabase by lazy { AppDatabase.create(context) }
    val history: HistoryRepository by lazy { HistoryRepository(database.resultDao()) }
    val settings = SettingsRepository(context)

    val premiumGateway: PremiumGateway = when {
        BuildConfig.PREMIUM_PRODUCT_ID.isNotBlank() -> PlayBillingGateway(
            context, BuildConfig.PREMIUM_PRODUCT_ID, PurchaseVerifier(BuildConfig.PLAY_LICENSE_KEY), appScope,
        )
        BuildConfig.ALLOW_SIMULATED_BILLING -> SimulatedBillingGateway(context, appScope)
        else -> DisabledBillingGateway()
    }

    val entitlements = EntitlementRepository(premiumGateway, settings, appScope)

    val ads = AdsManager(
        context = context,
        units = AdUnits(
            banner = BuildConfig.AD_BANNER_ID,
            interstitial = BuildConfig.AD_INTERSTITIAL_ID,
            rewarded = BuildConfig.AD_REWARDED_ID,
        ),
        settings = settings,
        scope = appScope,
    )
}

class CheckYourLoveApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.premiumGateway.start()
        // Bei jeder Rückkehr in die App Käufe abgleichen (ausstehende Zahlungen, Erstattungen).
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = container.premiumGateway.refresh()
        })
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as CheckYourLoveApp).container
