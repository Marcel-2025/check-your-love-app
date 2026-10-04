package dev.flondy.checkyourlove.monetization.ads

import android.app.Activity
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
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dev.flondy.checkyourlove.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/** Konfiguration der Anzeigenblöcke; leere ID = Format deaktiviert. */
data class AdUnits(
    val banner: String,
    val interstitial: String,
    val rewarded: String,
    /** true = AdMob-Format „Interstitial mit Prämie“, false = „Mit Prämie“ (Rewarded). */
    val rewardedIsInterstitial: Boolean = false,
)

/** Gemeinsame Sicht auf RewardedAd und RewardedInterstitialAd. */
private class LoadedReward(
    val setCallback: (FullScreenContentCallback) -> Unit,
    val show: (Activity, OnUserEarnedRewardListener) -> Unit,
)

enum class RewardedStatus { IDLE, LOADING, READY, UNAVAILABLE }

/**
 * Bündelt Einwilligung (UMP), Initialisierung und Vollbildanzeigen.
 *
 * Grundsätze:
 * - Keine Anzeigenanfrage ohne gültige Einwilligung ([canRequestAds]).
 * - Eingegebene Namen werden NIE an das Werbe-SDK übergeben (keine Keywords/Content-URLs).
 * - Fehler beim Laden blockieren nie die Kernfunktion.
 */
class AdsManager(
    context: Context,
    private val units: AdUnits,
    private val settings: SettingsRepository,
    private val scope: CoroutineScope,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val appContext = context.applicationContext
    private val consentInformation: ConsentInformation = UserMessagingPlatform.getConsentInformation(appContext)
    private val initStarted = AtomicBoolean(false)
    private val consentStarted = AtomicBoolean(false)

    private val _canRequestAds = MutableStateFlow(consentInformation.canRequestAds())
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _sdkReady = MutableStateFlow(false)
    val sdkReady: StateFlow<Boolean> = _sdkReady.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(isPrivacyOptionsRequired())
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    private val _rewardedStatus = MutableStateFlow(RewardedStatus.IDLE)
    val rewardedStatus: StateFlow<RewardedStatus> = _rewardedStatus.asStateFlow()

    val bannerUnitId: String get() = units.banner
    val rewardedEnabled: Boolean get() = units.rewarded.isNotBlank()

    private var interstitial: InterstitialAd? = null
    private var interstitialLoading = false
    private var rewarded: LoadedReward? = null
    private var testsThisSession = 0
    @Volatile private var pendingRewardedLoad = false

    /**
     * Fragt die Einwilligung ab (zeigt das Formular nur, wenn erforderlich) und startet danach
     * das Werbe-SDK. Wird nur für Nutzer ohne Premium aufgerufen.
     */
    fun gatherConsent(activity: Activity) {
        if (!consentStarted.compareAndSet(false, true)) {
            if (consentInformation.canRequestAds()) initializeSdk()
            return
        }
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    if (error != null) Log.w(TAG, "Consent form: ${error.message}")
                    onConsentUpdated()
                }
            },
            { error ->
                Log.w(TAG, "Consent info update failed: ${error.message}")
                onConsentUpdated()
            },
        )
        // Bereits erteilte Einwilligung aus einer früheren Sitzung sofort nutzen.
        if (consentInformation.canRequestAds()) onConsentUpdated()
    }

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) Log.w(TAG, "Privacy options: ${error.message}")
            onConsentUpdated()
        }
    }

    /** Nur für Debug-Builds: Einwilligung zurücksetzen, um den Ablauf erneut zu testen. */
    fun resetConsentForDebug() {
        consentInformation.reset()
        consentStarted.set(false)
        onConsentUpdated()
    }

    private fun onConsentUpdated() {
        _canRequestAds.value = consentInformation.canRequestAds()
        _privacyOptionsRequired.value = isPrivacyOptionsRequired()
        if (_canRequestAds.value) initializeSdk()
    }

    private fun isPrivacyOptionsRequired(): Boolean =
        consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    private fun initializeSdk() {
        if (!initStarted.compareAndSet(false, true)) return
        // Für ein Unterhaltungsangebot, das auch Jugendliche nutzen, werden nur Anzeigen
        // bis Altersfreigabe „Teen“ angefordert.
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_T)
                .build()
        )
        scope.launch {
            withContext(Dispatchers.IO) {
                MobileAds.initialize(appContext) {
                    _sdkReady.value = true
                    scope.launch(Dispatchers.Main) {
                        if (pendingRewardedLoad) {
                            pendingRewardedLoad = false
                            _rewardedStatus.value = RewardedStatus.IDLE
                            loadRewarded()
                        }
                    }
                }
            }
        }
    }

    fun newRequest(): AdRequest = AdRequest.Builder().build()

    // ------------------------------------------------------------------ Interstitial

    /** Wird nach jedem abgeschlossenen Test aufgerufen. */
    fun onTestCompleted(adsAllowed: Boolean) {
        testsThisSession++
        scope.launch { settings.incrementTestsSinceInterstitial() }
        if (adsAllowed) preloadInterstitialIfUseful()
    }

    private fun preloadInterstitialIfUseful() {
        if (units.interstitial.isBlank() || !_sdkReady.value || !_canRequestAds.value) return
        if (interstitial != null || interstitialLoading) return
        if (testsThisSession < InterstitialPolicy.MIN_TESTS_IN_SESSION - 1) return
        interstitialLoading = true
        InterstitialAd.load(appContext, units.interstitial, newRequest(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialLoading = false
                interstitial = ad
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialLoading = false
                interstitial = null
                Log.i(TAG, "Interstitial not loaded: ${error.code}")
            }
        })
    }

    /**
     * Natürliche Pause: Nutzer startet nach einem Ergebnis einen neuen Test.
     * [onDone] wird immer aufgerufen – sofort, wenn keine Anzeige gezeigt wird.
     */
    fun maybeShowInterstitial(activity: Activity, adsAllowed: Boolean, onDone: () -> Unit) {
        val ad = interstitial
        if (!adsAllowed || ad == null || !_canRequestAds.value) { onDone(); return }
        scope.launch {
            val counters = settings.adCounters()
            val now = clock()
            if (!InterstitialPolicy.shouldShow(now, counters.lastInterstitialAt, counters.testsSinceInterstitial, testsThisSession)) {
                onDone()
                return@launch
            }
            interstitial = null
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() = onDone()
                override fun onAdFailedToShowFullScreenContent(error: AdError) = onDone()
            }
            settings.markInterstitialShown(now)
            ad.show(activity)
        }
    }

    // ------------------------------------------------------------------ Rewarded

    fun loadRewarded() {
        if (!rewardedEnabled) { _rewardedStatus.value = RewardedStatus.UNAVAILABLE; return }
        if (rewarded != null) { _rewardedStatus.value = RewardedStatus.READY; return }
        if (_rewardedStatus.value == RewardedStatus.LOADING) return
        if (!_canRequestAds.value) { _rewardedStatus.value = RewardedStatus.UNAVAILABLE; return }
        if (!_sdkReady.value) {
            pendingRewardedLoad = true
            _rewardedStatus.value = RewardedStatus.LOADING
            return
        }
        _rewardedStatus.value = RewardedStatus.LOADING
        val onFailed: (LoadAdError) -> Unit = { error ->
            rewarded = null
            _rewardedStatus.value = RewardedStatus.UNAVAILABLE
            Log.i(TAG, "Rewarded not loaded: ${error.code}")
        }
        if (units.rewardedIsInterstitial) {
            RewardedInterstitialAd.load(appContext, units.rewarded, newRequest(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewarded = LoadedReward({ ad.fullScreenContentCallback = it }, { a, l -> ad.show(a, l) })
                    _rewardedStatus.value = RewardedStatus.READY
                }
                override fun onAdFailedToLoad(error: LoadAdError) = onFailed(error)
            })
        } else {
            RewardedAd.load(appContext, units.rewarded, newRequest(), object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewarded = LoadedReward({ ad.fullScreenContentCallback = it }, { a, l -> ad.show(a, l) })
                    _rewardedStatus.value = RewardedStatus.READY
                }
                override fun onAdFailedToLoad(error: LoadAdError) = onFailed(error)
            })
        }
    }

    /**
     * Zeigt die Rewarded Ad. [onReward] wird AUSSCHLIESSLICH im offiziellen
     * OnUserEarnedRewardListener aufgerufen, [onClosed] in jedem Fall danach.
     */
    fun showRewarded(activity: Activity, onReward: () -> Unit, onClosed: (rewarded: Boolean) -> Unit) {
        val ad = rewarded
        if (ad == null) { onClosed(false); loadRewarded(); return }
        rewarded = null
        _rewardedStatus.value = RewardedStatus.IDLE
        var earned = false
        ad.setCallback(object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onClosed(earned)
                loadRewarded()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                onClosed(false)
                loadRewarded()
            }
        })
        ad.show(activity) {
            earned = true
            onReward()
        }
    }

    private companion object { const val TAG = "Ads" }
}
