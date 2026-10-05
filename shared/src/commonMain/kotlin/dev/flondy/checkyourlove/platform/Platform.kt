package dev.flondy.checkyourlove.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/** Unicode-Normalisierung NFKC (Android: java.text.Normalizer, iOS: NSString). */
expect fun normalizeNfkc(text: String): String

/** Prüft, ob ein Unicode-Codepunkt ein Buchstabe ist (beliebige Schrift). */
expect fun isLetterCodePoint(codePoint: Int): Boolean

/** Datum mittel + Uhrzeit kurz, in der Sprache des Geräts. */
expect fun formatDateTime(epochMillis: Long): String

/** Datum kurz + Uhrzeit kurz, in der Sprache des Geräts. */
expect fun formatShortDateTime(epochMillis: Long): String

expect fun currentTimeMillis(): Long

/** Systemeinstellung „Bewegung reduzieren“ bzw. deaktivierte Animationen. */
@Composable
expect fun systemReducedMotion(): Boolean

/** PNG-Kodierung für die Teilen-Karte. */
expect fun ImageBitmap.encodeToPng(): ByteArray

/** true, solange die Bildschirmtastatur sichtbar ist. */
@Composable
expect fun isImeVisible(): Boolean
