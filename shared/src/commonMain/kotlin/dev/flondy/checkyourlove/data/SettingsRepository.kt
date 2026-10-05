package dev.flondy.checkyourlove.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.IOException

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class MotionPreference { SYSTEM, REDUCED, FULL }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val paletteId: String = "aubergine",
    val effectId: String = "confetti",
    val shareTemplateId: String = "classic",
    val haptics: Boolean = true,
    val motion: MotionPreference = MotionPreference.SYSTEM,
)

/** Zeitlich begrenzte Freischaltung durch eine freiwillige Rewarded Ad. */
data class RewardUnlock(val itemKey: String, val expiresAt: Long)

/** Zähler für die konservative Interstitial-Frequenz. */
data class AdCounters(val lastInterstitialAt: Long, val testsSinceInterstitial: Int)

/** Dateiname wie in der bisherigen Android-Version (DataStore „settings“), damit Einstellungen erhalten bleiben. */
const val SETTINGS_FILE_NAME = "settings.preferences_pb"

/** Erzeugt den DataStore. Pro Prozess genau einmal aufrufen. */
fun createSettingsDataStore(absolutePath: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { absolutePath.toPath() })

class SettingsRepository(private val store: DataStore<Preferences>) {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val palette = stringPreferencesKey("palette")
        val effect = stringPreferencesKey("effect")
        val shareTemplate = stringPreferencesKey("share_template")
        val haptics = booleanPreferencesKey("haptics")
        val motion = stringPreferencesKey("motion")
        val rewardUnlocks = stringSetPreferencesKey("reward_unlocks")
        val lastInterstitial = longPreferencesKey("last_interstitial_at")
        val testsSinceInterstitial = intPreferencesKey("tests_since_interstitial")
        val simulatedOwnership = stringPreferencesKey("simulated_ownership")
    }

    private val prefs: Flow<Preferences> = store.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    val settings: Flow<AppSettings> = prefs.map { p ->
        AppSettings(
            themeMode = p[Keys.themeMode].toEnum(ThemeMode.SYSTEM),
            paletteId = p[Keys.palette] ?: "aubergine",
            effectId = p[Keys.effect] ?: "confetti",
            shareTemplateId = p[Keys.shareTemplate] ?: "classic",
            haptics = p[Keys.haptics] ?: true,
            motion = p[Keys.motion].toEnum(MotionPreference.SYSTEM),
        )
    }

    val rewardUnlocks: Flow<List<RewardUnlock>> = prefs.map { p -> decodeUnlocks(p[Keys.rewardUnlocks].orEmpty()) }

    suspend fun setThemeMode(mode: ThemeMode) = store.edit { it[Keys.themeMode] = mode.name }
    suspend fun setPalette(id: String) = store.edit { it[Keys.palette] = id }
    suspend fun setEffect(id: String) = store.edit { it[Keys.effect] = id }
    suspend fun setShareTemplate(id: String) = store.edit { it[Keys.shareTemplate] = id }
    suspend fun setHaptics(enabled: Boolean) = store.edit { it[Keys.haptics] = enabled }
    suspend fun setMotion(pref: MotionPreference) = store.edit { it[Keys.motion] = pref.name }

    /** Fügt eine Freischaltung hinzu bzw. verlängert sie; abgelaufene Einträge werden entfernt. */
    suspend fun addRewardUnlock(itemKey: String, expiresAt: Long, now: Long) = store.edit { p ->
        val current = decodeUnlocks(p[Keys.rewardUnlocks].orEmpty())
            .filter { it.expiresAt > now && it.itemKey != itemKey }
        p[Keys.rewardUnlocks] = encodeUnlocks(current + RewardUnlock(itemKey, expiresAt))
    }

    suspend fun adCounters(): AdCounters = prefs.first().let {
        AdCounters(it[Keys.lastInterstitial] ?: 0L, it[Keys.testsSinceInterstitial] ?: 0)
    }

    suspend fun incrementTestsSinceInterstitial() = store.edit {
        it[Keys.testsSinceInterstitial] = (it[Keys.testsSinceInterstitial] ?: 0) + 1
    }

    suspend fun markInterstitialShown(now: Long) = store.edit {
        it[Keys.lastInterstitial] = now
        it[Keys.testsSinceInterstitial] = 0
    }

    suspend fun simulatedOwnership(): String? = prefs.first()[Keys.simulatedOwnership]
    suspend fun setSimulatedOwnership(value: String) = store.edit { it[Keys.simulatedOwnership] = value }

    companion object {
        fun encodeUnlocks(list: List<RewardUnlock>): Set<String> =
            list.map { "${it.itemKey}|${it.expiresAt}" }.toSet()

        fun decodeUnlocks(set: Set<String>): List<RewardUnlock> = set.mapNotNull { entry ->
            val idx = entry.lastIndexOf('|')
            if (idx <= 0) return@mapNotNull null
            val expires = entry.substring(idx + 1).toLongOrNull() ?: return@mapNotNull null
            RewardUnlock(entry.substring(0, idx), expires)
        }

        private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
            this?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default
    }
}
