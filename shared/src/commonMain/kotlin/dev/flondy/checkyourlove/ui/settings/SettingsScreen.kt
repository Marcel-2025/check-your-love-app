package dev.flondy.checkyourlove.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.flondy.checkyourlove.platform.AppConfig
import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.*
import dev.flondy.checkyourlove.data.AppSettings
import dev.flondy.checkyourlove.data.MotionPreference
import dev.flondy.checkyourlove.data.ThemeMode
import dev.flondy.checkyourlove.monetization.CatalogItem
import dev.flondy.checkyourlove.monetization.Entitlements
import dev.flondy.checkyourlove.monetization.ResultEffect
import dev.flondy.checkyourlove.monetization.billing.StoreAvailability
import dev.flondy.checkyourlove.ui.components.LockBadge
import dev.flondy.checkyourlove.ui.components.SectionCard
import dev.flondy.checkyourlove.ui.components.SectionLabel
import dev.flondy.checkyourlove.ui.components.drawHeart
import dev.flondy.checkyourlove.ui.theme.AppPalette
import dev.flondy.checkyourlove.ui.theme.LocalHaptics
import dev.flondy.checkyourlove.ui.theme.LocalPalette
import dev.flondy.checkyourlove.platform.formatShortDateTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    config: AppConfig,
    settings: AppSettings,
    entitlements: Entitlements,
    storeAvailability: StoreAvailability,
    darkPreview: Boolean,
    privacyOptionsRequired: Boolean,
    contentPadding: PaddingValues,
    onThemeMode: (ThemeMode) -> Unit,
    onPalette: (AppPalette) -> Unit,
    onEffect: (ResultEffect) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onMotion: (MotionPreference) -> Unit,
    onLocked: (CatalogItem) -> Unit,
    onOpenPremium: () -> Unit,
    onRestore: () -> Unit,
    onPrivacyOptions: () -> Unit,
    onResetConsent: () -> Unit,
) {
    val palette = LocalPalette.current
    val uriHandler = LocalUriHandler.current
    val haptics = LocalHaptics.current
    var showLicenses by rememberSaveable { mutableStateOf(false) }

    fun open(url: String) {
        runCatching { uriHandler.openUri(url) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = 640.dp)) {
            Text(
                stringResource(Res.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = palette.onBackground,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp).semantics { heading() },
            )

            // ---------------------------------------------------------- Erscheinungsbild
            SectionLabel(stringResource(Res.string.settings_section_appearance))
            SectionCard {
                Text(stringResource(Res.string.settings_mode), style = MaterialTheme.typography.titleSmall, color = palette.onBackground)
                Spacer(Modifier.height(8.dp))
                SegmentRow(
                    options = ThemeMode.entries,
                    selected = settings.themeMode,
                    label = {
                        stringResource(
                            when (it) {
                                ThemeMode.SYSTEM -> Res.string.settings_mode_system
                                ThemeMode.LIGHT -> Res.string.settings_mode_light
                                ThemeMode.DARK -> Res.string.settings_mode_dark
                            }
                        )
                    },
                    onSelect = onThemeMode,
                )
                Spacer(Modifier.height(18.dp))
                Text(stringResource(Res.string.settings_palette), style = MaterialTheme.typography.titleSmall, color = palette.onBackground)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(AppPalette.entries) { item ->
                        PaletteSwatch(
                            item = item,
                            dark = darkPreview,
                            selected = settings.paletteId == item.id && entitlements.isUnlocked(item),
                            unlocked = entitlements.isUnlocked(item),
                            onClick = {
                                haptics.tick()
                                if (entitlements.isUnlocked(item)) onPalette(item) else onLocked(item)
                            },
                        )
                    }
                }
                RewardHint(entitlements, AppPalette.byId(settings.paletteId))
                Spacer(Modifier.height(18.dp))
                Text(stringResource(Res.string.settings_effect), style = MaterialTheme.typography.titleSmall, color = palette.onBackground)
                Text(stringResource(Res.string.settings_effect_hint), style = MaterialTheme.typography.bodySmall, color = palette.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ResultEffect.entries.forEach { effect ->
                        val unlocked = entitlements.isUnlocked(effect)
                        FilterChip(
                            selected = settings.effectId == effect.id && unlocked,
                            onClick = {
                                haptics.tick()
                                if (unlocked) onEffect(effect) else onLocked(effect)
                            },
                            label = { Text(stringResource(effect.labelRes)) },
                            leadingIcon = if (!unlocked) {
                                { Icon(Icons.Rounded.Lock, contentDescription = stringResource(Res.string.locked_badge), modifier = Modifier.size(16.dp)) }
                            } else if (settings.effectId == effect.id) {
                                { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.primaryContainer,
                                selectedLabelColor = palette.onPrimaryContainer,
                                selectedLeadingIconColor = palette.onPrimaryContainer,
                            ),
                            modifier = Modifier.heightIn(min = 48.dp),
                        )
                    }
                }
                RewardHint(entitlements, ResultEffect.byId(settings.effectId))
            }

            // ---------------------------------------------------------- Bewegung & Haptik
            SectionLabel(stringResource(Res.string.settings_section_motion), Modifier.padding(top = 12.dp))
            SectionCard {
                SwitchRow(
                    title = stringResource(Res.string.settings_haptics),
                    subtitle = stringResource(Res.string.settings_haptics_desc),
                    checked = settings.haptics,
                    onChange = onHaptics,
                )
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = palette.outline.copy(alpha = 0.2f))
                Text(stringResource(Res.string.settings_motion), style = MaterialTheme.typography.titleSmall, color = palette.onBackground)
                Spacer(Modifier.height(8.dp))
                SegmentRow(
                    options = MotionPreference.entries,
                    selected = settings.motion,
                    label = {
                        stringResource(
                            when (it) {
                                MotionPreference.SYSTEM -> Res.string.settings_motion_system
                                MotionPreference.REDUCED -> Res.string.settings_motion_on
                                MotionPreference.FULL -> Res.string.settings_motion_off
                            }
                        )
                    },
                    onSelect = onMotion,
                )
            }

            // ---------------------------------------------------------- Premium
            if (storeAvailability != StoreAvailability.NOT_CONFIGURED || entitlements.premium) {
                SectionLabel(stringResource(Res.string.settings_section_premium), Modifier.padding(top = 12.dp))
                SectionCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                    LinkRow(
                        title = stringResource(if (entitlements.premium) Res.string.settings_premium_active else Res.string.settings_premium_inactive),
                        icon = Icons.Rounded.WorkspacePremium,
                        onClick = onOpenPremium,
                    )
                    LinkRow(title = stringResource(Res.string.settings_restore), onClick = onRestore)
                }
            }

            // ---------------------------------------------------------- Datenschutz
            SectionLabel(stringResource(Res.string.settings_section_privacy), Modifier.padding(top = 12.dp))
            SectionCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                Text(
                    stringResource(Res.string.settings_privacy_local),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
                if (privacyOptionsRequired) {
                    LinkRow(title = stringResource(Res.string.settings_privacy_options), onClick = onPrivacyOptions)
                }
                if (config.privacyPolicyUrl.isNotBlank()) {
                    LinkRow(title = stringResource(Res.string.settings_privacy_policy), external = true, onClick = { open(config.privacyPolicyUrl) })
                }
                if (config.isDebug) {
                    LinkRow(title = stringResource(Res.string.settings_consent_reset), onClick = onResetConsent)
                }
            }

            // ---------------------------------------------------------- Über
            SectionLabel(stringResource(Res.string.settings_section_about), Modifier.padding(top = 12.dp))
            SectionCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                Text(
                    stringResource(Res.string.settings_about_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
                LinkRow(title = stringResource(Res.string.settings_website), external = true, onClick = { open(config.websiteUrl) })
                if (config.moreAppsUrl.isNotBlank()) {
                    LinkRow(
                        title = stringResource(Res.string.settings_more_apps), external = true,
                        onClick = { open(config.moreAppsUrl) },
                    )
                }
                val subject = stringResource(Res.string.settings_contact_subject)
                LinkRow(
                    title = stringResource(Res.string.settings_contact), external = true,
                    onClick = {
                        open("mailto:${config.contactEmail}?subject=" + encodeMailSubject(subject))
                    },
                )
                LinkRow(title = stringResource(Res.string.settings_licenses), onClick = { showLicenses = true })
                Text(
                    stringResource(Res.string.settings_version, config.versionName),
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showLicenses) {
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            title = { Text(stringResource(Res.string.settings_licenses)) },
            text = { Text(stringResource(Res.string.settings_licenses_text)) },
            confirmButton = { TextButton(onClick = { showLicenses = false }) { Text(stringResource(Res.string.action_close)) } },
        )
    }
}

@Composable
private fun <T> SegmentRow(options: List<T>, selected: T, label: @Composable (T) -> String, onSelect: (T) -> Unit) {
    val palette = LocalPalette.current
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = palette.primaryContainer,
                    activeContentColor = palette.onPrimaryContainer,
                    inactiveContainerColor = Color.Transparent,
                ),
                modifier = Modifier.heightIn(min = 48.dp),
            ) { Text(label(option), maxLines = 1) }
        }
    }
}

@Composable
private fun PaletteSwatch(item: AppPalette, dark: Boolean, selected: Boolean, unlocked: Boolean, onClick: () -> Unit) {
    val colors = item.colors(dark)
    val palette = LocalPalette.current
    val border by animateColorAsState(if (selected) palette.primary else palette.outline.copy(alpha = 0.3f), label = "swatchBorder")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(84.dp)
            .clip(RoundedCornerShape(18.dp))
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 4.dp),
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(colors.backgroundAlt, colors.background)))
                .border(BorderStroke(if (selected) 3.dp else 1.dp, border), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(40.dp)) {
                drawHeart(Brush.linearGradient(listOf(colors.heartStart, colors.heartMid, colors.heartEnd)))
            }
            if (!unlocked) LockBadge(Modifier.align(Alignment.TopEnd).padding(4.dp), compact = true)
            if (selected) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(palette.primary),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Check, contentDescription = null, tint = palette.onPrimary, modifier = Modifier.size(14.dp)) }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(item.labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = palette.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}

@Composable
private fun RewardHint(entitlements: Entitlements, item: CatalogItem) {
    val reward = entitlements.rewardFor(item) ?: return
    val until = formatShortDateTime(reward.expiresAt)
    Text(
        stringResource(Res.string.unlocked_until, until),
        style = MaterialTheme.typography.bodySmall,
        color = LocalPalette.current.primary,
        modifier = Modifier.padding(top = 6.dp),
    )
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val palette = LocalPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = palette.onBackground)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = palette.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedTrackColor = palette.primary, checkedThumbColor = palette.onPrimary),
        )
    }
}

@Composable
private fun LinkRow(
    title: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    external: Boolean = false,
) {
    val palette = LocalPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = palette.primary)
            Spacer(Modifier.width(14.dp))
        }
        Text(title, style = MaterialTheme.typography.bodyLarge, color = palette.onBackground, modifier = Modifier.weight(1f))
        Icon(
            if (external) Icons.AutoMirrored.Rounded.OpenInNew else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = palette.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Prozent-Kodierung für den mailto-Betreff (RFC 6068). */
private fun encodeMailSubject(text: String): String = buildString {
    text.encodeToByteArray().forEach { b ->
        val c = b.toInt() and 0xff
        if (c in 'A'.code..'Z'.code || c in 'a'.code..'z'.code || c in '0'.code..'9'.code || c in "-_.~".map { it.code }) {
            append(c.toChar())
        } else {
            append('%'); append("0123456789ABCDEF"[c shr 4]); append("0123456789ABCDEF"[c and 15])
        }
    }
}
