package dev.flondy.checkyourlove.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import dev.flondy.checkyourlove.ui.theme.LocalMotion
import dev.flondy.checkyourlove.ui.theme.LocalPalette

/**
 * Herzform in einem Rahmen der Größe [size] (Seitenverhältnis ~ 80:76).
 * Gleiche Geometrie wie App-Icon und Teilen-Karte.
 */
fun heartPath(size: Size, origin: Offset = Offset.Zero): Path {
    val sx = size.width / 80f
    val sy = size.height / 76f
    fun x(v: Float) = origin.x + (v - 10f) * sx
    fun y(v: Float) = origin.y + (v - 12f) * sy
    return Path().apply {
        moveTo(x(50f), y(88f))
        cubicTo(x(50f), y(88f), x(10f), y(62f), x(10f), y(36f))
        cubicTo(x(10f), y(22f), x(21f), y(12f), x(33f), y(12f))
        cubicTo(x(41f), y(12f), x(47f), y(16f), x(50f), y(22f))
        cubicTo(x(53f), y(16f), x(59f), y(12f), x(67f), y(12f))
        cubicTo(x(79f), y(12f), x(90f), y(22f), x(90f), y(36f))
        cubicTo(x(90f), y(62f), x(50f), y(88f), x(50f), y(88f))
        close()
    }
}

/** Zeichnet ein gefülltes Herz zentriert in den aktuellen Zeichenbereich. */
fun DrawScope.drawHeart(brush: Brush, fraction: Float = 1f, alpha: Float = 1f) {
    val w = size.width * fraction
    val h = w * 76f / 80f
    val origin = Offset((size.width - w) / 2f, (size.height - h) / 2f)
    drawPath(heartPath(Size(w, h), origin), brush, alpha = alpha)
}

/**
 * Zentrales Herz-Element. Reagiert auf Druck, pulsiert während der Auswertung
 * (nur solange [pulsing] aktiv ist) und respektiert reduzierte Bewegung.
 */
@Composable
fun HeartButton(
    size: Dp,
    pulsing: Boolean,
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    val palette = LocalPalette.current
    val motion = LocalMotion.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "heartPress",
    )
    val pulse = if (pulsing && !motion.reduced) {
        val transition = rememberInfiniteTransition(label = "pulse")
        transition.animateFloat(
            initialValue = 1f, targetValue = 1.08f,
            animationSpec = infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "pulseScale",
        ).value
    } else 1f
    val glowAlpha by animateFloatAsState(if (pulsing) 0.75f else 0.45f, tween(400), label = "glow")
    val entrance = remember { Animatable(if (motion.reduced) 1f else 0.85f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow))
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(pressScale * pulse * entrance.value)
            .then(
                if (contentDescription.isEmpty()) {
                    Modifier.clearAndSetSemantics { }
                } else {
                    Modifier
                        .semantics { this.contentDescription = contentDescription }
                        .clickable(
                            interactionSource = interaction,
                            indication = null,
                            enabled = enabled,
                            role = Role.Button,
                            onClick = onClick,
                        )
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // weicher Schein hinter dem Herz
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(palette.glow.copy(alpha = glowAlpha), Color.Transparent),
                    center = center, radius = this.size.minDimension * 0.62f,
                ),
                radius = this.size.minDimension * 0.62f,
            )
            val heartBrush = Brush.linearGradient(
                listOf(palette.heartStart, palette.heartMid, palette.heartEnd),
                start = Offset(this.size.width * 0.15f, this.size.height * 0.1f),
                end = Offset(this.size.width * 0.9f, this.size.height * 0.95f),
            )
            drawHeart(heartBrush, fraction = 0.86f, alpha = if (enabled || pulsing) 1f else 0.55f)
            // dezentes Glanzlicht
            translate(left = this.size.width * 0.25f, top = this.size.height * 0.27f) {
                rotate(-28f, pivot = Offset.Zero) {
                    drawOval(
                        brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0f))),
                        size = Size(this.size.width * 0.13f, this.size.height * 0.07f),
                    )
                }
            }
        }
        content()
    }
}
