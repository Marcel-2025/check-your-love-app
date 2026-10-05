package dev.flondy.checkyourlove.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import dev.flondy.checkyourlove.monetization.ResultEffect
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float, val y: Float, val vx: Float, val vy: Float,
    val size: Float, val rotation: Float, val spin: Float,
    val color: Color, val shape: Int, val delay: Float, val sway: Float,
)

/**
 * Einmaliger, kurzer Partikeleffekt (ca. 2,6 s) – nie endlos, blockiert keine Eingaben
 * (reines Zeichnen ohne Klickfläche). [key] startet den Effekt neu.
 */
@Composable
fun ParticleBurst(
    effect: ResultEffect,
    colors: List<Color>,
    key: Any,
    modifier: Modifier = Modifier,
) {
    if (effect == ResultEffect.NONE) return
    val progress = remember(key) { Animatable(0f) }
    val particles = remember(key, effect) { createParticles(effect, colors, key.hashCode()) }
    LaunchedEffect(key) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 2600, easing = LinearEasing))
    }
    Canvas(modifier) {
        val t = progress.value
        if (t <= 0f || t >= 1f) return@Canvas
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            val local = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (local <= 0f) return@forEach
            val alpha = when {
                local < 0.1f -> local / 0.1f
                local > 0.7f -> (1f - local) / 0.3f
                else -> 1f
            }.coerceIn(0f, 1f)
            val (px, py) = when (effect) {
                ResultEffect.CONFETTI -> {
                    val g = 1.6f
                    Offset(p.x * w + p.vx * w * local, p.y * h + p.vy * h * local + g * h * local * local * 0.5f)
                }
                ResultEffect.HEART_RAIN -> Offset(
                    p.x * w + sin(local * 6f + p.sway) * w * 0.03f,
                    -0.08f * h + (p.y + p.vy * local) * h,
                )
                ResultEffect.SPARKLE -> {
                    val ease = 1f - (1f - local) * (1f - local)
                    Offset(w / 2f + p.vx * w * ease, h * 0.42f + p.vy * w * ease)
                }
                ResultEffect.NONE -> Offset.Zero
            }.let { it.x to it.y }
            val s = p.size * (if (effect == ResultEffect.SPARKLE) (1f - local * 0.6f) else 1f)
            translate(px, py) {
                rotate(p.rotation + p.spin * local * 360f, pivot = Offset.Zero) {
                    when (p.shape) {
                        0 -> drawRect(p.color, topLeft = Offset(-s / 2, -s / 4), size = Size(s, s / 2), alpha = alpha)
                        1 -> drawCircle(p.color, radius = s / 2.6f, center = Offset.Zero, alpha = alpha)
                        2 -> drawPath(heartPath(Size(s, s * 0.95f), Offset(-s / 2, -s / 2)), SolidColor(p.color), alpha = alpha)
                        else -> drawPath(starPath(s), SolidColor(p.color), alpha = alpha)
                    }
                }
            }
        }
    }
}

private fun starPath(s: Float): Path = Path().apply {
    val r = s / 2f
    val i = r * 0.38f
    moveTo(0f, -r)
    lineTo(i * 0.5f, -i * 0.5f); lineTo(r, 0f); lineTo(i * 0.5f, i * 0.5f)
    lineTo(0f, r); lineTo(-i * 0.5f, i * 0.5f); lineTo(-r, 0f); lineTo(-i * 0.5f, -i * 0.5f)
    close()
}

private fun createParticles(effect: ResultEffect, colors: List<Color>, seed: Int): List<Particle> {
    val rnd = Random(seed)
    fun color() = colors[rnd.nextInt(colors.size)]
    return when (effect) {
        ResultEffect.CONFETTI -> List(46) {
            val angle = (-PI / 2 + (rnd.nextFloat() - 0.5f) * PI * 0.9f).toFloat()
            val speed = 0.55f + rnd.nextFloat() * 0.6f
            Particle(
                x = 0.5f + (rnd.nextFloat() - 0.5f) * 0.2f, y = 0.62f,
                vx = cos(angle) * speed * 0.7f, vy = sin(angle) * speed,
                size = 14f + rnd.nextFloat() * 16f, rotation = rnd.nextFloat() * 360f, spin = rnd.nextFloat() * 2f - 1f,
                color = color(), shape = if (rnd.nextInt(5) == 0) 2 else rnd.nextInt(2),
                delay = rnd.nextFloat() * 0.12f, sway = 0f,
            )
        }
        ResultEffect.HEART_RAIN -> List(34) {
            Particle(
                x = rnd.nextFloat(), y = -rnd.nextFloat() * 0.3f, vx = 0f, vy = 0.9f + rnd.nextFloat() * 0.5f,
                size = 22f + rnd.nextFloat() * 26f, rotation = rnd.nextFloat() * 40f - 20f, spin = (rnd.nextFloat() - 0.5f) * 0.15f,
                color = color(), shape = 2, delay = rnd.nextFloat() * 0.35f, sway = rnd.nextFloat() * 6f,
            )
        }
        ResultEffect.SPARKLE -> List(40) {
            val angle = rnd.nextFloat() * 2f * PI.toFloat()
            val dist = 0.25f + rnd.nextFloat() * 0.35f
            Particle(
                x = 0.5f, y = 0.4f, vx = cos(angle) * dist, vy = sin(angle) * dist,
                size = 14f + rnd.nextFloat() * 22f, rotation = rnd.nextFloat() * 90f, spin = (rnd.nextFloat() - 0.5f) * 0.4f,
                color = color(), shape = if (rnd.nextInt(4) == 0) 1 else 3, delay = rnd.nextFloat() * 0.15f, sway = 0f,
            )
        }
        ResultEffect.NONE -> emptyList()
    }
}
