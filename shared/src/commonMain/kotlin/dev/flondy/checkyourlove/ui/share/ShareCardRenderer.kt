package dev.flondy.checkyourlove.ui.share

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.flondy.checkyourlove.monetization.ShareTemplate
import dev.flondy.checkyourlove.ui.theme.PaletteColors

/** Inhalt einer Ergebniskarte (bereits lokalisierte Texte). */
data class ShareCardData(
    val nameA: String,
    val nameB: String,
    val percent: Int,
    val title: String,
    val text: String,
    val note: String,
    val appName: String,
)

/** Schriften für die Karte (aus den Compose-Ressourcen geladen). */
data class ShareCardFonts(val serif: FontFamily, val sans: FontFamily)

/**
 * Zeichnet die Ergebniskarte plattformunabhängig mit Compose-Grafik in ein [ImageBitmap]
 * (Android und iOS identisch). Format 4:5, Referenzbreite 1080 px.
 */
class ShareCardRenderer(
    private val textMeasurer: TextMeasurer,
    private val fonts: ShareCardFonts,
) {
    fun render(data: ShareCardData, template: ShareTemplate, palette: PaletteColors, width: Int = WIDTH): ImageBitmap {
        val height = width * 5 / 4
        val bitmap = ImageBitmap(width, height)
        val s = width / WIDTH.toFloat()
        // Density 1: 1 sp = 1 px auf der 1080er-Referenzfläche
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(bitmap), Size(width.toFloat(), height.toFloat())) {
            scale(s, s, pivot = Offset.Zero) {
                when (template) {
                    ShareTemplate.CLASSIC -> drawClassic(data, palette)
                    ShareTemplate.POLAROID -> drawPolaroid(data, palette)
                    ShareTemplate.NEON -> drawNeon(data, palette)
                    ShareTemplate.MINIMAL -> drawMinimal(data, palette)
                }
            }
        }
        return bitmap
    }

    // ------------------------------------------------------------------ Vorlagen

    private fun DrawScope.drawClassic(d: ShareCardData, p: PaletteColors) {
        val w = WIDTH.toFloat(); val h = HEIGHT.toFloat()
        drawRect(Brush.linearGradient(listOf(p.backgroundAlt, p.background), Offset.Zero, Offset(w * 0.3f, h)), size = Size(w, h))
        glow(w / 2, 560f, 520f, p.glow, 0.43f)
        label(d.appName.uppercase(), w / 2, 92f, p.primary, 30f, spacing = 0.18f)
        names(d, w / 2, 168f, fonts.serif, 66f, p.onBackground, maxWidth = w - 160f)
        heart(w / 2, 590f, 600f, listOf(p.heartStart, p.heartMid, p.heartEnd))
        percentText(d.percent, w / 2, 650f, 210f, Color.White, fonts.serif)
        centered(d.title, w / 2, 950f, fonts.serif, 60f, p.primary, maxWidth = w - 160f)
        paragraph(d.text, 120f, 1040f, w - 240f, fonts.sans, 36f, p.onBackground, maxLines = 4)
        label(d.note, w / 2, h - 96f, p.onSurfaceVariant, 28f, spacing = 0.06f)
    }

    private fun DrawScope.drawPolaroid(d: ShareCardData, p: PaletteColors) {
        val w = WIDTH.toFloat(); val h = HEIGHT.toFloat()
        drawRect(Color(0xFFEDE6DD), size = Size(w, h))
        val card = Rect(90f, 90f, w - 90f, h - 90f)
        // weicher Schatten (mehrere transparente Ebenen)
        for (i in 1..6) {
            drawRoundRect(
                Color.Black.copy(alpha = 0.035f),
                topLeft = Offset(card.left + 6f - i * 3, card.top + 18f - i * 2),
                size = Size(card.width + i * 6, card.height + i * 5),
                cornerRadius = CornerRadius(12f + i * 3),
            )
        }
        drawRoundRect(Color(0xFFFFFCF7), card.topLeft, card.size, CornerRadius(12f))
        val photo = Rect(card.left + 50f, card.top + 50f, card.right - 50f, card.top + 50f + (card.width - 100f))
        drawRect(Brush.linearGradient(listOf(p.backgroundAlt, p.background), photo.topLeft, photo.bottomRight), photo.topLeft, photo.size)
        clipRect(photo.left, photo.top, photo.right, photo.bottom) {
            glow(photo.center.x, photo.center.y, photo.width * 0.6f, p.glow, 0.47f)
            heart(photo.center.x, photo.center.y - 10f, photo.width * 0.7f, listOf(p.heartStart, p.heartMid, p.heartEnd))
            percentText(d.percent, photo.center.x, photo.center.y + 50f, 180f, Color.White, fonts.serif)
        }
        val ink = Color(0xFF2B2230)
        names(d, w / 2, photo.bottom + 50f, fonts.serif, 62f, ink, maxWidth = card.width - 120f, italic = true)
        centered(d.title, w / 2, photo.bottom + 160f, fonts.sans, 34f, Color(0xFF7A6470), maxWidth = card.width - 120f)
        label("${d.appName} · ${d.note}", w / 2, card.bottom - 66f, Color(0xFF9C8C93), 26f, spacing = 0.05f)
    }

    private fun DrawScope.drawNeon(d: ShareCardData, p: PaletteColors) {
        val w = WIDTH.toFloat(); val h = HEIGHT.toFloat()
        drawRect(Color(0xFF07040D), size = Size(w, h))
        glow(w / 2, 600f, 640f, p.heartEnd, 0.27f)
        val path = heartPath(w / 2, 600f, 640f)
        // Leuchten: mehrere breite, transparente Konturen
        listOf(60f to 0.06f, 44f to 0.09f, 30f to 0.14f, 20f to 0.22f).forEach { (width, alpha) ->
            drawPath(path, p.heartMid.copy(alpha = alpha), style = Stroke(width = width, join = StrokeJoin.Round))
        }
        drawPath(
            path,
            Brush.linearGradient(listOf(p.heartMid, p.heartEnd), Offset(w / 2 - 320f, 300f), Offset(w / 2 + 320f, 900f)),
            style = Stroke(width = 12f, join = StrokeJoin.Round),
        )
        drawPath(path, Color.White.copy(alpha = 0.8f), style = Stroke(width = 3f))
        percentText(d.percent, w / 2, 660f, 200f, Color(0xFFFFF0F6), fonts.sans, bold = true, glowColor = p.heartMid)
        label(d.appName.uppercase(), w / 2, 102f, p.heartEnd, 30f, spacing = 0.3f)
        names(d, w / 2, 965f, fonts.sans, 58f, Color.White, maxWidth = w - 160f, bold = true)
        centered(d.title.uppercase(), w / 2, 1070f, fonts.sans, 34f, p.heartMid, maxWidth = w - 160f, spacing = 0.12f)
        label(d.note, w / 2, h - 96f, Color.White.copy(alpha = 0.6f), 28f, spacing = 0.06f)
    }

    private fun DrawScope.drawMinimal(d: ShareCardData, p: PaletteColors) {
        val w = WIDTH.toFloat(); val h = HEIGHT.toFloat()
        drawRect(Color(0xFFFBF8F5), size = Size(w, h))
        val ink = Color(0xFF1F1A22)
        val accent = p.heartMid
        label("${d.nameA}  ·  ${d.nameB}".uppercase(), w / 2, 168f, ink, 32f, spacing = 0.2f, maxWidth = w - 160f)
        drawLine(accent, Offset(w / 2 - 60f, 250f), Offset(w / 2 + 60f, 250f), strokeWidth = 3f)
        drawPath(heartPath(w / 2, 600f, 520f), accent, style = Stroke(width = 4f))
        percentText(d.percent, w / 2, 660f, 230f, ink, fonts.serif)
        centered(d.title, w / 2, 960f, fonts.serif, 60f, ink, maxWidth = w - 200f, italic = true)
        paragraph(d.text, 150f, 1050f, w - 300f, fonts.sans, 32f, Color(0xFF5E5560), maxLines = 3)
        label("${d.appName.uppercase()} — ${d.note}", w / 2, h - 104f, Color(0xFF9A9198), 24f, spacing = 0.14f)
    }

    // ------------------------------------------------------------------ Bausteine

    /** Misst Text auf der 1080er-Referenzfläche (1 sp = 1 px, unabhängig von der Bildschirmdichte). */
    private fun measure(
        text: String,
        style: TextStyle,
        overflow: TextOverflow = TextOverflow.Clip,
        softWrap: Boolean = true,
        maxLines: Int = Int.MAX_VALUE,
        constraints: Constraints = Constraints(),
    ) = textMeasurer.measure(
        text = text, style = style, overflow = overflow, softWrap = softWrap, maxLines = maxLines,
        constraints = constraints, layoutDirection = LayoutDirection.Ltr, density = REFERENCE_DENSITY,
    )

    private fun heartPath(cx: Float, cy: Float, width: Float): Path {
        val h = width * 76f / 80f
        val left = cx - width / 2f
        val top = cy - h / 2f
        val sx = width / 80f
        val sy = h / 76f
        fun x(v: Float) = left + (v - 10f) * sx
        fun y(v: Float) = top + (v - 12f) * sy
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

    private fun DrawScope.heart(cx: Float, cy: Float, width: Float, colors: List<Color>) {
        drawPath(
            heartPath(cx, cy, width),
            Brush.linearGradient(colors, Offset(cx - width / 2, cy - width / 2), Offset(cx + width / 2, cy + width / 2)),
        )
        rotate(-28f, pivot = Offset(cx - width * 0.24f, cy - width * 0.24f)) {
            drawOval(
                Color.White.copy(alpha = 0.15f),
                topLeft = Offset(cx - width * 0.31f, cy - width * 0.27f),
                size = Size(width * 0.14f, width * 0.06f),
            )
        }
    }

    private fun DrawScope.glow(cx: Float, cy: Float, radius: Float, color: Color, alpha: Float) {
        drawCircle(
            Brush.radialGradient(listOf(color.copy(alpha = alpha), color.copy(alpha = 0f)), Offset(cx, cy), radius),
            radius = radius,
            center = Offset(cx, cy),
        )
    }

    private fun style(family: FontFamily, size: Float, color: Color, bold: Boolean = false, italic: Boolean = false, spacing: Float = 0f) =
        TextStyle(
            fontFamily = family,
            fontSize = size.sp,
            color = color,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            letterSpacing = spacing.em,
            textAlign = TextAlign.Center,
        )

    /** Zentrierte, einzeilige Zeile; zu lange Texte werden verkleinert und dann gekürzt. [top] = Oberkante. */
    private fun DrawScope.centered(
        text: String, cx: Float, top: Float, family: FontFamily, size: Float, color: Color, maxWidth: Float,
        bold: Boolean = false, italic: Boolean = false, spacing: Float = 0f,
    ) {
        var fontSize = size
        var layout = measure(text, style(family, fontSize, color, bold, italic, spacing), maxLines = 1, softWrap = false)
        while (layout.size.width > maxWidth && fontSize > size * 0.6f) {
            fontSize -= 2f
            layout = measure(text, style(family, fontSize, color, bold, italic, spacing), maxLines = 1, softWrap = false)
        }
        if (layout.size.width > maxWidth) {
            layout = measure(
                text, style(family, fontSize, color, bold, italic, spacing),
                overflow = TextOverflow.Ellipsis, maxLines = 1, softWrap = false,
                constraints = Constraints(maxWidth = maxWidth.toInt()),
            )
        }
        drawText(layout, topLeft = Offset(cx - layout.size.width / 2f, top))
    }

    private fun DrawScope.label(text: String, cx: Float, top: Float, color: Color, size: Float, spacing: Float = 0f, maxWidth: Float = WIDTH - 120f) =
        centered(text, cx, top, fonts.sans, size, color, maxWidth, bold = true, spacing = spacing)

    /** Namenspaar: einzeilig, bei langen Namen zweizeilig statt stark gekürzt. */
    private fun DrawScope.names(
        d: ShareCardData, cx: Float, top: Float, family: FontFamily, size: Float, color: Color,
        maxWidth: Float, bold: Boolean = false, italic: Boolean = false,
    ) {
        val single = "${d.nameA} & ${d.nameB}"
        val probe = measure(single, style(family, size * 0.8f, color, bold, italic), maxLines = 1, softWrap = false)
        if (probe.size.width <= maxWidth) {
            centered(single, cx, top, family, size, color, maxWidth, bold, italic)
        } else {
            val first = top - size * 0.45f
            centered("${d.nameA} &", cx, first, family, size * 0.8f, color, maxWidth, bold, italic)
            centered(d.nameB, cx, first + size * 0.95f, family, size * 0.8f, color, maxWidth, bold, italic)
        }
    }

    /** Großer Prozentwert; [baseline] ≈ Grundlinie der Ziffern. */
    private fun DrawScope.percentText(
        percent: Int, cx: Float, baseline: Float, size: Float, color: Color, family: FontFamily,
        bold: Boolean = false, glowColor: Color? = null,
    ) {
        val numberStyle = style(family, size, color, bold).copy(textAlign = TextAlign.Start)
        val signStyle = numberStyle.copy(fontSize = (size * 0.42f).sp)
        val number = measure(percent.toString(), numberStyle, maxLines = 1, softWrap = false)
        val sign = measure(" %", signStyle, maxLines = 1, softWrap = false)
        val total = number.size.width + sign.size.width
        val left = cx - total / 2f
        val numberTop = baseline - number.firstBaseline
        val signTop = baseline - sign.firstBaseline
        if (glowColor != null) {
            listOf(10f, 6f, 3f).forEach { r ->
                listOf(Offset(r, 0f), Offset(-r, 0f), Offset(0f, r), Offset(0f, -r)).forEach { o ->
                    drawText(number, color = glowColor, topLeft = Offset(left + o.x, numberTop + o.y), alpha = 0.18f)
                }
            }
        }
        drawText(number, topLeft = Offset(left, numberTop))
        drawText(sign, topLeft = Offset(left + number.size.width, signTop))
    }

    private fun DrawScope.paragraph(
        text: String, left: Float, top: Float, width: Float, family: FontFamily, size: Float, color: Color, maxLines: Int,
    ) {
        val layout = measure(
            text,
            style(family, size, color).copy(lineHeight = (size * 1.25f).sp),
            overflow = TextOverflow.Ellipsis,
            maxLines = maxLines,
            constraints = Constraints(minWidth = width.toInt(), maxWidth = width.toInt()),
        )
        drawText(layout, topLeft = Offset(left, top))
    }

    companion object {
        const val WIDTH = 1080
        const val HEIGHT = 1350
        private val REFERENCE_DENSITY = Density(1f)
    }
}
