package dev.flondy.checkyourlove.monetization

import com.google.common.truth.Truth.assertThat
import dev.flondy.checkyourlove.data.RewardUnlock
import dev.flondy.checkyourlove.monetization.ads.InterstitialPolicy
import dev.flondy.checkyourlove.ui.theme.AppPalette
import org.junit.Test

class EntitlementsTest {
    private val hour = 60L * 60 * 1000
    private val now = 1_000_000_000_000L

    @Test fun `free items are always unlocked, premium items only with premium or reward`() {
        val free = Entitlements(premium = false, resolved = true)
        assertThat(free.isUnlocked(AppPalette.AUBERGINE)).isTrue()
        assertThat(free.isUnlocked(ResultEffect.CONFETTI)).isTrue()
        assertThat(free.isUnlocked(AppPalette.AURORA)).isFalse()
        assertThat(free.isUnlocked(ShareTemplate.NEON)).isFalse()

        val premium = Entitlements(premium = true, resolved = true)
        assertThat(premium.isUnlocked(AppPalette.AURORA)).isTrue()
        assertThat(premium.isUnlocked(ResultEffect.HEART_RAIN)).isTrue()

        val rewarded = Entitlements(activeRewards = listOf(RewardUnlock(AppPalette.AURORA.key, now + hour)))
        assertThat(rewarded.isUnlocked(AppPalette.AURORA)).isTrue()
        assertThat(rewarded.isUnlocked(AppPalette.VELVET)).isFalse()
    }

    @Test fun `rewards expire and manipulated expiry is rejected`() {
        val all = listOf(
            RewardUnlock("palette:aurora", now + 23 * hour),     // aktiv
            RewardUnlock("effect:sparkle", now - 1),             // abgelaufen
            RewardUnlock("template:neon", now + 48 * hour),      // unplausibel (Uhr zurückgestellt)
        )
        val active = Entitlements.activeRewards(all, now)
        assertThat(active.map { it.itemKey }).containsExactly("palette:aurora")
        assertThat(Entitlements.activeRewards(all.take(2), now + 24 * hour)).isEmpty()
    }

    @Test fun `ads only when premium status is resolved and not premium`() {
        assertThat(Entitlements(premium = false, resolved = false).showAds).isFalse()
        assertThat(Entitlements(premium = false, resolved = true).showAds).isTrue()
        assertThat(Entitlements(premium = true, resolved = true).showAds).isFalse()
    }

    @Test fun `premium never changes the calculation`() {
        // Die Berechnung hat keinen Zugriff auf Berechtigungen – Signatur prüft das statisch.
        val a = dev.flondy.checkyourlove.core.LoveCalculator.calculate("Kim", "Lee")
        val b = dev.flondy.checkyourlove.core.LoveCalculator.calculate("Lee", "Kim")
        assertThat(a.percent).isEqualTo(b.percent)
    }

    @Test fun `interstitial policy is conservative`() {
        val min = InterstitialPolicy.MIN_INTERVAL_MS
        // nie in den ersten Tests einer Sitzung
        assertThat(InterstitialPolicy.shouldShow(now, 0, 10, 2)).isFalse()
        // nicht ohne genug Tests seit der letzten Anzeige
        assertThat(InterstitialPolicy.shouldShow(now, 0, 3, 5)).isFalse()
        // nicht zu früh nach der letzten Anzeige
        assertThat(InterstitialPolicy.shouldShow(now, now - min + 1, 10, 10)).isFalse()
        // erst wenn alles erfüllt ist
        assertThat(InterstitialPolicy.shouldShow(now, now - min, 4, 3)).isTrue()
    }

    @Test fun `catalog keys are unique`() {
        val keys = (AppPalette.entries + ResultEffect.entries + ShareTemplate.entries).map { it.key }
        assertThat(keys).containsNoDuplicates()
        assertThat(AppPalette.entries.count { !it.premium }).isEqualTo(2)
        assertThat(AppPalette.entries.count { it.premium }).isEqualTo(4)
    }
}
