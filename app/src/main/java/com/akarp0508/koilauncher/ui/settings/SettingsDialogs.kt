package com.akarp0508.koilauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.akarp0508.koilauncher.R
import com.akarp0508.koilauncher.data.AppInfo
import com.akarp0508.koilauncher.data.WeatherLocation
import com.akarp0508.koilauncher.search.AppSearch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private val DialogListMaxHeight = 420.dp

/** Single-choice list; [extra] adds a trailing entry such as "import font file…". */
@Composable
fun <T> OptionDialog(
    title: String,
    options: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    optionStyle: (T) -> TextStyle? = { null },
    extra: Pair<String, () -> Unit>? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(title) },
        text = {
            LazyColumn(Modifier.heightIn(max = DialogListMaxHeight)) {
                items(options) { option ->
                    DialogItem(
                        text = label(option),
                        style = optionStyle(option) ?: MaterialTheme.typography.bodyLarge,
                        highlighted = option == selected,
                        onClick = { onSelect(option) },
                    )
                }
                if (extra != null) {
                    item { DialogItem(extra.first, MaterialTheme.typography.bodyLarge, false, extra.second) }
                }
            }
        },
    )
}

@Composable
private fun DialogItem(text: String, style: TextStyle, highlighted: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        // The launcher's text shadow looks muddy on a dialog surface.
        style = style.copy(shadow = null),
        color = if (highlighted) MaterialTheme.colorScheme.primary else Color.Unspecified,
        maxLines = 1,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    )
}

@Composable
fun AppPickerDialog(apps: List<AppInfo>, onPick: (AppInfo) -> Unit, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(apps, query) { AppSearch.filter(apps, query) { it.label } }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
        title = { Text(stringResource(R.string.app_picker_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.drawer_search_hint)) },
                )
                LazyColumn(Modifier.heightIn(max = DialogListMaxHeight).padding(top = 8.dp)) {
                    items(results, key = { it.key }) { app ->
                        DialogItem(app.label, MaterialTheme.typography.bodyLarge, false) { onPick(app) }
                    }
                }
            }
        },
    )
}

@Composable
fun WeatherLocationDialog(
    search: suspend (String) -> List<WeatherLocation>,
    onPick: (WeatherLocation) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<WeatherLocation>?>(null) }
    var loading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    fun runSearch() {
        if (query.isBlank() || loading) return
        loading = true
        failed = false
        scope.launch {
            try {
                results = search(query)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                failed = true
            } finally {
                loading = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = ::runSearch, enabled = !loading) {
                Text(stringResource(R.string.weather_search_button))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
        title = { Text(stringResource(R.string.weather_search_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.weather_search_hint)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { runSearch() }),
                )
                val current = results
                when {
                    loading -> CircularProgressIndicator(Modifier.padding(top = 16.dp).size(24.dp))
                    failed -> Text(stringResource(R.string.weather_search_error), Modifier.padding(top = 16.dp))
                    current != null && current.isEmpty() ->
                        Text(stringResource(R.string.weather_search_empty), Modifier.padding(top = 16.dp))
                    current != null -> LazyColumn(Modifier.heightIn(max = DialogListMaxHeight).padding(top = 8.dp)) {
                        items(current) { location ->
                            DialogItem(location.name, MaterialTheme.typography.bodyLarge, false) { onPick(location) }
                        }
                    }
                }
            }
        },
    )
}

@Composable
fun HexColorDialog(initial: Int, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf("#%06X".format(initial and 0xFFFFFF)) }
    val parsed = parseHexColor(text)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { parsed?.let(onPick) }, enabled = parsed != null) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
        title = { Text(stringResource(R.string.color_custom_title)) },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.color_custom_hint)) },
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier
                        .padding(start = 16.dp)
                        .size(36.dp)
                        .background(parsed?.let(::Color) ?: Color.Transparent, CircleShape),
                )
            }
        },
    )
}

/** Accepts RRGGBB with or without a leading '#'; always opaque (0 is reserved for "auto"). */
internal fun parseHexColor(text: String): Int? {
    val hex = text.trim().removePrefix("#")
    if (hex.length != 6) return null
    val value = hex.toLongOrNull(16) ?: return null
    return (0xFF000000 or value).toInt()
}
