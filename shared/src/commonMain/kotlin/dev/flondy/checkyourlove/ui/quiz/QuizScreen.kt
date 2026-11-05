package dev.flondy.checkyourlove.ui.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.flondy.checkyourlove.content.QuizPack
import dev.flondy.checkyourlove.core.LoveCalculator
import dev.flondy.checkyourlove.core.NameError
import dev.flondy.checkyourlove.monetization.Entitlements
import dev.flondy.checkyourlove.monetization.ResultEffect
import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.*
import dev.flondy.checkyourlove.ui.components.GradientButton
import dev.flondy.checkyourlove.ui.components.GradientProgress
import dev.flondy.checkyourlove.ui.components.HeartButton
import dev.flondy.checkyourlove.ui.components.LockBadge
import dev.flondy.checkyourlove.ui.components.NameField
import dev.flondy.checkyourlove.ui.components.ParticleBurst
import dev.flondy.checkyourlove.ui.components.ScreenHeader
import dev.flondy.checkyourlove.ui.components.SectionCard
import dev.flondy.checkyourlove.ui.components.SoftIconButton
import dev.flondy.checkyourlove.ui.components.staggerIn
import dev.flondy.checkyourlove.ui.test.PercentRing
import dev.flondy.checkyourlove.ui.theme.LocalHaptics
import dev.flondy.checkyourlove.ui.theme.LocalMotion
import dev.flondy.checkyourlove.ui.theme.LocalPalette
import dev.flondy.checkyourlove.ui.theme.LocalWindowLayout
import org.jetbrains.compose.resources.stringResource

@Composable
fun QuizScreen(
    state: QuizUiState,
    entitlements: Entitlements,
    effect: ResultEffect,
    onSettings: (() -> Unit)?,
    onNameA: (String) -> Unit,
    onNameB: (String) -> Unit,
    onPack: (QuizPack) -> Unit,
    onLocked: (QuizPack) -> Unit,
    onStart: () -> Unit,
    onAnswer: (Int) -> Unit,
    onReady: () -> Unit,
    onGuess: (Int) -> Unit,
    onNext: () -> Unit,
    onPlayAgain: () -> Unit,
    onChangeNames: () -> Unit,
    onShare: (String) -> Unit,
) {
    val layout = LocalWindowLayout.current
    val motion = LocalMotion.current
    val palette = LocalPalette.current
    val maxWidth = if (layout.sideBySide) 900.dp else layout.contentMaxWidth
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.phase == QuizPhase.SETUP) {
                ScreenHeader(stringResource(Res.string.quiz_title), maxWidth, onSettings)
            } else if (state.phase != QuizPhase.RESULT) {
                QuizTopBar(state, maxWidth, onQuit = onChangeNames)
            } else {
                ScreenHeader(stringResource(Res.string.quiz_title), maxWidth, onSettings)
            }
            AnimatedContent(
                targetState = state.phase to state.index,
                transitionSpec = {
                    if (motion.reduced) fadeIn(tween(120)) togetherWith fadeOut(tween(120))
                    else (slideInHorizontally(tween(360)) { it / 5 } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(260)) { -it / 6 } + fadeOut(tween(200)))
                },
                label = "quizStep",
                modifier = Modifier.widthIn(max = maxWidth).fillMaxWidth(),
            ) { (phase, _) ->
                when (phase) {
                    QuizPhase.SETUP -> QuizSetup(state, entitlements, onNameA, onNameB, onPack, onLocked, onStart)
                    QuizPhase.ANSWER -> QuestionStep(
                        prompt = stringResource(Res.string.quiz_answer_prompt, state.answererName),
                        state = state, selected = null, onSelect = onAnswer,
                    )
                    QuizPhase.HANDOVER -> Handover(state, onReady)
                    QuizPhase.GUESS -> QuestionStep(
                        prompt = stringResource(Res.string.quiz_guess_prompt, state.answererName),
                        state = state, selected = null, onSelect = onGuess,
                    )
                    QuizPhase.REVEAL -> Reveal(state, onNext)
                    QuizPhase.RESULT -> QuizResult(state, onPlayAgain, onChangeNames, onShare)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        if (state.phase == QuizPhase.RESULT && state.score >= 80 && !motion.reduced) {
            ParticleBurst(
                effect = effect,
                colors = listOf(palette.heartStart, palette.heartMid, palette.heartEnd, palette.secondary, palette.tertiary),
                key = state.score to state.questions.firstOrNull()?.id,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun QuizTopBar(state: QuizUiState, maxWidth: androidx.compose.ui.unit.Dp, onQuit: () -> Unit) {
    val palette = LocalPalette.current
    Column(Modifier.widthIn(max = maxWidth).fillMaxWidth().padding(top = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.quiz_question_counter, state.index + 1, state.total),
                style = MaterialTheme.typography.titleMedium,
                color = palette.onBackground,
                modifier = Modifier.weight(1f),
            )
            SoftIconButton(Icons.Rounded.Close, stringResource(Res.string.quiz_quit), onQuit, tint = palette.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))
        val stepsDone = state.index + if (state.phase == QuizPhase.REVEAL) 1 else 0
        GradientProgress(stepsDone / state.total.coerceAtLeast(1).toFloat())
    }
}

@Composable
private fun QuizSetup(
    state: QuizUiState,
    entitlements: Entitlements,
    onNameA: (String) -> Unit,
    onNameB: (String) -> Unit,
    onPack: (QuizPack) -> Unit,
    onLocked: (QuizPack) -> Unit,
    onStart: () -> Unit,
) {
    val palette = LocalPalette.current
    val haptics = LocalHaptics.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            HeartButton(size = 72.dp, pulsing = true, enabled = false, contentDescription = "", onClick = {}) {}
            HeartButton(size = 72.dp, pulsing = true, enabled = false, contentDescription = "", onClick = {}) {}
        }
        Spacer(Modifier.height(10.dp))
        Text(stringResource(Res.string.quiz_subtitle), style = MaterialTheme.typography.bodyLarge, color = palette.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        SectionCard(Modifier.widthIn(max = 560.dp), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
            NameField(
                value = state.nameA, onValueChange = onNameA, label = stringResource(Res.string.quiz_name_a), placeholder = "",
                leadingIcon = Icons.Rounded.FavoriteBorder, errorText = state.errorA?.let { nameError(it) }, counterText = "",
                enabled = true, imeAction = ImeAction.Next, onImeAction = {},
            )
            NameField(
                value = state.nameB, onValueChange = onNameB, label = stringResource(Res.string.quiz_name_b), placeholder = "",
                leadingIcon = Icons.Rounded.Favorite,
                errorText = state.errorB?.let { nameError(it) } ?: if (state.sameNames) stringResource(Res.string.quiz_same_names) else null,
                counterText = "", enabled = true, imeAction = ImeAction.Done, onImeAction = {},
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(stringResource(Res.string.quiz_pack_title), style = MaterialTheme.typography.titleSmall, color = palette.onSurfaceVariant, modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        Column(Modifier.widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            QuizPack.entries.forEachIndexed { i, pack ->
                PackRow(
                    pack = pack,
                    selected = state.pack == pack,
                    unlocked = entitlements.isUnlocked(pack),
                    modifier = Modifier.staggerIn(i),
                    onClick = { haptics.tick(); if (entitlements.isUnlocked(pack)) onPack(pack) else onLocked(pack) },
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        GradientButton(
            text = stringResource(Res.string.quiz_start),
            leadingIcon = Icons.Rounded.PlayArrow,
            onClick = { haptics.tick(); onStart() },
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(0.85f),
        )
    }
}

@Composable
private fun PackRow(pack: QuizPack, selected: Boolean, unlocked: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val palette = LocalPalette.current
    val border by animateColorAsState(if (selected) palette.primary else palette.outline.copy(alpha = 0.3f), label = "packBorder")
    Row(
        modifier.fillMaxWidth().clip(MaterialTheme.shapes.large)
            .background(palette.surface.copy(alpha = if (palette.isDark) 0.86f else 0.94f))
            .border(BorderStroke(if (selected) 2.dp else 1.dp, border), MaterialTheme.shapes.large)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(pack.labelRes), style = MaterialTheme.typography.titleMedium, color = palette.onBackground)
            Text(stringResource(pack.descriptionRes), style = MaterialTheme.typography.bodySmall, color = palette.onSurfaceVariant)
        }
        if (!unlocked) LockBadge()
        else if (selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = palette.primary)
    }
}

@Composable
private fun QuestionStep(prompt: String, state: QuizUiState, selected: Int?, onSelect: (Int) -> Unit) {
    val palette = LocalPalette.current
    val haptics = LocalHaptics.current
    val question = state.question ?: return
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(20.dp))
        Text(prompt, style = MaterialTheme.typography.titleMedium, color = palette.primary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(
            question.text,
            style = MaterialTheme.typography.headlineMedium,
            color = palette.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(22.dp))
        OptionsGrid(question.options, state.phase.toString() + state.index) { i, text, mod ->
            OptionButton(text, highlight = if (selected == i) OptionHighlight.SELECTED else OptionHighlight.NONE, modifier = mod.testTag("quiz_option_$i")) {
                haptics.tick(); onSelect(i)
            }
        }
    }
}

/** Antworten: im Querformat 2 × 2, sonst untereinander. */
@Composable
private fun OptionsGrid(options: List<String>, key: String, item: @Composable (Int, String, Modifier) -> Unit) {
    val wide = LocalWindowLayout.current.sideBySide
    if (wide) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.withIndex().chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (i, t) -> item(i, t, Modifier.weight(1f).staggerIn(i, key)) }
                }
            }
        }
    } else {
        Column(Modifier.widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEachIndexed { i, t -> item(i, t, Modifier.fillMaxWidth().staggerIn(i, key)) }
        }
    }
}

private enum class OptionHighlight { NONE, SELECTED, CORRECT, WRONG }

@Composable
private fun OptionButton(text: String, highlight: OptionHighlight, modifier: Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val palette = LocalPalette.current
    val good = Color(0xFF3DBE7A)
    val bad = MaterialTheme.colorScheme.error
    val border by animateColorAsState(
        when (highlight) {
            OptionHighlight.NONE -> palette.outline.copy(alpha = 0.35f)
            OptionHighlight.SELECTED -> palette.primary
            OptionHighlight.CORRECT -> good
            OptionHighlight.WRONG -> bad
        }, tween(250), label = "optBorder",
    )
    Row(
        modifier.heightIn(min = 56.dp).clip(MaterialTheme.shapes.large)
            .background(palette.surface.copy(alpha = if (palette.isDark) 0.86f else 0.96f))
            .border(BorderStroke(if (highlight == OptionHighlight.NONE) 1.dp else 2.dp, border), MaterialTheme.shapes.large)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = palette.onBackground, modifier = Modifier.weight(1f))
        when (highlight) {
            OptionHighlight.CORRECT -> Icon(Icons.Rounded.Check, contentDescription = null, tint = good)
            OptionHighlight.WRONG -> Icon(Icons.Rounded.Close, contentDescription = null, tint = bad)
            else -> Unit
        }
    }
}

@Composable
private fun Handover(state: QuizUiState, onReady: () -> Unit) {
    val palette = LocalPalette.current
    val haptics = LocalHaptics.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(36.dp))
        HeartButton(size = 150.dp, pulsing = true, enabled = false, contentDescription = "", onClick = {}) {
            Text("→", style = MaterialTheme.typography.headlineLarge, color = Color.White, modifier = Modifier.padding(bottom = 10.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(Res.string.quiz_handover_title, state.guesserName),
            style = MaterialTheme.typography.headlineMedium,
            color = palette.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(Res.string.quiz_handover_text, state.guesserName, state.answererName),
            style = MaterialTheme.typography.bodyLarge,
            color = palette.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(28.dp))
        GradientButton(stringResource(Res.string.quiz_ready), { haptics.confirm(); onReady() }, Modifier.widthIn(min = 220.dp))
    }
}

@Composable
private fun Reveal(state: QuizUiState, onNext: () -> Unit) {
    val palette = LocalPalette.current
    val haptics = LocalHaptics.current
    val motion = LocalMotion.current
    val question = state.question ?: return
    val hit = state.guess == state.answer
    val pop = remember(state.index) { Animatable(if (motion.reduced) 1f else 0.3f) }
    LaunchedEffect(state.index) {
        if (hit) haptics.confirm() else haptics.tick()
        if (!motion.reduced) pop.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 300f))
    }
    val good = Color(0xFF3DBE7A)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier.size(76.dp).scale(pop.value).clip(CircleShape)
                .background(Brush.linearGradient(if (hit) listOf(good, good.copy(alpha = 0.7f)) else listOf(palette.heartStart, palette.heartEnd))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(if (hit) Icons.Rounded.Check else Icons.Rounded.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(if (hit) Res.string.quiz_correct else Res.string.quiz_wrong),
            style = MaterialTheme.typography.headlineMedium,
            color = if (hit) good else palette.primary,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(Res.string.quiz_their_answer, state.answererName, question.options[state.answer ?: 0]),
            style = MaterialTheme.typography.bodyLarge,
            color = palette.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        Text(question.text, style = MaterialTheme.typography.titleMedium, color = palette.onBackground, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        OptionsGrid(question.options, "reveal" + state.index) { i, text, mod ->
            val h = when {
                i == state.answer -> OptionHighlight.CORRECT
                i == state.guess -> OptionHighlight.WRONG
                else -> OptionHighlight.NONE
            }
            OptionButton(text, h, mod, enabled = false) {}
        }
        Spacer(Modifier.height(22.dp))
        GradientButton(
            stringResource(if (state.isLast) Res.string.quiz_show_result else Res.string.quiz_next),
            { haptics.tick(); onNext() },
            Modifier.widthIn(min = 220.dp),
        )
    }
}

@Composable
private fun QuizResult(state: QuizUiState, onPlayAgain: () -> Unit, onChangeNames: () -> Unit, onShare: (String) -> Unit) {
    val palette = LocalPalette.current
    val haptics = LocalHaptics.current
    val motion = LocalMotion.current
    val layout = LocalWindowLayout.current
    val counter = remember(state.questions) { Animatable(if (motion.reduced) state.score.toFloat() else 0f) }
    LaunchedEffect(state.questions) {
        if (!motion.reduced) counter.animateTo(state.score.toFloat(), tween(1300, easing = FastOutSlowInEasing))
    }
    val level = when {
        state.score >= 90 -> 3
        state.score >= 70 -> 2
        state.score >= 40 -> 1
        else -> 0
    }
    val title = stringResource(listOf(Res.string.quiz_result_title_0, Res.string.quiz_result_title_1, Res.string.quiz_result_title_2, Res.string.quiz_result_title_3)[level])
    val text = stringResource(listOf(Res.string.quiz_result_text_0, Res.string.quiz_result_text_1, Res.string.quiz_result_text_2, Res.string.quiz_result_text_3)[level])
    val a = LoveCalculator.displayName(state.nameA)
    val b = LoveCalculator.displayName(state.nameB)
    val shareText = stringResource(Res.string.quiz_share_text, a, b, state.score)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(12.dp))
        Text("$a & $b", style = MaterialTheme.typography.titleLarge, color = palette.onBackground, textAlign = TextAlign.Center)
        Text(stringResource(Res.string.quiz_result_label), style = MaterialTheme.typography.bodyMedium, color = palette.onSurfaceVariant)
        Spacer(Modifier.height(14.dp))
        PercentRing(value = counter.value, target = state.score, ringSize = layout.ringSize)
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, color = palette.primary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = palette.onBackground, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(Res.string.quiz_result_score, state.correct, state.total), style = MaterialTheme.typography.bodyMedium, color = palette.onSurfaceVariant)
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SoftIconButton(Icons.Rounded.Share, stringResource(Res.string.quiz_share), { haptics.tick(); onShare(shareText) }, tint = palette.primary, size = 56.dp)
            GradientButton(stringResource(Res.string.quiz_play_again), { haptics.tick(); onPlayAgain() }, Modifier.widthIn(min = 200.dp), leadingIcon = Icons.Rounded.Replay)
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onChangeNames) {
            Icon(Icons.Rounded.Edit, contentDescription = null, tint = palette.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(stringResource(Res.string.quiz_change), color = palette.primary)
        }
    }
}

@Composable
private fun nameError(error: NameError): String = when (error) {
    NameError.EMPTY -> stringResource(Res.string.error_name_empty)
    NameError.NO_LETTER -> stringResource(Res.string.error_name_no_letter)
    NameError.TOO_LONG -> stringResource(Res.string.error_name_too_long, LoveCalculator.MAX_NAME_LENGTH)
}
