package dev.flondy.checkyourlove.ui.group

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.flondy.checkyourlove.core.GroupCalculator
import dev.flondy.checkyourlove.core.GroupPair
import dev.flondy.checkyourlove.core.LoveCalculator
import dev.flondy.checkyourlove.core.NameError
import dev.flondy.checkyourlove.core.ResultTexts
import dev.flondy.checkyourlove.core.TestMode
import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.*
import dev.flondy.checkyourlove.ui.components.GradientButton
import dev.flondy.checkyourlove.ui.components.GradientProgress
import dev.flondy.checkyourlove.ui.components.ModeToggle
import dev.flondy.checkyourlove.ui.components.ScreenHeader
import dev.flondy.checkyourlove.ui.components.SectionCard
import dev.flondy.checkyourlove.ui.components.SoftIconButton
import dev.flondy.checkyourlove.ui.components.drawHeart
import dev.flondy.checkyourlove.ui.components.staggerIn
import dev.flondy.checkyourlove.ui.test.ResultUi
import dev.flondy.checkyourlove.ui.theme.LocalHaptics
import dev.flondy.checkyourlove.ui.theme.LocalMotion
import dev.flondy.checkyourlove.ui.theme.LocalPalette
import dev.flondy.checkyourlove.ui.theme.LocalWindowLayout
import org.jetbrains.compose.resources.stringResource

@Composable
fun GroupScreen(
    state: GroupUiState,
    onSettings: (() -> Unit)?,
    onName: (Int, String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onMode: (TestMode) -> Unit,
    onEvaluate: () -> Unit,
    onEdit: () -> Unit,
    onOpenPair: (ResultUi) -> Unit,
    onShare: (String) -> Unit,
) {
    val layout = LocalWindowLayout.current
    val motion = LocalMotion.current
    val maxWidth = if (layout.sideBySide) 980.dp else layout.contentMaxWidth
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenHeader(stringResource(Res.string.group_title), maxWidth, onSettings)
        AnimatedContent(
            targetState = state.results != null,
            transitionSpec = {
                if (motion.reduced) fadeIn(tween(120)) togetherWith fadeOut(tween(120))
                else (fadeIn(tween(360, 80)) + scaleIn(tween(400), initialScale = 0.95f)) togetherWith fadeOut(tween(180))
            },
            label = "group",
            modifier = Modifier.widthIn(max = maxWidth).fillMaxWidth(),
        ) { showResults ->
            val results = state.results
            if (showResults && results != null) {
                GroupResults(results, state.mode, layout.sideBySide, onEdit, onOpenPair, onShare)
            } else {
                GroupInput(state, onName, onAdd, onRemove, onMode, onEvaluate)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun GroupInput(
    state: GroupUiState,
    onName: (Int, String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onMode: (TestMode) -> Unit,
    onEvaluate: () -> Unit,
) {
    val palette = LocalPalette.current
    val haptics = LocalHaptics.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        GroupIllustration()
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(Res.string.group_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = palette.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        ModeToggle(state.mode, enabled = true, onMode = onMode)
        Spacer(Modifier.height(16.dp))
        SectionCard(
            modifier = Modifier.widthIn(max = 560.dp).animateContentSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        ) {
            state.names.forEachIndexed { i, name ->
                androidx.compose.runtime.key(state.keys.getOrElse(i) { i }) {
                    val error = state.errors[i]
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        dev.flondy.checkyourlove.ui.components.NameField(
                            value = name,
                            onValueChange = { onName(i, it) },
                            label = stringResource(Res.string.group_name_hint, i + 1),
                            placeholder = "",
                            leadingIcon = Icons.Rounded.Person,
                            errorText = when (error) {
                                is GroupFieldError.Invalid -> nameError(error.error)
                                GroupFieldError.Duplicate -> stringResource(Res.string.group_duplicate)
                                null -> null
                            },
                            counterText = "",
                            enabled = true,
                            imeAction = if (i == state.names.lastIndex) ImeAction.Done else ImeAction.Next,
                            onImeAction = {},
                            modifier = Modifier.weight(1f),
                        )
                        if (state.canRemove) {
                            SoftIconButton(
                                icon = Icons.Rounded.Close,
                                contentDescription = stringResource(Res.string.group_remove),
                                onClick = { haptics.tick(); onRemove(i) },
                                tint = palette.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, bottom = 18.dp),
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(state.canAdd, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                TextButton(onClick = { haptics.tick(); onAdd() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = palette.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.group_add), color = palette.primary)
                }
            }
        }
        if (state.tooFew) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(Res.string.group_min, GroupCalculator.MIN_NAMES), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(20.dp))
        GradientButton(
            text = stringResource(Res.string.group_start),
            leadingIcon = Icons.Rounded.Groups,
            onClick = { haptics.tick(); onEvaluate() },
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(0.85f),
        )
    }
}

/** Drei überlappende Herzen als kleine Illustration. */
@Composable
private fun GroupIllustration() {
    val palette = LocalPalette.current
    val motion = LocalMotion.current
    val pop = remember { Animatable(if (motion.reduced) 1f else 0.6f) }
    LaunchedEffect(Unit) { if (!motion.reduced) pop.animateTo(1f, androidx.compose.animation.core.spring(dampingRatio = 0.45f)) }
    Box(Modifier.size(width = 150.dp, height = 76.dp)) {
        androidx.compose.foundation.Canvas(Modifier.size(54.dp).align(Alignment.CenterStart).padding(start = 4.dp)) {
            drawHeart(Brush.linearGradient(listOf(palette.heartEnd, palette.heartMid)), alpha = 0.75f)
        }
        androidx.compose.foundation.Canvas(Modifier.size(54.dp).align(Alignment.CenterEnd).padding(end = 4.dp)) {
            drawHeart(Brush.linearGradient(listOf(palette.heartStart, palette.heartMid)), alpha = 0.75f)
        }
        androidx.compose.foundation.Canvas(
            Modifier.size(76.dp).align(Alignment.Center)
                .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
        ) {
            drawHeart(Brush.linearGradient(listOf(palette.heartStart, palette.heartMid, palette.heartEnd)))
        }
    }
}

@Composable
private fun GroupResults(
    results: List<GroupPair>,
    mode: TestMode,
    wide: Boolean,
    onEdit: () -> Unit,
    onOpenPair: (ResultUi) -> Unit,
    onShare: (String) -> Unit,
) {
    val palette = LocalPalette.current
    val haptics = LocalHaptics.current
    val top = results.firstOrNull() ?: return
    val header = stringResource(Res.string.group_share_header)
    val shareLines = results.mapIndexed { i, p -> "${i + 1}. ${p.nameA} & ${p.nameB} – ${p.calculation.percent} %" }
    val shareText = (listOf(header) + shareLines.take(10)).joinToString("\n")

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        TopPairCard(top, mode, onClick = { onOpenPair(top.toResultUi(mode)) })
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.group_results_title),
                style = MaterialTheme.typography.titleLarge,
                color = palette.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(stringResource(Res.string.group_pairs_count, results.size), style = MaterialTheme.typography.bodySmall, color = palette.onSurfaceVariant)
        }
        Text(stringResource(Res.string.group_tap_hint), style = MaterialTheme.typography.bodySmall, color = palette.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        val rows = results.drop(1).withIndex().toList()
        if (wide) {
            // Querformat: Rangliste zweispaltig, Platzierung bleibt zeilenweise lesbar
            rows.chunked(2).forEach { chunk ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    chunk.forEach { (i, p) -> RankRow(i + 2, p, mode, Modifier.weight(1f)) { onOpenPair(p.toResultUi(mode)) } }
                    if (chunk.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
            }
        } else {
            rows.forEach { (i, p) ->
                RankRow(i + 2, p, mode, Modifier.fillMaxWidth()) { onOpenPair(p.toResultUi(mode)) }
                Spacer(Modifier.height(10.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SoftIconButton(Icons.Rounded.Share, stringResource(Res.string.group_share), { haptics.tick(); onShare(shareText) }, tint = palette.primary, size = 56.dp)
            GradientButton(
                text = stringResource(Res.string.group_edit),
                leadingIcon = Icons.Rounded.Edit,
                onClick = { haptics.tick(); onEdit() },
                modifier = Modifier.widthIn(min = 220.dp),
            )
        }
    }
}

@Composable
private fun TopPairCard(pair: GroupPair, mode: TestMode, onClick: () -> Unit) {
    val palette = LocalPalette.current
    val motion = LocalMotion.current
    val counter = remember(pair) { Animatable(if (motion.reduced) pair.calculation.percent.toFloat() else 0f) }
    LaunchedEffect(pair) {
        if (!motion.reduced) counter.animateTo(pair.calculation.percent.toFloat(), tween(1100, easing = FastOutSlowInEasing))
    }
    val title = stringResource(ResultTexts.title(pair.calculation.range, mode))
    val a11y = stringResource(Res.string.group_rank_a11y, 1, pair.nameA, pair.nameB, pair.calculation.percent)
    SectionCard(
        modifier = Modifier.widthIn(max = 560.dp).clip(MaterialTheme.shapes.large)
            .clickable(role = Role.Button, onClick = onClick).clearAndSetSemantics { contentDescription = a11y },
        contentPadding = PaddingValues(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(Brush.linearGradient(listOf(palette.heartStart, palette.heartEnd))),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color.White) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.group_top).uppercase(), style = MaterialTheme.typography.labelMedium, color = palette.primary)
                Text(
                    "${pair.nameA} & ${pair.nameB}",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(title, style = MaterialTheme.typography.bodyMedium, color = palette.onSurfaceVariant)
            }
            Text(
                "${counter.value.toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                color = palette.primary,
            )
        }
        Spacer(Modifier.height(14.dp))
        GradientProgress(pair.calculation.percent / 100f, height = 10.dp)
    }
}

@Composable
private fun RankRow(rank: Int, pair: GroupPair, mode: TestMode, modifier: Modifier, onClick: () -> Unit) {
    val palette = LocalPalette.current
    val a11y = stringResource(Res.string.group_rank_a11y, rank, pair.nameA, pair.nameB, pair.calculation.percent)
    SectionCard(
        modifier = modifier.staggerIn(rank - 2, key = pair.calculation.pairKey)
            .clip(MaterialTheme.shapes.large)
            .clickable(role = Role.Button, onClick = onClick)
            .clearAndSetSemantics { contentDescription = a11y },
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$rank", style = MaterialTheme.typography.titleMedium, color = palette.onSurfaceVariant, modifier = Modifier.width(28.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${pair.nameA} & ${pair.nameB}",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                GradientProgress(pair.calculation.percent / 100f, height = 6.dp, delayMillis = 120 + (rank - 2) * 70)
            }
            Spacer(Modifier.width(12.dp))
            Text("${pair.calculation.percent}%", style = MaterialTheme.typography.titleMedium, color = palette.primary, modifier = Modifier.heightIn(min = 24.dp))
        }
    }
}

private fun GroupPair.toResultUi(mode: TestMode) = ResultUi(
    id = null, nameA = nameA, nameB = nameB, percent = calculation.percent,
    range = calculation.range, variant = calculation.variant, favorite = false, mode = mode,
)

@Composable
private fun nameError(error: NameError): String = when (error) {
    NameError.EMPTY -> stringResource(Res.string.error_name_empty)
    NameError.NO_LETTER -> stringResource(Res.string.error_name_no_letter)
    NameError.TOO_LONG -> stringResource(Res.string.error_name_too_long, LoveCalculator.MAX_NAME_LENGTH)
}

