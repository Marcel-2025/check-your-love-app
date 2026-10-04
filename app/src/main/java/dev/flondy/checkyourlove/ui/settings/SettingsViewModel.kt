package dev.flondy.checkyourlove.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.flondy.checkyourlove.data.AppSettings
import dev.flondy.checkyourlove.data.MotionPreference
import dev.flondy.checkyourlove.data.SettingsRepository
import dev.flondy.checkyourlove.data.ThemeMode
import dev.flondy.checkyourlove.monetization.ResultEffect
import dev.flondy.checkyourlove.monetization.ShareTemplate
import dev.flondy.checkyourlove.ui.theme.AppPalette
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Gemeinsames ViewModel für Einstellungen (wird auch für die Design-Auswahl in der App genutzt). */
class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AppSettings?> = repo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repo.setThemeMode(mode) }
    fun setPalette(palette: AppPalette) = viewModelScope.launch { repo.setPalette(palette.id) }
    fun setEffect(effect: ResultEffect) = viewModelScope.launch { repo.setEffect(effect.id) }
    fun setShareTemplate(template: ShareTemplate) = viewModelScope.launch { repo.setShareTemplate(template.id) }
    fun setHaptics(enabled: Boolean) = viewModelScope.launch { repo.setHaptics(enabled) }
    fun setMotion(pref: MotionPreference) = viewModelScope.launch { repo.setMotion(pref) }
}
