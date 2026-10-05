package dev.flondy.checkyourlove

import android.app.Application
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dev.flondy.checkyourlove.data.androidDatabaseBuilder
import dev.flondy.checkyourlove.data.androidSettingsPath
import dev.flondy.checkyourlove.data.createSettingsDataStore
import dev.flondy.checkyourlove.monetization.ads.AdUnits
import dev.flondy.checkyourlove.monetization.ads.BannerAd
import dev.flondy.checkyourlove.monetization.billing.DisabledBillingGateway
import dev.flondy.checkyourlove.monetization.billing.PlayBillingGateway
import dev.flondy.checkyourlove.monetization.billing.PurchaseVerifier
import dev.flondy.checkyourlove.monetization.billing.SimulatedBillingGateway
import dev.flondy.checkyourlove.platform.AndroidAdsPlatform
import dev.flondy.checkyourlove.platform.AndroidShareService
import dev.flondy.checkyourlove.platform.AppConfig
import dev.flondy.checkyourlove.platform.AppServices

class CheckYourLoveApp : Application() {
    lateinit var services: AppServices
        private set

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(CurrentActivity)
        services = createServices(this)
        // Bei jeder Rückkehr in die App Käufe abgleichen (ausstehende Zahlungen, Erstattungen).
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) = services.premiumGateway.refresh()
        })
    }

    companion object {
        fun createServices(context: Context): AppServices = AppServices.create(
            config = AppConfig(
                versionName = BuildConfig.VERSION_NAME,
                isDebug = BuildConfig.DEBUG,
                privacyPolicyUrl = BuildConfig.PRIVACY_POLICY_URL,
                adUnits = AdUnits(
                    banner = BuildConfig.AD_BANNER_ID,
                    interstitial = BuildConfig.AD_INTERSTITIAL_ID,
                    rewarded = BuildConfig.AD_REWARDED_ID,
                    rewardedIsInterstitial = BuildConfig.AD_REWARDED_IS_INTERSTITIAL,
                ),
                moreAppsUrl = "https://play.google.com/store/apps/developer?id=Flondy",
                storeName = "Google Play",
            ),
            database = androidDatabaseBuilder(context),
            settingsStore = createSettingsDataStore(androidSettingsPath(context)),
            gateway = { settings, scope ->
                when {
                    BuildConfig.PREMIUM_PRODUCT_ID.isNotBlank() -> PlayBillingGateway(
                        context, BuildConfig.PREMIUM_PRODUCT_ID, PurchaseVerifier(BuildConfig.PLAY_LICENSE_KEY), scope,
                    )
                    BuildConfig.ALLOW_SIMULATED_BILLING -> SimulatedBillingGateway(settings, scope)
                    else -> DisabledBillingGateway()
                }
            },
            adsPlatform = AndroidAdsPlatform(context),
            share = AndroidShareService(context),
            banner = { unitId, onFailed, modifier -> BannerAd(unitId, onFailed, modifier) },
        )
    }
}

val Context.appServices: AppServices
    get() = (applicationContext as CheckYourLoveApp).services
