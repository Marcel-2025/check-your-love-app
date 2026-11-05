package dev.flondy.checkyourlove.screenshots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import dev.flondy.checkyourlove.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

/** Gemeinsamer Ablauf für Tablet- und Querformat-Layouts. */
abstract class LayoutFlow(private val prefix: String) {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun flow() {
        compose.waitForIdle()
        shot("01_start")
        compose.onNodeWithText("Dein Name").performTextInput("Anna")
        compose.onNodeWithText("Name deines Schwarms").performTextInput("Ben")
        compose.onNodeWithContentDescription("Liebe prüfen").performClick()
        idle(4)
        compose.onNode(hasContentDescription("79 Prozent", substring = true)).assertIsDisplayed()
        shot("02_result")
        compose.onNodeWithContentDescription("Zu Favoriten hinzufügen").performClick()
        compose.onNodeWithText("Verlauf").performClick()
        idle(1)
        compose.onNodeWithText("Anna & Ben").assertIsDisplayed()
        shot("03_history")
        compose.onNodeWithText("Einstellungen").performClick()
        idle(1)
        shot("04_settings")
        compose.onNodeWithContentDescription("Zurück").performClick()
        idle(1)
        compose.onNodeWithText("Liebestest").performClick()
        idle(1)
        compose.onNodeWithContentDescription("Teilen").performClick()
        idle(3)
        shot("05_share")
    }

    private fun shot(name: String) =
        compose.onNode(isRoot()).captureRoboImage("build/screenshots/${prefix}_$name.png")

    private fun idle(seconds: Long) {
        repeat(seconds.toInt() * 10) {
            Thread.sleep(15)
            ShadowLooper.idleMainLooper(100, TimeUnit.MILLISECONDS)
            compose.mainClock.advanceTimeBy(100)
        }
        compose.waitForIdle()
    }

    @Suppress("unused")
    private fun AndroidComposeTestRule<*, *>.root() = onNode(isRoot())
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-w1280dp-h800dp-land-night-hdpi")
class TabletLandscapeTest : LayoutFlow("tablet_land")

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-w800dp-h1280dp-port-night-hdpi")
class TabletPortraitTest : LayoutFlow("tablet_port")

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-w891dp-h411dp-land-night-xxhdpi")
class PhoneLandscapeTest : LayoutFlow("phone_land")
