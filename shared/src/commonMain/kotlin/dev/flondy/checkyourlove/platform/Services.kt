package dev.flondy.checkyourlove.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.flondy.checkyourlove.monetization.ads.AdUnits

/** Plattform- und Build-abhängige Konfiguration. */
data class AppConfig(
    val versionName: String,
    val isDebug: Boolean,
    /** Leer = Menüpunkt ausgeblendet. */
    val privacyPolicyUrl: String,
    val adUnits: AdUnits,
    val contactEmail: String = "flondy.dev@gmail.com",
    val websiteUrl: String = "http://flondy.com",
    /** Store-Seite mit weiteren Apps; leer = ausgeblendet. */
    val moreAppsUrl: String = "",
    /** Name des Stores für Texte, z. B. „Google Play“ oder „App Store“. */
    val storeName: String,
)

/** Teilen eines Bildes über den System-Dialog. */
interface ShareService {
    fun shareImage(png: ByteArray, text: String, chooserTitle: String)
}

/** Verankertes Banner der jeweiligen Plattform (AdMob). [onFailed] blendet den Platz aus. */
typealias BannerComposable = @Composable (unitId: String, onFailed: () -> Unit, modifier: Modifier) -> Unit
