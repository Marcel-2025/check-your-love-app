package dev.flondy.checkyourlove.platform

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dev.flondy.checkyourlove.CurrentActivity
import dev.flondy.checkyourlove.monetization.ads.AdsPlatform
import java.util.concurrent.Executors

/** Google Mobile Ads + User Messaging Platform für Android. */
class AndroidAdsPlatform(context: Context) : AdsPlatform {
    private val appContext = context.applicationContext
    private val consent: ConsentInformation = UserMessagingPlatform.getConsentInformation(appContext)

    private var interstitial: InterstitialAd? = null
    private var showReward: ((onReward: () -> Unit, onClosed: () -> Unit) -> Boolean)? = null

    override fun canRequestAds(): Boolean = consent.canRequestAds()

    override fun isPrivacyOptionsRequired(): Boolean =
        consent.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    override fun requestConsent(onDone: () -> Unit) {
        val activity = CurrentActivity.get() ?: run { onDone(); return }
        consent.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    if (error != null) Log.w(TAG, "Consent form: ${error.message}")
                    onDone()
                }
            },
            { error ->
                Log.w(TAG, "Consent info update failed: ${error.message}")
                onDone()
            },
        )
        // Einwilligung aus früherer Sitzung sofort nutzen
        if (consent.canRequestAds()) onDone()
    }

    override fun showPrivacyOptions(onDone: () -> Unit) {
        val activity = CurrentActivity.get() ?: run { onDone(); return }
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) Log.w(TAG, "Privacy options: ${error.message}")
            onDone()
        }
    }

    override fun resetConsent() = consent.reset()

    override fun startSdk(onReady: () -> Unit) {
        // Für ein Unterhaltungsangebot, das auch Jugendliche nutzen: nur Anzeigen bis „Teen“.
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_T).build()
        )
        Executors.newSingleThreadExecutor().execute {
            MobileAds.initialize(appContext) { onReady() }
        }
    }

    private fun request() = AdRequest.Builder().build()

    override fun loadInterstitial(unitId: String, onResult: (Boolean) -> Unit) {
        InterstitialAd.load(appContext, unitId, request(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad; onResult(true) }
            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitial = null
                Log.i(TAG, "Interstitial not loaded: ${error.code}")
                onResult(false)
            }
        })
    }

    override fun showInterstitial(onDismissed: () -> Unit): Boolean {
        val ad = interstitial ?: return false
        val activity = CurrentActivity.get() ?: return false
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = onDismissed()
            override fun onAdFailedToShowFullScreenContent(error: AdError) = onDismissed()
        }
        ad.show(activity)
        return true
    }

    override fun loadRewarded(unitId: String, interstitialFormat: Boolean, onResult: (Boolean) -> Unit) {
        val failed: (LoadAdError) -> Unit = { error ->
            showReward = null
            Log.i(TAG, "Rewarded not loaded: ${error.code}")
            onResult(false)
        }
        if (interstitialFormat) {
            RewardedInterstitialAd.load(appContext, unitId, request(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    showReward = { onReward, onClosed ->
                        val activity = CurrentActivity.get()
                        if (activity == null) false else {
                            ad.fullScreenContentCallback = closeCallback(onClosed)
                            ad.show(activity) { onReward() }
                            true
                        }
                    }
                    onResult(true)
                }
                override fun onAdFailedToLoad(error: LoadAdError) = failed(error)
            })
        } else {
            RewardedAd.load(appContext, unitId, request(), object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    showReward = { onReward, onClosed ->
                        val activity = CurrentActivity.get()
                        if (activity == null) false else {
                            ad.fullScreenContentCallback = closeCallback(onClosed)
                            ad.show(activity) { onReward() }
                            true
                        }
                    }
                    onResult(true)
                }
                override fun onAdFailedToLoad(error: LoadAdError) = failed(error)
            })
        }
    }

    override fun showRewarded(onReward: () -> Unit, onClosed: () -> Unit): Boolean {
        val show = showReward ?: return false
        showReward = null
        return show(onReward, onClosed)
    }

    private fun closeCallback(onClosed: () -> Unit) = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() = onClosed()
        override fun onAdFailedToShowFullScreenContent(error: AdError) = onClosed()
    }

    private companion object { const val TAG = "Ads" }
}
