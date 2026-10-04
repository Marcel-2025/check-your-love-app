package dev.flondy.checkyourlove.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.flondy.checkyourlove.R
import dev.flondy.checkyourlove.core.ResultTexts
import dev.flondy.checkyourlove.ui.components.SoftIconButton
import dev.flondy.checkyourlove.ui.test.FavoriteButton
import dev.flondy.checkyourlove.ui.test.PercentRing
import dev.flondy.checkyourlove.ui.test.ResultUi
import dev.flondy.checkyourlove.ui.theme.LocalHaptics
import dev.flondy.checkyourlove.ui.theme.LocalPalette

/** Detailansicht eines gespeicherten Ergebnisses. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultDetailSheet(
    result: ResultUi,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalPalette.current
    val haptics = LocalHaptics.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.result_pair, result.nameA, result.nameB),
                style = MaterialTheme.typography.titleLarge,
                color = palette.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            PercentRing(value = result.percent.toFloat(), target = result.percent, ringSize = 180.dp)
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(ResultTexts.title(result.range)),
                style = MaterialTheme.typography.headlineSmall,
                color = palette.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(ResultTexts.text(result.range, result.variant)),
                style = MaterialTheme.typography.bodyLarge,
                color = palette.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                FavoriteButton(favorite = result.favorite, enabled = result.id != null, onClick = { haptics.toggle(); onToggleFavorite() })
                Spacer(Modifier.size(16.dp))
                SoftIconButton(
                    icon = Icons.Rounded.Share,
                    contentDescription = stringResource(R.string.action_share),
                    onClick = onShare,
                    tint = palette.primary,
                    size = 56.dp,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.entertainment_note),
                style = MaterialTheme.typography.bodySmall,
                color = palette.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
