package dev.flondy.checkyourlove.monetization

/**
 * Brücke zu Apple StoreKit 2 – implementiert in Swift (iosApp/StoreBridge.swift).
 * StoreKit prüft die Signatur jeder Transaktion selbst (VerificationResult); in die App
 * gelangen nur verifizierte Ergebnisse.
 */
interface IosStoreBridge {
    /** Lokalisierter Preis vom App Store, oder null, wenn das Produkt nicht gefunden wurde. */
    fun fetchProduct(productId: String, onResult: (formattedPrice: String?) -> Unit)

    /** true, wenn eine gültige (verifizierte, nicht widerrufene) Berechtigung besteht. */
    fun isEntitled(productId: String, onResult: (entitled: Boolean) -> Unit)

    /**
     * Startet den Kauf. Ergebnis: "success", "pending", "cancelled", "failed" oder "unverified".
     */
    fun purchase(productId: String, onResult: (result: String) -> Unit)

    /** Synchronisiert Käufe mit dem App Store (AppStore.sync()). */
    fun restore(onDone: (success: Boolean) -> Unit)

    /** Meldet Änderungen aus Transaction.updates (z. B. Freigabe ausstehender Käufe, Erstattung). */
    fun observeUpdates(onChanged: () -> Unit)
}

/** Erzeugt Banner-Views (Swift, GoogleMobileAds). */
interface IosBannerFactory {
    /** Höhe des adaptiven Banners in Punkten für die gegebene Breite. */
    fun bannerHeight(width: Double): Double
    fun createBannerView(unitId: String, width: Double, onFailed: () -> Unit): platform.UIKit.UIView
}
