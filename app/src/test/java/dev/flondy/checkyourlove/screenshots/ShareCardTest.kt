package dev.flondy.checkyourlove.screenshots

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dev.flondy.checkyourlove.monetization.ShareTemplate
import dev.flondy.checkyourlove.platform.encodeToPng
import dev.flondy.checkyourlove.ui.share.ShareCardData
import dev.flondy.checkyourlove.ui.share.ShareCardFonts
import dev.flondy.checkyourlove.ui.share.ShareCardRenderer
import dev.flondy.checkyourlove.ui.theme.AppPalette
import dev.flondy.checkyourlove.ui.theme.bodyFamily
import dev.flondy.checkyourlove.ui.theme.displayFamily
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Rendert alle Teilen-Karten mit dem plattformunabhängigen Renderer und speichert sie als PNG. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-w411dp-h891dp-night-xxhdpi")
class ShareCardTest {
    @get:Rule val compose = createComposeRule()

    @Test fun renderAllTemplates() {
        val data = ShareCardData("Anna", "Ben", 79, "Starke Verbindung", "Schöne Werte! Ein Lächeln ist quasi vorprogrammiert.", "Nur zum Spaß", "Check your Love")
        val long = data.copy(nameA = "Maximiliane-Charlotte von Hohenstein", nameB = "Bartholomäus Alexander")
        val results = mutableMapOf<String, ByteArray>()
        compose.setContent {
            val renderer = ShareCardRenderer(rememberTextMeasurer(), ShareCardFonts(displayFamily(), bodyFamily()))
            LaunchedEffect(Unit) {
                ShareTemplate.entries.forEach { results["card_${it.id}"] = renderer.render(data, it, AppPalette.AUBERGINE.dark).encodeToPng() }
                results["card_long_names"] = renderer.render(long, ShareTemplate.CLASSIC, AppPalette.AURORA.dark).encodeToPng()
            }
        }
        compose.waitUntil(10_000) { results.size == 5 }
        File("build/screenshots").mkdirs()
        results.forEach { (name, png) ->
            assertThat(png.size).isGreaterThan(10_000)
            File("build/screenshots/$name.png").writeBytes(png)
        }
    }
}
