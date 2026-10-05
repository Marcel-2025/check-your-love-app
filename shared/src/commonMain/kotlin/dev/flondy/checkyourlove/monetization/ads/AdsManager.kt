package dev.flondy.checkyourlove.monetization.ads

import dev.flondy.checkyourlove.data.SettingsRepository
import dev.flondy.checkyourlove.platform.currentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RewardedStatus { IDLE, LOADING, READY, UNAVAILABLE }

/**
 * Plattformneutrale Werbelogik.
 *
 * Grundsätze:
 * - Keine Anzeigenanfrage ohne gültige Einwilligung ([canRequestAds]).
 * - Eingegebene Namen werden NIE an das Werbe-SDK übergeben.
 * - Fehler beim Laden blockieren nie die Kernfunktion.
 */
class AdsManager(
    private val platform: AdsPlatform,
    private val units: AdUnits,
    private val settings: SettingsRepository,
    private val scope: CoroutineScope,
    private val clock: () -> Long = ::currentTimeMillis,
) {
    private val _canRequestAds = MutableStateFlow(false)
    val canRequestAds: StateFlow<Boolean> = _canRequestAds.asStateFlow()

    private val _sdkReady = MutableStateFlow(false)
    val sdkReady: StateFlow<Boolean> = _sdkReady.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(false)
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    private val _rewardedStatus = MutableStateFlow(RewardedStatus.IDLE)
    val rewardedStatus: StateFlow<RewardedStatus> = _rewardedStatus.asStateFlow()

    val bannerUnitId: String get() = units.banner
    val rewardedEnabled: Boolean get() = units.rewarded.isNotBlank()

    private var consentStarted = false
    private var initStarted = false
    private var interstitialReady = false
    private var interstitialLoading = false
    private var rewardedReady = false
    private var pendingRewardedLoad = false
    private var testsThisSession = 0

    private fun onMain(block: () -> Unit) { scope.launch(Dispatchers.Main) { block() } }

    /** Fragt die Einwilligung ab und startet danach das Werbe-SDK (nur ohne Premium). */
    fun gatherConsent() {
        refreshConsentState()
        if (consentStarted) return
        consentStarted = true
        platform.requestConsent { onMain { refreshConsentState() } }
    }

    fun showPrivacyOptions() {
        platform.showPrivacyOptions { onMain { refreshConsentState() } }
    }

    fun resetConsentForDebug() {
        platform.resetConsent()
        consentStarted = false
        refreshConsentState()
    }

    private fun refreshConsentState() {
        _canRequestAds.value = platform.canRequestAds()
        _privacyOptionsRequired.value = platform.isPrivacyOptionsRequired()
        if (_canRequestAds.value && !initStarted) {
            initStarted = true
            platform.startSdk {
                onMain {
                    _sdkReady.value = true
                    if (pendingRewardedLoad) {
                        pendingRewardedLoad = false
                        _rewardedStatus.value = RewardedStatus.IDLE
                        loadRewarded()
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ Interstitial

    /** Nach jedem abgeschlossenen Test aufrufen. */
    fun onTestCompleted(adsAllowed: Boolean) {
        testsThisSession++
        scope.launch { settings.incrementTestsSinceInterstitial() }
        if (adsAllowed) preloadInterstitialIfUseful()
    }

    private fun preloadInterstitialIfUseful() {
        if (units.interstitial.isBlank() || !_sdkReady.value || !_canRequestAds.value) return
        if (interstitialReady || interstitialLoading) return
        if (testsThisSession < InterstitialPolicy.MIN_TESTS_IN_SESSION - 1) return
        interstitialLoading = true
        platform.loadInterstitial(units.interstitial) { loaded ->
            onMain {
                interstitialLoading = false
                interstitialReady = loaded
            }
        }
    }

    /**
     * Natürliche Pause: „Neuer Test“ nach einem Ergebnis.
     * [onDone] wird immer aufgerufen – sofort, wenn keine Anzeige gezeigt wird.
     */
    fun maybeShowInterstitial(adsAllowed: Boolean, onDone: () -> Unit) {
        if (!adsAllowed || !interstitialReady || !_canRequestAds.value) { onDone(); return }
        scope.launch {
            val counters = settings.adCounters()
            val now = clock()
            if (!InterstitialPolicy.shouldShow(now, counters.lastInterstitialAt, counters.testsSinceInterstitial, testsThisSession)) {
                onDone()
                return@launch
            }
            interstitialReady = false
            settings.markInterstitialShown(now)
            val shown = platform.showInterstitial { onMain(onDone) }
            if (!shown) onDone()
        }
    }

    // ------------------------------------------------------------------ Rewarded

    fun loadRewarded() {
        if (!rewardedEnabled) { _rewardedStatus.value = RewardedStatus.UNAVAILABLE; return }
        if (rewardedReady) { _rewardedStatus.value = RewardedStatus.READY; return }
        if (_rewardedStatus.value == RewardedStatus.LOADING) return
        if (!_canRequestAds.value) { _rewardedStatus.value = RewardedStatus.UNAVAILABLE; return }
        _rewardedStatus.value = RewardedStatus.LOADING
        if (!_sdkReady.value) { pendingRewardedLoad = true; return }
        platform.loadRewarded(units.rewarded, units.rewardedIsInterstitial) { loaded ->
            onMain {
                rewardedReady = loaded
                _rewardedStatus.value = if (loaded) RewardedStatus.READY else RewardedStatus.UNAVAILABLE
            }
        }
    }

    /**
     * [onReward] wird AUSSCHLIESSLICH nach dem offiziellen Belohnungs-Callback aufgerufen,
     * [onClosed] in jedem Fall danach (mit der Info, ob belohnt wurde).
     */
    fun showRewarded(onReward: () -> Unit, onClosed: (rewarded: Boolean) -> Unit) {
        if (!rewardedReady) { onClosed(false); loadRewarded(); return }
        rewardedReady = false
        _rewardedStatus.value = RewardedStatus.IDLE
        var earned = false
        val shown = platform.showRewarded(
            onReward = { onMain { earned = true; onReward() } },
            onClosed = { onMain { onClosed(earned); loadRewarded() } },
        )
        if (!shown) { onClosed(false); loadRewarded() }
    }
}
