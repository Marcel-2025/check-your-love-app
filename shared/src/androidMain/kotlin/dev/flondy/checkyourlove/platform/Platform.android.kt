package dev.flondy.checkyourlove.platform

import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.text.Normalizer
import java.util.Date

actual fun normalizeNfkc(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFKC)

actual fun isLetterCodePoint(codePoint: Int): Boolean = Character.isLetter(codePoint)

actual fun formatDateTime(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))

actual fun formatShortDateTime(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(epochMillis))

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

@Composable
actual fun systemReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

actual fun ImageBitmap.encodeToPng(): ByteArray {
    val out = ByteArrayOutputStream()
    asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
    return out.toByteArray()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
actual fun isImeVisible(): Boolean = WindowInsets.isImeVisible
