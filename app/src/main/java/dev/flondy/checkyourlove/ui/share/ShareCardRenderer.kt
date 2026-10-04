package dev.flondy.checkyourlove.ui.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import dev.flondy.checkyourlove.R
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

/**
 * Zeichnet die Ergebniskarte direkt mit android.graphics – unabhängig vom Bildschirm,
 * deterministisch und ohne sichtbare Composables. Format 4:5 (1080 × 1350 px).
 */
class ShareCardRenderer(context: Context) {
    private val serif: Typeface = ResourcesCompat.getFont(context, R.font.dm_serif_display) ?: Typeface.SERIF
    private val serifItalic: Typeface = ResourcesCompat.getFont(context, R.font.dm_serif_display_italic) ?: Typeface.SERIF
    private val sans: Typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_sans) ?: Typeface.SANS_SERIF

    fun render(data: ShareCardData, template: ShareTemplate, palette: PaletteColors, width: Int = WIDTH): Bitmap {
        val height = width * 5 / 4
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val s = width / WIDTH.toFloat()
        canvas.scale(s, s)
        when (template) {
            ShareTemplate.CLASSIC -> drawClassic(canvas, data, palette)
            ShareTemplate.POLAROID -> drawPolaroid(canvas, data, palette)
            ShareTemplate.NEON -> drawNeon(canvas, data, palette)
            ShareTemplate.MINIMAL -> drawMinimal(canvas, data, palette)
        }
        return bitmap
    }

    // ------------------------------------------------------------------ Vorlagen

    private fun drawClassic(c: Canvas, d: ShareCardData, p: PaletteColors) {
        val w = WIDTH.toFloat(); val h = HEIGHT.toFloat()
        val bg = Paint().apply {
            shader = LinearGradient(0f, 0f, w * 0.3f, h, p.backgroundAlt.toArgb(), p.background.toArgb(), Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, w, h, bg)
        glow(c, w / 2, 560f, 520f, p.glow.toArgb(), 110)
        label(c, d.appName.uppercase(), w / 2, 120f, p.primary.toArgb(), 30f, spacing = 0.18f)
        names(c, d, w / 2, 230f, serif, 66f, p.onBackground.toArgb(), maxWidth = w - 160f)
        heart(c, w / 2, 590f, 600f, intArrayOf(p.heartStart.toArgb(), p.heartMid.toArgb(), p.heartEnd.toArgb()))
        percentText(c, d.percent, w / 2, 650f, 210f, android.graphics.Color.WHITE, serif)
        centeredText(c, d.title, w / 2, 1000f, serif, 60f, p.primary.toArgb(), maxWidth = w - 160f)
        paragraph(c, d.text, 120f, 1040f, w - 240f, sans, 36f, p.onBackground.toArgb(), maxLines = 4)
        label(c, d.note, w / 2, h - 70f, p.onSurfaceVariant.toArgb(), 28f, spacing = 0.06f)
    }

    private fun drawPolaroid(c: Canvas, d: ShareCardData, p: PaletteColors) {
        val w = WIDTH.toFloat(); val h = HEIGHT.toFloat()
        c.drawColor(0xFFEDE6DD.toInt())
        // Karte mit Schatten
        val card = RectF(90f, 90f, w - 90f, h - 90f)
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x33000000; maskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL)
        }
        c.drawRoundRect(RectF(card.left + 6, card.top + 18, card.right + 6, card.bottom + 18), 12f, 12f, shadow)
        c.drawRoundRect(card, 12f, 12f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFCF7.toInt() })
        val photo = RectF(card.left + 50f, card.top + 50f, card.right - 50f, card.top + 50f + (card.width() - 100f))
        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(photo.left, photo.top, photo.right, photo.bottom, p.backgroundAlt.toArgb(), p.background.toArgb(), Shader.TileMode.CLAMP)
        }
        c.drawRect(photo, photoPaint)
        c.save(); c.clipRect(photo)
        glow(c, photo.centerX(), photo.centerY(), photo.width() * 0.6f, p.glow.toArgb(), 120)
        heart(c, photo.centerX(), photo.centerY() - 10f, photo.width() * 0.7f, intArrayOf(p.heartStart.toArgb(), p.heartMid.toArgb(), p.heartEnd.toArgb()))
        percentText(c, d.percent, photo.centerX(), photo.centerY() + 50f, 180f, android.graphics.Color.WHITE, serif)
        c.restore()
        val ink = 0xFF2B2230.toInt()
        names(c, d, w / 2, photo.bottom + 110f, serifItalic, 62f, ink, maxWidth = card.width() - 120f, upward = false)
        centeredText(c, d.title, w / 2, photo.bottom + 190f, sans, 34f, 0xFF7A6470.toInt(), maxWidth = card.width() - 120f)
        label(c, "${d.appName} · ${d.note}", w / 2, card.bottom - 40f, 0xFF9C8C93.toInt(), 26f, spacing = 0.05f)
    }

    private fun drawNeon(c: Canvas, d: ShareCardData, p: PaletteColors) {
        val w = WIDTH.toFloat(); val h = HEIGHT.toFloat()
        c.drawColor(0xFF07040D.toInt())
        glow(c, w / 2, 600f, 640f, p.heartEnd.toArgb(), 70)
        val neonA = p.heartMid.toArgb()
        val neonB = p.heartEnd.toArgb()
        val path = heartPath(w / 2, 600f, 640f)
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 34f; color = neonA
            maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL); alpha = 200
        }
        c.drawPath(path, glowPaint)
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 12f; strokeJoin = Paint.Join.ROUND
            shader = LinearGradient(w / 2 - 320f, 300f, w / 2 + 320f, 900f, neonA, neonB, Shader.TileMode.CLAMP)
        }
        c.drawPath(path, line)
        c.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 3f; color = 0xCCFFFFFF.toInt() })
        val pctGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply { maskFilter = BlurMaskFilter(24f, BlurMaskFilter.Blur.NORMAL) }
        percentText(c, d.percent, w / 2, 660f, 200f, neonA, sans, pctGlow, bold = true)
        percentText(c, d.percent, w / 2, 660f, 200f, 0xFFFFF0F6.toInt(), sans, bold = true)
        label(c, d.appName.uppercase(), w / 2, 130f, neonB, 30f, spacing = 0.3f)
        names(c, d, w / 2, 1020f, sans, 58f, 0xFFFFFFFF.toInt(), maxWidth = w - 160f, bold = true, upward = false)
        centeredText(c, d.title.uppercase(), w / 2, 1100f, sans, 34f, neonA, maxWidth = w - 160f, spacing = 0.12f)
        label(c, d.note, w / 2, h - 70f, 0x99FFFFFF.toInt(), 28f, spacing = 0.06f)
    }

    private fun drawMinimal(c: Canvas, d: ShareCardData, p: PaletteColors) {
        val w = WIDTH.toFloat(); val h = HEIGHT.toFloat()
        c.drawColor(0xFFFBF8F5.toInt())
        val ink = 0xFF1F1A22.toInt()
        val accent = p.heartMid.toArgb()
        label(c, "${d.nameA}  ·  ${d.nameB}".uppercase(), w / 2, 200f, ink, 32f, spacing = 0.2f, maxWidth = w - 160f)
        c.drawLine(w / 2 - 60f, 250f, w / 2 + 60f, 250f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent; strokeWidth = 3f })
        val outline = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 4f; color = accent }
        c.drawPath(heartPath(w / 2, 600f, 520f), outline)
        percentText(c, d.percent, w / 2, 660f, 230f, ink, serif)
        centeredText(c, d.title, w / 2, 1010f, serifItalic, 60f, ink, maxWidth = w - 200f)
        paragraph(c, d.text, 150f, 1050f, w - 300f, sans, 32f, 0xFF5E5560.toInt(), maxLines = 3)
        label(c, "${d.appName.uppercase()} — ${d.note}", w / 2, h - 80f, 0xFF9A9198.toInt(), 24f, spacing = 0.14f)
    }

    // ------------------------------------------------------------------ Bausteine

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

    private fun heart(c: Canvas, cx: Float, cy: Float, width: Float, colors: IntArray) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(cx - width / 2, cy - width / 2, cx + width / 2, cy + width / 2, colors, null, Shader.TileMode.CLAMP)
        }
        c.drawPath(heartPath(cx, cy, width), paint)
        val shine = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x26FFFFFF }
        c.save()
        c.rotate(-28f, cx - width * 0.24f, cy - width * 0.24f)
        c.drawOval(RectF(cx - width * 0.31f, cy - width * 0.27f, cx - width * 0.17f, cy - width * 0.21f), shine)
        c.restore()
    }

    private fun glow(c: Canvas, cx: Float, cy: Float, radius: Float, color: Int, alpha: Int) {
        val start = (color and 0x00FFFFFF) or (alpha shl 24)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(cx, cy, radius, start, color and 0x00FFFFFF, Shader.TileMode.CLAMP)
        }
        c.drawCircle(cx, cy, radius, paint)
    }

    private fun percentText(
        c: Canvas, percent: Int, cx: Float, baseline: Float, size: Float, color: Int, face: Typeface,
        base: Paint? = null, bold: Boolean = false,
    ) {
        val number = Paint(base ?: Paint(Paint.ANTI_ALIAS_FLAG)).apply {
            isAntiAlias = true
            typeface = if (bold) Typeface.create(face, Typeface.BOLD) else face
            textSize = size; this.color = color
        }
        val sign = Paint(number).apply { textSize = size * 0.42f }
        val numberText = percent.toString()
        val total = number.measureText(numberText) + sign.measureText(" %")
        val startX = cx - total / 2f
        c.drawText(numberText, startX, baseline, number)
        c.drawText(" %", startX + number.measureText(numberText), baseline, sign)
    }

    private fun label(
        c: Canvas, text: String, cx: Float, baseline: Float, color: Int, size: Float,
        spacing: Float = 0f, maxWidth: Float = WIDTH - 120f,
    ) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(sans, Typeface.BOLD); textSize = size; this.color = color
            letterSpacing = spacing; textAlign = Paint.Align.CENTER
        }
        val fitted = TextUtils.ellipsize(text, paint, maxWidth, TextUtils.TruncateAt.END).toString()
        c.drawText(fitted, cx, baseline, paint)
    }

    private fun centeredText(
        c: Canvas, text: String, cx: Float, baseline: Float, face: Typeface, size: Float, color: Int,
        maxWidth: Float, bold: Boolean = false, spacing: Float = 0f,
    ) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = if (bold) Typeface.create(face, Typeface.BOLD) else face
            textSize = size; this.color = color; textAlign = Paint.Align.CENTER; letterSpacing = spacing
        }
        // Lange Namen: zuerst verkleinern, dann kürzen
        while (paint.measureText(text) > maxWidth && paint.textSize > size * 0.6f) paint.textSize -= 2f
        val fitted = TextUtils.ellipsize(text, paint, maxWidth, TextUtils.TruncateAt.END).toString()
        c.drawText(fitted, cx, baseline, paint)
    }

    /** Namenspaar: einzeilig, bei langen Namen zweizeilig statt stark gekürzt. */
    private fun names(
        c: Canvas, d: ShareCardData, cx: Float, baseline: Float, face: Typeface, size: Float, color: Int,
        maxWidth: Float, bold: Boolean = false, upward: Boolean = true,
    ) {
        val single = "${d.nameA} & ${d.nameB}"
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = if (bold) Typeface.create(face, Typeface.BOLD) else face; textSize = size * 0.8f
        }
        if (paint.measureText(single) <= maxWidth) {
            centeredText(c, single, cx, baseline, face, size, color, maxWidth, bold)
        } else {
            val first = if (upward) baseline - size * 0.55f else baseline - size * 0.25f
            centeredText(c, "${d.nameA} &", cx, first, face, size * 0.8f, color, maxWidth, bold)
            centeredText(c, d.nameB, cx, first + size * 0.95f, face, size * 0.8f, color, maxWidth, bold)
        }
    }

    private fun paragraph(
        c: Canvas, text: String, left: Float, top: Float, width: Float, face: Typeface, size: Float, color: Int, maxLines: Int,
    ) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = face; textSize = size; this.color = color }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt())
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.25f)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
        c.save()
        c.translate(left, top)
        layout.draw(c)
        c.restore()
    }

    companion object {
        const val WIDTH = 1080
        const val HEIGHT = 1350
    }
}
