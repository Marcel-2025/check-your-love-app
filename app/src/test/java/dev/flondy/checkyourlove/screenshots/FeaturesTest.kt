package dev.flondy.checkyourlove.screenshots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
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

/** Neue Bereiche: Freundschaft, Gruppe, Quiz, Ideen – Ablauf + Screenshots. */
abstract class FeaturesFlow(private val prefix: String) {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun friendship() {
        idle(1)
        compose.onNodeWithText("Freundschaft").performClick()
        compose.onNodeWithText("Wie stark ist eure Freundschaft?").assertIsDisplayed()
        compose.onNodeWithText("Dein Name").performTextInput("Lena")
        compose.onNodeWithText("Name deines Lieblingsmenschen").performTextInput("Mia")
        compose.onNodeWithContentDescription("Liebe prüfen").performClick()
        idle(4)
        shot("friend_result")
    }

    @Test fun group() {
        idle(1)
        compose.onNodeWithText("Gruppe").performClick()
        idle(1)
        shot("group_input")
        compose.onNodeWithText("Name 1").performTextInput("Anna")
        compose.onNodeWithText("Name 2").performTextInput("Ben")
        compose.onNodeWithText("Name 3").performTextInput("Lena")
        compose.onNodeWithText("Name hinzufügen").performScrollTo().performClick()
        idle(1)
        compose.onNodeWithText("Name 4").performScrollTo().performTextInput("Tom")
        compose.onNodeWithText("Alle Paare testen").performScrollTo().performClick()
        idle(3)
        compose.onNode(hasContentDescription("Platz 1:", substring = true)).assertIsDisplayed()
        shot("group_results")
    }

    @Test fun quiz() {
        idle(1)
        compose.onNodeWithText("Quiz").performClick()
        idle(1)
        shot("quiz_setup")
        compose.onNodeWithText("Erste Person").performTextInput("Anna")
        compose.onNodeWithText("Zweite Person").performTextInput("Ben")
        compose.onNodeWithText("Quiz starten").performScrollTo().performClick()
        idle(1)
        shot("quiz_question")
        repeat(10) { round ->
            compose.onNodeWithTag("quiz_option_1").performScrollTo().performClick()
            idle(1)
            if (round == 0) shot("quiz_handover")
            compose.onNodeWithText("Bereit!").performScrollTo().performClick()
            idle(1)
            compose.onNodeWithTag("quiz_option_${if (round % 3 == 0) 2 else 1}").performScrollTo().performClick()
            idle(1)
            if (round == 0) shot("quiz_reveal")
            val next = if (round == 9) "Ergebnis zeigen" else "Weiter"
            compose.onNodeWithText(next).performScrollTo().performClick()
            idle(1)
        }
        idle(2)
        compose.onNode(hasContentDescription("Quiz beenden")).assertDoesNotExistSafe()
        shot("quiz_result")
    }

    @Test fun ideas() {
        idle(1)
        compose.onNodeWithText("Ideen").performClick()
        idle(1)
        shot("ideas_dates")
        compose.onNodeWithText("Flirt-Sprüche").performClick()
        idle(1)
        compose.onNodeWithText("Nächste Idee").performScrollTo().performClick()
        idle(1)
        shot("ideas_flirt")
        compose.onNodeWithTag("ideas_categories").performScrollToNode(androidx.compose.ui.test.hasText("Pärchen-Challenges"))
        compose.onNodeWithText("Pärchen-Challenges").performClick()
        idle(1)
        shot("ideas_locked")
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertDoesNotExistSafe() =
        runCatching { assertDoesNotExist() }

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
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-w411dp-h891dp-night-xxhdpi")
class PhoneFeaturesTest : FeaturesFlow("feat_phone")

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-w1280dp-h800dp-land-night-hdpi")
class TabletFeaturesTest : FeaturesFlow("feat_tablet")
