import AppTrackingTransparency
import GoogleMobileAds
import Shared
import UIKit
import UserMessagingPlatform

/// Google Mobile Ads + User Messaging Platform für iOS.
/// Implementiert das Kotlin-Protokoll `AdsPlatform`; die eigentliche Logik
/// (Frequenz, Belohnungen, Einwilligung abwarten) liegt im gemeinsamen Kotlin-Code.
final class AdsPlatformImpl: NSObject, AdsPlatform {
    static let shared = AdsPlatformImpl()

    /// Ohne AdMob-App-ID (z. B. Release ohne Konfiguration) wird das SDK nie gestartet.
    private let adsConfigured = !AppConfiguration.value("GADApplicationIdentifier").isEmpty

    private var interstitial: InterstitialAd?
    private var rewarded: RewardedAd?
    private var rewardedInterstitial: RewardedInterstitialAd?
    private var fullScreenDelegate: FullScreenDelegate?

    func canRequestAds() -> Bool {
        adsConfigured && ConsentInformation.shared.canRequestAds
    }

    func isPrivacyOptionsRequired() -> Bool {
        ConsentInformation.shared.privacyOptionsRequirementStatus == .required
    }

    func requestConsent(onDone: @escaping () -> Void) {
        guard adsConfigured else { onDone(); return }
        let parameters = RequestParameters()
        ConsentInformation.shared.requestConsentInfoUpdate(with: parameters) { error in
            if let error { print("Consent info update failed: \(error.localizedDescription)") }
            Task { @MainActor in
                if let presenter = topViewController() {
                    do { try await ConsentForm.loadAndPresentIfRequired(from: presenter) }
                    catch { print("Consent form: \(error.localizedDescription)") }
                }
                await self.requestTrackingIfNeeded()
                onDone()
            }
        }
        // Einwilligung aus einer früheren Sitzung sofort nutzen
        if ConsentInformation.shared.canRequestAds { onDone() }
    }

    /// App Tracking Transparency: nur fragen, wenn noch nicht entschieden und Werbung erlaubt ist.
    @MainActor
    private func requestTrackingIfNeeded() async {
        guard ConsentInformation.shared.canRequestAds,
              ATTrackingManager.trackingAuthorizationStatus == .notDetermined else { return }
        _ = await ATTrackingManager.requestTrackingAuthorization()
    }

    func showPrivacyOptions(onDone: @escaping () -> Void) {
        Task { @MainActor in
            if let presenter = topViewController() {
                do { try await ConsentForm.presentPrivacyOptionsForm(from: presenter) }
                catch { print("Privacy options: \(error.localizedDescription)") }
            }
            onDone()
        }
    }

    func resetConsent() {
        ConsentInformation.shared.reset()
    }

    func startSdk(onReady: @escaping () -> Void) {
        guard adsConfigured else { return }
        // Für ein Unterhaltungsangebot, das auch Jugendliche nutzen: nur Anzeigen bis „Teen“.
        MobileAds.shared.requestConfiguration.maxAdContentRating = .teen
        MobileAds.shared.start { _ in onReady() }
    }

    func loadInterstitial(unitId: String, onResult: @escaping (KotlinBoolean) -> Void) {
        Task { @MainActor in
            do {
                interstitial = try await InterstitialAd.load(with: unitId, request: Request())
                onResult(KotlinBoolean(bool: true))
            } catch {
                interstitial = nil
                onResult(KotlinBoolean(bool: false))
            }
        }
    }

    func showInterstitial(onDismissed: @escaping () -> Void) -> Bool {
        guard let ad = interstitial, let presenter = topViewController() else { return false }
        interstitial = nil
        let delegate = FullScreenDelegate(onClosed: onDismissed)
        fullScreenDelegate = delegate
        ad.fullScreenContentDelegate = delegate
        ad.present(from: presenter)
        return true
    }

    func loadRewarded(unitId: String, interstitialFormat: Bool, onResult: @escaping (KotlinBoolean) -> Void) {
        Task { @MainActor in
            do {
                if interstitialFormat {
                    rewardedInterstitial = try await RewardedInterstitialAd.load(with: unitId, request: Request())
                } else {
                    rewarded = try await RewardedAd.load(with: unitId, request: Request())
                }
                onResult(KotlinBoolean(bool: true))
            } catch {
                rewarded = nil
                rewardedInterstitial = nil
                onResult(KotlinBoolean(bool: false))
            }
        }
    }

    func showRewarded(onReward: @escaping () -> Void, onClosed: @escaping () -> Void) -> Bool {
        guard let presenter = topViewController() else { return false }
        let delegate = FullScreenDelegate(onClosed: onClosed)
        fullScreenDelegate = delegate
        if let ad = rewardedInterstitial {
            rewardedInterstitial = nil
            ad.fullScreenContentDelegate = delegate
            // Belohnung ausschließlich über den offiziellen Callback des SDK
            ad.present(from: presenter) { onReward() }
            return true
        }
        if let ad = rewarded {
            rewarded = nil
            ad.fullScreenContentDelegate = delegate
            ad.present(from: presenter) { onReward() }
            return true
        }
        return false
    }
}

/// Meldet das Schließen (oder Fehlschlagen) einer Vollbildanzeige.
private final class FullScreenDelegate: NSObject, FullScreenContentDelegate {
    private let onClosed: () -> Void
    private var called = false

    init(onClosed: @escaping () -> Void) { self.onClosed = onClosed }

    private func finish() {
        guard !called else { return }
        called = true
        onClosed()
    }

    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) { finish() }

    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) { finish() }
}
