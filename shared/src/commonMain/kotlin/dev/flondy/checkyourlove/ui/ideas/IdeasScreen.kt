package dev.flondy.checkyourlove.ui.ideas

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.flondy.checkyourlove.content.Idea
import dev.flondy.checkyourlove.content.IdeaCategory
import dev.flondy.checkyourlove.monetization.Entitlements
import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.*
import dev.flondy.checkyourlove.ui.components.GradientButton
import dev.flondy.checkyourlove.ui.components.ScreenHeader
import dev.flondy.checkyourlove.ui.components.SoftIconButton
import dev.flondy.checkyourlove.ui.components.drawHeart
import dev.flondy.checkyourlove.ui.test.FavoriteButton
import dev.flondy.checkyourlove.ui.theme.LocalHaptics
import dev.flondy.checkyourlove.ui.theme.LocalMotion
import dev.flondy.checkyourlove.ui.theme.LocalPalette
import dev.flondy.checkyourlove.ui.theme.LocalWindowLayout
import org.jetbrains.compose.resources.stringResource

private fun IdeaCategory.icon(): ImageVector = when (this) {
    IdeaCategory.DATES -> Icons.Rounded.Restaurant
    IdeaCategory.TALK -> Icons.Rounded.ChatBubble
    IdeaCategory.FLIRT -> Icons.Rounded.EmojiEmotions
    IdeaCategory.COMPLIMENTS -> Icons.Rounded.AutoAwesome
    IdeaCategory.ROMANCE -> Icons.Rounded.LocalFlorist
    IdeaCategory.CHALLENGES -> Icons.Rounded.SportsEsports
}

@Composable
fun IdeasScreen(
    state: IdeasUiState,
    entitlements: Entitlements,
    onSettings: (() -> Unit)?,
    onCategory: (IdeaCategory) -> Unit,
    onFavorites: () -> Unit,
    onLocked: (IdeaCategory) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleFavorite: (Idea) -> Unit,
    onShare: (String) -> Unit,
) {
    val palette = LocalPalette.current
    val layout = LocalWindowLayout.current
    val haptics = LocalHaptics.current
    val maxWidth = layout.contentMaxWidth
    val locked = !state.showFavorites && !entitlements.isUnlocked(state.category)
    val suffix = stringResource(Res.string.ideas_share_suffix)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenHeader(stringResource(Res.string.ideas_title), maxWidth, onSettings)
        Text(
            stringResource(Res.string.ideas_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.onSurfaceVariant,
            modifier = Modifier.widthIn(max = maxWidth).fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.widthIn(max = maxWidth).fillMaxWidth().testTag("ideas_categories"),
        ) {
            items(IdeaCategory.entries) { cat ->
                val selected = !state.showFavorites && state.category == cat
                val unlocked = entitlements.isUnlocked(cat)
                FilterChip(
                    selected = selected,
                    onClick = { haptics.tick(); onCategory(cat) },
                    label = { Text(stringResource(cat.labelRes)) },
                    leadingIcon = { Icon(if (unlocked) cat.icon() else Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.primaryContainer,
                        selectedLabelColor = palette.onPrimaryContainer,
                        selectedLeadingIconColor = palette.onPrimaryContainer,
                    ),
                )
            }
            item {
                FilterChip(
                    selected = state.showFavorites,
                    onClick = { haptics.tick(); onFavorites() },
                    label = { Text(stringResource(Res.string.ideas_favorites) + " (${state.favorites.size})") },
                    leadingIcon = { Icon(Icons.Rounded.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = palette.primaryContainer,
                        selectedLabelColor = palette.onPrimaryContainer,
                        selectedLeadingIconColor = palette.onPrimaryContainer,
                    ),
                )
            }
        }
        Spacer(Modifier.height(18.dp))

        val cardWidth = if (layout.sideBySide) 640.dp else 520.dp
        Box(Modifier.widthIn(max = cardWidth).fillMaxWidth()) {
            when {
                locked -> LockedCard(state.category) { onLocked(state.category) }
                state.current == null -> EmptyCard()
                else -> IdeaCardPager(state, onNext, onPrevious)
            }
        }
        Spacer(Modifier.height(18.dp))
        val idea = state.current
        if (!locked && idea != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                FavoriteButton(favorite = idea.id in state.favorites, enabled = true, onClick = { haptics.toggle(); onToggleFavorite(idea) })
                SoftIconButton(Icons.Rounded.Share, stringResource(Res.string.action_share), { haptics.tick(); onShare("${idea.text} $suffix") }, tint = palette.primary, size = 56.dp)
                GradientButton(
                    text = stringResource(Res.string.ideas_next),
                    leadingIcon = Icons.Rounded.ArrowForward,
                    onClick = { haptics.tick(); onNext() },
                    modifier = Modifier.widthIn(min = 180.dp),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Ideen-Karte, die beim Weiterblättern wie eine Karte vom Stapel gleitet; Wischen funktioniert auch. */
@Composable
private fun IdeaCardPager(state: IdeasUiState, onNext: () -> Unit, onPrevious: () -> Unit) {
    val motion = LocalMotion.current
    var drag = 0f
    AnimatedContent(
        targetState = state.current!!,
        transitionSpec = {
            val dir = state.direction
            if (motion.reduced) fadeIn(tween(120)) togetherWith fadeOut(tween(120))
            else (slideInHorizontally(tween(380)) { dir * it / 2 } + fadeIn(tween(300)) + scaleIn(tween(380), initialScale = 0.9f)) togetherWith
                (slideOutHorizontally(tween(300)) { -dir * it / 2 } + fadeOut(tween(240)) + scaleOut(tween(300), targetScale = 0.9f))
        },
        label = "ideaCard",
        modifier = Modifier.fillMaxWidth().pointerInput(state.category, state.showFavorites) {
            detectHorizontalDragGestures(
                onDragStart = { drag = 0f },
                onDragEnd = {
                    if (drag < -80f) onNext() else if (drag > 80f) onPrevious()
                },
                onHorizontalDrag = { _, amount -> drag += amount },
            )
        },
    ) { idea ->
        IdeaCard(idea, state.position + 1, state.ideas.size)
    }
}

@Composable
private fun IdeaCard(idea: Idea, number: Int, total: Int) {
    val palette = LocalPalette.current
    Box(
        Modifier.fillMaxWidth().heightIn(min = 300.dp).clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(palette.surfaceHigh, palette.surface)))
            .padding(28.dp),
    ) {
        // Dezentes Herz im Hintergrund
        androidx.compose.foundation.Canvas(Modifier.size(180.dp).align(Alignment.BottomEnd).padding(start = 40.dp, top = 40.dp)) {
            drawHeart(Brush.linearGradient(listOf(palette.heartStart, palette.heartEnd)), alpha = 0.12f)
        }
        Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(Brush.linearGradient(listOf(palette.heartStart, palette.heartEnd))),
                    contentAlignment = Alignment.Center,
                ) { Icon(idea.category.icon(), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                Spacer(Modifier.width(12.dp))
                Text(stringResource(idea.category.labelRes), style = MaterialTheme.typography.labelLarge, color = palette.primary, modifier = Modifier.weight(1f))
                Text(stringResource(Res.string.ideas_counter, number, total), style = MaterialTheme.typography.labelMedium, color = palette.onSurfaceVariant)
            }
            Spacer(Modifier.height(28.dp))
            Text(
                idea.text,
                style = MaterialTheme.typography.headlineSmall,
                color = palette.onBackground,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

@Composable
private fun LockedCard(category: IdeaCategory, onUnlock: () -> Unit) {
    val palette = LocalPalette.current
    val name = stringResource(category.labelRes)
    Column(
        Modifier.fillMaxWidth().heightIn(min = 300.dp).clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(palette.surfaceHigh, palette.surface)))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(Brush.linearGradient(listOf(palette.heartStart, palette.heartEnd))),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp)) }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(Res.string.ideas_locked_title, name), style = MaterialTheme.typography.titleLarge, color = palette.onBackground, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(Res.string.ideas_locked_text), style = MaterialTheme.typography.bodyMedium, color = palette.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        GradientButton(stringResource(Res.string.ideas_unlock), onUnlock, Modifier.widthIn(min = 200.dp))
    }
}

@Composable
private fun EmptyCard() {
    val palette = LocalPalette.current
    Column(
        Modifier.fillMaxWidth().aspectRatio(1.4f).clip(MaterialTheme.shapes.extraLarge).background(palette.surface.copy(alpha = 0.7f)).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Rounded.Favorite, contentDescription = null, tint = palette.heartMid, modifier = Modifier.size(40.dp))
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.ideas_empty_favorites), style = MaterialTheme.typography.bodyLarge, color = palette.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
