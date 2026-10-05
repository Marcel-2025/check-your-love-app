package dev.flondy.checkyourlove.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.flondy.checkyourlove.core.ResultRange
import dev.flondy.checkyourlove.data.HistoryRepository
import dev.flondy.checkyourlove.data.db.LoveResultEntity
import dev.flondy.checkyourlove.ui.test.ResultUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HistoryFilter { ALL, FAVORITES }

data class HistoryUiState(
    val filter: HistoryFilter = HistoryFilter.ALL,
    val items: List<LoveResultEntity> = emptyList(),
    val loading: Boolean = true,
)

fun LoveResultEntity.toResultUi() = ResultUi(
    id = id, nameA = nameA, nameB = nameB, percent = percent,
    range = ResultRange.of(percent), variant = variant, favorite = favorite,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(private val history: HistoryRepository) : ViewModel() {
    private val filter = MutableStateFlow(HistoryFilter.ALL)

    val state: StateFlow<HistoryUiState> = filter.flatMapLatest { f ->
        val source = if (f == HistoryFilter.ALL) history.observeAll() else history.observeFavorites()
        source.map { HistoryUiState(filter = f, items = it, loading = false) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun setFilter(value: HistoryFilter) { filter.value = value }

    fun toggleFavorite(item: LoveResultEntity) {
        viewModelScope.launch { history.setFavorite(item.id, !item.favorite) }
    }

    fun delete(item: LoveResultEntity) {
        viewModelScope.launch { history.delete(item.id) }
    }

    fun undoDelete(item: LoveResultEntity) {
        viewModelScope.launch { history.restore(item) }
    }

    fun clear(keepFavorites: Boolean) {
        viewModelScope.launch { history.clear(keepFavorites) }
    }
}
