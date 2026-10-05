import GoogleMobileAds
import Shared
import UIKit

/// Erzeugt verankerte adaptive Banner für die Compose-Oberfläche.
final class BannerFactoryImpl: NSObject, IosBannerFactory {
    private var delegates: [ObjectIdentifier: BannerDelegate] = [:]

    func bannerHeight(width: Double) -> Double {
        Double(largeAnchoredAdaptiveBanner(width: CGFloat(width)).size.height)
    }

    func createBannerView(unitId: String, width: Double, onFailed: @escaping () -> Void) -> UIView {
        let banner = BannerView(adSize: largeAnchoredAdaptiveBanner(width: CGFloat(width)))
        banner.adUnitID = unitId
        banner.rootViewController = topViewController()
        let delegate = BannerDelegate(onFailed: onFailed)
        delegates[ObjectIdentifier(banner)] = delegate
        banner.delegate = delegate
        banner.load(Request())
        return banner
    }
}

private final class BannerDelegate: NSObject, BannerViewDelegate {
    private let onFailed: () -> Void
    init(onFailed: @escaping () -> Void) { self.onFailed = onFailed }

    func bannerView(_ bannerView: BannerView, didFailToReceiveAdWithError error: Error) {
        print("Banner not loaded: \(error.localizedDescription)")
        onFailed()
    }
}
