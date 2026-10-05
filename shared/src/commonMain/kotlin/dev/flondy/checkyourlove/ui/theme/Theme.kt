package dev.flondy.checkyourlove.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import dev.flondy.checkyourlove.platform.systemReducedMotion
import androidx.compose.ui.unit.dp

/** Bewegungsvorgaben: reduziert = keine Partikel, keine Pulsation, kurze Wartezeit. */
@Immutable
data class MotionSpec(val reduced: Boolean) {
    val evaluationMillis: Long get() = if (reduced) 350L else 1_700L
    val counterMillis: Int get() = if (reduced) 0 else 1_300
}

/** Haptik, die die Nutzereinstellung respektiert (Android und iOS über Compose). */
class Haptics(private val enabled: Boolean, private val feedback: HapticFeedback?) {
    fun tick() { if (enabled) feedback?.performHapticFeedback(HapticFeedbackType.KeyboardTap) }
    fun confirm() { if (enabled) feedback?.performHapticFeedback(HapticFeedbackType.Confirm) }
    fun toggle() { if (enabled) feedback?.performHapticFeedback(HapticFeedbackType.ToggleOn) }
}

val LocalPalette = staticCompositionLocalOf { AppPalette.AUBERGINE.dark }
val LocalMotion = staticCompositionLocalOf { MotionSpec(reduced = false) }
val LocalHaptics = staticCompositionLocalOf { Haptics(false, null) }

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

fun PaletteColors.toColorScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    val containerLow = surface
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onPrimary,
        secondaryContainer = primaryContainer.copy(alpha = 0.7f).compositeOver(surface),
        onSecondaryContainer = onPrimaryContainer,
        tertiary = tertiary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onBackground,
        surfaceVariant = surfaceHigh,
        onSurfaceVariant = onSurfaceVariant,
        surfaceContainerLowest = background,
        surfaceContainerLow = containerLow,
        surfaceContainer = surfaceHigh.copy(alpha = 0.6f).compositeOver(surface),
        surfaceContainerHigh = surfaceHigh,
        surfaceContainerHighest = surfaceHigh.copy(alpha = 0.85f).compositeOver(outline),
        inverseSurface = onBackground,
        inverseOnSurface = background,
        outline = outline,
        outlineVariant = outline.copy(alpha = 0.45f).compositeOver(surface),
        error = error,
        scrim = Color.Black,
    )
}

fun PaletteColors.backgroundBrush(): Brush =
    Brush.verticalGradient(listOf(backgroundAlt, background, background))

fun PaletteColors.heartBrush(): Brush = Brush.linearGradient(listOf(heartStart, heartMid, heartEnd))

@Composable
fun CheckYourLoveTheme(
    palette: AppPalette = AppPalette.AUBERGINE,
    dark: Boolean,
    reducedMotionPreference: Boolean? = null,
    haptics: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = palette.colors(dark)
    val systemReduced = systemReducedMotion()
    val hapticFeedback = LocalHapticFeedback.current
    val motion = MotionSpec(reduced = reducedMotionPreference ?: systemReduced)
    val hapticsImpl = remember(haptics, hapticFeedback) { Haptics(haptics, hapticFeedback) }
    CompositionLocalProvider(
        LocalPalette provides colors,
        LocalMotion provides motion,
        LocalHaptics provides hapticsImpl,
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            typography = appTypography(),
            shapes = AppShapes,
            content = content,
        )
    }
}
