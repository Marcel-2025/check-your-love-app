package dev.flondy.checkyourlove

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import androidx.compose.ui.window.ComposeUIViewController
import dev.flondy.checkyourlove.data.createSettingsDataStore
import dev.flondy.checkyourlove.monetization.IosBannerFactory
import dev.flondy.checkyourlove.monetization.IosStoreBridge
import dev.flondy.checkyourlove.monetization.StoreKitGateway
import dev.flondy.checkyourlove.monetization.ads.AdUnits
import dev.flondy.checkyourlove.monetization.ads.AdsPlatform
import dev.flondy.checkyourlove.platform.AppConfig
import dev.flondy.checkyourlove.platform.AppServices
import dev.flondy.checkyourlove.platform.IosShareService
import dev.flondy.checkyourlove.platform.iosDatabaseBuilder
import dev.flondy.checkyourlove.platform.iosSettingsPath
import dev.flondy.checkyourlove.ui.CheckYourLoveApp
import platform.UIKit.UIViewController

/** Werte aus Swift (Info.plist / Build-Konfiguration). */
data class IosAppConfig(
    val versionName: String,
    val isDebug: Boolean,
    val privacyPolicyUrl: String,
    val bannerId: String,
    val interstitialId: String,
    val rewardedId: String,
    val rewardedIsInterstitial: Boolean,
    val premiumProductId: String,
    val moreAppsUrl: String,
)

private var services: AppServices? = null

/** Einmalig beim App-Start aus Swift aufrufen. */
fun setUpServices(config: IosAppConfig, ads: AdsPlatform, store: IosStoreBridge, banners: IosBannerFactory) {
    if (services != null) return
    services = AppServices.create(
        config = AppConfig(
            versionName = config.versionName,
            isDebug = config.isDebug,
            privacyPolicyUrl = config.privacyPolicyUrl,
            adUnits = AdUnits(config.bannerId, config.interstitialId, config.rewardedId, config.rewardedIsInterstitial),
            moreAppsUrl = config.moreAppsUrl,
            storeName = "App Store",
        ),
        database = iosDatabaseBuilder(),
        settingsStore = createSettingsDataStore(iosSettingsPath()),
        gateway = { _, scope -> StoreKitGateway(store, config.premiumProductId, scope) },
        adsPlatform = ads,
        share = IosShareService(),
        banner = { unitId, onFailed, modifier ->
            BoxWithConstraints(modifier.fillMaxWidth()) {
                val width = maxWidth.value.toDouble()
                val height = banners.bannerHeight(width)
                UIKitView(
                    factory = { banners.createBannerView(unitId, width, onFailed) },
                    modifier = Modifier.fillMaxWidth().height(height.dp),
                )
            }
        },
    )
}

/** Rückkehr in den Vordergrund: Käufe abgleichen (ausstehende Zahlungen, Erstattungen). */
fun onAppForeground() {
    services?.premiumGateway?.refresh()
}

/** Die komplette Oberfläche als UIViewController für SwiftUI/UIKit. */
fun MainViewController(onDarkThemeChanged: (Boolean) -> Unit): UIViewController {
    val s = requireNotNull(services) { "setUpServices() muss vorher aufgerufen werden" }
    return ComposeUIViewController { CheckYourLoveApp(s, onDarkThemeChanged) }
}
