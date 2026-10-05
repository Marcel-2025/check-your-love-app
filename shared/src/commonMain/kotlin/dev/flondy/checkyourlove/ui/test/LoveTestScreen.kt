package dev.flondy.checkyourlove.ui.test

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.*
import dev.flondy.checkyourlove.core.LoveCalculator
import dev.flondy.checkyourlove.core.NameError
import dev.flondy.checkyourlove.core.ResultTexts
import dev.flondy.checkyourlove.monetization.ResultEffect
import dev.flondy.checkyourlove.ui.components.GradientButton
import dev.flondy.checkyourlove.ui.components.HeartButton
import dev.flondy.checkyourlove.ui.components.NameField
import dev.flondy.checkyourlove.ui.components.ParticleBurst
import dev.flondy.checkyourlove.ui.components.SectionCard
import dev.flondy.checkyourlove.ui.components.SoftIconButton
import dev.flondy.checkyourlove.ui.theme.LocalHaptics
import dev.flondy.checkyourlove.ui.theme.LocalMotion
import dev.flondy.checkyourlove.ui.theme.LocalPalette

@Composable
fun LoveTestScreen(
    state: LoveTestUiState,
    effect: ResultEffect,
    showPremiumEntry: Boolean,
    contentPadding: PaddingValues,
    onNameA: (String) -> Unit,
    onNameB: (String) -> Unit,
    onSwap: () -> Unit,
    onReset: () -> Unit,
    onEvaluate: () -> Unit,
    onNewTest: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: (ResultUi) -> Unit,
    onOpenPremium: () -> Unit,
) {
    val palette = LocalPalette.current
    val motion = LocalMotion.current
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopRow(showPremiumEntry = showPremiumEntry, onOpenPremium = onOpenPremium)
            AnimatedContent(
                targetState = state.phase == TestPhase.RESULT,
                transitionSpec = {
                    if (motion.reduced) fadeIn(tween(120)) togetherWith fadeOut(tween(120))
                    else (fadeIn(tween(380, delayMillis = 90)) + scaleIn(tween(420), initialScale = 0.94f)) togetherWith fadeOut(tween(200))
                },
                label = "phase",
                modifier = Modifier.widthIn(max = 560.dp),
            ) { showResult ->
                if (showResult && state.result != null) {
                    ResultContent(
                        result = state.result,
                        onNewTest = onNewTest,
                        onToggleFavorite = onToggleFavorite,
                        onShare = { onShare(state.result) },
                    )
                } else {
                    InputContent(
                        state = state,
                        onNameA = onNameA,
                        onNameB = onNameB,
                        onSwap = onSwap,
                        onReset = onReset,
                        onEvaluate = onEvaluate,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(Res.string.entertainment_note),
                style = MaterialTheme.typography.bodySmall,
                color = palette.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 420.dp).padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(24.dp))
        }
        val result = state.result
        if (state.phase == TestPhase.RESULT && result != null && result.range.isHigh && !motion.reduced) {
            ParticleBurst(
                effect = effect,
                colors = listOf(palette.heartStart, palette.heartMid, palette.heartEnd, palette.secondary, palette.tertiary),
                key = result.percent to result.nameA + result.nameB,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun TopRow(showPremiumEntry: Boolean, onOpenPremium: () -> Unit) {
    val palette = LocalPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = palette.onBackground,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        if (showPremiumEntry) {
            SoftIconButton(
                icon = Icons.Rounded.WorkspacePremium,
                contentDescription = stringResource(Res.string.premium_open),
                onClick = onOpenPremium,
                tint = palette.primary,
            )
        }
    }
}

@Composable
private fun InputContent(
    state: LoveTestUiState,
    onNameA: (String) -> Unit,
    onNameB: (String) -> Unit,
    onSwap: () -> Unit,
    onReset: () -> Unit,
    onEvaluate: () -> Unit,
) {
    val palette = LocalPalette.current
    val haptics = LocalHaptics.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val secondFocus = remember { FocusRequester() }
    val evaluating = state.phase == TestPhase.EVALUATING

    val submit = {
        keyboard?.hide()
        focusManager.clearFocus()
        haptics.tick()
        onEvaluate()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(Res.string.test_headline),
            style = MaterialTheme.typography.headlineMedium,
            color = palette.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(Res.string.test_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = palette.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        SectionCard(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp)) {
            NameField(
                value = state.nameA,
                onValueChange = onNameA,
                label = stringResource(Res.string.field_name_a),
                placeholder = stringResource(Res.string.field_name_a_placeholder),
                leadingIcon = Icons.Rounded.Person,
                errorText = state.errorA?.let { errorText(it) },
                counterText = counter(state.nameA),
                enabled = !evaluating,
                imeAction = ImeAction.Next,
                onImeAction = { secondFocus.requestFocus() },
            )
            Row(
                Modifier.fillMaxWidth().padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SoftIconButton(
                    icon = Icons.Rounded.SwapVert,
                    contentDescription = stringResource(Res.string.action_swap),
                    onClick = { haptics.tick(); onSwap() },
                    enabled = state.canSwap,
                    tint = palette.primary,
                )
                Spacer(Modifier.size(12.dp))
                SoftIconButton(
                    icon = Icons.Rounded.Refresh,
                    contentDescription = stringResource(Res.string.action_reset),
                    onClick = { haptics.tick(); onReset() },
                    enabled = state.canReset,
                    tint = palette.onSurfaceVariant,
                )
            }
            NameField(
                value = state.nameB,
                onValueChange = onNameB,
                label = stringResource(Res.string.field_name_b),
                placeholder = stringResource(Res.string.field_name_b_placeholder),
                leadingIcon = Icons.Rounded.FavoriteBorder,
                errorText = state.errorB?.let { errorText(it) },
                counterText = counter(state.nameB),
                enabled = !evaluating,
                imeAction = ImeAction.Done,
                onImeAction = submit,
                modifier = Modifier.focusRequester(secondFocus),
            )
        }
        Spacer(Modifier.height(20.dp))
        val checkLabel = stringResource(Res.string.action_check)
        HeartButton(
            size = 196.dp,
            pulsing = evaluating,
            enabled = !evaluating,
            contentDescription = if (evaluating) stringResource(Res.string.evaluating_a11y) else checkLabel,
            onClick = submit,
        ) {
            AnimatedVisibility(visible = !evaluating, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    checkLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 18.dp, start = 36.dp, end = 36.dp),
                )
            }
        }
        Text(
            if (evaluating) stringResource(Res.string.evaluating) else stringResource(Res.string.action_check_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.onSurfaceVariant,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

@Composable
private fun ResultContent(
    result: ResultUi,
    onNewTest: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
) {
    val palette = LocalPalette.current
    val motion = LocalMotion.current
    val haptics = LocalHaptics.current
    val counter = remember(result) { Animatable(if (motion.counterMillis == 0) result.percent.toFloat() else 0f) }
    val revealText = remember(result) { Animatable(if (motion.reduced) 1f else 0f) }
    LaunchedEffect(result) {
        if (motion.counterMillis > 0) {
            counter.animateTo(result.percent.toFloat(), tween(motion.counterMillis, easing = FastOutSlowInEasing))
        }
        revealText.animateTo(1f, tween(if (motion.reduced) 0 else 450))
    }
    val title = stringResource(ResultTexts.title(result.range))
    val text = stringResource(ResultTexts.text(result.range, result.variant))
    val a11y = stringResource(Res.string.result_a11y, result.nameA, result.nameB, result.percent, title)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics {
                    contentDescription = a11y
                    liveRegion = LiveRegionMode.Polite
                },
        ) {
            Text(
                stringResource(Res.string.result_pair, result.nameA, result.nameB),
                style = MaterialTheme.typography.titleLarge,
                color = palette.onBackground,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(18.dp))
            PercentRing(value = counter.value, target = result.percent)
            Spacer(Modifier.height(18.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsAlpha(revealText.value)
                    .padding(horizontal = 8.dp),
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium.copy(lineBreak = LineBreak.Heading, hyphens = Hyphens.Auto),
                    color = palette.primary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = palette.onBackground,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.graphicsAlpha(revealText.value),
        ) {
            FavoriteButton(
                favorite = result.favorite,
                enabled = result.id != null,
                onClick = { haptics.toggle(); onToggleFavorite() },
            )
            Spacer(Modifier.size(16.dp))
            SoftIconButton(
                icon = Icons.Rounded.Share,
                contentDescription = stringResource(Res.string.action_share),
                onClick = { haptics.tick(); onShare() },
                tint = palette.primary,
                size = 56.dp,
            )
        }
        Spacer(Modifier.height(20.dp))
        GradientButton(
            text = stringResource(Res.string.action_new_test),
            onClick = { haptics.tick(); onNewTest() },
            leadingIcon = Icons.Rounded.Replay,
            modifier = Modifier.fillMaxWidth(0.8f).heightIn(min = 56.dp),
        )
    }
}

/** Großer Ring mit animiertem Prozentwert. */
@Composable
fun PercentRing(value: Float, target: Int, modifier: Modifier = Modifier, ringSize: androidx.compose.ui.unit.Dp = 220.dp) {
    val palette = LocalPalette.current
    Box(modifier.size(ringSize), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.07f
            val inset = stroke / 2f + 4f
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(palette.glow.copy(alpha = 0.28f), Color.Transparent),
                    center = center, radius = size.minDimension / 2f,
                ),
            )
            drawArc(
                color = palette.outline.copy(alpha = 0.25f),
                startAngle = 0f, sweepAngle = 360f, useCenter = false,
                topLeft = Offset(inset, inset), size = arcSize,
                style = Stroke(width = stroke),
            )
            // Verlauf beginnt oben (12 Uhr) und läuft im Uhrzeigersinn mit dem Bogen
            rotate(-90f) {
                drawArc(
                    brush = run {
                        val f = (value / 100f).coerceIn(0.02f, 0.97f)
                        Brush.sweepGradient(
                            0f to palette.heartStart, f * 0.5f to palette.heartMid, f to palette.heartEnd,
                            (f + (1f - f) * 0.35f) to palette.heartEnd, 1f to palette.heartStart, center = center,
                        )
                    },
                    startAngle = 0f, sweepAngle = 360f * (value / 100f), useCenter = false,
                    topLeft = Offset(inset, inset), size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value.toInt().coerceIn(0, target.coerceAtLeast(0)).toString(),
                style = MaterialTheme.typography.displayLarge,
                color = palette.onBackground,
            )
            Text(
                "%",
                style = MaterialTheme.typography.headlineLarge,
                color = palette.primary,
                modifier = Modifier.padding(bottom = 14.dp, start = 2.dp),
            )
        }
    }
}

/** Favoriten-Herz mit kurzer Feder-Animation beim Umschalten. */
@Composable
fun FavoriteButton(favorite: Boolean, enabled: Boolean, onClick: () -> Unit, size: androidx.compose.ui.unit.Dp = 56.dp) {
    val palette = LocalPalette.current
    val bounce = remember { Animatable(1f) }
    LaunchedEffect(favorite) {
        if (favorite) {
            bounce.snapTo(0.7f)
            bounce.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
        }
    }
    val tint by animateColorAsState(if (favorite) palette.heartMid else palette.onSurfaceVariant, tween(250), label = "favTint")
    Box(Modifier.scale(bounce.value)) {
        SoftIconButton(
            icon = if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = stringResource(if (favorite) Res.string.action_favorite_remove else Res.string.action_favorite_add),
            onClick = onClick,
            enabled = enabled,
            tint = tint,
            size = size,
        )
    }
}

@Composable
private fun errorText(error: NameError): String = when (error) {
    NameError.EMPTY -> stringResource(Res.string.error_name_empty)
    NameError.NO_LETTER -> stringResource(Res.string.error_name_no_letter)
    NameError.TOO_LONG -> stringResource(Res.string.error_name_too_long, LoveCalculator.MAX_NAME_LENGTH)
}

@Composable
private fun counter(value: String): String =
    stringResource(Res.string.field_counter, LoveCalculator.codePointLength(value), LoveCalculator.MAX_NAME_LENGTH)

private fun Modifier.graphicsAlpha(alpha: Float): Modifier = this.alpha(alpha)
