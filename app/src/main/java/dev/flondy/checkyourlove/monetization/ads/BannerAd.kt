package dev.flondy.checkyourlove.monetization.ads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import dev.flondy.checkyourlove.R
import dev.flondy.checkyourlove.ui.theme.LocalPalette

/**
 * Dezentes, verankertes adaptives Banner. Es sitzt in einem eigenen Bereich oberhalb der
 * Navigation und überdeckt nie Eingaben oder Ergebnisse. Bei Ladefehlern verschwindet es.
 */
@Composable
fun BannerAd(adUnitId: String, ads: AdsManager, modifier: Modifier = Modifier) {
    if (adUnitId.isBlank()) return
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var failed by remember(adUnitId) { mutableStateOf(false) }
    AnimatedVisibility(visible = !failed, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically(), modifier = modifier) {
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val widthDp = maxWidth.value.toInt().coerceAtLeast(320)
            val screenHeightDp = LocalConfiguration.current.screenHeightDp
            // Kleine Displays: kompaktes Standard-Banner, damit Inhalte genug Platz behalten.
            @Suppress("DEPRECATION")
            val adSize = remember(widthDp, screenHeightDp) {
                if (screenHeightDp >= 720) AdSize.getLargeAnchoredAdaptiveBannerAdSize(context, widthDp)
                else AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
            }
            val heightDp = with(LocalDensity.current) { adSize.getHeightInPixels(context).toDp() }
            val adView = remember(adUnitId, widthDp) {
                AdView(context).apply {
                    setAdSize(adSize)
                    this.adUnitId = adUnitId
                    adListener = object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) { failed = true }
                    }
                    loadAd(ads.newRequest())
                }
            }
            DisposableEffect(adView, lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> adView.resume()
                        Lifecycle.Event.ON_PAUSE -> adView.pause()
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    adView.destroy()
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.ad_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalPalette.current.onSurfaceVariant,
                )
                Box(Modifier.fillMaxWidth().height(heightDp), contentAlignment = Alignment.Center) {
                    AndroidView(factory = { adView })
                }
            }
        }
    }
}
