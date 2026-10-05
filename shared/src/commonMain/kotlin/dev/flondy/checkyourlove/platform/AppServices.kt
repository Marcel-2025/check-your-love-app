package dev.flondy.checkyourlove.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import dev.flondy.checkyourlove.data.HistoryRepository
import dev.flondy.checkyourlove.data.SettingsRepository
import dev.flondy.checkyourlove.data.db.AppDatabase
import dev.flondy.checkyourlove.monetization.EntitlementRepository
import dev.flondy.checkyourlove.monetization.ads.AdsManager
import dev.flondy.checkyourlove.monetization.ads.AdsPlatform
import dev.flondy.checkyourlove.monetization.billing.PremiumGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Alle App-Dienste an einem Ort (einfache manuelle Abhängigkeitsverwaltung).
 * Wird je Plattform genau einmal erzeugt: Android in der Application-Klasse,
 * iOS in MainViewController.
 */
class AppServices(
    val config: AppConfig,
    val settings: SettingsRepository,
    val history: HistoryRepository,
    val premiumGateway: PremiumGateway,
    val entitlements: EntitlementRepository,
    val ads: AdsManager,
    val share: ShareService,
    val banner: BannerComposable?,
    val scope: CoroutineScope,
) {
    companion object {
        fun create(
            config: AppConfig,
            database: RoomDatabase.Builder<AppDatabase>,
            settingsStore: DataStore<Preferences>,
            gateway: (SettingsRepository, CoroutineScope) -> PremiumGateway,
            adsPlatform: AdsPlatform,
            share: ShareService,
            banner: BannerComposable?,
        ): AppServices {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            val settings = SettingsRepository(settingsStore)
            val db = AppDatabase.build(database)
            val premium = gateway(settings, scope)
            return AppServices(
                config = config,
                settings = settings,
                history = HistoryRepository(db.resultDao()),
                premiumGateway = premium,
                entitlements = EntitlementRepository(premium, settings, scope),
                ads = AdsManager(adsPlatform, config.adUnits, settings, scope),
                share = share,
                banner = banner,
                scope = scope,
            ).also { premium.start() }
        }
    }
}
