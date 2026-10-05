package dev.flondy.checkyourlove.screenshots

import android.content.Context
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import dev.flondy.checkyourlove.MainActivity
import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class ScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private fun idle(ms: Long = 1000) {
        repeat((ms / 100).toInt()) {
            Thread.sleep(15)
            ShadowLooper.idleMainLooper(100, TimeUnit.MILLISECONDS)
            compose.mainClock.advanceTimeBy(100)
        }
        compose.waitForIdle()
    }

    private fun shot(name: String) = compose.onNode(isRoot()).captureRoboImage("build/screenshots/$name.png")
    private fun str(id: StringResource, vararg args: Any): String = runBlocking { getString(id, *args) }

    private fun runTest(a: String, b: String) {
        compose.onNodeWithText(str(Res.string.field_name_a)).performTextInput(a)
        compose.onNodeWithText(str(Res.string.field_name_b)).performTextInput(b)
        compose.onNodeWithContentDescription(str(Res.string.action_check)).performScrollTo().performClick()
        idle(4000)
    }

    @Test @Config(qualifiers = "en-w411dp-h891dp-notnight-xxhdpi")
    fun lightEnglish() {
        idle()
        shot("light_01_input_en")
        runTest("Lena", "Tom")
        shot("light_02_result_en")
        compose.onNodeWithText(str(Res.string.nav_settings)).performClick(); idle()
        shot("light_03_settings_en")
        compose.onNodeWithText(str(Res.string.nav_history)).performClick(); idle()
        shot("light_04_history_en")
    }

    @Test @Config(qualifiers = "de-w411dp-h891dp-night-xxhdpi")
    fun premiumAndShare() {
        idle()
        compose.onNodeWithContentDescription(str(Res.string.premium_open)).performClick(); idle()
        shot("premium_01_top")
        compose.onNodeWithText(str(Res.string.sim_banner)).performScrollTo(); idle()
        shot("premium_02_bottom")
        compose.onNodeWithContentDescription(str(Res.string.nav_back)).performScrollTo().performClick(); idle()
        shot("premium_03_after_back")
        runTest("Mia", "Noah")
        compose.onNodeWithContentDescription(str(Res.string.action_share)).performScrollTo().performClick(); idle(2000)
        captureScreenRoboImage("build/screenshots/share_sheet.png")
    }

    @Test @Config(qualifiers = "de-w320dp-h640dp-night-mdpi", fontScale = 1.5f)
    fun smallScreenLargeFont() {
        idle()
        shot("small_01_input_largefont")
        runTest("Maximiliane-Charlotte", "Bartholomäus")
        shot("small_02_result_largefont")
        compose.onNodeWithText(str(Res.string.action_new_test)).performScrollTo()
        idle(300)
        shot("small_03_result_scrolled_largefont")
    }

    @Test @Config(qualifiers = "de-w411dp-h891dp-night-xxhdpi")
    fun lockedItemDialogAndSimulatedPurchase() {
        idle()
        compose.onNodeWithText(str(Res.string.nav_settings)).performClick(); idle()
        compose.onNodeWithText(str(Res.string.effect_heart_rain)).performScrollTo().performClick(); idle()
        captureScreenRoboImage("build/screenshots/locked_dialog.png")
        compose.onNodeWithText(str(Res.string.locked_go_premium)).performClick(); idle()
        // Simulation: Abbruch -> kein Premium
        compose.onNodeWithText(str(Res.string.sim_cancel)).performScrollTo().performClick()
        compose.onNodeWithText(str(Res.string.premium_buy, str(Res.string.sim_price))).performScrollTo().performClick(); idle(1500)
        compose.onNodeWithText(str(Res.string.premium_buy, str(Res.string.sim_price))).assertExists()
        idle(5000) // Snackbar ausblenden lassen
        // Simulation: ausstehend -> kein Premium, Hinweis sichtbar
        compose.onNodeWithText(str(Res.string.sim_pending)).performScrollTo().performClick()
        compose.onNodeWithText(str(Res.string.premium_buy, str(Res.string.sim_price))).performScrollTo().performClick(); idle(1500)
        compose.onNodeWithText(str(Res.string.premium_pending)).assertExists()
        shot("premium_03_pending")
        idle(5000)
        // Ausstehenden Kauf abschließen -> Premium aktiv
        compose.onNodeWithText(str(Res.string.sim_complete_pending)).performScrollTo().performClick(); idle(500)
        compose.onNodeWithText(str(Res.string.premium_owned)).performScrollTo().assertExists()
        shot("premium_04_owned")
        idle(5000)
        compose.onNodeWithContentDescription(str(Res.string.nav_back)).performScrollTo().performClick(); idle()
        // Premium-Effekt ist jetzt frei wählbar
        compose.onNodeWithText(str(Res.string.effect_heart_rain)).performScrollTo().performClick(); idle()
        shot("settings_premium_unlocked")
        // Erstattung -> Berechtigung entfällt wieder
        compose.onNodeWithText(str(Res.string.settings_premium_active)).performScrollTo().performClick(); idle()
        compose.onNodeWithText(str(Res.string.sim_refund)).performScrollTo().performClick(); idle(500)
        compose.onNodeWithText(str(Res.string.premium_buy, str(Res.string.sim_price))).performScrollTo().assertExists()
    }

    @Test @Config(qualifiers = "de-w411dp-h891dp-night-xxhdpi")
    fun rewardDialogExplainsReward() {
        idle()
        compose.onNodeWithText(str(Res.string.nav_settings)).performClick(); idle()
        compose.onNodeWithText(str(Res.string.effect_sparkle)).performScrollTo().performClick(); idle()
        compose.onNodeWithText(str(Res.string.locked_watch_video)).performClick(); idle(1500)
        compose.onNodeWithText(str(Res.string.reward_dialog_text, str(Res.string.effect_sparkle))).assertExists()
        captureScreenRoboImage("build/screenshots/reward_dialog.png")
        // Abbrechen vergibt keine Belohnung
        compose.onNodeWithText(str(Res.string.action_cancel)).performClick(); idle()
        compose.onNodeWithText(str(Res.string.effect_sparkle)).performScrollTo().performClick(); idle()
        compose.onNodeWithText(str(Res.string.locked_go_premium)).assertExists()
    }

    @Test @Config(qualifiers = "de-w411dp-h891dp-notnight-xxhdpi")
    fun empty_history_light() {
        idle()
        compose.onNodeWithText(str(Res.string.nav_history)).performClick(); idle()
        shot("light_05_history_empty_de")
    }

}
