package com.akarp0508.koilauncher.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.akarp0508.koilauncher.R
import com.akarp0508.koilauncher.data.AUTO_TEXT_COLOR
import com.akarp0508.koilauncher.ui.theme.DarkText
import com.akarp0508.koilauncher.ui.theme.LauncherTheme
import com.akarp0508.koilauncher.ui.theme.LightText

@Composable
fun SectionHeader(@StringRes title: Int) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.labelLarge,
        color = LauncherTheme.colors.accent,
        modifier = Modifier.padding(top = 32.dp, bottom = 4.dp),
    )
}

@Composable
fun SettingRow(
    @StringRes title: Int,
    value: String?,
    valueStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    ) {
        Text(stringResource(title), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(
                text = value,
                style = valueStyle,
                color = LauncherTheme.colors.accent,
                maxLines = 1,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}

@Composable
fun ToggleRow(@StringRes title: Int, checked: Boolean, onToggle: () -> Unit) {
    SettingRow(
        title = title,
        value = stringResource(if (checked) R.string.settings_on else R.string.settings_off),
        onClick = onToggle,
    )
}

@Composable
fun StepperRow(@StringRes title: Int, value: String, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    val style = MaterialTheme.typography.bodyLarge
    val accent = LauncherTheme.colors.accent
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(title), style = style, modifier = Modifier.weight(1f))
        Text("−", style = style, color = accent, modifier = Modifier.clickable(onClick = onDecrease).padding(14.dp))
        Text(value, style = style, color = accent)
        Text("+", style = style, color = accent, modifier = Modifier.clickable(onClick = onIncrease).padding(14.dp))
    }
}

/** A title with a row of swatches underneath; the last swatch opens a custom hex color dialog. */
@Composable
fun ColorRow(
    @StringRes title: Int,
    selected: Int,
    presets: List<Int>,
    allowAuto: Boolean,
    onSelect: (Int) -> Unit,
    onCustom: () -> Unit,
) {
    val isCustom = selected !in presets && !(allowAuto && selected == AUTO_TEXT_COLOR)
    Column(Modifier.padding(vertical = 10.dp)) {
        Text(stringResource(title), style = MaterialTheme.typography.bodyLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(top = 10.dp),
        ) {
            if (allowAuto) {
                Swatch(
                    brush = Brush.horizontalGradient(0.5f to LightText, 0.5f to DarkText),
                    selected = selected == AUTO_TEXT_COLOR,
                    onClick = { onSelect(AUTO_TEXT_COLOR) },
                )
            }
            presets.forEach { color ->
                Swatch(SolidColor(Color(color)), selected = selected == color, onClick = { onSelect(color) })
            }
            Swatch(
                brush = SolidColor(if (isCustom) Color(selected) else Color.Transparent),
                selected = isCustom,
                onClick = onCustom,
                label = "#",
            )
        }
    }
}

@Composable
private fun Swatch(brush: Brush, selected: Boolean, onClick: () -> Unit, label: String? = null) {
    val colors = LauncherTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(32.dp)
            .border(if (selected) 3.dp else 1.dp, if (selected) colors.accent else colors.textDim, CircleShape)
            .padding(4.dp)
            .background(brush, CircleShape)
            .clickable(onClick = onClick),
    ) {
        if (label != null) Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
