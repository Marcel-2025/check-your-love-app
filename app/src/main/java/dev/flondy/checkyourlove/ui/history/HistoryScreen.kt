package dev.flondy.checkyourlove.ui.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.HistoryToggleOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.flondy.checkyourlove.R
import dev.flondy.checkyourlove.core.ResultRange
import dev.flondy.checkyourlove.core.ResultTexts
import dev.flondy.checkyourlove.data.db.LoveResultEntity
import dev.flondy.checkyourlove.ui.components.GradientButton
import dev.flondy.checkyourlove.ui.components.drawHeart
import dev.flondy.checkyourlove.ui.theme.LocalHaptics
import dev.flondy.checkyourlove.ui.theme.LocalPalette
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    contentPadding: PaddingValues,
    onFilter: (HistoryFilter) -> Unit,
    onOpen: (LoveResultEntity) -> Unit,
    onToggleFavorite: (LoveResultEntity) -> Unit,
    onDelete: (LoveResultEntity) -> Unit,
    onClear: (keepFavorites: Boolean) -> Unit,
    onStartTest: () -> Unit,
) {
    val palette = LocalPalette.current
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(start = 20.dp, end = 8.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.history_title),
                style = MaterialTheme.typography.headlineMedium,
                color = palette.onBackground,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            if (state.items.isNotEmpty() || state.filter == HistoryFilter.FAVORITES) {
                IconButton(onClick = { confirmClear = true }) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = stringResource(R.string.history_clear), tint = palette.onSurfaceVariant)
                }
            }
        }
        SingleChoiceSegmentedButtonRow(
            Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            HistoryFilter.entries.forEachIndexed { index, filter ->
                SegmentedButton(
                    selected = state.filter == filter,
                    onClick = { onFilter(filter) },
                    shape = SegmentedButtonDefaults.itemShape(index, HistoryFilter.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = palette.primaryContainer,
                        activeContentColor = palette.onPrimaryContainer,
                        inactiveContainerColor = Color.Transparent,
                    ),
                ) {
                    Text(stringResource(if (filter == HistoryFilter.ALL) R.string.history_filter_all else R.string.history_filter_favorites))
                }
            }
        }
        AnimatedContent(
            targetState = state.items.isEmpty() && !state.loading,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "historyEmpty",
            modifier = Modifier.weight(1f),
        ) { empty ->
            if (empty) {
                EmptyHistory(favorites = state.filter == HistoryFilter.FAVORITES, onStartTest = onStartTest)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(state.items, key = { it.id }) { item ->
                        HistoryItem(
                            item = item,
                            onOpen = { onOpen(item) },
                            onToggleFavorite = { onToggleFavorite(item) },
                            onDelete = { onDelete(item) },
                            modifier = Modifier.animateItem().widthIn(max = 640.dp),
                        )
                    }
                }
            }
        }
    }

    if (confirmClear) {
        var keepFavorites by remember { mutableStateOf(true) }
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            icon = { Icon(Icons.Rounded.DeleteSweep, contentDescription = null) },
            title = { Text(stringResource(R.string.history_clear_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.history_clear_text))
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .toggleable(keepFavorites, role = Role.Checkbox) { keepFavorites = it },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = keepFavorites, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.history_clear_keep_favorites))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; onClear(keepFavorites) }) {
                    Text(stringResource(R.string.history_clear_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
private fun HistoryItem(
    item: LoveResultEntity,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    val haptics = LocalHaptics.current
    val range = ResultRange.of(item.percent)
    val date = remember(item.createdAt) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(item.createdAt))
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = palette.surface.copy(alpha = if (palette.isDark) 0.86f else 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.outline.copy(alpha = 0.25f)),
        shadowElevation = if (palette.isDark) 0.dp else 1.dp,
    ) {
        Row(
            Modifier
                .clickable(onClick = onOpen)
                .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(58.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawHeart(Brush.linearGradient(listOf(palette.heartStart, palette.heartMid, palette.heartEnd)), fraction = 0.98f)
                }
                Text(
                    "${item.percent}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.result_pair, item.nameA, item.nameB),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(ResultTexts.title(range)) + " · " + stringResource(R.string.result_percent, item.percent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(date, style = MaterialTheme.typography.bodySmall, color = palette.onSurfaceVariant)
            }
            IconButton(onClick = { haptics.toggle(); onToggleFavorite() }) {
                Icon(
                    if (item.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = stringResource(if (item.favorite) R.string.action_favorite_remove else R.string.action_favorite_add),
                    tint = if (item.favorite) palette.heartMid else palette.onSurfaceVariant,
                )
            }
            IconButton(onClick = { haptics.tick(); onDelete() }) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = stringResource(R.string.history_delete_item), tint = palette.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyHistory(favorites: Boolean, onStartTest: () -> Unit) {
    val palette = LocalPalette.current
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawHeart(Brush.linearGradient(listOf(palette.heartStart, palette.heartEnd)), fraction = 0.9f, alpha = 0.25f)
            }
            Icon(
                if (favorites) Icons.Rounded.FavoriteBorder else Icons.Rounded.HistoryToggleOff,
                contentDescription = null,
                tint = palette.primary,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(if (favorites) R.string.history_empty_favorites_title else R.string.history_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = palette.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(if (favorites) R.string.history_empty_favorites_text else R.string.history_empty_text),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (!favorites) {
            Spacer(Modifier.height(20.dp))
            GradientButton(text = stringResource(R.string.history_start_test), onClick = onStartTest)
        }
    }
}
