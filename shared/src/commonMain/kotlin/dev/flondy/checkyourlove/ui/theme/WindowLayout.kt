package dev.flondy.checkyourlove.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.min

/**
 * Beschreibt das verfügbare App-Fenster und leitet daraus die Layout-Entscheidungen ab
 * (Handy, Tablet, Hoch- und Querformat). Bewusst ohne Mehrspalten-Ansichten mit getrennten
 * Bildschirmen: Jeder Screen bleibt ein Screen, er ordnet seine Inhalte nur passend an.
 */
@Immutable
data class WindowLayout(val width: Dp, val height: Dp) {
    /** Ab 600 dp Breite (Tablet, Handy quer): Navigationsleiste seitlich statt unten. */
    val useNavigationRail: Boolean get() = width >= 600.dp

    val isLandscape: Boolean get() = width > height

    /** Tablet-Größe (kürzere Seite mindestens 600 dp). */
    val isTablet: Boolean get() = min(width, height) >= 600.dp

    /** Breites Querformat: Inhalte eines Screens nebeneinander statt untereinander. */
    val sideBySide: Boolean get() = isLandscape && width >= 700.dp

    /** Wenig Höhe (Handy quer): Abstände und Zusatztexte reduzieren. */
    val isShort: Boolean get() = height < 520.dp

    /** Maximale Breite für einspaltige Inhalte (Listen, Einstellungen, Premium). */
    val contentMaxWidth: Dp get() = if (isTablet) 720.dp else 640.dp

    /** Maximale Breite für den Liebestest-Screen. */
    val testMaxWidth: Dp get() = when {
        sideBySide -> 1040.dp
        isTablet -> 620.dp
        else -> 560.dp
    }

    /** Größe des Herz-Buttons, abhängig von Breite und verfügbarer Höhe. */
    val heartSize: Dp get() = when {
        sideBySide -> (height * 0.42f).coerceIn(150.dp, 260.dp)
        isTablet -> 240.dp
        else -> 196.dp
    }

    /** Größe des Prozent-Rings im Ergebnis. */
    val ringSize: Dp get() = when {
        sideBySide -> (height * 0.48f).coerceIn(190.dp, 300.dp)
        isTablet -> 270.dp
        else -> 220.dp
    }

    /** Ergebnis-Verlauf ab großer Breite als Raster mit mehreren Karten pro Zeile. */
    val historyColumns: Int get() = when {
        width >= 1000.dp -> 3
        width >= 700.dp -> 2
        else -> 1
    }

    companion object {
        val Phone = WindowLayout(411.dp, 891.dp)
    }
}

val LocalWindowLayout = compositionLocalOf { WindowLayout.Phone }
