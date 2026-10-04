package dev.flondy.checkyourlove.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import dev.flondy.checkyourlove.R
import dev.flondy.checkyourlove.ui.theme.LocalPalette
import dev.flondy.checkyourlove.ui.theme.backgroundBrush
import androidx.compose.ui.res.stringResource

/** Ganzflächiger, dezenter Verlaufshintergrund des aktuellen Designs. */
@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val palette = LocalPalette.current
    Box(
        modifier
            .fillMaxSize()
            .background(palette.background)
            .background(palette.backgroundBrush()),
        content = content,
    )
}

/** Skaliert beim Drücken leicht – für hochwertiges, direktes Feedback. */
@Composable
fun Modifier.pressScale(interaction: MutableInteractionSource, pressedScale: Float = 0.96f): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) pressedScale else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "pressScale",
    )
    return this.scale(scale)
}

/** Karte mit leichter Tiefe und feiner Kontur. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalPalette.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = palette.surface.copy(alpha = if (palette.isDark) 0.86f else 0.94f),
        tonalElevation = 0.dp,
        shadowElevation = if (palette.isDark) 0.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, palette.outline.copy(alpha = if (palette.isDark) 0.35f else 0.25f)),
    ) {
        Column(Modifier.padding(contentPadding), content = content)
    }
}

/** Primäre Pill-Schaltfläche mit Verlauf. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val palette = LocalPalette.current
    val interaction = remember { MutableInteractionSource() }
    val brush = if (enabled) {
        Brush.horizontalGradient(listOf(palette.heartStart, palette.heartMid, palette.heartEnd))
    } else {
        Brush.horizontalGradient(listOf(palette.outline.copy(alpha = 0.5f), palette.outline.copy(alpha = 0.5f)))
    }
    Box(
        modifier = modifier
            .pressScale(interaction)
            .heightIn(min = 56.dp)
            .shadow(if (enabled) 10.dp else 0.dp, CircleShape, ambientColor = palette.glow, spotColor = palette.glow)
            .clip(CircleShape)
            .background(brush)
            .clickable(interaction, indication = androidx.compose.material3.ripple(color = Color.White), enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Kleines „Premium“-Abzeichen mit Schloss. */
@Composable
fun LockBadge(modifier: Modifier = Modifier, compact: Boolean = false) {
    val palette = LocalPalette.current
    Row(
        modifier
            .clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(palette.heartStart, palette.heartEnd)))
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        if (!compact) {
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.locked_badge), color = Color.White, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/**
 * Hochwertiges Namensfeld: klare Beschriftung, animierter Fokusrahmen,
 * Zeichenzähler und verständliche Fehlermeldung (auch für TalkBack).
 */
@Composable
fun NameField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    errorText: String?,
    counterText: String,
    enabled: Boolean,
    imeAction: ImeAction,
    onImeAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalPalette.current
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val iconTint by animateColorAsState(
        when {
            errorText != null -> MaterialTheme.colorScheme.error
            focused -> palette.primary
            else -> palette.onSurfaceVariant
        }, tween(220), label = "iconTint",
    )
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .semantics { if (errorText != null) error(errorText) },
        enabled = enabled,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = iconTint) },
        trailingIcon = if (errorText != null) {
            { Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
        } else null,
        supportingText = {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    errorText ?: "",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.error,
                )
                Text(counterText, color = palette.onSurfaceVariant)
            }
        },
        isError = errorText != null,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        interactionSource = interaction,
        textStyle = MaterialTheme.typography.titleMedium,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            keyboardType = KeyboardType.Text,
            imeAction = imeAction,
            autoCorrectEnabled = false,
        ),
        keyboardActions = KeyboardActions(onAny = { onImeAction() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = palette.surface,
            unfocusedContainerColor = palette.surface.copy(alpha = if (palette.isDark) 0.7f else 0.9f),
            disabledContainerColor = palette.surface.copy(alpha = 0.5f),
            focusedBorderColor = palette.primary,
            unfocusedBorderColor = palette.outline.copy(alpha = 0.6f),
            focusedLabelColor = palette.primary,
            cursorColor = palette.primary,
        ),
    )
}

/** Abschnittsüberschrift in Listen. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = LocalPalette.current.primary,
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp, top = 8.dp),
    )
}

/** Runder Icon-Knopf mit Druck-Animation und ausreichender Touchfläche (48 dp). */
@Composable
fun SoftIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 48.dp,
) {
    val palette = LocalPalette.current
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .pressScale(interaction, 0.9f)
            .size(size)
            .clip(CircleShape)
            .background(palette.surfaceHigh.copy(alpha = if (enabled) 0.9f else 0.4f))
            .border(1.dp, palette.outline.copy(alpha = 0.25f), CircleShape)
            .clickable(interaction, indication = androidx.compose.material3.ripple(), enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint.copy(alpha = if (enabled) 1f else 0.4f))
    }
}

@Suppress("unused")
private val PillShape = RoundedCornerShape(50)
