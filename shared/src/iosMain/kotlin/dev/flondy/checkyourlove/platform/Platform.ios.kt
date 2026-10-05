package dev.flondy.checkyourlove.platform

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.precomposedStringWithCompatibilityMapping
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

@OptIn(BetaInteropApi::class)
actual fun normalizeNfkc(text: String): String =
    NSString.create(string = text).precomposedStringWithCompatibilityMapping

actual fun isLetterCodePoint(codePoint: Int): Boolean =
    NSCharacterSet.letterCharacterSet.longCharacterIsMember(codePoint.toUInt())

private fun format(epochMillis: Long, dateStyle: ULong): String {
    val formatter = NSDateFormatter()
    formatter.dateStyle = dateStyle
    formatter.timeStyle = NSDateFormatterShortStyle
    return formatter.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0))
}

actual fun formatDateTime(epochMillis: Long): String = format(epochMillis, NSDateFormatterMediumStyle)

actual fun formatShortDateTime(epochMillis: Long): String = format(epochMillis, NSDateFormatterShortStyle)

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

@Composable
actual fun systemReducedMotion(): Boolean = remember { UIAccessibilityIsReduceMotionEnabled() }

@OptIn(ExperimentalForeignApi::class)
actual fun ImageBitmap.encodeToPng(): ByteArray {
    val image = Image.makeFromBitmap(asSkiaBitmap())
    val data = image.encodeToData(EncodedImageFormat.PNG) ?: error("PNG-Kodierung fehlgeschlagen")
    return data.bytes
}

@Composable
actual fun isImeVisible(): Boolean = WindowInsets.ime.getBottom(LocalDensity.current) > 0
