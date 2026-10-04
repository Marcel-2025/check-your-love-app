package dev.flondy.checkyourlove.monetization

import dev.flondy.checkyourlove.data.RewardUnlock
import dev.flondy.checkyourlove.data.SettingsRepository
import dev.flondy.checkyourlove.monetization.billing.Ownership
import dev.flondy.checkyourlove.monetization.billing.PremiumGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Aktuelle Berechtigungen. Grundfunktionen sind immer frei. */
data class Entitlements(
    val premium: Boolean = false,
    /** true, sobald feststeht, ob Premium besteht (oder die Prüfung zu lange dauert). */
    val resolved: Boolean = false,
    val activeRewards: List<RewardUnlock> = emptyList(),
) {
    fun isUnlocked(item: CatalogItem): Boolean =
        !item.premium || premium || activeRewards.any { it.itemKey == item.key }

    fun rewardFor(item: CatalogItem): RewardUnlock? =
        if (premium) null else activeRewards.firstOrNull { it.itemKey == item.key }

    /** Werbung nur für Nicht-Premium-Nutzer und erst, wenn der Status feststeht. */
    val showAds: Boolean get() = resolved && !premium

    companion object {
        const val REWARD_DURATION_MS = 24L * 60 * 60 * 1000

        /**
         * Filtert aktive Freischaltungen. Einträge, die weiter als die maximale Dauer in der
         * Zukunft liegen (z. B. Uhr zurückgestellt), werden verworfen.
         */
        fun activeRewards(all: List<RewardUnlock>, now: Long): List<RewardUnlock> =
            all.filter { it.expiresAt > now && it.expiresAt - now <= REWARD_DURATION_MS + 60_000 }
    }
}

class EntitlementRepository(
    private val gateway: PremiumGateway,
    private val settings: SettingsRepository,
    scope: CoroutineScope,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val resolveTimeout = MutableStateFlow(false)

    private val ticker = flow {
        while (true) {
            emit(clock())
            delay(15_000)
        }
    }

    val entitlements: StateFlow<Entitlements> = combine(
        gateway.state, settings.rewardUnlocks, ticker, resolveTimeout,
    ) { state, rewards, now, timedOut ->
        val premium = state.ownership == Ownership.OWNED
        Entitlements(
            premium = premium,
            resolved = state.ownership != Ownership.UNKNOWN || timedOut,
            activeRewards = Entitlements.activeRewards(rewards, now),
        )
    }.stateIn(scope, SharingStarted.Eagerly, Entitlements())

    init {
        scope.launch {
            delay(3_000)
            resolveTimeout.value = true
        }
    }

    suspend fun grantReward(item: CatalogItem) {
        val now = clock()
        settings.addRewardUnlock(item.key, now + Entitlements.REWARD_DURATION_MS, now)
    }
}
