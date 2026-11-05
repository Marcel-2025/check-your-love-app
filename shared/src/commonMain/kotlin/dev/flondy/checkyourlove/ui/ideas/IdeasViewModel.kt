package dev.flondy.checkyourlove.ui.ideas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.flondy.checkyourlove.content.Content
import dev.flondy.checkyourlove.content.Idea
import dev.flondy.checkyourlove.content.IdeaCategory
import dev.flondy.checkyourlove.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class IdeasUiState(
    val category: IdeaCategory = IdeaCategory.DATES,
    val showFavorites: Boolean = false,
    /** Aktuelle Liste (Kategorie in gemischter Reihenfolge oder gemerkte Ideen). */
    val ideas: List<Idea> = emptyList(),
    val position: Int = 0,
    val favorites: Set<String> = emptySet(),
    /** Richtung der letzten Navigation für die Kartenanimation (1 = vor, -1 = zurück). */
    val direction: Int = 1,
) {
    val current: Idea? get() = ideas.getOrNull(position)
}

class IdeasViewModel(
    private val settings: SettingsRepository,
    private val german: Boolean = Content.isGerman(),
    random: Random = Random.Default,
) : ViewModel() {
    private val seed = random.nextInt()
    private val selection = MutableStateFlow(Selection(IdeaCategory.DATES, false, 0, 1))

    private data class Selection(val category: IdeaCategory, val favorites: Boolean, val position: Int, val direction: Int)

    val state: StateFlow<IdeasUiState> = combine(selection, settings.ideaFavorites) { sel, favs ->
        val list = if (sel.favorites) {
            favs.sorted().mapNotNull { Content.idea(it, german) }
        } else {
            // Pro Kategorie eine feste, gemischte Reihenfolge (pro App-Start neu)
            Content.ideas(sel.category, german).shuffled(Random(seed + sel.category.ordinal))
        }
        IdeasUiState(sel.category, sel.favorites, list, sel.position.coerceIn(0, (list.size - 1).coerceAtLeast(0)), favs, sel.direction)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, IdeasUiState())

    fun selectCategory(category: IdeaCategory) {
        selection.value = Selection(category, false, 0, 1)
    }

    fun showFavorites() {
        selection.value = selection.value.copy(favorites = true, position = 0, direction = 1)
    }

    fun next() {
        val size = state.value.ideas.size
        if (size == 0) return
        val s = selection.value
        selection.value = s.copy(position = (state.value.position + 1) % size, direction = 1)
    }

    fun previous() {
        val size = state.value.ideas.size
        if (size == 0) return
        val s = selection.value
        selection.value = s.copy(position = (state.value.position - 1 + size) % size, direction = -1)
    }

    fun toggleFavorite(idea: Idea) {
        viewModelScope.launch { settings.toggleIdeaFavorite(idea.id) }
    }
}
