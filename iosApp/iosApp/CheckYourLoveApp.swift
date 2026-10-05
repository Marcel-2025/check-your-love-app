import SwiftUI
import Shared

/// Einstiegspunkt der iOS-App. Die komplette Oberfläche und Logik kommen aus dem
/// gemeinsamen Kotlin-Modul `shared` (Compose Multiplatform); Swift steuert nur
/// die Apple-/Google-SDKs bei (StoreKit, Google Mobile Ads, UMP).
@main
struct CheckYourLoveApp: App {
    @Environment(\.scenePhase) private var scenePhase

    init() {
        MainViewControllerKt.setUpServices(
            config: AppConfiguration.load(),
            ads: AdsPlatformImpl.shared,
            store: StoreBridgeImpl(),
            banners: BannerFactoryImpl()
        )
    }

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
        }
        .onChange(of: scenePhase) { phase in
            if phase == .active {
                MainViewControllerKt.onAppForeground()
            }
        }
    }
}

/// Liest die Werte aus Info.plist (gesetzt über Configuration/Debug.xcconfig bzw. Release.xcconfig).
enum AppConfiguration {
    static func value(_ key: String) -> String {
        (Bundle.main.object(forInfoDictionaryKey: key) as? String)?
            .trimmingCharacters(in: .whitespaces) ?? ""
    }

    static func load() -> IosAppConfig {
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif
        let adsConfigured = !value("GADApplicationIdentifier").isEmpty
        return IosAppConfig(
            versionName: value("CFBundleShortVersionString"),
            isDebug: isDebug,
            privacyPolicyUrl: value("CYLPrivacyPolicyUrl"),
            bannerId: adsConfigured ? value("CYLBannerId") : "",
            interstitialId: adsConfigured ? value("CYLInterstitialId") : "",
            rewardedId: adsConfigured ? value("CYLRewardedId") : "",
            rewardedIsInterstitial: value("CYLRewardedIsInterstitial").uppercased() == "YES",
            premiumProductId: value("CYLPremiumProductId"),
            // Link auf weitere Apps im App Store, sobald vorhanden (z. B. https://apps.apple.com/developer/…)
            moreAppsUrl: ""
        )
    }
}

/// Bettet den Compose-UIViewController ein und passt die Statusleiste an das App-Design an.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let container = StatusBarContainerViewController()
        let compose = MainViewControllerKt.MainViewController { dark in
            container.isDark = dark.boolValue
        }
        container.embed(compose)
        return container
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

final class StatusBarContainerViewController: UIViewController {
    var isDark: Bool = true {
        didSet { setNeedsStatusBarAppearanceUpdate() }
    }

    override var preferredStatusBarStyle: UIStatusBarStyle {
        isDark ? .lightContent : .darkContent
    }

    func embed(_ child: UIViewController) {
        addChild(child)
        child.view.frame = view.bounds
        child.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(child.view)
        child.didMove(toParent: self)
    }
}

/// Oberster sichtbarer ViewController (für Werbe- und Einwilligungsdialoge).
func topViewController() -> UIViewController? {
    let window = UIApplication.shared.connectedScenes
        .compactMap { $0 as? UIWindowScene }
        .flatMap { $0.windows }
        .first { $0.isKeyWindow }
    var top = window?.rootViewController
    while let presented = top?.presentedViewController { top = presented }
    return top
}
