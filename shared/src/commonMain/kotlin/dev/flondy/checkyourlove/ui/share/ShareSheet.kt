package dev.flondy.checkyourlove.ui.share

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.flondy.checkyourlove.resources.Res
import dev.flondy.checkyourlove.resources.*
import dev.flondy.checkyourlove.core.ResultTexts
import dev.flondy.checkyourlove.monetization.ShareTemplate
import dev.flondy.checkyourlove.ui.components.GradientButton
import dev.flondy.checkyourlove.ui.components.LockBadge
import dev.flondy.checkyourlove.ui.test.ResultUi
import dev.flondy.checkyourlove.ui.theme.LocalPalette
import dev.flondy.checkyourlove.ui.theme.PaletteColors
import dev.flondy.checkyourlove.ui.theme.bodyFamily
import dev.flondy.checkyourlove.ui.theme.displayFamily
import dev.flondy.checkyourlove.platform.ShareService
import dev.flondy.checkyourlove.platform.encodeToPng
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.rememberTextMeasurer
import kotlinx.coroutines.launch

@Composable
fun rememberShareCardData(result: ResultUi): ShareCardData {
    val title = stringResource(ResultTexts.title(result.range))
    val text = stringResource(ResultTexts.text(result.range, result.variant))
    val note = stringResource(Res.string.share_card_note)
    val app = stringResource(Res.string.app_name)
    return remember(result, title, text) {
        ShareCardData(result.nameA, result.nameB, result.percent, title, text, note, app)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(
    result: ResultUi,
    selected: ShareTemplate,
    /** Für Teilen-Karten wird immer die dunkle Variante des Designs verwendet. */
    sharePalette: PaletteColors,
    isUnlocked: (ShareTemplate) -> Boolean,
    onSelect: (ShareTemplate) -> Unit,
    onLocked: (ShareTemplate) -> Unit,
    shareService: ShareService,
    onDismiss: () -> Unit,
) {
    val palette = LocalPalette.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val data = rememberShareCardData(result)
    val textMeasurer = rememberTextMeasurer(cacheSize = 32)
    val fonts = ShareCardFonts(serif = displayFamily(), sans = bodyFamily())
    val renderer = remember(textMeasurer, fonts) { ShareCardRenderer(textMeasurer, fonts) }
    val previews = remember { mutableStateMapOf<ShareTemplate, ImageBitmap>() }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val effective = if (isUnlocked(selected)) selected else ShareTemplate.CLASSIC

    LaunchedEffect(data, sharePalette, renderer) {
        ShareTemplate.entries.forEach { template ->
            previews[template] = renderer.render(data, template, sharePalette, width = 540)
        }
    }

    val chooserTitle = stringResource(Res.string.share_chooser)
    val shareText = stringResource(Res.string.share_text, result.nameA, result.nameB, result.percent)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = palette.surface) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(Res.string.share_title), style = MaterialTheme.typography.headlineSmall, color = palette.onBackground)
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier
                    .fillMaxWidth(0.62f)
                    .aspectRatio(4f / 5f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.surfaceHigh),
                contentAlignment = Alignment.Center,
            ) {
                val bmp = previews[effective]
                if (bmp != null) {
                    Image(bmp, contentDescription = null, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
                } else {
                    CircularProgressIndicator(color = palette.primary)
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                stringResource(Res.string.share_template),
                style = MaterialTheme.typography.titleSmall,
                color = palette.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                items(ShareTemplate.entries) { template ->
                    val unlocked = isUnlocked(template)
                    val isSelected = template == effective
                    val border by animateColorAsState(if (isSelected) palette.primary else palette.outline.copy(alpha = 0.3f), label = "tplBorder")
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(92.dp)
                            .semantics { this.selected = isSelected }
                            .clickable(role = Role.RadioButton) {
                                if (unlocked) onSelect(template) else onLocked(template)
                            },
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 5f)
                                .clip(RoundedCornerShape(14.dp))
                                .border(BorderStroke(if (isSelected) 3.dp else 1.dp, border), RoundedCornerShape(14.dp))
                                .background(palette.surfaceHigh),
                        ) {
                            previews[template]?.let {
                                Image(it, contentDescription = null, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
                            }
                            if (!unlocked) LockBadge(Modifier.align(Alignment.TopEnd).padding(6.dp), compact = true)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(template.labelRes), style = MaterialTheme.typography.labelMedium, color = palette.onBackground)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                if (failed) stringResource(Res.string.share_failed) else stringResource(Res.string.share_names_hint),
                style = MaterialTheme.typography.bodySmall,
                color = if (failed) MaterialTheme.colorScheme.error else palette.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            GradientButton(
                text = if (busy) stringResource(Res.string.share_preparing) else stringResource(Res.string.share_button),
                leadingIcon = Icons.Rounded.IosShare,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    busy = true
                    failed = false
                    scope.launch {
                        val png = runCatching { renderer.render(data, effective, sharePalette).encodeToPng() }.getOrNull()
                        busy = false
                        if (png == null) {
                            failed = true
                        } else {
                            runCatching { shareService.shareImage(png, shareText, chooserTitle) }.onFailure { failed = true }
                        }
                    }
                },
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

