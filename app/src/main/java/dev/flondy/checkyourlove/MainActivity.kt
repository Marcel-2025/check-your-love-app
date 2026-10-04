package dev.flondy.checkyourlove

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.flondy.checkyourlove.ui.CheckYourLoveRoot

/** Einzige Activity: Navigation erfolgt vollständig in Compose (kein setContentView-Wechsel mehr). */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { CheckYourLoveRoot(appContainer) }
    }
}
