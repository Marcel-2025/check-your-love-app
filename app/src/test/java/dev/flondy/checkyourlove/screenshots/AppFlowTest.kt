package dev.flondy.checkyourlove.screenshots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import dev.flondy.checkyourlove.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** End-to-End-Ablauf mit der echten App (Robolectric, ohne Emulator). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-w411dp-h891dp-night-xxhdpi")
class AppFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun fullFlow() {
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("build/screenshots/flow_01_start.png")
        compose.onNodeWithContentDescription("Liebe prüfen").performClick()
        compose.onAllNodesWithText("Bitte gib einen Namen ein.")[0].assertIsDisplayed()
        compose.onRoot().captureRoboImage("build/screenshots/flow_02_errors.png")
        compose.onNodeWithText("Dein Name").performTextInput("Anna")
        compose.onNodeWithText("Name deines Schwarms").performTextInput("Ben")
        compose.onNodeWithContentDescription("Liebe prüfen").performClick()
        compose.mainClock.advanceTimeBy(600)
        compose.onRoot().captureRoboImage("build/screenshots/flow_03_evaluating.png")
        idle(4)
        compose.onRoot().captureRoboImage("build/screenshots/flow_04_result.png")
        compose.onNode(androidx.compose.ui.test.hasContentDescription("79 Prozent", substring = true)).assertIsDisplayed()
        compose.onNodeWithContentDescription("Zu Favoriten hinzufügen").performClick()
        compose.onNodeWithText("Verlauf").performClick()
        idle(1)
        compose.onNodeWithText("Anna & Ben").assertIsDisplayed()
        compose.onRoot().captureRoboImage("build/screenshots/flow_05_history.png")
        compose.onNodeWithText("Einstellungen").performClick()
        idle(1)
        compose.onRoot().captureRoboImage("build/screenshots/flow_06_settings.png")
    }

    private fun idle(seconds: Long) {
        repeat(seconds.toInt() * 10) {
            Thread.sleep(15)
            org.robolectric.shadows.ShadowLooper.idleMainLooper(100, java.util.concurrent.TimeUnit.MILLISECONDS)
            compose.mainClock.advanceTimeBy(100)
        }
        compose.waitForIdle()
    }

    private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.onRoot() =
        onNode(androidx.compose.ui.test.isRoot())
}
