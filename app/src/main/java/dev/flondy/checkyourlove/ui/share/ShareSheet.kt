package dev.flondy.checkyourlove.ui.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import dev.flondy.checkyourlove.R
import dev.flondy.checkyourlove.core.ResultTexts
import dev.flondy.checkyourlove.monetization.ShareTemplate
import dev.flondy.checkyourlove.ui.components.GradientButton
import dev.flondy.checkyourlove.ui.components.LockBadge
import dev.flondy.checkyourlove.ui.test.ResultUi
import dev.flondy.checkyourlove.ui.theme.LocalPalette
import dev.flondy.checkyourlove.ui.theme.PaletteColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun rememberShareCardData(result: ResultUi): ShareCardData {
    val title = stringResource(ResultTexts.title(result.range))
    val text = stringResource(ResultTexts.text(result.range, result.variant))
    val note = stringResource(R.string.share_card_note)
    val app = stringResource(R.string.app_name)
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
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val palette = LocalPalette.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val data = rememberShareCardData(result)
    val renderer = remember { ShareCardRenderer(context.applicationContext) }
    val previews = remember { mutableStateMapOf<ShareTemplate, Bitmap>() }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val effective = if (isUnlocked(selected)) selected else ShareTemplate.CLASSIC

    LaunchedEffect(data, sharePalette) {
        ShareTemplate.entries.forEach { template ->
            val bmp = withContext(Dispatchers.Default) { renderer.render(data, template, sharePalette, width = 540) }
            previews[template] = bmp
        }
    }

    val chooserTitle = stringResource(R.string.share_chooser)
    val shareText = stringResource(R.string.share_text, result.nameA, result.nameB, result.percent)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = palette.surface) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.share_title), style = MaterialTheme.typography.headlineSmall, color = palette.onBackground)
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
                    Image(bmp.asImageBitmap(), contentDescription = null, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
                } else {
                    CircularProgressIndicator(color = palette.primary)
                }
            }
            Spacer(Modifier.height(18.dp))
            Text(
                stringResource(R.string.share_template),
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
                                Image(it.asImageBitmap(), contentDescription = null, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
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
                if (failed) stringResource(R.string.share_failed) else stringResource(R.string.share_names_hint),
                style = MaterialTheme.typography.bodySmall,
                color = if (failed) MaterialTheme.colorScheme.error else palette.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
            GradientButton(
                text = if (busy) stringResource(R.string.share_preparing) else stringResource(R.string.share_button),
                leadingIcon = Icons.Rounded.IosShare,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    busy = true
                    failed = false
                    scope.launch {
                        val intent = runCatching {
                            withContext(Dispatchers.Default) {
                                val bmp = renderer.render(data, effective, sharePalette)
                                ShareFiles.createShareIntent(context, bmp, shareText)
                            }
                        }.getOrNull()
                        busy = false
                        if (intent == null) {
                            failed = true
                        } else {
                            context.startActivity(Intent.createChooser(intent, chooserTitle))
                        }
                    }
                },
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

object ShareFiles {
    /** Speichert das Bild im freigegebenen Cache-Ordner und baut einen ACTION_SEND-Intent. */
    fun createShareIntent(context: Context, bitmap: Bitmap, text: String): Intent {
        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, "check-your-love-${System.currentTimeMillis()}.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.share", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
