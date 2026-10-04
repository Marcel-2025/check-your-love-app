package dev.flondy.checkyourlove.monetization

import androidx.annotation.StringRes
import dev.flondy.checkyourlove.R

/** Art eines (ggf. freischaltbaren) Gestaltungselements. */
enum class ItemKind(@StringRes val labelRes: Int) {
    PALETTE(R.string.reward_kind_palette),
    EFFECT(R.string.reward_kind_effect),
    TEMPLATE(R.string.reward_kind_template),
}

/** Gemeinsame Sicht auf Designs, Effekte und Vorlagen für Premium/Rewarded-Logik. */
interface CatalogItem {
    val id: String
    val kind: ItemKind
    val premium: Boolean
    @get:StringRes val labelRes: Int
    val key: String get() = "${kind.name.lowercase()}:$id"
}

enum class ResultEffect(
    override val id: String,
    override val labelRes: Int,
    override val premium: Boolean,
) : CatalogItem {
    NONE("none", R.string.effect_none, false),
    CONFETTI("confetti", R.string.effect_confetti, false),
    HEART_RAIN("heart_rain", R.string.effect_heart_rain, true),
    SPARKLE("sparkle", R.string.effect_sparkle, true);

    override val kind: ItemKind get() = ItemKind.EFFECT

    companion object {
        fun byId(id: String): ResultEffect = entries.firstOrNull { it.id == id } ?: CONFETTI
    }
}

enum class ShareTemplate(
    override val id: String,
    override val labelRes: Int,
    override val premium: Boolean,
) : CatalogItem {
    CLASSIC("classic", R.string.template_classic, false),
    POLAROID("polaroid", R.string.template_polaroid, true),
    NEON("neon", R.string.template_neon, true),
    MINIMAL("minimal", R.string.template_minimal, true);

    override val kind: ItemKind get() = ItemKind.TEMPLATE

    companion object {
        fun byId(id: String): ShareTemplate = entries.firstOrNull { it.id == id } ?: CLASSIC
    }
}
