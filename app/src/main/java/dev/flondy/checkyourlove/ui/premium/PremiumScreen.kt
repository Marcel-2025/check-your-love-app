package dev.flondy.checkyourlove.ui.premium

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.flondy.checkyourlove.R
import dev.flondy.checkyourlove.monetization.CatalogItem
import dev.flondy.checkyourlove.monetization.Entitlements
import dev.flondy.checkyourlove.monetization.ResultEffect
import dev.flondy.checkyourlove.monetization.ShareTemplate
import dev.flondy.checkyourlove.monetization.ads.RewardedStatus
import dev.flondy.checkyourlove.monetization.billing.Ownership
import dev.flondy.checkyourlove.monetization.billing.PremiumState
import dev.flondy.checkyourlove.monetization.billing.SimulatedBillingGateway.Outcome
import dev.flondy.checkyourlove.monetization.billing.StoreAvailability
import dev.flondy.checkyourlove.ui.components.GradientButton
import dev.flondy.checkyourlove.ui.components.HeartButton
import dev.flondy.checkyourlove.ui.components.SectionCard
import dev.flondy.checkyourlove.ui.components.SoftIconButton
import dev.flondy.checkyourlove.ui.components.drawHeart
import dev.flondy.checkyourlove.ui.theme.AppPalette
import dev.flondy.checkyourlove.ui.theme.LocalPalette
import java.text.DateFormat
import java.util.Date

/** Steuerung der Debug-Simulation (null in echten Builds). */
data class SimulationControls(
    val outcome: Outcome,
    val onOutcome: (Outcome) -> Unit,
    val onCompletePending: () -> Unit,
    val onRefund: () -> Unit,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PremiumScreen(
    state: PremiumState,
    entitlements: Entitlements,
    darkPreview: Boolean,
    simulation: SimulationControls?,
    rewardedEnabled: Boolean,
    rewardedStatus: RewardedStatus,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onBuy: () -> Unit,
    onRestore: () -> Unit,
    onTry: (CatalogItem) -> Unit,
) {
    val palette = LocalPalette.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth().widthIn(max = 640.dp).padding(top = 8.dp)) {
            SoftIconButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.nav_back), onBack)
        }
        Column(Modifier.widthIn(max = 640.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            HeartButton(size = 140.dp, pulsing = false, enabled = false, contentDescription = "", onClick = {}) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.padding(bottom = 10.dp).size(34.dp))
            }
            Text(
                stringResource(R.string.premium_title),
                style = MaterialTheme.typography.headlineLarge,
                color = palette.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.premium_subtitle), style = MaterialTheme.typography.bodyLarge, color = palette.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))

            SectionCard {
                Benefit(Icons.Rounded.Block, R.string.premium_benefit_ads, R.string.premium_benefit_ads_desc)
                Benefit(Icons.Rounded.Palette, R.string.premium_benefit_designs, R.string.premium_benefit_designs_desc)
                Benefit(Icons.Rounded.AutoAwesome, R.string.premium_benefit_effects, R.string.premium_benefit_effects_desc)
                Benefit(Icons.Rounded.IosShare, R.string.premium_benefit_share, R.string.premium_benefit_share_desc, last = true)
            }

            Spacer(Modifier.height(18.dp))
            Text(
                stringResource(R.string.premium_preview),
                style = MaterialTheme.typography.titleSmall,
                color = palette.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                items(AppPalette.entries.filter { it.premium }) { item -> PhonePreview(item, darkPreview) }
            }

            Spacer(Modifier.height(20.dp))
            PurchaseArea(state, simulation != null, onBuy)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.premium_fair_note),
                style = MaterialTheme.typography.bodySmall,
                color = palette.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (state.availability != StoreAvailability.NOT_CONFIGURED) {
                TextButton(onClick = onRestore, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.premium_restore))
                }
            }

            if (simulation != null) {
                Spacer(Modifier.height(8.dp))
                SimulationPanel(simulation, state)
            }

            if (!entitlements.premium && rewardedEnabled) {
                Spacer(Modifier.height(16.dp))
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PlayCircle, contentDescription = null, tint = palette.primary)
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.premium_try_title), style = MaterialTheme.typography.titleMedium, color = palette.onBackground)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.premium_try_text), style = MaterialTheme.typography.bodyMedium, color = palette.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(R.string.premium_try_choose), style = MaterialTheme.typography.labelMedium, color = palette.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    val items: List<CatalogItem> = AppPalette.entries.filter { it.premium } +
                        ResultEffect.entries.filter { it.premium } + ShareTemplate.entries.filter { it.premium }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items.forEach { item ->
                            val active = entitlements.rewardFor(item) != null
                            AssistChip(
                                onClick = { onTry(item) },
                                label = { Text(stringResource(item.labelRes)) },
                                leadingIcon = {
                                    Icon(
                                        if (active) Icons.Rounded.CheckCircle else Icons.Rounded.HourglassTop,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(leadingIconContentColor = palette.primary),
                                modifier = Modifier.heightIn(min = 48.dp),
                            )
                        }
                    }
                    if (rewardedStatus == RewardedStatus.UNAVAILABLE) {
                        Text(stringResource(R.string.reward_unavailable), style = MaterialTheme.typography.bodySmall, color = palette.onSurfaceVariant)
                    }
                    if (entitlements.activeRewards.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.premium_active_unlocks), style = MaterialTheme.typography.labelMedium, color = palette.onSurfaceVariant)
                        entitlements.activeRewards.forEach { reward ->
                            val item = items.firstOrNull { it.key == reward.itemKey } ?: return@forEach
                            val until = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(reward.expiresAt))
                            Text(
                                "${stringResource(item.labelRes)} – ${stringResource(R.string.unlocked_until, until)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.primary,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun Benefit(icon: ImageVector, title: Int, desc: Int, last: Boolean = false) {
    val palette = LocalPalette.current
    Row(Modifier.padding(bottom = if (last) 0.dp else 14.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(palette.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = palette.onPrimaryContainer, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium, color = palette.onBackground)
            Text(stringResource(desc), style = MaterialTheme.typography.bodyMedium, color = palette.onSurfaceVariant)
        }
    }
}

@Composable
private fun PhonePreview(item: AppPalette, dark: Boolean) {
    val c = item.colors(dark)
    val palette = LocalPalette.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(width = 96.dp, height = 168.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.verticalGradient(listOf(c.backgroundAlt, c.background)))
                .border(BorderStroke(1.dp, palette.outline.copy(alpha = 0.35f)), RoundedCornerShape(20.dp))
                .padding(10.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(6.dp)).background(c.surfaceHigh))
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(6.dp)).background(c.surfaceHigh))
                Spacer(Modifier.height(14.dp))
                Canvas(Modifier.size(52.dp)) { drawHeart(Brush.linearGradient(listOf(c.heartStart, c.heartMid, c.heartEnd))) }
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier.fillMaxWidth(0.8f).height(16.dp).clip(CircleShape)
                        .background(Brush.horizontalGradient(listOf(c.heartStart, c.heartEnd)))
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(stringResource(item.labelRes), style = MaterialTheme.typography.labelMedium, color = palette.onBackground)
    }
}

@Composable
private fun PurchaseArea(state: PremiumState, simulation: Boolean, onBuy: () -> Unit) {
    val palette = LocalPalette.current
    when {
        state.ownership == Ownership.OWNED -> StatusLine(Icons.Rounded.CheckCircle, stringResource(R.string.premium_owned))
        state.ownership == Ownership.PENDING -> StatusLine(Icons.Rounded.HourglassTop, stringResource(R.string.premium_pending))
        state.availability == StoreAvailability.NOT_CONFIGURED -> StatusLine(Icons.Rounded.Block, stringResource(R.string.premium_not_configured))
        state.availability == StoreAvailability.UNAVAILABLE -> StatusLine(Icons.Rounded.Block, stringResource(R.string.premium_unavailable))
        state.availability == StoreAvailability.CHECKING -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = palette.primary)
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.premium_loading_price), color = palette.onSurfaceVariant)
            }
        }
        else -> {
            val price = state.formattedPrice ?: if (simulation) stringResource(R.string.sim_price) else null
            if (price == null) {
                StatusLine(Icons.Rounded.Block, stringResource(R.string.premium_unavailable))
            } else {
                GradientButton(
                    text = stringResource(R.string.premium_buy, price),
                    onClick = onBuy,
                    enabled = !state.purchaseInProgress,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.premium_one_time), style = MaterialTheme.typography.bodySmall, color = palette.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusLine(icon: ImageVector, text: String) {
    val palette = LocalPalette.current
    SectionCard(contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = palette.primary)
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, color = palette.onBackground)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SimulationPanel(controls: SimulationControls, state: PremiumState) {
    val palette = LocalPalette.current
    SectionCard(contentPadding = PaddingValues(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Science, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.sim_banner), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.sim_outcome), style = MaterialTheme.typography.labelMedium, color = palette.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Outcome.entries.forEach { outcome ->
                FilterChip(
                    selected = controls.outcome == outcome,
                    onClick = { controls.onOutcome(outcome) },
                    label = {
                        Text(
                            stringResource(
                                when (outcome) {
                                    Outcome.SUCCESS -> R.string.sim_success
                                    Outcome.PENDING -> R.string.sim_pending
                                    Outcome.CANCEL -> R.string.sim_cancel
                                    Outcome.ERROR -> R.string.sim_error
                                }
                            )
                        )
                    },
                )
            }
        }
        if (state.ownership == Ownership.PENDING) {
            OutlinedButton(onClick = controls.onCompletePending) { Text(stringResource(R.string.sim_complete_pending)) }
        }
        if (state.ownership == Ownership.OWNED) {
            OutlinedButton(onClick = controls.onRefund) { Text(stringResource(R.string.sim_refund)) }
        }
    }
}
