package dev.flondy.checkyourlove.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.flondy.checkyourlove.core.TestMode
import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.*
import dev.flondy.checkyourlove.ui.theme.LocalMotion
import dev.flondy.checkyourlove.ui.theme.LocalPalette
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

/** Kopfzeile eines Haupt-Screens mit optionalem Zahnrad für die Einstellungen. */
@Composable
fun ScreenHeader(
    title: String,
    maxWidth: Dp,
    onSettings: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val palette = LocalPalette.current
    Row(
        modifier.widthIn(max = maxWidth).fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = palette.onBackground,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        actions()
        if (onSettings != null) {
            Spacer(Modifier.width(8.dp))
            SoftIconButton(
                icon = Icons.Rounded.Settings,
                contentDescription = stringResource(Res.string.settings_open),
                onClick = onSettings,
                tint = palette.onSurfaceVariant,
            )
        }
    }
}

/** Umschalter Liebe / Freundschaft. */
@Composable
fun ModeToggle(mode: TestMode, enabled: Boolean, onMode: (TestMode) -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalPalette.current
    SingleChoiceSegmentedButtonRow(modifier.widthIn(max = 420.dp).fillMaxWidth()) {
        TestMode.entries.forEachIndexed { i, m ->
            SegmentedButton(
                selected = mode == m,
                onClick = { onMode(m) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(i, TestMode.entries.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = palette.primaryContainer,
                    activeContentColor = palette.onPrimaryContainer,
                    inactiveContainerColor = Color.Transparent,
                ),
            ) {
                Text(stringResource(if (m == TestMode.LOVE) Res.string.mode_love else Res.string.mode_friendship))
            }
        }
    }
}

/** Fortschrittsbalken mit Verlauf, der weich auf [fraction] animiert. */
@Composable
fun GradientProgress(fraction: Float, modifier: Modifier = Modifier, height: Dp = 8.dp, delayMillis: Int = 0) {
    val palette = LocalPalette.current
    val motion = LocalMotion.current
    val animated by animateFloatAsState(
        fraction.coerceIn(0f, 1f),
        tween(if (motion.reduced) 0 else 700, delayMillis = if (motion.reduced) 0 else delayMillis, easing = FastOutSlowInEasing),
        label = "progress",
    )
    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(palette.outline.copy(alpha = 0.22f))) {
        Box(
            Modifier.fillMaxHeight().fillMaxWidth(animated).clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(palette.heartStart, palette.heartMid, palette.heartEnd)))
        )
    }
}

/** Element blendet sich zeitversetzt ein (für Listen und Antwortknöpfe). */
@Composable
fun Modifier.staggerIn(index: Int, key: Any? = Unit, stepMillis: Long = 70): Modifier {
    val reduced = LocalMotion.current.reduced
    val progress = remember(key) { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(key) {
        if (!reduced) {
            delay(index * stepMillis)
            progress.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
        }
    }
    return this.graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 28.dp.toPx()
    }
}
