package dev.flondy.checkyourlove.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.dm_serif_display
import dev.flondy.checkyourlove.resources.dm_serif_display_italic
import dev.flondy.checkyourlove.resources.plus_jakarta_sans
import org.jetbrains.compose.resources.Font

/** Serifenschrift für Überschriften und den Prozentwert. */
@Composable
fun displayFamily(): FontFamily = FontFamily(
    Font(Res.font.dm_serif_display, FontWeight.Normal),
    Font(Res.font.dm_serif_display_italic, FontWeight.Normal, FontStyle.Italic),
)

/** Variable Plus Jakarta Sans für Fließtext. */
@Composable
fun bodyFamily(): FontFamily = FontFamily(
    Font(Res.font.plus_jakarta_sans, FontWeight(400), variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(Res.font.plus_jakarta_sans, FontWeight(500), variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(Res.font.plus_jakarta_sans, FontWeight(600), variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(Res.font.plus_jakarta_sans, FontWeight(700), variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

@Composable
fun appTypography(): Typography {
    val display = displayFamily()
    val body = bodyFamily()
    return Typography(
        displayLarge = TextStyle(fontFamily = display, fontSize = 88.sp, lineHeight = 92.sp, letterSpacing = (-0.02).em),
        displayMedium = TextStyle(fontFamily = display, fontSize = 56.sp, lineHeight = 60.sp),
        displaySmall = TextStyle(fontFamily = display, fontSize = 40.sp, lineHeight = 46.sp),
        headlineLarge = TextStyle(fontFamily = display, fontSize = 34.sp, lineHeight = 40.sp),
        headlineMedium = TextStyle(fontFamily = display, fontSize = 28.sp, lineHeight = 34.sp),
        headlineSmall = TextStyle(fontFamily = display, fontSize = 24.sp, lineHeight = 30.sp),
        titleLarge = TextStyle(fontFamily = body, fontWeight = FontWeight(700), fontSize = 20.sp, lineHeight = 26.sp),
        titleMedium = TextStyle(fontFamily = body, fontWeight = FontWeight(600), fontSize = 16.sp, lineHeight = 22.sp),
        titleSmall = TextStyle(fontFamily = body, fontWeight = FontWeight(600), fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = body, fontWeight = FontWeight(400), fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = body, fontWeight = FontWeight(400), fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontFamily = body, fontWeight = FontWeight(400), fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = TextStyle(fontFamily = body, fontWeight = FontWeight(600), fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.01.em),
        labelMedium = TextStyle(fontFamily = body, fontWeight = FontWeight(600), fontSize = 12.sp, lineHeight = 16.sp),
        labelSmall = TextStyle(fontFamily = body, fontWeight = FontWeight(600), fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.04.em),
    )
}
