package dev.flondy.checkyourlove.ui.theme

import androidx.compose.ui.graphics.Color
import dev.flondy.checkyourlove.R
import dev.flondy.checkyourlove.monetization.CatalogItem
import dev.flondy.checkyourlove.monetization.ItemKind

/** Farbwerte eines Designs für einen Modus (hell oder dunkel). */
data class PaletteColors(
    val background: Color,
    val backgroundAlt: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val onBackground: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val tertiary: Color,
    val heartStart: Color,
    val heartMid: Color,
    val heartEnd: Color,
    val glow: Color,
    val error: Color,
    val isDark: Boolean,
)

enum class AppPalette(
    override val id: String,
    override val labelRes: Int,
    override val premium: Boolean,
    val dark: PaletteColors,
    val light: PaletteColors,
) : CatalogItem {
    AUBERGINE(
        "aubergine", R.string.palette_aubergine, false,
        dark = PaletteColors(
            background = Color(0xFF170B1C), backgroundAlt = Color(0xFF2A1430),
            surface = Color(0xFF231329), surfaceHigh = Color(0xFF301B37),
            onBackground = Color(0xFFF8EAF0), onSurfaceVariant = Color(0xFFD4BCCB), outline = Color(0xFF6E5468),
            primary = Color(0xFFFF8A7A), onPrimary = Color(0xFF3B0A10),
            primaryContainer = Color(0xFF5A2135), onPrimaryContainer = Color(0xFFFFD9DE),
            secondary = Color(0xFFF4B6C6), tertiary = Color(0xFFBFA6FF),
            heartStart = Color(0xFFFF8A6B), heartMid = Color(0xFFFF5C8A), heartEnd = Color(0xFFA77BFF),
            glow = Color(0xFFFF6F91), error = Color(0xFFFFB4AB), isDark = true,
        ),
        light = PaletteColors(
            background = Color(0xFFFFF6F3), backgroundAlt = Color(0xFFF7E6F0),
            surface = Color(0xFFFFFFFF), surfaceHigh = Color(0xFFFBEFF3),
            onBackground = Color(0xFF2A1430), onSurfaceVariant = Color(0xFF6A5263), outline = Color(0xFFB79FB0),
            primary = Color(0xFFC2364A), onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDDE2), onPrimaryContainer = Color(0xFF450A1A),
            secondary = Color(0xFF9C4F69), tertiary = Color(0xFF6B4FC4),
            heartStart = Color(0xFFFF7A5C), heartMid = Color(0xFFF2457A), heartEnd = Color(0xFF8E62F0),
            glow = Color(0xFFFF7A9A), error = Color(0xFFBA1A1A), isDark = false,
        ),
    ),
    MIDNIGHT(
        "midnight", R.string.palette_midnight, false,
        dark = PaletteColors(
            background = Color(0xFF0B1022), backgroundAlt = Color(0xFF16203F),
            surface = Color(0xFF131A33), surfaceHigh = Color(0xFF1C2647),
            onBackground = Color(0xFFEAF0FF), onSurfaceVariant = Color(0xFFB9C3E0), outline = Color(0xFF4F5B80),
            primary = Color(0xFFFF8E80), onPrimary = Color(0xFF3A0C0A),
            primaryContainer = Color(0xFF3B2A52), onPrimaryContainer = Color(0xFFFFDAD4),
            secondary = Color(0xFFF7B8C8), tertiary = Color(0xFF9FB4FF),
            heartStart = Color(0xFFFF9A7A), heartMid = Color(0xFFFF6A8E), heartEnd = Color(0xFF8C9BFF),
            glow = Color(0xFF8C9BFF), error = Color(0xFFFFB4AB), isDark = true,
        ),
        light = PaletteColors(
            background = Color(0xFFF5F7FF), backgroundAlt = Color(0xFFE6EBFA),
            surface = Color(0xFFFFFFFF), surfaceHigh = Color(0xFFEEF1FB),
            onBackground = Color(0xFF141B36), onSurfaceVariant = Color(0xFF4F5876), outline = Color(0xFFA2AAC4),
            primary = Color(0xFFB8384A), onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDDE0), onPrimaryContainer = Color(0xFF40081A),
            secondary = Color(0xFF8E4F67), tertiary = Color(0xFF3F55C4),
            heartStart = Color(0xFFFF7E63), heartMid = Color(0xFFEE4C7C), heartEnd = Color(0xFF5B6CF0),
            glow = Color(0xFF7F8CFF), error = Color(0xFFBA1A1A), isDark = false,
        ),
    ),
    ROSE_GOLD(
        "rose_gold", R.string.palette_rose_gold, true,
        dark = PaletteColors(
            background = Color(0xFF1C1214), backgroundAlt = Color(0xFF33201F),
            surface = Color(0xFF261A1B), surfaceHigh = Color(0xFF342425),
            onBackground = Color(0xFFFBEDE7), onSurfaceVariant = Color(0xFFDCC2B8), outline = Color(0xFF75605A),
            primary = Color(0xFFEDB9A0), onPrimary = Color(0xFF3E1E10),
            primaryContainer = Color(0xFF5B3A2E), onPrimaryContainer = Color(0xFFFFDCCB),
            secondary = Color(0xFFF2C4C4), tertiary = Color(0xFFE8CF9A),
            heartStart = Color(0xFFF6CDB0), heartMid = Color(0xFFE59A8C), heartEnd = Color(0xFFC77D8F),
            glow = Color(0xFFE8B49A), error = Color(0xFFFFB4AB), isDark = true,
        ),
        light = PaletteColors(
            background = Color(0xFFFFF7F2), backgroundAlt = Color(0xFFF6E4DB),
            surface = Color(0xFFFFFFFF), surfaceHigh = Color(0xFFFAEEE8),
            onBackground = Color(0xFF2E1D1A), onSurfaceVariant = Color(0xFF6B5650), outline = Color(0xFFBCA59C),
            primary = Color(0xFF9A4F3A), onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDBCD), onPrimaryContainer = Color(0xFF3A0F02),
            secondary = Color(0xFF8D5059), tertiary = Color(0xFF7C5E1E),
            heartStart = Color(0xFFF0B496), heartMid = Color(0xFFD9806F), heartEnd = Color(0xFFB2617A),
            glow = Color(0xFFE9A88E), error = Color(0xFFBA1A1A), isDark = false,
        ),
    ),
    AURORA(
        "aurora", R.string.palette_aurora, true,
        dark = PaletteColors(
            background = Color(0xFF071A1F), backgroundAlt = Color(0xFF16233F),
            surface = Color(0xFF0E2329), surfaceHigh = Color(0xFF173039),
            onBackground = Color(0xFFE5FBF5), onSurfaceVariant = Color(0xFFB2D1CB), outline = Color(0xFF47686A),
            primary = Color(0xFF7EF0C8), onPrimary = Color(0xFF00382A),
            primaryContainer = Color(0xFF1F4A55), onPrimaryContainer = Color(0xFFB8FFE6),
            secondary = Color(0xFFA5E4FF), tertiary = Color(0xFFC9A2FF),
            heartStart = Color(0xFF7EF0C8), heartMid = Color(0xFF6CC6FF), heartEnd = Color(0xFFC08CFF),
            glow = Color(0xFF6CE3D0), error = Color(0xFFFFB4AB), isDark = true,
        ),
        light = PaletteColors(
            background = Color(0xFFF1FBF8), backgroundAlt = Color(0xFFE5ECFA),
            surface = Color(0xFFFFFFFF), surfaceHigh = Color(0xFFE9F5F2),
            onBackground = Color(0xFF0D2A2A), onSurfaceVariant = Color(0xFF465F5D), outline = Color(0xFF97B0AD),
            primary = Color(0xFF006B55), onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFB4F2DC), onPrimaryContainer = Color(0xFF002118),
            secondary = Color(0xFF005F86), tertiary = Color(0xFF6C46B8),
            heartStart = Color(0xFF2FC9A0), heartMid = Color(0xFF3B98E0), heartEnd = Color(0xFF9B6AF0),
            glow = Color(0xFF4FD3BC), error = Color(0xFFBA1A1A), isDark = false,
        ),
    ),
    BLOSSOM(
        "blossom", R.string.palette_blossom, true,
        dark = PaletteColors(
            background = Color(0xFF221019), backgroundAlt = Color(0xFF3A1A2B),
            surface = Color(0xFF2D1622), surfaceHigh = Color(0xFF3B1F2E),
            onBackground = Color(0xFFFFEAF2), onSurfaceVariant = Color(0xFFE6BCD0), outline = Color(0xFF7E5469),
            primary = Color(0xFFFF9EC2), onPrimary = Color(0xFF4A0626),
            primaryContainer = Color(0xFF63203F), onPrimaryContainer = Color(0xFFFFD9E6),
            secondary = Color(0xFFFFC2D6), tertiary = Color(0xFFFFD3A6),
            heartStart = Color(0xFFFFC2D6), heartMid = Color(0xFFFF7FAF), heartEnd = Color(0xFFE5579A),
            glow = Color(0xFFFF8FB8), error = Color(0xFFFFB4AB), isDark = true,
        ),
        light = PaletteColors(
            background = Color(0xFFFFF5F8), backgroundAlt = Color(0xFFFDE3EC),
            surface = Color(0xFFFFFFFF), surfaceHigh = Color(0xFFFFEDF3),
            onBackground = Color(0xFF3A1226), onSurfaceVariant = Color(0xFF75506A), outline = Color(0xFFCBA0B6),
            primary = Color(0xFFB0306A), onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFD9E6), onPrimaryContainer = Color(0xFF3E0021),
            secondary = Color(0xFF9A4566), tertiary = Color(0xFF8C5414),
            heartStart = Color(0xFFFFA3C4), heartMid = Color(0xFFF2669B), heartEnd = Color(0xFFCC3D80),
            glow = Color(0xFFFF8FB8), error = Color(0xFFBA1A1A), isDark = false,
        ),
    ),
    VELVET(
        "velvet", R.string.palette_velvet, true,
        dark = PaletteColors(
            background = Color(0xFF16060B), backgroundAlt = Color(0xFF32101C),
            surface = Color(0xFF220C13), surfaceHigh = Color(0xFF30131D),
            onBackground = Color(0xFFFFE9EC), onSurfaceVariant = Color(0xFFE2B7BF), outline = Color(0xFF7A4752),
            primary = Color(0xFFFF6B81), onPrimary = Color(0xFF4F0016),
            primaryContainer = Color(0xFF6B1A2C), onPrimaryContainer = Color(0xFFFFD9DD),
            secondary = Color(0xFFFFB3BE), tertiary = Color(0xFFFFCB6B),
            heartStart = Color(0xFFFF6B6B), heartMid = Color(0xFFE5304F), heartEnd = Color(0xFF9E1440),
            glow = Color(0xFFFF4D6A), error = Color(0xFFFFB4AB), isDark = true,
        ),
        light = PaletteColors(
            background = Color(0xFFFFF4F4), backgroundAlt = Color(0xFFF8DFE3),
            surface = Color(0xFFFFFFFF), surfaceHigh = Color(0xFFFCEBEE),
            onBackground = Color(0xFF33070F), onSurfaceVariant = Color(0xFF734850), outline = Color(0xFFC79CA4),
            primary = Color(0xFFA8122F), onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFFFDADD), onPrimaryContainer = Color(0xFF40000D),
            secondary = Color(0xFF8C3D4B), tertiary = Color(0xFF7A5900),
            heartStart = Color(0xFFFF5C63), heartMid = Color(0xFFD9203F), heartEnd = Color(0xFF8E0E38),
            glow = Color(0xFFFF4D6A), error = Color(0xFFBA1A1A), isDark = false,
        ),
    );

    override val kind: ItemKind get() = ItemKind.PALETTE

    fun colors(dark: Boolean): PaletteColors = if (dark) this.dark else light

    companion object {
        fun byId(id: String): AppPalette = entries.firstOrNull { it.id == id } ?: AUBERGINE
    }
}
