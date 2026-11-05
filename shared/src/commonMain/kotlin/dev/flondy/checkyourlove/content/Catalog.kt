package dev.flondy.checkyourlove.content

import androidx.compose.ui.text.intl.Locale
import dev.flondy.checkyourlove.monetization.CatalogItem
import dev.flondy.checkyourlove.monetization.ItemKind
import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.*
import org.jetbrains.compose.resources.StringResource

/** Eine Quizfrage mit vier Antwortmöglichkeiten. */
data class QuizQuestion(val id: String, val text: String, val options: List<String>)

/** Fragenpakete des Paar-Quiz. Das Grundpaket ist immer frei. */
enum class QuizPack(
    override val id: String,
    override val labelRes: StringResource,
    val descriptionRes: StringResource,
    override val premium: Boolean,
) : CatalogItem {
    BASIC("basic", Res.string.quiz_pack_basic, Res.string.quiz_pack_basic_desc, false),
    ROMANCE("romance", Res.string.quiz_pack_romance, Res.string.quiz_pack_romance_desc, true),
    FUTURE("future", Res.string.quiz_pack_future, Res.string.quiz_pack_future_desc, true);

    override val kind: ItemKind get() = ItemKind.PACK

    companion object {
        fun byId(id: String?): QuizPack = entries.firstOrNull { it.id == id } ?: BASIC
    }
}

/** Kategorien im Bereich „Ideen“. Vier sind frei, zwei gehören zu Premium. */
enum class IdeaCategory(
    override val id: String,
    override val labelRes: StringResource,
    override val premium: Boolean,
) : CatalogItem {
    DATES("dates", Res.string.ideas_cat_dates, false),
    TALK("talk", Res.string.ideas_cat_talk, false),
    FLIRT("flirt", Res.string.ideas_cat_flirt, false),
    COMPLIMENTS("compliments", Res.string.ideas_cat_compliments, false),
    ROMANCE("romance", Res.string.ideas_cat_romance, true),
    CHALLENGES("challenges", Res.string.ideas_cat_challenges, true);

    override val kind: ItemKind get() = ItemKind.CATEGORY

    companion object {
        fun byId(id: String?): IdeaCategory = entries.firstOrNull { it.id == id } ?: DATES
    }
}

/** Eine Idee mit stabiler Kennung (für Favoriten). */
data class Idea(val id: String, val category: IdeaCategory, val text: String)

/** Inhalte in der Sprache des Geräts (Deutsch, sonst Englisch). */
object Content {
    fun isGerman(): Boolean = Locale.current.language == "de"

    fun quiz(pack: QuizPack, german: Boolean = isGerman()): List<QuizQuestion> {
        val raw = if (german) ContentDe.quiz.getValue(pack) else ContentEn.quiz.getValue(pack)
        return raw.mapIndexed { i, q -> QuizQuestion("${pack.id}:$i", q.first, q.second) }
    }

    fun ideas(category: IdeaCategory, german: Boolean = isGerman()): List<Idea> {
        val raw = if (german) ContentDe.ideas.getValue(category) else ContentEn.ideas.getValue(category)
        return raw.mapIndexed { i, text -> Idea("${category.id}:$i", category, text) }
    }

    fun idea(id: String, german: Boolean = isGerman()): Idea? {
        val (cat, index) = id.split(":").takeIf { it.size == 2 } ?: return null
        val category = IdeaCategory.entries.firstOrNull { it.id == cat } ?: return null
        return ideas(category, german).getOrNull(index.toIntOrNull() ?: return null)
    }
}
