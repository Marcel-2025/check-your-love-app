package dev.flondy.checkyourlove.monetization

import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.*
import org.jetbrains.compose.resources.StringResource

/** Art eines (ggf. freischaltbaren) Gestaltungselements. */
enum class ItemKind(val labelRes: StringResource) {
    PALETTE(Res.string.reward_kind_palette),
    EFFECT(Res.string.reward_kind_effect),
    TEMPLATE(Res.string.reward_kind_template),
}

/** Gemeinsame Sicht auf Designs, Effekte und Vorlagen für Premium/Rewarded-Logik. */
interface CatalogItem {
    val id: String
    val kind: ItemKind
    val premium: Boolean
    val labelRes: StringResource
    val key: String get() = "${kind.name.lowercase()}:$id"
}

enum class ResultEffect(
    override val id: String,
    override val labelRes: StringResource,
    override val premium: Boolean,
) : CatalogItem {
    NONE("none", Res.string.effect_none, false),
    CONFETTI("confetti", Res.string.effect_confetti, false),
    HEART_RAIN("heart_rain", Res.string.effect_heart_rain, true),
    SPARKLE("sparkle", Res.string.effect_sparkle, true);

    override val kind: ItemKind get() = ItemKind.EFFECT

    companion object {
        fun byId(id: String): ResultEffect = entries.firstOrNull { it.id == id } ?: CONFETTI
    }
}

enum class ShareTemplate(
    override val id: String,
    override val labelRes: StringResource,
    override val premium: Boolean,
) : CatalogItem {
    CLASSIC("classic", Res.string.template_classic, false),
    POLAROID("polaroid", Res.string.template_polaroid, true),
    NEON("neon", Res.string.template_neon, true),
    MINIMAL("minimal", Res.string.template_minimal, true);

    override val kind: ItemKind get() = ItemKind.TEMPLATE

    companion object {
        fun byId(id: String): ShareTemplate = entries.firstOrNull { it.id == id } ?: CLASSIC
    }
}
