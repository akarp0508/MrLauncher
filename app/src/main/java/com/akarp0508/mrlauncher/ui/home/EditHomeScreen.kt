package com.akarp0508.mrlauncher.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.akarp0508.mrlauncher.R
import com.akarp0508.mrlauncher.data.AppInfo
import com.akarp0508.mrlauncher.data.LabelAlignment
import com.akarp0508.mrlauncher.search.AppSearch
import com.akarp0508.mrlauncher.ui.components.AppLabel
import com.akarp0508.mrlauncher.ui.components.textAlign
import com.akarp0508.mrlauncher.ui.theme.LauncherTheme
import kotlin.math.abs

/**
 * Home screen editing: reorder by dragging the handle, remove with ✕, add from a searchable
 * list. Apps are drawn in the home style so it's roughly what you'll get.
 */
@Composable
fun EditHomeScreen(
    homeApps: List<AppInfo>,
    /** Every non-hidden app, for the add list. */
    availableApps: List<AppInfo>,
    alignment: LabelAlignment,
    onReorder: (List<String>) -> Unit,
    onToggleApp: (AppInfo) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onDone: () -> Unit,
) {
    val colors = LauncherTheme.colors
    var addingApps by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 32.dp, vertical = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.edit_home_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.edit_home_done),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.accent,
                modifier = Modifier
                    .clickable(onClick = onDone)
                    .padding(8.dp),
            )
        }
        Text(
            text = stringResource(R.string.edit_home_hint),
            style = MaterialTheme.typography.labelLarge,
            color = colors.textDim,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        )

        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            ReorderableAppList(
                apps = homeApps,
                alignment = alignment,
                onReorder = onReorder,
                onRemove = onToggleApp,
                onAppClick = onAppClick,
            )
        }

        Text(
            text = stringResource(R.string.edit_home_add),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.accent,
            modifier = Modifier
                .clickable { addingApps = true }
                .padding(vertical = 16.dp),
        )
    }

    if (addingApps) {
        AddHomeAppsDialog(
            apps = availableApps,
            homeKeys = homeApps.map { it.key }.toSet(),
            onToggle = onToggleApp,
            onDismiss = { addingApps = false },
        )
    }
}

@Composable
private fun ReorderableAppList(
    apps: List<AppInfo>,
    alignment: LabelAlignment,
    onReorder: (List<String>) -> Unit,
    onRemove: (AppInfo) -> Unit,
    onAppClick: (AppInfo) -> Unit,
) {
    val colors = LauncherTheme.colors
    val currentOnReorder by rememberUpdatedState(onReorder)
    // Local copy so rows move live while dragging; saved once when the drag ends. One state object
    // for the screen's lifetime: the drag handlers below capture it and aren't restarted.
    var order by remember { mutableStateOf(apps) }
    var draggingKey by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(apps) {
        if (draggingKey == null) order = apps
    }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val rowHeights = remember { mutableStateMapOf<String, Int>() }

    fun endDrag() {
        if (draggingKey != null) currentOnReorder(order.map { it.key })
        draggingKey = null
        dragOffset = 0f
    }

    if (order.isEmpty()) {
        Text(
            text = stringResource(R.string.edit_home_empty),
            style = MaterialTheme.typography.labelLarge,
            color = colors.textDim,
        )
    }

    order.forEach { app ->
        key(app.key) {
            val isDragging = app.key == draggingKey
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { rowHeights[app.key] = it.height }
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffset else 0f
                        alpha = if (isDragging) 0.7f else 1f
                    },
            ) {
                val reorderDescription = stringResource(R.string.edit_home_reorder)
                Text(
                    text = "≡",
                    style = LauncherTheme.textStyles.homeApp,
                    color = colors.textDim,
                    modifier = Modifier
                        .semantics { contentDescription = reorderDescription }
                        .pointerInput(app.key) {
                            detectDragGestures(
                                onDragStart = {
                                    draggingKey = app.key
                                    dragOffset = 0f
                                },
                                onDragEnd = ::endDrag,
                                onDragCancel = ::endDrag,
                            ) { change, amount ->
                                change.consume()
                                dragOffset += amount.y
                                // Swap with a neighbour once dragged past half of its height.
                                val index = order.indexOfFirst { it.key == app.key }
                                val neighbour = if (dragOffset > 0) index + 1 else index - 1
                                if (neighbour !in order.indices) return@detectDragGestures
                                val height = rowHeights[order[neighbour].key] ?: return@detectDragGestures
                                if (abs(dragOffset) > height / 2f) {
                                    order = order.toMutableList().apply { add(neighbour, removeAt(index)) }
                                    dragOffset += if (neighbour > index) -height else height
                                }
                            }
                        }
                        .padding(end = 16.dp),
                )
                AppLabel(
                    app = app,
                    style = LauncherTheme.textStyles.homeApp,
                    textAlign = alignment.textAlign,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppClick(app) },
                    modifier = Modifier.weight(1f),
                )
                val removeDescription = stringResource(R.string.edit_home_remove)
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textDim,
                    modifier = Modifier
                        .semantics { contentDescription = removeDescription }
                        .clickable { onRemove(app) }
                        .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun AddHomeAppsDialog(
    apps: List<AppInfo>,
    homeKeys: Set<String>,
    onToggle: (AppInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(apps, query) { AppSearch.filter(apps, query) { it.label } }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.edit_home_done)) }
        },
        title = { Text(stringResource(R.string.edit_home_add_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.drawer_search_hint)) },
                )
                LazyColumn(Modifier.heightIn(max = 420.dp).padding(top = 8.dp)) {
                    items(results, key = { it.key }) { app ->
                        val onHome = app.key in homeKeys
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggle(app) }
                                .padding(vertical = 12.dp),
                        ) {
                            Text(
                                text = app.label,
                                style = MaterialTheme.typography.bodyLarge.copy(shadow = null),
                                color = if (onHome) MaterialTheme.colorScheme.primary else Color.Unspecified,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            if (onHome) {
                                Text("✓", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        },
    )
}
